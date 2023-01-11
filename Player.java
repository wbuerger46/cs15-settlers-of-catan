package myindy.settlersOfCatan;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * This is the Player class! It contains all the logic for how players get cards and spend cards. It also keeps track
 * of all of the player's settlements/cities so that it can accurately track which cards to receive. This class
 * has an ArrayList that contains all of its structures and two HashMaps: one to keep track of on which tiles to
 * collect resources and another to keep track of the Player's cards. Also, this class creates the Player Pane, which
 * is shown in the Settings Pane, and updates the Labels as necessary.
 */
public class Player {
	private Color _color;
	private String _name;
	private int _score;
	private ArrayList<Structure> _myStructures;
	private HashMap<Tile,Integer> _myTiles;
	private HashMap<String,Integer> _myCards;
	private VBox _playerPane;
	private int _knightsUsed;
	private Label _nameLabel;
	private Label _lumberLabel;
	private Label _brickLabel;
	private Label _wheatLabel;
	private Label _woolLabel;
	private Label _oreLabel;
	private Label _knightLabel;

	/**
	 * The Player constructor initializes all of the instance variables and sets the player's name based on its color.
	 * The HashMap of cards starts with 0 of each card.
	 */
	public Player(Color color) {
		_color = color;
		switch (_color.toString()) {
		case "0x0000ffff":
			_name = ("Blue Player");
			break;
		case "0xffffffff":
			_name = ("White Player");
			break;
		case "0xffa500ff":
			_name = ("Orange Player");
			break;
		case "0xff0000ff":
			_name = ("Red Player");
			break;
		}
		_myStructures = new ArrayList<Structure>();
		_myTiles = new HashMap<Tile,Integer>();
		_myCards = new HashMap<String,Integer>();
		_myCards.put("Lumber", 0);
		_myCards.put("Brick", 0);
		_myCards.put("Wheat", 0);
		_myCards.put("Wool", 0);
		_myCards.put("Ore", 0);
		_myCards.put("Knight",0);
		_score = 0;
		_knightsUsed = 0;
		this.makePlayerPane();
	}
	
	/**
	 * This method sets up this Player's VBox that reflects its cards and points! This is only called once during
	 * initialization.
	 */
	private void makePlayerPane() {
		_playerPane = new VBox();
		_playerPane.setPrefWidth((Constants.SCENE_WIDTH - Constants.GAME_PANE_WIDTH) / 2);
		_nameLabel = new Label(_name + "  (0 pts)");
		_nameLabel.setFont(Constants.PLAIN_FONT);
		_nameLabel.setStyle("-fx-font-weight: bold");
		_lumberLabel = new Label("Lumber: 0");
		_brickLabel = new Label("Brick: 0");
		_wheatLabel = new Label("Wheat: 0");
		_woolLabel = new Label("Wool: 0");
		_oreLabel = new Label("Ore: 0");
		_knightLabel = new Label("Knights Used: 0");
		_lumberLabel.setFont(Constants.PLAIN_FONT);
		_brickLabel.setFont(Constants.PLAIN_FONT);
		_wheatLabel.setFont(Constants.PLAIN_FONT);
		_woolLabel.setFont(Constants.PLAIN_FONT);
		_oreLabel.setFont(Constants.PLAIN_FONT);
		_knightLabel.setFont(Constants.PLAIN_FONT);
		_playerPane.getChildren().addAll(_nameLabel, _lumberLabel, _brickLabel, _wheatLabel,
				_woolLabel, _oreLabel,_knightLabel);
		_playerPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.DASHED, null, null)));
	}
	
	/**
	 * This method is called on every dice roll. If the player has structures by the tiles that were rolled, the
	 * player collects more of those resources.
	 */
	public void distributeResource(Tile tile) {
		if (_myTiles.containsKey(tile)) {
			_myCards.compute(tile.getResource(), (k,v) -> v + _myTiles.get(tile));
			this.updateResourceCount();
		}
	}
	
	/**
	 * During pre-game set up and choosing of initial resources, those resources are distributed differently (not based
	 * on a roll of the dice). This method deals with distributing those initial resources, given the parameter of
	 * which structure the player wants to receive resources from.
	 */
	public void preGameResources(Structure structure) {
		for (Tile tile: structure.getTiles()) {
			if (tile.getResource() != "Desert") {
				_myCards.compute(tile.getResource(), (k,v) -> v+1);
				this.updateResourceCount();
			}
		}
	}
	
	/**
	 * This method is called whenever cards have been added or removed so that the Labels shows the change.
	 */
	private void updateResourceCount() {
		_lumberLabel.setText("Lumber: " + _myCards.get("Lumber"));
		_brickLabel.setText("Brick: " + _myCards.get("Brick"));
		_wheatLabel.setText("Wheat: " + _myCards.get("Wheat"));
		_woolLabel.setText("Wool: " + _myCards.get("Wool"));
		_oreLabel.setText("Ore: " + _myCards.get("Ore"));
	}
	
	/**
	 * This method is called anytime the player places a new settlement or city so it knows to collect more resources
	 * associated with that structure.
	 */
	private void increaseTile(Tile tile) {
		if (_myTiles.containsKey(tile)) {
			//increases count by 1
			_myTiles.compute(tile, (k,v) -> v+1);
		} else {
			_myTiles.put(tile, 1);
		}
	}
	
	/**
	 * This method deals with making sure the player knows it has the newly-purchased settlement and all tiles
	 * associated with it. Also updates score!
	 */
	public void newSettlement(Structure structure) {
		_myStructures.add(structure);
		for (Tile tile: structure.getTiles()) {
			this.increaseTile(tile);
		}
		_score += 1;
		_nameLabel.setText(_name + "  (" + _score + " pts)");
	}
	
	/**
	 * This method is separate from newSettlement() above since during setUp, the player doesn't need to pay for its
	 * settlements! Called whenever a settlement is bought during gameplay, so player loses the proper cards.
	 */
	public void boughtSettlement(Structure structure) {
		this.newSettlement(structure);
		this.loseResource("Lumber", 1);
		this.loseResource("Brick", 1);
		this.loseResource("Wheat", 1);
		this.loseResource("Wool", 1);
		this.updateResourceCount();
	}
	
	/**
	 * Called whenever this player buys a city so that it updates how many resources it gets per tile and loses the
	 * proper cards (3 ore + 2 wheat).
	 */
	public void boughtCity(Structure structure) {
		for (Tile tile: structure.getTiles()) {
			this.increaseTile(tile);
		}
		_score += 1;
		_nameLabel.setText(_name + "  (" + _score + " pts)");
		this.loseResource("Wheat", 2);
		this.loseResource("Ore", 3);
		this.updateResourceCount();
	}
	
	/**
	 * Called whenever this player buys a road to lose one lumber and one brick.
	 */
	public void boughtRoad() {
		this.loseResource("Lumber", 1);
		this.loseResource("Brick", 1);
		this.updateResourceCount();
	}
	
	/**
	 * Called whenever this player buys a development card to lose one wool, one ore, and one wheat.
	 */
	public void boughtDevCard() {
		this.loseResource("Wool", 1);
		this.loseResource("Ore", 1);
		this.loseResource("Wheat", 1);
		this.updateResourceCount();
	}
	
	/**
	 * Called everytime the player loses a card. Takes parameter resource (to know which resource to take away from)
	 * and also an integer that shows how many of that card to lose.
	 */
	private void loseResource(String resource, int loss) {
		_myCards.compute(resource, (k,v) -> v-loss);
	}
	
	/**
	 * Returns true if the player has the right cards to buy a road.
	 */
	public Boolean canBuyRoad() {
		if (_myCards.get("Brick") >= 1 && _myCards.get("Lumber") >= 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the player has the right cards to buy a settlement.
	 */
	public Boolean canBuySettlement() {
		if (_myCards.get("Brick") >= 1 && _myCards.get("Lumber") >= 1 && _myCards.get("Wheat") >= 1 &&
				_myCards.get("Wool") >= 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the player has the right cards to buy a city and has a settlement that can be upgraded.
	 */
	public Boolean canBuyCity() {
		if (_myCards.get("Ore") >= 3 && _myCards.get("Wheat") >= 2) {
			for (Structure structure: _myStructures) {
				if (structure.getStatus() == 1) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the player has the right cards to buy a development card.
	 */
	public Boolean canBuyDevCard() {
		if (_myCards.get("Ore") >= 1 && _myCards.get("Wool") >= 1 && _myCards.get("Wheat") >= 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns true if the player has any Knight cards to use.
	 */
	public Boolean canUseKnight() {
		if (_myCards.get("Knight") >= 1) {
			return true;
		}
		return false;
	}
	
	/**
	 * Called anytime the player gets points for a task that doesn't including buying real estate (e.g. getting
	 * longest road, largest army, or point development card).
	 */
	public void addVictoryPoints(int points) {
		_score += points;
		_nameLabel.setText(_name + "  (" + _score + " pts)");
	}
	
	/**
	 * Called when the player gets a knight card.
	 */
	public void oneKnightCard() {
		_myCards.compute("Knight", (k,v) -> v+1);
	}
	
	/**
	 * Called whenever the player uses a knight card.
	 */
	public void usedKnight() {
		_myCards.compute("Knight", (k,v) -> v-1);
		_knightsUsed += 1;
		_knightLabel.setText("Knights Used: " + _knightsUsed);
	}
	
	/**
	 * Returns true if the player has won the game (by having 8 or more points.
	 */
	public Boolean hasWon() {
		if (_score >= 8) {
			return true;
		}
		return false;
	}
	
	/**
	 * Accessor method so that we know what color this player's structures and roads should be!
	 */
	public Color getColor() {
		return _color;
	}
	
	/**
	 * This accessor method is just used for the instructions label, so it can address the proper player.
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Accessor method so the board can keep track of if this player has achieved largest army.
	 * @return
	 */
	public int getKnightsUsed() {
		return _knightsUsed;
	}
	
	/**
	 * This accessor method returns all of the player's purchased settlements/cities for many uses in the Board class.
	 */
	public ArrayList<Structure> getStructures() {
		return _myStructures;
	}
	
	/**
	 * Basic accessor method so this pane can be added to the Settings Pane.
	 */
	public Pane getPlayerPane() {
		return _playerPane;
	}
}
