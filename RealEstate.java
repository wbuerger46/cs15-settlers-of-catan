package myindy.settlersOfCatan;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * This is the RealEstate abstract class! This class is inherited by the Road and Structure classes, and it
 * establishes many the methods shared because of how the board and the players interact with both roads and
 * structures. Most of this class relates to how the RealEstate can be opened for purchased, ultimately bought, and
 * how the graphics change with various mouse interactions. The BuyHandler in this class is the only EventHandler in
 * any of the RealEstate/Road/Structure classes. This class also has a handful of accessor and mutator methods so
 * that the subclasses can interact with a few of the instance variables.
 */
public abstract class RealEstate {

	private Shape _image;
	private Board _board;
	private Pane _gamePane;
	private Player _owner;
	private Player _currPlayer;
	private Boolean _canBuy;
	private int _status;
	
	/**
	 * The RealEstate constructor just takes the Board as a parameter to set up its association with the Board, which
	 * is crucial to gameplay so that the game knows the proceed once the player has placed its RealEstate. Otherwise,
	 * this superclass constructor just initializes the instance variables.
	 */
	public RealEstate(Board board) {
		_image = null;
		_board = board;
		_gamePane = _board.getGamePane();
		_owner = null;
		_currPlayer = null;
		_canBuy = false;
		_status = 0;
	}
	
	/**
	 * This method is called anytime this piece of RealEstate is eligible to be purchased. It makes sure
	 * status < 2 so that a city is not mistakenly purchased again; the Board class ensures that roads aren't
	 * repeatedly purchased.
	 */
	public void readyForPurchase(Player player) {
		if (_status < 2) {
			_currPlayer = player;
			_image.setVisible(true);
			_canBuy = true;
			_image.setOnMouseEntered(new BuyHandler());
		}
	}
	
	/**
	 * When a RealEstate is bought, this method is called. The graphic is changed separately in the BuyHandler.
	 */
	public void buy() {
		_owner = _currPlayer;
		_status += 1;
		_canBuy = false;
	}
	
	/**
	 * This is the fundamental difference between the Road and Structure classes - they have completely different
	 * graphics. This abstract method is called every time a graphic changes, whether it's toggling on a mouse hover
	 * or permanently changing for a purchase. It takes parameter status to indicate to what graphic it should show
	 * (more info in the respective classes). The parameter color just reflects the current player's color.
	 */
	public abstract void newImage(int status, Color color);
	
	
	/**
	 * This method is called every time a graphic is added or changed! The graphic has already been made in the
	 * subclass, but the instance variable _image is only in the parent class, so this is called from the Road
	 * and Structure classes.
	 */
	public void setImage(Shape image) {
		_gamePane.getChildren().remove(_image);
		_image = image;
		_gamePane.getChildren().add(_image);
	}
	
	/**
	 * This accessor method is important so that the graphic can be added to the board, and so that the subclasses
	 * can have access to the graphic.
	 */
	public Shape getImage() {
		return _image;
	}
	
	/**
	 * This accessor method is necessary so that the board knows who owns which RealEstate.
	 */
	public Player getOwner() {
		return _owner;
	}
	
	/**
	 * Only this parent class has access to the board; this method is used by the Structure class to know when
	 * the gameplay has started.
	 */
	public Boolean isGameplay() {
		return _board.isGameplay();
	}
	
	/**
	 * Most of the status adjustments occur within this abstract class, but the Structure class needs to set its
	 * status to 0 when it has become an invalid move, so it calls this mutator method.
	 */
	public void setStatus(int status) {
		_status = status;
	}
	
	/**
	 * This accessor method is called by the Structure class but also mainly by the Board class so it knows which
	 * structures are settlements and which are cities.
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Usually the Boolean _canBuy is set in other methods, but since we use the same "BuyHandler" (to consolidate all
	 * Mouse Interaction into one EventHandler), we set _canBuy to true when the settlement is possibly going to be
	 * chosen by the player for selecting its initial resources.
	 */
	public void readyForSelection() {
		_canBuy = true;
	}
	
	/**
	 * This mutator method is called whenever a different piece of RealEstate has been purchased, so this one can't
	 * be bought anymore!
	 */
	public void cannotBuy() {
		_canBuy = false;
	}
	
	/**
	 * This is the only EventHandler used for all RealEstate, specifically how players are placing roads, settlements,
	 * and cities onto the board. The boolean instance variable _canBuy is used to indicate when the RealEstate can be
	 * purchased (or interacted with the mouse). If this EventHandler is called while choosing pregame resources, it
	 * tells the player to collect its resources. I also have functionality so that while a player is placing a
	 * settlement, the mouse's "hovering" graphically shows that RealEstate, toggled as the mouse enters and exits
	 * the image.
	 */
	private class BuyHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent e) {
			if (_canBuy) {
				if (_board.choosingPregameResources()) {
					if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
						_currPlayer.preGameResources((Structure) RealEstate.this);
						_canBuy = false;
						_board.setUpTurns(_currPlayer, null);
					}
				} else {
					if (e.getEventType() == MouseEvent.MOUSE_CLICKED) {
						//Since the Mouse already had to enter before clicking is possible, the image would already have
						//changed to reflect the purchase.
						RealEstate.this.buy();
						_board.purchased(_currPlayer, RealEstate.this);
					} else if (e.getEventType() == MouseEvent.MOUSE_ENTERED) {
						RealEstate.this.newImage(_status + 1,_currPlayer.getColor());
						//I have to add these EventHandlers *after* the image changes
						_image.setOnMouseExited(new BuyHandler());
						_image.setOnMouseClicked(new BuyHandler());
					} else {
						RealEstate.this.newImage(_status,_currPlayer.getColor());
						//Again, EventHandler is added *after* the image changes
						_image.setOnMouseEntered(new BuyHandler());
					}
				}
			}
		}
	}
}
