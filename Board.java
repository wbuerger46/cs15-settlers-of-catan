package myindy.settlersOfCatan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import javafx.scene.layout.Pane;

/**
 * This is the Board class! Because the game class primarily concerns itself with the actual turn-switching and
 * choosing what to do on your turn, this class contains most of the actual game functionality. This class has a bunch
 * of algorithms dedicated to setting up the board (which includes the tiles, structures, and roads) and setting up
 * all relationships that are important between tiles, roads, structures, and players. The set-up rotations don't
 * follow typical gameplay (i.e. players can't choose when to end their turn; they just place a structure, then it goes
 * to the next player), so all pre-gameplay rotation logic is also in this class. Finally, this class has all the
 * algorithms that have to do with finding valid locations to place roads/settlements/cities (a bunch of recursion!),
 * many methods that "reset" the board once things are placed, logic for moving the robber around, and other algorithms
 * that track achievements like "Longest Road" and "Largest Army".
 */
public class Board {

	private Game _game;
	private Tile[][] _board;
	private Structure[][] _structures;
	private HashMap<Integer,Tile[]> _diceRollToTiles;
	private Stack<Player> _setUpOrder;
	private Player _currPlayer;
	private int _longestRoad;
	private Player _longestRoadOwner;
	private HashSet<Road> _checkedRoads;
	private int _largestArmy;
	private Player _largestArmyOwner;
	//All of these Booleans are used to keep track of what stage of gameplay we're in.
	private Boolean _gameplay;
	private Boolean _settlementsPlaced;
	private Boolean _choosingPregameResources;
	private Boolean _usingKnight;
	
	/**
	 * This is the constructor for the Board! It takes parameter Game so it can set up the association between the
	 * Board and the Game. First, the constructor sets up all the initial conditions of the board (the background,
	 * the actual board, all the structures, then all the roads). Then it initializes the pre-gameplay setup conditions
	 * (when all players place 2 settlements and 2 roads). It gets the setupOrder from the game, sets the firstPlayer,
	 * and gets the board ready for the first player to place a settlement.
	 */
	public Board(Game game) {
		_game = game;
		this.makeBackground();
		this.makeBoard();
		this.makeStructures();
		this.makeRoads();
		_setUpOrder = _game.setupOrder(false);
		_currPlayer = _setUpOrder.pop();
		_longestRoad = 4;
		_longestRoadOwner = null;
		_largestArmy = 2;
		_largestArmyOwner = null;
		_checkedRoads = new HashSet<Road>();
		_gameplay = false;
		_settlementsPlaced = false;
		_choosingPregameResources = false;
		_usingKnight = false;
		this.purchaseAnySettlement(_currPlayer);
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                          **THESE METHODS ARE ONLY USED IN THE INITIALIZATION OF THE GAMEBOARD**                   //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Since the background consists of hexagonal Ocean tiles, I had to specifically place each tile rather than
	 * set that image to repeat for the background. Also, I wanted these hexagons to fit into the board hexagons.
	 * Therefore, this method uses for-loops by "row" and "column" to place the ocean tiles. Since each row is
	 * offset by a specific "TILE_X_INCREMENT", the first x-value of the row alternates, always storing the
	 * "firstX" and "nextFirstX".
	 */
	private void makeBackground() {
		double firstX = Constants.FIRST_BACKGROUND_TILE_X;
		double nextFirstX = firstX - Constants.TILE_X_INCREMENT;
		double firstY = Constants.FIRST_BACKGROUND_TILE_Y;
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 7; col++) {
				Tile tile = new Tile("Ocean", this);
				tile.setLoc(firstX + col*Constants.TILE_X_INCREMENT*2, firstY);
			}
			//The intermediate is just needed to swap "firstX" and "nextFirstX"
			double intermediate = firstX;
			firstX = nextFirstX;
			nextFirstX = intermediate;
			firstY += Constants.TILE_Y_INCREMENT;
		}
	}
	
	/**
	 * This method graphically and logically builds the board into its 2D Array. It first calls randomizeTiles() to
	 * return a random list of the board tiles. Since the board isn't exactly 5x5 (only the middle column and middle
	 * row have a full 5 elements), we first set the "empty" locations to empty "Ocean" tiles. Then, I use nested
	 * for-loops by row and column through the 2D Array that represents the board. If the entry doesn't have an "empty"
	 * Ocean tile, it adds the next random tile to the Array. Due to the unique shape of the board, each time we move
	 * down a row, we have to increment the first y-value as well as the first x-value.
	 */
	private void makeBoard() {
		List<Tile> tiles = this.randomizeTiles();
		_board = new Tile[5][5];
		for (int[] coordPair: Constants.EMPTY_TILE_LOCS) {
			//Since the ocean background was already created, these aren't graphically added
			_board[coordPair[0]][coordPair[1]] = new Tile("Ocean", this);
		}
		//The index refers to the index of the list of randomized tiles. It's incremented each time we add a tile
		int index = 0;
		double firstY = Constants.FIRST_TILE_Y;
		double firstX = Constants.FIRST_TILE_X;
		for (int row = 0; row <= 4; row++) {
			for (int col = 0; col <= 4; col++) {
				//If the entry isn't null, it's already been set with an Ocean tile since it's an empty location
				if (_board[row][col] == null) {
					_board[row][col] = tiles.get(index);
					tiles.get(index).setLoc(firstX + col*Constants.TILE_X_INCREMENT*2, firstY);
					index += 1;
				}
			}
			firstX += -Constants.TILE_X_INCREMENT;
			firstY += Constants.TILE_Y_INCREMENT;
		}
		
	}
	
	/**
	 * This method creates a new random arrangement of the tiles and new random assignment of numbers (representing
	 * a dice roll value) for each game! This method also creates the HashMap that stores the relationship between
	 * which dice rolls indicate which tiles. First we make a list (since shuffle() only works on lists) with all of
	 * the tiles (4xWool, 4xWheat, 4xLumber, 3xBrick, 3xOre, 1xDesert). After the list is randomized once, we go
	 * through numbers 2 - 12 and assign numbers to the tiles (via that randomized list). At each number, we also add
	 * an entry to the HashMap: the key is the number and the value is an Array with the associated tiles. Once all
	 * tiles are assigned a number, the list is randomized once more to its final order. The method returns that List
	 * of Tiles to the makeBoard() method.
	 */
	private List<Tile> randomizeTiles() {
		List<Tile> tiles = Arrays.asList(new Tile("Wool",this),new Tile("Wool",this),new Tile("Wool",this),
				new Tile("Wool",this),new Tile("Wheat",this),new Tile("Wheat",this),new Tile("Wheat",this),
				new Tile("Wheat",this),new Tile("Lumber",this),new Tile("Lumber",this),new Tile("Lumber",this),
				new Tile("Lumber",this),new Tile("Brick",this),new Tile("Brick",this),new Tile("Brick",this),
				new Tile("Ore",this),new Tile("Ore",this),new Tile("Ore",this),new Tile("Desert",this));
		Collections.shuffle(tiles);
		_diceRollToTiles = new HashMap<Integer,Tile[]>();
		//The index refers to the index of the list of randomized tiles and is needed since some numbers are assigned
		//to one tile but others are assigned to two tiles.
		int index = 0;
		for (int i = 2; i <= 12; i++) {
			//The number 7 isn't assigned to any tiles
			if (i ==  7) {
				continue;
			}
		//This code assigns the number to the first tile
			Tile firstTile = tiles.get(index);
			//The desert isn't assigned a number, so skip it!
			if (firstTile.getResource() == "Desert") {
				index += 1;
				firstTile = tiles.get(index);
			}
			//setDiceRoll() adds the number token graphic to the tile
			firstTile.setDiceRoll(i);
			index += 1;
			//2 and 12 are only assigned to one tile, so it'll go to the next iteration after putting
			//this dice-tile relationship into the HashMap.
			if (i == 2 || i == 12) {
				//It has to be an Array with one element since the others are an Array with two elements
				_diceRollToTiles.put(i, new Tile[] {firstTile});
				continue;
			}
		//This code assigns the number to the second tile
			Tile secondTile = tiles.get(index);
			if (secondTile.getResource() == "Desert") {
				index += 1;
				secondTile = tiles.get(index);
			}
			secondTile.setDiceRoll(i);
			_diceRollToTiles.put(i, new Tile[] {firstTile,secondTile});
			index += 1;
		}
		Collections.shuffle(tiles);
		return tiles;
	}
	
	/**
	 * This method creates all the structure locations in the _structures 2D Array. This assignment uses a similar
	 * logic as I did in makeBoard(), but it is a bit more complex since the rows of structures aren't spaced out
	 * equally (hexagons are annoying sometimes). Each row alternates between having orientation = 1 and 2, so we
	 * keep "thisOrientation", which adjusts after each row is set (see the Structure class for more about these
	 * orientations). 
	 */
	private void makeStructures() {
		_structures = new Structure[12][6];
		for (int[] coordinate: Constants.EMPTY_STRUCTURE_LOCS) {
			//Place-holder "empty" structures
			_structures[coordinate[0]][coordinate[1]] = new Structure(0,0,0,this);
		}
		int thisOrientation = 1;
		double x = Constants.FIRST_STRUCTURE_X;
		double y = Constants.FIRST_STRUCTURE_Y;
		for (int row = 0; row < 12; row++) {
			for (int col = 0; col < 6; col ++) {
				if (_structures[row][col] == null) {
					_structures[row][col] = new Structure(thisOrientation,
							x+col*Constants.TILE_X_INCREMENT*2,y,this);
				}
			}
			if (thisOrientation == 1) {
				x += -Constants.TILE_X_INCREMENT;
				y += Constants.STRUCTURE_SMALLER_Y_INCREMENT;
				thisOrientation = 2;
			} else {
				y += Constants.STRUCTURE_LARGER_Y_INCREMENT;
				thisOrientation = 1;
			}
		}
		this.assignTiles();
	}
	
	/**
	 * This method associates the tiles with which structure locations would get resources from them (so that when
	 * a player buys a settlement, he knows which tiles he can get resources from). Unfortunately, not all structures
	 * exist between 3 tiles, so there are a lot of edge cases (hence all the if-statements). And even between those
	 * structures that do exist between 3 tiles, structures with different orientations have to retrieve tiles with a
	 * different relationship between the arrays. I use a switch statement based on the structure's orientation to
	 * assign the tiles. The Board has 5 rows while the structure array has 12 rows, so to find the correct row, we
	 * use the formula (int)(row/2) to get the row below the structure and (int)(row/2)-1 to get the row above the
	 * structure.
	 */
	private void assignTiles() {
		for (int row = 0; row < 12; row++) {
			for (int col = 0; col < 6; col ++) {
				Structure structure = _structures[row][col];
				switch (structure.getOrientation()) {
				case 1:
					if (col!= 0 && row != 0) {
						//This is the tile to the upper left of the structure
						structure.assignTile(_board[(int)(row/2)-1][col-1]);
					}
					if (col != 5 && row != 0) {
						//This is the tile to the upper right of the structure
						structure.assignTile(_board[(int)(row/2)-1][col]);
					}
					if (row != 10 && col != 5) {
						//This is the tile directly below the structure
						structure.assignTile(_board[(int)(row/2)][col]);
					}
					break;
				case 2:
					if (row != 1 && col != 0) {
						//This is the tile directly above the structure
						structure.assignTile(_board[(int)(row/2)-1][col-1]);
					}
					if (row != 11 && col != 0) {
						//This is the tile to the lower left of the structure
						structure.assignTile(_board[(int)(row/2)][col-1]);
					}
					if (row != 11 && col != 5) {
						//This is the tile to the lower right of the structure
						structure.assignTile(_board[(int)(row/2)][col]);
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * This method creates all of the roads on the board and sets up the relationship so that all roads know their
	 * two adjacent structures and all structures know their three adjacent roads. Again, the placement of roads
	 * depends on the orientation of the structure, so I use another switch statement. And again, lots of edge cases!
	 * The basic set-up is that if it has orientation 1, we find the road above it (already created) and establish
	 * their relationship, then build the two lower roads. If it has orientation 2, we find the two roads above it and
	 * establish those relationships, then build the lower road. The roads' orientations essentially indicate their
	 * angle (see more about this in the Road class).
	 */
	private void makeRoads() {
		for (int row = 0; row < 12; row++) {
			for (int col = 0; col < 6; col++) {
				Structure structure = _structures[row][col];
				switch (structure.getOrientation()) {
				case 1:
					if (row != 0) {
						//Find the road that flanks the structure directly from above
						Road roadA = _structures[row-1][col].getRoad(-1);
						//The next two lines make it so that the road knows about the structure AND the structure
						//knows about the road. These two lines are used many times in this method.
						structure.addRoad(roadA);
						roadA.setStructure(structure);
					}
					if (col != 5) {
						//Create a new road to the lower right
						this.newRoad(2, structure);
					}
					//Needs to make sure there is an existing structure to the lower left side
					if (_structures[row+1][col].getOrientation() != 0) {
						//Creates a new road to the lower left
						this.newRoad(3, structure);
					}
					break;
				case 2:
					//Needs to make sure there is an existing structure to the upper right side
					if (_structures[row-1][col].getOrientation() != 0) {
						//Find the road that flanks the structure from the upper right
						Road roadA = _structures[row-1][col].getRoad(-1);
						structure.addRoad(roadA);
						roadA.setStructure(structure);
					}
					if (col != 0) {
						/**
						 * This is an extra edge case since the topmost row only has 2 roads, so to find the road
						 * that the structure from the upper left, we want the above's structure's first road.
						 * Otherwise, we want their second road.
						 */
						Road roadB = null;
						if (row == 1) {
							roadB = _structures[row-1][col-1].getRoad(0);
						} else {
							roadB = _structures[row-1][col-1].getRoad(1);
						}
						structure.addRoad(roadB);
						roadB.setStructure(structure);
					}
					if (row != 11) {
						//Creates a row directly below the structure
						this.newRoad(1,structure);
					}
					break;
				default:
					break;
				}
			}
		}
	}
	
	/**
	 * This method is called from makeRoads() each time it actually needs to create a new road. It takes the parameter
	 * orientation so it can create the new road with the proper angle, and it also takes the structure so it can set
	 * up the necessary relationship between the road and the structure.
	 */
	private void newRoad(int orientation, Structure structure) {
		Road road = new Road(orientation, structure.getImage().getLayoutX(),structure.getImage().getLayoutY(),this);
		structure.addRoad(road);
		road.setStructure(structure);
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                               **THESE METHODS ARE ONLY USED DURING SET-UP PLACEMENTS**                            //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * This method is used only during set-up, and it allows the current player to purchase a settlement at any open
	 * location on the board. Therefore, it uses nested for-loops to go through every structure and set it as a
	 * possible purchase (if valid).
	 */
	private void purchaseAnySettlement(Player player) {
		_game.changeInstructions(player.getName() + ":  Place a Structure");
		for (Structure[] row: _structures) {
			for (Structure structure: row) {
				//As long as the structure is a valid move and it isn't already purchased, this player can buy it!
				if (structure.getOrientation() != 0 && structure.getOwner() == null) {
					structure.readyForPurchase(player);
				}
			}
		}
	}
	
	/**
	 * Since the turn rotation during set-up only has each player place one item, the Game class doesn't need to
	 * worry about it! Instead, the Board class facilitates all of set-up. Whenever an item, it placed during before
	 * gameplay, it calls this method. If _setUpOrder is empty, it either means all the settlements have been placed
	 * (in which case it resets _setUpOrder and has them all place roads), all the roads have been placed (in which
	 * case it gets the final _setUpOrder so the players can choose initial resources), or all players have gotten
	 * resource cards (in which case gameplay can begin).
	 */
	public void setUpTurns(Player player, RealEstate bought) {
		if (_setUpOrder.isEmpty()) {
			if (_choosingPregameResources) {
				_gameplay = true;
				_game.startGameplay();
				_choosingPregameResources = false;
				//Empty return so that it doesn't try to have people place more roads!
				return;
			}
			if (_settlementsPlaced) {
				this.roadPurchased(player);
				_choosingPregameResources = true;
				_setUpOrder = _game.setupOrder(true);
			} else {
				this.hideEmptyStructures();
				_settlementsPlaced = true;
				_setUpOrder = _game.setupOrder(false);
			}
		}
		_currPlayer = _setUpOrder.pop();
		if (_choosingPregameResources) {
			for (Structure structure: _currPlayer.getStructures()) {
				structure.readyForSelection();
			}
			_game.changeInstructions(_currPlayer.getName() + ": Choose One Settlement from which to receive"
					+ " initial resources");
		} else if (_settlementsPlaced) {
			this.roadPurchased(player);
			this.purchaseRoad(_currPlayer);
		} else {
			this.purchaseAnySettlement(_currPlayer);
		}
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                    **THESE METHODS ARE USED TO FACILITATE PURCHASING REAL ESTATE DURING GAMEPLAY**                //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Each time a player wants to purchase a road, we have to find all valid road placements for them and set those
	 * as ready for purchase. Since each road links to others, I use recursion to find all the valid road spots. To
	 * ensure it doesn't keep iterating through a chain of roads, whenever it checks a road, it adds it to a Set of
	 * roads that shouldn't be checked anymore. 
	 */
	public void purchaseRoad(Player player) {
		HashSet<Road> checked = new HashSet<Road>();
		_game.changeInstructions(player.getName() + ": Place a Road");
		for (Structure structure: player.getStructures()) {
			//Starts the recursion
			this.toggleValidRoads(structure, player, checked);
		}
	}
	
	/**
	 * This is the recursive method to find all valid road placements. It starts with a structure and checks all of
	 * its roads as possible spots. If it is already owned by that player, it checks the next road in the sequence.
	 * If it is not owned, it is marked as ready for purchase.
	 */
	private void toggleValidRoads(Structure structure, Player player, HashSet<Road> checked) {
		for (Road road: structure.getOtherRoads(null)) {
			//If this road is owned by the player and hasn't been checked, keep recursing!
			if (road.getOwner() == player && !checked.contains(road)) {
				checked.add(road);
				this.toggleValidRoads(road.getOtherStructure(structure), player, checked);
			} else if (road.getOwner() == null){
				road.readyForPurchase(player);
			}
		}
	}
	
	/**
	 * This method checks IF there are locations where a player can place settlements (so that the "Buy a Settlement"
	 * button knows when it is allowed to be on). We have to use recursion, starting at each of the player's
	 * pre-existing settlements to find if there are valid locations. I use a HashSet "checked" to keep track of which
	 * Structures have already been checked so that it doesn't reach a StackOverflow error. The return Boolean
	 * indicates true if there is at least one spot where the player can build.
	 */
	public Boolean hasValidSettlementLoc(Player player) {
		HashSet<Structure> checked = new HashSet<Structure>();
		for (Structure structure: player.getStructures()) {
			checked.add(structure);
			if (this.findValidSettlements(structure, null, player, checked)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This is the recursive method associated with hasValidSettlementLoc() ^above. It takes parameters structure to
	 * know where to start looking, prevRoad to know which direction recursion is going, player to know which player's
	 * RealEstate we're looking at, and HashSet checked so we don't keep checking the same structures.
	 */
	private Boolean findValidSettlements(Structure structure, Road prevRoad, Player player,
			HashSet<Structure> checked) {
		for (Road nextRoad: structure.getOtherRoads(prevRoad)) {
			//In order for a location to be valid, the road before it has to be owned by that player
			if (nextRoad.getOwner() == player) {
				Structure otherStructure = nextRoad.getOtherStructure(structure);
				//Status 0 indicates it is unowned, orientation != 0 indicates it is a valid location
				if (otherStructure.getOrientation() != 0 && otherStructure.getStatus() == 0) {
					return true;
				}
				if (!checked.contains(otherStructure)) {
					checked.add(otherStructure);
					if (this.findValidSettlements(otherStructure, nextRoad, player, checked)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Each time a player wants to purchase a settlement (not during set-up), we have to find all valid locations and
	 * set them as ready for purchase. This method is almost identical to hasValidSettlementLoc() except we are
	 * performing an action on structures rather than returning a Boolean.
	 */
	public void purchaseSettlement(Player player) {
		_game.changeInstructions(player.getName() + ":  Place a Structure");
		HashSet<Structure> checked = new HashSet<Structure>();
		for (Structure structure: player.getStructures()) {
			checked.add(structure);
			this.toggleValidSettlements(structure, null, player, checked);
		}
	}
	
	/**
	 * This recursive method is very similar to findValidSettlements() except it tells the open structure locations to
	 * be ready for purchase.
	 */
	private void toggleValidSettlements(Structure structure, Road prevRoad, Player player,
			HashSet<Structure> checked) {
		for (Road nextRoad: structure.getOtherRoads(prevRoad)) {
			if (nextRoad.getOwner() == player) {
				Structure otherStructure = nextRoad.getOtherStructure(structure);
				if (otherStructure.getOrientation() != 0 && otherStructure.getStatus() == 0) {
					otherStructure.readyForPurchase(player);
				}
				if (!checked.contains(otherStructure)) {
					checked.add(otherStructure);
					this.toggleValidSettlements(otherStructure, nextRoad, player, checked);
				}
			}
		}
	}
	
	/**
	 * It's a lot easier to buy a city! Just find all of that player's settlements and set them as ready for
	 * purchase!
	 */
	public void purchaseCity(Player player) {
		_game.changeInstructions(player.getName() + ": Place a City");
		for (Structure structure: player.getStructures()) {
			if (structure.getStatus() == 1) {
				structure.readyForPurchase(player);
			}
		}
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                        **THESE METHODS ARE USED AFTER A PIECE OF REAL ESTATE HAS BEEN PLACED**                    //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * This method is called every time any real estate has been placed on the board. If it's not actually during
	 * gameplay yet (i.e. during setup), it calls that separate method setUpTurns(). Otherwise, it checks what kind
	 * of RealEstate it is and proceeds with whatever actions are necessary. At the end we check to see if the game
	 * is over since placing RealEstate often increases points.
	 */
	public void purchased(Player player, RealEstate bought) {
		if (_gameplay) {
			if (bought instanceof Road) {
				player.boughtRoad();
				this.roadPurchased(player);
				this.longestRoad((Road) bought, player);
			} else if (bought.getStatus() == 1) {
				player.boughtSettlement((Structure) bought);
				this.hideEmptyStructures();
			} else {
				player.boughtCity((Structure) bought);
				for (Structure structure: player.getStructures()) {
					structure.cannotBuy();
				}
			}
			_game.changeInstructions(_currPlayer.getName() + "'s Turn");
			_game.toggleButtons();
		} else {
			this.setUpTurns(player, bought);
		}
		if (player.hasWon()) {
			_game.gameOver(player);
		}
	}
	
	/**
	 * More recursion! Since the roads aren't stored in any data structure, I'm using recursion to access all the
	 * roads that may have been up for purchase and set them back to not available. Again, I use a HashSet "checked"
	 * so it doesn't get a StackOverflow error.
	 */
	private void roadPurchased(Player player) {
		HashSet<Road> checked = new HashSet<Road>();
		for (Structure structure: player.getStructures()) {
			this.hideEmptyRoads(structure, checked);
		}
	}
	
	/**
	 * This method recursively goes through all of the roads on the board and sets them as not visible unless they
	 * have an owner (i.e. were bought as settlements or cities). This method is initially called by roadPurchased().
	 */
	private void hideEmptyRoads(Structure structure, HashSet<Road> checked) {
		for (Road road: structure.getOtherRoads(null)) {
			if (!checked.contains(road)) {
				checked.add(road);
				this.hideEmptyRoads(road.getOtherStructure(structure), checked);
			} else if (road.getOwner() == null){
				road.getImage().setVisible(false);
			}
		}
	}
	
	/**
	 * This method uses for-loops to go through all the structures on the board and hide any locations that haven't
	 * been purchased yet. It's called at the end of pre-gameplay settlement placements and also any other time
	 * a settlement is purchased.
	 */
	private void hideEmptyStructures() {
		for (Structure[] row: _structures) {
			for (Structure structure: row) {
				if (structure.getOrientation() != 0 && structure.getOwner() == null) {
					structure.getImage().setVisible(false);
				}
			}
		}
	}
	
	/**
	 * This method calculates if the player who just set down a road has achieved a longest road. To do this, it
	 * calculates the longest chain of roads to the left side of it and also the longest chain of roads to the right
	 * side of it; that sum is the maximum length of the road. Again, we don't want it checking the same roads again
	 * and again, so we use a HashSet to store the roads that have been checked. However, we also don't want any
	 * overlap between roads checked on the right side and roads checked on the left side; since the recursion begins
	 * two separate times, I needed _checkedRoads to be an instance variable.
	 */
	private void longestRoad(Road road, Player player) {
		_checkedRoads = new HashSet<Road>();
		_checkedRoads.add(road);
		int length = 0;
		//For each of the two structures (one on the left, one on the right)
		for (Structure structure: road.getStructures()) {
			length += this.getRoadLength(structure, road, player);
		}
		//Both recursive checks include the purchased road; we remove 1 to account for that overlap
		length--;
		if (length > _longestRoad) {
			_longestRoad = length;
			if (player != _longestRoadOwner) {
				if (_longestRoadOwner != null) {
					//If someone else is taking "Longest Road", the previous owner loses the 2 points
					_longestRoadOwner.addVictoryPoints(-2);;
				}
				_longestRoadOwner = player;
				_game.newLongestRoad(player);
			}
		}
	}
	
	/**
	 * This is the recursive method associated with calculating the longest road. At each branching of roads,
	 * it checks whether the left branch or right branch (somewhat arbitrarily labeled) has more roads. It returns
	 * the maximum of those two values. Sometimes, there will only by one branch (along the edges of the board).
	 */
	private int getRoadLength(Structure structure, Road road, Player player) {
		int leftCount = 1;
		int rightCount = 1;
		ArrayList<Road> otherRoads = structure.getOtherRoads(road);
		Road otherRoad = otherRoads.get(0);
		if (!_checkedRoads.contains(otherRoad) && otherRoad.getOwner() == player) {
			_checkedRoads.add(otherRoad);
			leftCount += this.getRoadLength(otherRoad.getOtherStructure(structure), otherRoad, player);
		}
		//If there indeed are two roads branching off
		if (otherRoads.size() == 2) {
			otherRoad = otherRoads.get(1);
			if (!_checkedRoads.contains(otherRoad) && otherRoad.getOwner() == player) {
				_checkedRoads.add(otherRoad);
				rightCount += this.getRoadLength(otherRoad.getOtherStructure(structure), otherRoad, player);
			}
		}
		return Math.max(leftCount, rightCount);
	}
	
	/**
	 * This method is called anytime a player gets to move the robber. It takes Boolean parameter to indicate
	 * whether the player is using a knight card (versus if a 7 was rolled). That Boolean is important so that
	 * after the robber is placed, it accurately tracks who uses knight cards. The nested for-loops turn each tile
	 * ready to accept the robber. If the tile has the robber, it is removed before it is replaced on the board.
	 */
	public void placeRobber(Player player, Boolean usingKnightCard) {
		_usingKnight = usingKnightCard;
		_currPlayer = player;
		for (Tile[] row: _board) {
			for (Tile tile: row) {
				tile.canPlaceRobber();
				if (tile.hasRobber()) {
					tile.losesRobber();
				}
			}
		}
	}
	
	/**
	 * This method is called immediately after the robber was placed on the board. First it sets all the tiles on the
	 * board so that they aren't accepting mouse interaction to take the robber. If a knight card was used, it signals
	 * for the player to increase their number of night cards used, and it checks if that player now has the largest
	 * army. It also checks to see if the game is over since the change in points may trigger the end.
	 */
	public void robberPlaced() {
		for (Tile[] row: _board) {
			for (Tile tile: row) {
				tile.cannotPlaceRobber();
			}
		}
		if (_usingKnight) {
			_currPlayer.usedKnight();
			_usingKnight = false;
			if (_currPlayer.getKnightsUsed() > _largestArmy) {
				_largestArmy = _currPlayer.getKnightsUsed();
				if (_currPlayer != _largestArmyOwner) {
					if (_largestArmyOwner != null) {
						_largestArmyOwner.addVictoryPoints(-2);
					}
					_largestArmyOwner = _currPlayer;
					_game.newLargestArmy(_currPlayer);
					if (_currPlayer.hasWon()) {
						_game.gameOver(_currPlayer);
					}
				}
			}
		}
		_game.changeInstructions(_currPlayer.getName() + "'s Turn");
		_game.toggleButtons();
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//                                                                                                                   //
//                                           **THESE ARE ACCESSOR METHODS**                                          //
//                                                                                                                   //
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * The RealEstate classes need access to the gamePane since they have a lot of graphical changes going on, but
	 * they don't need access to the Game as a whole, so this accessor method is the middleman.
	 */
	public Pane getGamePane() {
		return _game.getGamePane();
	}
	
	/**
	 * The Structure class needs to know if it's gameplay or not when a settlement has been placed (since during
	 * set-up the player doesn't lose resources for a settlement), so this accessor method provides that info.
	 */
	public Boolean isGameplay() {
		return _gameplay;
	}
	
	/**
	 * The RealEstate classes need to know if the players are choosing their initial resources so that they know what
	 * their response to mouse interactino should be!
	 */
	public Boolean choosingPregameResources() {
		return _choosingPregameResources;
	}

	/**
	 * This accessor method returns the HashMap so that the game class can access it whenever the dice is rolled.
	 */
	public HashMap<Integer,Tile[]> getDiceRollToTiles() {
		return _diceRollToTiles;
	}
}
