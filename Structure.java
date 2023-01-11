package myindy.settlersOfCatan;

import java.util.ArrayList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * This is the Structure class! This class extends the RealEstate class, mostly by setting up the relationships
 * between the Structure and adjacent Roads, as well as between the Structure and its Tiles. The orientation of a
 * Structure indicates its position in relation to the roads around it. In the Board class's 2D Array, even-indexed
 * rows, starting with row 0, are orientation 1 (roads flank above, below left, and below right), and odd-indexed
 * rows are orientation 2 (roads flank above right, above left, and below). I also use an orientation 0 to indicate
 * that structure as an invalid move. This class also clarifies the graphical components of the structure, and how
 * it changes from unsettled --> settlement --> city.
 */
public class Structure extends RealEstate{
	
	private int _orientation;
	private ArrayList<Road> _roads;
	private ArrayList<Tile> _tiles;
	private double _xLoc;
	private double _yLoc;
	
	/**
	 * The Structure constructor extends a bit more than the inherited RealEstate constructor, mostly just by
	 * initializing a bunch of extra instance variables. Also, if it is a valid location (i.e. orientation isn't 0),
	 * it sets up the initial structure image.
	 */
	public Structure(int orientation, double x, double y, Board board) {
		super(board);
		_xLoc = x;
		_yLoc = y;
		_orientation = orientation;
		_roads = new ArrayList<Road>();
		_tiles = new ArrayList<Tile>();
		if (_orientation != 0) {
			this.newImage(0,Color.BLACK);
		}
	}
	
	/**
	 * Buying a Structure is a bit more complicated than buying a Road, so I've partially overridden the code from
	 * the parent class. If it's a city, it just needs to tell the owner that it bought this city. If it's a
	 * settlement, it needs to eliminate all structure locations that exist exactly one road away and then tell the
	 * player that it placed that new settlement.
	 */
	@Override
	public void buy() {
		super.buy();
		if (this.getStatus() == 1) {
			for (Road road: _roads) {
				road.getOtherStructure(this).nullifyStructure();
			}
			if (this.isGameplay()) {
				this.getOwner().boughtSettlement(this);
			} else {
				//If it's during setup, the player shouldn't lose any resources, so we just call newSettlement()
				this.getOwner().newSettlement(this);
			}
		} else {
			this.getOwner().boughtCity(this);
		}

	}
	
	/**
	 * This method fills out the abstract method from RealEstate. It uses a switch statement to assign the proper
	 * graphic. Also each time it has to reassign its location coordinates. Finally, it calls setImage with parameter
	 * of the created graphic so the pane reflects the change graphically.
	 */
	@Override
	public void newImage(int status, Color color) {
		Shape newImage = null;
		switch (status) {
		case 0:
			newImage = new Circle(10);
			break;
		case 1:
			newImage = new Polygon(Constants.SETTLEMENT_SHAPE);
			newImage.setFill(color);
			break;
		case 2:
			newImage = new Polygon(Constants.CITY_SHAPE);
			newImage.setFill(color);
			break;
		}
		newImage.setLayoutX(_xLoc);
		newImage.setLayoutY(_yLoc);
		newImage.setStroke(Color.BLACK);
		this.setImage(newImage);
	}
	
	/**
	 * Whenever this structure's location is no longer a valid move, this method is called so that this location
	 * can never be used as a valid placement.
	 */
	public void nullifyStructure() {
		if (_orientation != 0) {
			this.getImage().setVisible(false);
			_orientation = 0;
			this.setStatus(0);
		}
	}
	
	/**
	 * Typical accessor method called a lot during setup of the board and also to see if this structure is a valid
	 * location or not (returns 0 if not valid).
	 */
	public int getOrientation() {
		return _orientation;
	}
	
	/**
	 * This method is used during initialization of the board so that the structure knows with which tiles it is
	 * associated.
	 */
	public void assignTile(Tile tile) {
		if (tile.getResource() != "Ocean") {
			_tiles.add(tile);
		}
	}
	
	/**
	 * This method is called whenever a player buys a settlement or city so that it can update its HashMap
	 * of which Tiles it receives resources from.
	 */
	public ArrayList<Tile> getTiles() {
		return _tiles;
	}
	
	/**
	 * This method is called during assignment of roads to the adjacent structures! It takes the parameter of a road
	 * and puts that road into this structure's ArrayList of roads.
	 */
	public void addRoad(Road road) {
		_roads.add(road);
	}
	
	/**
	 * This method is called any time the board needs access to the structure's adjacent roads. Many times, this is
	 * necessary during a recursive method, so this accessor method takes one road as a parameter and will return all
	 * the roads except that one. During recursion, that is especially helpful so that the recursion doesn't keep
	 * repeating in the direction from which it already occured. If the road parameter is null, it just returns all
	 * of the roads (so that we don't need a separate getRoads() method).
	 */
	public ArrayList<Road> getOtherRoads(Road road) {
		if (road == null) {
			return _roads;
		}
		ArrayList<Road> otherRoads = new ArrayList<Road>();
		for (Road otherRoad: _roads) {
			if (otherRoad != road) {
				otherRoads.add(otherRoad);
			}
		}
		return otherRoads;
	}
	
	/**
	 * Unlike the getOtherRoads method, this method returns a single road at a certain index. In the Board class while
	 * assigning roads to their adjacent structures and vice versa, we need access to the last or second-to-last road
	 * in the structure's ArrayList, so this method can take a negative index (-1 means last, -2 means second-to-last).
	 */
	public Road getRoad(int index) {
		if (index < 0) {
			return _roads.get(_roads.size() + index);
		} else {
			return _roads.get(index);
		}
	}
}
