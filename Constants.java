package myindy.settlersOfCatan;

import javafx.scene.text.Font;

/**
 * This is the Constants class! It contains many important constants used in the game. Most of them are graphical
 * constants, whether by indicating dimensions, or points of a shape, or coordinates of an array that should be empty. 
 */
public class Constants {

	public static final int SCENE_WIDTH = 1100; //UNITS: Pixels
	public static final int SCENE_HEIGHT = 750; //UNITS: Pixels
	public static final int GAME_PANE_WIDTH = 750; //UNITS: Pixels
	
	public static final int TILE_SCALAR = 70; //UNITS: Pixels
	public static final int[][] EMPTY_TILE_LOCS = {{0,3},{0,4},{1,4},{3,0},{4,0},{4,1}}; //Array Coordinates
	public static final double FIRST_TILE_Y = 120; //UNIT: Pixels
	public static final double FIRST_TILE_X = 205; //UNIT: Pixels
	public static final double TILE_X_INCREMENT = TILE_SCALAR * Math.sqrt(3)/2; //UNIT: Pixels
	public static final double TILE_Y_INCREMENT = TILE_SCALAR * 3/2; //UNIT: Pixels
	public static final int[][] EMPTY_STRUCTURE_LOCS = {{0,3},{0,4},{0,5},{1,4},{1,5},{2,4},{2,5},{3,5},{4,5},
			{7,0},{8,0},{9,0},{9,1},{10,0},{10,1},{11,0},{11,1},{11,2}}; //Array Coordinates
	public static final double FIRST_STRUCTURE_Y = FIRST_TILE_Y - TILE_SCALAR * 1/2; //UNIT: Pixels
	public static final double FIRST_STRUCTURE_X = FIRST_TILE_X + TILE_X_INCREMENT; //UNIT: Pixels
	public static final double STRUCTURE_SMALLER_Y_INCREMENT = TILE_SCALAR * 1/2; //UNIT: Pixels
	public static final double STRUCTURE_LARGER_Y_INCREMENT = TILE_SCALAR; //UNIT: Pixels
	public static final double FIRST_BACKGROUND_TILE_X = FIRST_TILE_X - TILE_X_INCREMENT * 4; //UNITS: Pixels
	public static final double FIRST_BACKGROUND_TILE_Y = FIRST_TILE_Y - TILE_Y_INCREMENT * 2; //UNITS: Pixels
	
	public static final double ROBBER_X_OFFSET = 49;
	public static final double TOKEN_Y_OFFSET = 10; //UNIT: Pixels
	public static final double TOKEN_X_OFFSET = 37; //UNIT: Pixels
	public static final double TILE_Y_OFFSET = 22*Math.sqrt(3); //UNIT: Pixels
	
	public static final double ROAD_WIDTH = 6; //UNIT: Pixels
	public static final double ROAD_LENGTH = 44; //UNIT: Pixels
	public static final double ROAD_X_OFFSET = 13; //UNIT: Pixels

	public static final double[] SETTLEMENT_SHAPE = {-9,-4,0,-14,9,-4,9,11,-9,11}; //UNITS: Pixels
	public static final double[] CITY_SHAPE = {-15,-5,-6,-15,3,-5,3,-2,14,-2,14,11,-15,11}; //UNITS: Pixels
	
	public static final Font PLAIN_FONT = new Font(13);
	
	public static final double NOTIFICATION_PANE_X = 136; //UNITS: Pixels
	public static final double NOTIFICATION_PANE_Y = 200; //UNITS: Pixels
}
