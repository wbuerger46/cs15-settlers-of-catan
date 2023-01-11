package myindy.settlersOfCatan;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
 * This is the Tile class! The Tiles themselves aren't too involved with game functionality, so almost all code in
 * this class is only used during initialization of the game (with the exception of the getResource() method).
 */
public class Tile {

	private Board _board;
	private ImageView _tile;
	private ImageView _numToken;
	private ImageView _robber;
	private String _resource;
	private Boolean _hasRobber;
	private Boolean _canPlaceRobber;
	
	/**
	 * The constructor of the tile sets up the tile's image, which corresponds to its resource. It also adds the
	 * robber to every tile, but the robber is only visible if it is truly on that tile.
	 */
	public Tile(String resource, Board board) {
		_resource = resource;
		_board = board;
		_tile = new ImageView(new Image(this.getClass().getResourceAsStream( resource + ".png")));
		_numToken = new ImageView();
		_robber = new ImageView(new Image(this.getClass().getResourceAsStream("Robber.png")));
		_robber.setPreserveRatio(true);
		_robber.setFitHeight(60);
		_tile.setOnMouseClicked(new PlaceRobberHandler());
		if (_resource == "Desert") {
			_hasRobber = true;
		} else {
			_robber.setVisible(false);
			_hasRobber = false;
		}
		_canPlaceRobber = false;
		_board.getGamePane().getChildren().addAll(_tile,_numToken,_robber);
	}
	
	/**
	 * This method is only called once, during board initialization. It sets the location of all graphics
	 * that are associated with the tile.
	 */
	public void setLoc(double x, double y) {
		_tile.setLayoutX(x);
		_tile.setLayoutY(y - Constants.TILE_Y_OFFSET);
		_numToken.setLayoutX(x + Constants.TOKEN_X_OFFSET);
		_numToken.setLayoutY(y + Constants.TOKEN_Y_OFFSET);
		_robber.setLayoutX(x + Constants.ROBBER_X_OFFSET);
		_robber.setLayoutY(y);
	}
	
	/**
	 * Once the tile has been assigned a dice roll value, we can add the number token image to the tile here.
	 */
	public void setDiceRoll(int roll) {
		_numToken = new ImageView(new Image(this.getClass().getResourceAsStream(roll + ".png")));
		_numToken.setFitHeight(50);
		_numToken.setFitWidth(50);
		//The graphic needs to be updated!
		_board.getGamePane().getChildren().removeAll(_numToken,_robber);
		_board.getGamePane().getChildren().addAll(_numToken,_robber);
		_numToken.setOnMouseClicked(new PlaceRobberHandler());
	}
	
	/**
	 * This method is called whenever this tile was chosen to hold the robber.
	 */
	private void setRobber() {
		_hasRobber = true;
		_robber.setVisible(true);
	}
	
	/**
	 * This method is called whenever this tile no longer has to hold the robber.
	 */
	public void losesRobber() {
		_hasRobber = false;
		_robber.setVisible(false);
	}
	
	/**
	 * This method is actually really important since every time this tile's value is rolled by the dice, it
	 * distributes this resource to all players with that tile!
	 */
	public String getResource() {
		return _resource;
	}
	
	/**
	 * This accessor method returns whether or not this tile has the robber so it knows if it should block
	 * resource production.
	 */
	public Boolean hasRobber() {
		return _hasRobber;
	}
	
	/**
	 * This is called whenever a player is about to move the robber so that the tile knows it should be ready to
	 * accept the robber.
	 */
	public void canPlaceRobber() {
		_canPlaceRobber = true;
	}
	
	/**
	 * This is called whenever a player moves the robber to another tile so this tile knows it doesn't need to be
	 * ready to accept the robber anymore.
	 */
	public void cannotPlaceRobber() {
		_canPlaceRobber = false;
	}
	
	/**
	 * This EventHandler is used to trigger placement of the robber onto this tile. It only occurs if _canPlaceRobber
	 * is true (i.e. when a player is about to place the robber).
	 */
	private class PlaceRobberHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent e) {
			if (_canPlaceRobber) {
				Tile.this.setRobber();
				_board.robberPlaced();
			}
		}
	}
}
