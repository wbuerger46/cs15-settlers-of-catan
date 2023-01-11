package myindy.settlersOfCatan;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ah, yes, the App class! As usual, this launches the game and sets the stage for the game to begin!
 */

public class App extends Application {

	/**
	 * The start method creates the top-level object PaneOrganizer and sets the scene onto the stage.
	 * I chose to setResizable to false so that the stage always maintains the dimensions that I assigned.
	 */
    @Override
    public void start(Stage stage) throws Exception {
    	PaneOrganizer organizer = new PaneOrganizer();
    	Scene scene = new Scene(organizer.getRoot(),Constants.SCENE_WIDTH,Constants.SCENE_HEIGHT);
    	stage.setScene(scene);
    	stage.setTitle("SETTLERS OF CATAN");
    	stage.setResizable(false);
    	stage.show();
    }

    public static void main(String[] args) {
        launch(args); // launch is a method inherited from Application
    }
}
