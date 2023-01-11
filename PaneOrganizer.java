package myindy.settlersOfCatan;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * This PaneOrganizer is very simple since all of the organization logic exists in the Game and Board classes. This
 * class just deals with the home screen and choosing between a 3-player or 4-player game. Once chosen, this class
 * just puts all the pieces together into a nice, neat root BorderPane.
 */
public class PaneOrganizer {
	private BorderPane _root;
	
	/**
	 * This constructor creates the root BorderPane, and sets up the "Welcome" screen. It is very basic, with just
	 * the Settlers of Catan logo and two buttons for the user to choose between a 3-player or 4-player game.
	 */
	public PaneOrganizer() {
		_root = new BorderPane();
		_root.setBackground(new Background(new BackgroundImage(
				new Image(this.getClass().getResourceAsStream("BackgroundTexture.png")),BackgroundRepeat.REPEAT,
				BackgroundRepeat.REPEAT,BackgroundPosition.CENTER,BackgroundSize.DEFAULT)));
		ImageView logo = new ImageView(new Image(this.getClass().getResourceAsStream("CatanLogo")));
		logo.setPreserveRatio(true);
		logo.setFitWidth(500);
		BorderPane.setAlignment(logo, Pos.CENTER);
		_root.setTop(logo);
		Button threePlayerGame = new Button("3 Players");
		Button fourPlayerGame = new Button("4 Players");
		threePlayerGame.setFont(new Font(40));
		fourPlayerGame.setFont(new Font(40));
		threePlayerGame.setOnAction(new StartGameHandler(3));
		fourPlayerGame.setOnAction(new StartGameHandler(4));
		HBox buttons = new HBox(threePlayerGame, fourPlayerGame);
		buttons.setSpacing(50);
		buttons.setAlignment(Pos.CENTER);
		_root.setCenter(buttons);
	}
	
	/**
	 * This method is called immediately after one of the Buttons on the Home screen is clicked. It then
	 * initializes the Game and adds the Game Pane and Settings Pane to their sides of the root pane.
	 */
	public void makeGame(int numPlayers) {
		Game game = new Game(numPlayers);
		_root.setTop(null);
		_root.setCenter(game.getGamePane());
		_root.setRight(game.getSettingsPane());
	}
	
	/**
	 * Returns the root pane so it can be added to the stage!
	 */
	public BorderPane getRoot() {
		return _root;
	}
	
	/**
	 * This EventHandler deals with when one of the start game buttons is clicked. Depending on the integer
	 * parameter it will trigger the start of the game with either 3 or 4 players.
	 */
	private class StartGameHandler implements EventHandler<ActionEvent> {
		private int _numPlayers;
		public StartGameHandler(int numPlayers) {
			_numPlayers = numPlayers;
		}
		
		@Override
		public void handle(ActionEvent e) {
			PaneOrganizer.this.makeGame(_numPlayers);
		}
	}
}
