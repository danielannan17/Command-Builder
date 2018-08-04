package graphics;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import windows.Main;

public class Connector extends Line {
	private GraphicalNode firstNode, lastNode;
	private Pin firstPin, secondPin;

	/**
	 * Class Constructor
	 */
	public Connector() {
		this.setStrokeWidth(5);
		this.setMouseTransparent(true);
		this.setStroke(Color.GRAY);

			

	}

	/**
	 * Gets the first pin this connector is connected to
	 * @return the first pin
	 */
	public Pin getFirstPin() {
		return firstPin;
	}

	/**
	 * Sets whether this node is selected
	 * @param selected Whether its selected
	 */
	public void setSelected(boolean selected) {

		if (selected)
			this.setStroke(Color.RED);
		else
			this.setStroke(Color.GRAY);
	}

	/**
	 * Gets the second pin this connector is connected to
	 * @return the second pin
	 */
	public Pin getSecondPin() {
		return secondPin;
	}

	/**
	 * Deletes the connector and unbinds the connected nodes
	 */
	public void remove() {
		if (firstPin != null) {
			firstPin.setConnect(this, null);
			firstPin.setBound(false);
		}
		if (secondPin != null) {
			secondPin.setConnect(this, null);
			secondPin.setBound(false);
		}
		Main.graphics.removeConnector(this);
	}

	/**
	 * Deletes the connector and unbinds the only one node
	 * @param node The node that will stay bound
	 */
	public void remove(GraphicalNode node) {
		if (node == lastNode) {
			firstPin.setConnect(this, null);
			firstPin.setBound(false);
		} else if (node == firstNode)
			if (secondPin != null) {
				secondPin.setConnect(this, null);
				secondPin.setBound(false);
			}
		Main.graphics.removeConnector(this);
	}

	/**
	 * Sets the first node
	 * @param firstNode the node its connected to
	 */
	public void setFirstNode(GraphicalNode firstNode) {
		this.firstNode = firstNode;
	}

	/**
	 * Sets the last node
	 * @param lastNode the node its connected to
	 */
	public void setLastNode(GraphicalNode lastNode) {
		this.lastNode = lastNode;
	}

	/**
	 * Sets the first pin
	 * @param pin the pin
	 */
	public void setFirstPin(Pin pin) {
		firstPin = pin;
		
	}

	/**
	 * Sets the second pin
	 * @param pin the pin
	 */
	public void setSecondPin(Pin pin) {
		secondPin = pin;

	

		
	}

	/**
	 * Checks if the node is the first node
	 * @param node The node that is being checked
	 * @return whether it is the first node
	 */
	public boolean isFirst(GraphicalNode node) {
		if (node.equals(firstNode))
			return true;
		return false;
	}

	/**
	 * Gets the first node
	 * @return the first node
	 */
	public GraphicalNode getFirstNode() {
		return firstNode;
	}

	/**
	 * Gets the last node
	 * @return the last node
	 */
	public GraphicalNode getLastNode() {
		return lastNode;
	}

	/**
	 * Gets the node at the other end of the connector. Works by checking if the node is one
	 * of the nodes the connector is connected to and if it is it returns the other node, otherwise
	 * it returns null.
	 * @param node The node being compared
	 * @return The other node
	 */
	public GraphicalNode getOtherEnd(GraphicalNode node) {
		if (node.equals(firstNode)) {
			return lastNode;
		} else if (node.equals(lastNode)) {
			return firstNode;
		}
		return null;
	}

	/**
	 * Gets the pin at one end of the node
	 * @param node The parent old node holding the pin that will not be returned
	 * @return The pin
	 */
	public Pin getOtherPin(GraphicalNode node) {
		if (node.equals(firstNode)) {
			return secondPin;
		} else if (node.equals(lastNode)) {
			return firstPin;
		}
		return null;
	}

}
