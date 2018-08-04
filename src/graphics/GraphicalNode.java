package graphics;

import java.util.UUID;

import javax.swing.JButton;

import classes.Tree;
import graphics.Pin.PinType;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import windows.Main;

/**
 * The super class used for creating a node which can be added to the scene
 * @author Daniel Annan - 1569423
 *
 */

public abstract class GraphicalNode extends Group {
	protected Rectangle visual;
	String nodeId;
	Text text;
	Pin output;
	NodeType nodeType;
	boolean clicked = false;
	Pin[] arguments;
	Connector tempConnector;
	JButton moveButton;
	Point2D a;
	static double headerHeight = 40;

	/**
	 * Class Constructor
	 * @param string The text to be displayed on the node
	 */
	GraphicalNode(String string) {
		setNodeType();
		nodeId = UUID.randomUUID().toString();
		text = new Text(string);
		text.setFill(Color.WHITE);
		visual = makeVisual();
		setupDragNDrop();
		visual.setStrokeWidth(3);
		visual.setOnMouseEntered(e -> {
			if (Main.graphics.getCarrying() == null && Main.graphics.getSelected() != this) {
				visual.setStroke(Color.BLUE);
			}
		});
		
		visual.setOnMouseExited(e -> {
			Main.graphics.setHovering(null);
			if (Main.graphics.getHovering() == this) {
				Main.graphics.setHovering(null);
			}
			if (Main.graphics.getSelected() != this) {
				visual.setStroke(null);
			}
	
		});
	
	}

	/**
	 * Converts this node into a string to be used for storage.
	 * @return The string to be used for storing
	 * @throws Exception
	 */
	public abstract String toStorageValue() throws Exception;



	/**
	 * Gets the Pin corresponding to the given id
	 * @param id The id of the pin
	 * @return The pin object returned
	 */
	public abstract Pin getPin(String id);

	/**
	 * Sets the node type
	 * @param nodeType The new node Type
	 */
	protected abstract void setNodeType();
	
	/**
	 * Gets the moveButton
	 * @return
	 */
	public JButton getMoveButton() {
		return moveButton;
	}

	/**
	 * Sets the moveButton
	 * @param associatedMove
	 */
	public void setMoveButton(JButton moveButton) {
		this.moveButton = moveButton;
	}

	/**
	 * Deletes this node from the scene
	 */
	public void remove() {
		if (arguments != null)
			for (Pin arg : arguments) {
				if (arg.isBound())
					arg.getConnect().remove();
			}
		if (output != null && output.isBound())
			output.getConnect().remove();

	}

	/**
	 * Gets the nodeId
	 * @return
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Sets the nodeId
	 * @param nodeId
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	
	/**
	 * Gets the node type
	 * @return the node types
	 */
	public NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * Gets the argument description at a given index
	 * @param i The index of the argument
	 * @return the argument description
	 */
	public abstract String getArgumentName(int i);

	/**
	 * Adds a context menu and fills it with the necessary options
	 */
	protected abstract void setupContextMenu();

	/**
	 * Converts this node into a Tree Object
	 * @return The Tree
	 */
	public abstract Tree toTree();

	/**
	 * Gets a list of the graphical representation of the arguments
	 * @return
	 */
	public Pin[] getArguments() {
		return arguments;
	}

	/**
	 * Creates a FlowPane to store arguments
	 * @param ori The orientation of the flow pane
	 * @return The created flow pane
	 */
	protected FlowPane makeFlowPane(Orientation ori) {
		double spacing = 5;
		if (ori == Orientation.VERTICAL) {
			FlowPane p = new FlowPane(Orientation.VERTICAL);
			p.setPadding(new Insets(spacing, 0, spacing, 0));
			p.setVgap(spacing / 2);
			p.setHgap(spacing / 2);
			p.setPrefWrapLength((Pin.size * 2) + (spacing * 2));
			return p;
		} else {
			FlowPane p = new FlowPane(Orientation.HORIZONTAL);
			p.setPadding(new Insets(0, spacing, 0, spacing));
			p.setVgap(spacing / 2);
			p.setHgap(spacing / 2);
			p.setPrefWrapLength((Pin.size * 2) + (spacing * 2));
			return p;
		}
	}

	/**
	 * Creates the visual representation of this node to be displayed
	 * on the scene
	 * @return
	 */
	public abstract Rectangle makeVisual();
	
	/**
	 * Adds a connector to an arguments Pin
	 * @param arg The pin of the argument to add the connector to
	 */
	void addConnector(Pin arg) {
		arg.getVisual().setOnMousePressed(e -> {
			if (Main.graphics.getCarrying() == null) {
				Main.graphics.setCarrying(arg);
				tempConnector = new Connector();
				tempConnector.startXProperty()
						.bind(Bindings.createDoubleBinding(() -> arg.getVisual().getParent()
								.localToParent(arg.getVisual().getBoundsInParent()).getMinX() + (Pin.size / 2),
								this.layoutXProperty()));

				tempConnector.startYProperty()
						.bind(Bindings.createDoubleBinding(() -> arg.getVisual().getParent()
								.localToParent(arg.getVisual().getBoundsInParent()).getMinY() + (Pin.size / 2),
								this.layoutYProperty()));
				Point2D p = GraphicsManager.container.sceneToLocal(new Point2D(e.getSceneX(), e.getSceneY()));

				tempConnector.setEndX(p.getX());
				tempConnector.setEndY(p.getY());
				Main.graphics.addConnector(tempConnector);
				Main.graphics.connector = tempConnector;
				tempConnector.toBack();

				tempConnector.setFirstNode(this);
				Main.graphics.getScene().setOnMouseMoved(x -> {
					Point2D p2 = GraphicsManager.container.sceneToLocal(new Point2D(x.getSceneX(), x.getSceneY()));
					if (!tempConnector.endXProperty().isBound()) {
					tempConnector.endXProperty().set(p2.getX());
					tempConnector.endYProperty().set(p2.getY());
					
					}
				});
			}
		});
	}
		
	/**
	 * Returns the nodes visual representation
	 * @return The visual
	 */
	public Rectangle getVisual() {
		return visual;
	}

	/**
	 * Gets the X position in the scene
	 * @return The X position
	 */
	public double getSceneX() {
		return visual.getParent().localToScene(visual.getBoundsInParent()).getMinX()
				+ visual.getBoundsInLocal().getWidth() / 2;
	}

	/**
	 * Gets the Y position in the scene
	 * @return The Y position
	 */
	public double getSceneY() {
		return visual.getParent().localToScene(visual.getBoundsInParent()).getMinY()
				+ visual.getBoundsInLocal().getHeight() / 2;
	}

	/**
	 * Sets up the drag and drop functionality for the node
	 */
	private void setupDragNDrop() {
		
		visual.setOnMousePressed(e -> {
			clicked = true;
			Main.graphics.setSelected(this);
		});

		visual.setOnMouseDragged(e -> {
			a = GraphicsManager.container.sceneToLocal(
					new Point2D(e.getSceneX() - visual.getWidth() / 2, e.getSceneY() - visual.getHeight() / 2));
			this.relocate(a.getX(), a.getY());
			clicked = false;
		});

	}

	/**
	 * Gets the output pin
	 * @return The pin
	 */
	public Pin getOutput() {
		return output;
	}

}
