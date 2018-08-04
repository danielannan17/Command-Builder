package graphics;

import java.util.ArrayList;
import java.util.UUID;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import windows.Main;

/**
 * Used to display arguments and outputs on the node the
 * the pin is related to.
 * @author Daniel Annan - 1569423
 *
 */
public class Pin {
	String pinId;
	Rectangle visual;
	ArrayList<Connector> connectors;
	String description;
	static double size = 10;
	GraphicalNode parent;
	public static enum PinType {
		Input, Output, Next,OutputNext, Infinite, Disabled, InfiniteOutput
	}

	PinType type;
	private boolean canBind = true;
	SimpleBooleanProperty boundProperty = new SimpleBooleanProperty();
	/**
	 * Class Constructor
	 * @param parent The graphical node that acts as this nodes parent
	 * @param type The type of pin
	 */
	public Pin(GraphicalNode parent, PinType type) {
		this.parent = parent;
		this.type = type;
		this.visual = makeRectangle();
		connectors = new ArrayList<Connector>();
		addFunctions();
		pinId = UUID.randomUUID().toString();
	}
	
	/**
	 * Gets the bound property
	 * @return the bound property
	 */
	public SimpleBooleanProperty getBoundProperty() {
		return boundProperty;
	}

	/**
	 * Gets the type
	 * @return the type
	 */
	public PinType getType() {
		return type;
	}

	/**
	 * Changes the style of the pin based on the style chosen
	 * @param r The rectangle used for displaying the pin
	 */
	private void setStyle(Rectangle r) {
		setStyle(r,isBound());
	}
	
	/**
	 * Changes the style of the pin based on the style chosen
	 * @param r The rectangle used for displaying the pin
	 * @param b whether the pin is bound
	 */
	private void setStyle(Rectangle r, Boolean b) {
		if (type == PinType.Input) {
			if (b) {
				canBind = false;
				r.setStroke(Color.BLACK);
				r.setFill(Color.GREY);
			} else {
				r.setStroke(Color.GREY);
				r.setFill(Color.LAWNGREEN);
				canBind = true;
			}
		} else if (type == PinType.Infinite) {
				r.setStroke(Color.GREY);
				r.setFill(Color.LAWNGREEN);
				canBind = true;
		} else if (type == PinType.Output || type == PinType.Next || type == PinType.OutputNext|| 
				type == PinType.InfiniteOutput) {
			r.setStroke(Color.GREY);
			r.setFill(Color.DARKRED);
			canBind = false;
		} else if (type == PinType.Disabled) {
			canBind = false;
			r.setStroke(Color.BLACK);
			r.setFill(Color.GREY);
		}
	}
	
	
	/**
	 * Changes the style of the pin based on the style chosen
	 * @param style The style to change it to
	 */
	private void setStyle() {
		setStyle(visual, isBound());
	}

	/**
	 * Changes the style of the pin based on the style chosen
	 * @param style The style to change it to
	 */
	private void setStyle(boolean b) {
		setStyle(visual, b);
	}
	
	/**
	 * Adds the functionality for this pin
	 */
	private void addFunctions() {

		visual.setOnMouseEntered(e -> {
			Main.graphics.setHovering(visual);
			Main.graphics.setTempPin(this);
			if (Main.graphics.getSelected() != visual) {
				visual.setStroke(Color.BLUE);
			}
		});

		visual.setOnMouseExited(e -> {
			Main.graphics.setHovering(visual);
			if (Main.graphics.getHovering() == visual) {
				Main.graphics.setHovering(null);
			}
			if (Main.graphics.getSelected() != visual) {
				if (type == PinType.Input) {
					if (boundProperty.get())
						visual.setStroke(Color.BLACK);
					else
						visual.setStroke(Color.GRAY);
				} else if (type == PinType.Output || type == PinType.Infinite || type == PinType.Next ||
						type == PinType.OutputNext || type == PinType.InfiniteOutput) {
					visual.setStroke(Color.GRAY);

				} else if (type == PinType.Disabled) {
					visual.setStroke(Color.BLACK);
				}
			}
		});

		visual.setOnMouseClicked(e -> {
			Main.graphics.setSelected(visual);
		});

	}

	/**
	 * Creates the rectangle used as the visual for this pin
	 * @param style The style of the rectangle
	 * @return
	 */
	Rectangle makeRectangle() {
		Rectangle arg = new Rectangle();
		arg.setWidth(size);
		arg.setHeight(size);
		arg.setArcHeight(size / 2);
		arg.setArcWidth(size / 2);
		arg.setStrokeWidth(2);

		setStyle(arg);
		return arg;
	}

	/**
	 * Gets the pinId
	 * @return the pinId
	 */
	public String getPinId() {
		return pinId;
	}

	/**
	 * Sets the pinId
	 * @param id The new id
	 */
	public void setPinId(String id) {
		pinId = id;
	}

	
	public void setCanBind(boolean bind) {
		canBind = bind;
	}

	/**
	 * Sets whether the pin can be bound to
	 * @return when it can bind
	 */
	public boolean getCanBind() {
		return canBind;
	}

	/**
	 * Returns the visual for the pin
	 * @return the visual 
	 */
	public Rectangle getVisual() {
		return visual;
	}

	/**
	 * Sets the visual to a new rectangle
	 * @param rect The new visual
	 */
	public void setVisual(Rectangle rect) {
		this.visual = rect;
	}

	/**
	 * Returns the connector for this pin
	 * @return the connector
	 */
	public Connector getConnect() {
		if (type != PinType.Infinite && !connectors.isEmpty()) {
			return connectors.get(0);
		}
		return null;
	}

	
	/**
	 * Sets the connector for this pin, deleting the old if it has one
	 * @param old The old connector if it has one
	 * @param connect The new connector
	 */
	public void setConnect(Connector old, Connector connect) {
		if (type != PinType.Infinite) {
			setConnect(connect);
		} else {
			if (connect == null) {
				connectors.remove(old);
			} else if (old == null) {
				connectors.add(connect);
			} else if (old != null && connect != null) {
				connectors.remove(old);
				connectors.add(connect);
			} else {
				System.err.println("Pin - SetConnect:");
			}
		}
	}

	/**
	 * Sets the connector for this pin
	 * @param connect The new connector
	 */
	public void setConnect(Connector connect) {
		if (type != PinType.Infinite) {
			if (connectors.isEmpty())
				connectors.add(connect);
			else
				connectors.set(0, connect);
		}
	}

	
	/**
	 * Gets the parent for this pin
	 * @return the parent
	 */
	public GraphicalNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent of this node
	 * @param the parent
	 */
	public void setParent(GraphicalNode parent) {
		this.parent = parent;
	}

	/**
	 * Sets whether the pin is bound
	 * @param b
	 */
	public void setBound(boolean b) {
		if (b && type == PinType.Input) {
			setStyle(b);
		} else if (!b && type == PinType.Input) {
			setStyle(b);
		}

		if (type == PinType.Infinite)
			boundProperty.set(false);
		else
			boundProperty.set(b);
		setStyle();
	}

	/**
	 * Checks whether this pin is bound
	 * @return Whether the pin is bound
	 */
	public boolean isBound() {
		return boundProperty.get();
	}

	/**
	 * Gets the list connectors connected to this pin
	 * @return The list of connectors
	 */
	public ArrayList<Connector> getConnectors() {
		return connectors;
	}
}
