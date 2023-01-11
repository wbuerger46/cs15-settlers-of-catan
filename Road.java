package myindy.settlersOfCatan;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

/**
 * This is the Road class! It extends the RealEstate class by adding the graphical components of the road and adding
 * the Array that holds the structures which this road connects. The orientation of the road indicates its physical
 * orientation. 1 means the road extends vertically between structures, 2 means the road slopes downward to the right,
 * and 3 means the road slopes upward to the right.
 */
public class Road extends RealEstate {
	
	private Structure[] _structures;

	/**
	 * The constructor of the Road extends the RealEstate constructor by graphically making the road and
	 * initializing the Structure array.
	 */
	public Road(int orientation, double x, double y, Board board) {
		super(board);
		this.makeRoad(orientation, x, y);
		_structures = new Structure[2];
	}
	
	/**
	 * Unlike structures, the actual shape graphic of the Road never changes, so we only need to make the road once
	 * during initialization. I use a switch statement since the angle of road depends on its orientation.
	 */
	private void makeRoad(int orientation, double x, double y) {
		Rectangle road = new Rectangle(Constants.ROAD_LENGTH,Constants.ROAD_WIDTH);
		road.setX(x + Constants.ROAD_X_OFFSET);
		road.setY(y - Constants.ROAD_WIDTH/2);
		double angle = 0;
		switch (orientation) {
		case 1:
			angle = 90;
			break;
		case 2:
			angle = 30;
			break;
		case 3:
			angle = 150;
			break;
		}
		road.getTransforms().add(new Rotate(angle,x,y));
		road.setVisible(false);
		this.setImage(road);
	}
	
	/**
	 * This method is used while the board is being set up so that the roads know which settlements exist at each end
	 * of the road. A road always connects two structures, so _structures is an Array with two entries.
	 */
	public void setStructure(Structure structure) {
		if (_structures[0] == null) {
			_structures[0] = structure;
		} else {
			_structures[1] = structure;
		}
	}
	
	/**
	 * This method is called during many of the Board's recursive methods to find valid structures or roads. Given a
	 * parameter of one of the road's structure's, it returns the other structure so the recursion continue in the
	 * proper direction.
	 */
	public Structure getOtherStructure(Structure structure) {
		for (Structure otherStructure: _structures) {
			if (otherStructure != structure) {
				return otherStructure;
			}
		}
		return null;
	}
	
	/**
	 * Simple accessor method returns both of this road's connecting structures.
	 */
	public Structure[] getStructures() {
		return _structures;
	}
	
	/**
	 * This method fills out the abstract method from the RealEstate parent class. It uses a switch statement
	 * depending on the "status" of the road (purchased vs. unpurchased) to set its color.
	 */
	@Override
	public void newImage(int status, Color color) {
		switch (status) {
		case 0:
			this.getImage().setFill(Color.BLACK);
			break;
		case 1:
			this.getImage().setFill(color);
			this.getImage().setStroke(Color.BLACK);
		}
	}
}
