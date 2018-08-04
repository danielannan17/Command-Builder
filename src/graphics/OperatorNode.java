package graphics;

import java.util.Hashtable;

import com.fasterxml.jackson.core.JsonProcessingException;

import classes.Branch;
import classes.Leaf;
import classes.TooltipMenuItem;
import classes.Tree;
import graphics.Pin.PinType;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import windows.Main;

public class OperatorNode extends GraphicalNode {
	
	ContextMenu menu;
	Operation selected = null;
	public static enum Operation {
		SEMICOLON, ERROR, SAVE, APPEND
	}
	
	/**
	 * Class Constructor for creating node at a given position
	 * @param x The X position
	 * @param y The Y position
	 */
	public OperatorNode(double x, double y) {
		super("Operator");
		output = new Pin(this,PinType.Next);
		output.getVisual().xProperty().bind(Bindings.createDoubleBinding(() -> 
		(visual.getWidth()/2) + (text.getBoundsInLocal().getWidth()/2) +5, 
		visual.widthProperty(),text.textProperty()));
		this.getChildren().add(output.getVisual());
		output.getVisual().yProperty().bind(Bindings.createDoubleBinding(() -> 
		(output.getVisual().getHeight()/2) + text.getY() + (text.getBoundsInLocal().getHeight()/2), 
		text.boundsInLocalProperty())); 
		output.getVisual().xProperty().bind(Bindings.createDoubleBinding(() -> 
		(visual.getWidth()/2) + (text.getBoundsInLocal().getWidth()/2) -10, 
		visual.widthProperty(),text.textProperty()));
		this.relocate(x,y);
		arguments = new Pin[1]; 
	    arguments[0] = new Pin(this,PinType.Input);
	    arguments[0].getVisual().setVisible(false);
	    arguments[0].getVisual().setX(5);
	    arguments[0].getVisual().yProperty().bind(Bindings.createDoubleBinding(() -> 
		(arguments[0].getVisual().getHeight()/2) + text.getY() + (text.getBoundsInLocal().getHeight()/2), 
		text.boundsInLocalProperty()));
	    
	    Tooltip t = new Tooltip(getInput());
    	Tooltip.install(arguments[0].getVisual(), t);
    	this.getChildren().add(arguments[0].getVisual());

		output.getVisual().setVisible(false);
		setupContextMenu();
		
	}
	
	/**
	 * Creates an operator node from the hash table
	 * @param table The table to create the node from
	 */
	public OperatorNode(Hashtable<String, String> table) {
		this(Double.parseDouble(table.get("x")), Double.parseDouble(table.get("y")));
		changeToOperation(Operation.valueOf(table.get("operator")));
		
		this.setNodeId(table.get("id"));
		try {
			if (arguments != null) {
				String args = table.get("args");
				String[] argArray = args.split(",");
				for (int i = 0; i < arguments.length; i++) {
					arguments[i].setPinId(argArray[i]);
				}
			}
			output.setPinId(table.get("output"));
		} catch (Exception e) {

		}
	}

	public String toStorageValue() throws JsonProcessingException {
		if (selected == null)
			return null;
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("id", this.getNodeId());
		table.put("type", "" + NodeType.OperatorNode.getType());
		table.put("operator", selected.name());
		table.put("x", "" + this.getLayoutX());
		table.put("y", "" + this.getLayoutY());

		String args = "";
		if (arguments != null) {
			for (Pin pin : arguments) {
				args += pin.getPinId() + ",";
			}
			args = args.substring(0, args.length() - 1);
		}
		table.put("args", args);
		table.put("output", output.getPinId());
		return Main.mapper.writeValueAsString(table);
		
	}
	
	/**
	 * Gets the name of the operator
	 * @return the name
	 */
	public String getName() {
		switch (selected) {
        case SEMICOLON:
        	return "; (Semi-Colon)";
        case SAVE:
        	return "> (Save)";
        case APPEND:
        	return ">> (Append)";
        case ERROR:
        	return "2> (Error)";
        default:
        	return "[Operator]";
        }
		
	}
		
	/**
	 * Gets the description of what the operator does
	 * @return The description
	 */
	public String getDescription() {
		return getDescription(selected);
	}
	
	/**
	 * Gets the description for the operation given
	 * @param op the operation
	 * @return the description
	 */
	public String getDescription(Operation op) {
		switch (op) {
		case SEMICOLON:
        	return "; (Semi-Colon)";
        case SAVE:
        	return "> (Save)";
        case APPEND:
        	return ">> (Append)";
        
        case ERROR:
        	return "2> (Error)";
        default:
        	return "[Operator]";
        }
	}
	
	/**
	 * Gets the symbol of this operatorNode
	 * @return The symbol
	 */
	public String getSymbol() {
		switch (selected) {
        case SEMICOLON:
        	return ";";
        case SAVE:
        	return ">";
        case APPEND:
        	return ">>";
        
        case ERROR:
        	return "2>";
        default:
        	return "[Operator]";
        }

	}
	
	/**
	 * Gets the description for the input
	 * @return the value used as the description
	 */
	public String getInput() {
		if (selected == Operation.SEMICOLON) {
			return "First command";
		} else {
			return "From";
		}
	}
	
	/**
	 * Gets the description for the output
	 * @return the value used as the description
	 */
	public String getOutputVal() {
		if (selected == Operation.SEMICOLON)
			return "Second command";
		else
			return "To";
	}
	
	/**
	 * Changes the operator of this node
	 * @param op The new operator
	 */
	public void changeToOperation(Operation op) {
		selected = op;
        text.setText(getSymbol());
        Tooltip t = new Tooltip(getDescription());
    	Tooltip.install(visual,t);
    	
	    t = new Tooltip(getInput());
    	Tooltip.install(arguments[0].getVisual(), t);

    	
 	    arguments[0].getVisual().setVisible(true);
 	 
    	
    	output.getVisual().setVisible(true);
    	t = new Tooltip(getOutputVal());
    	Tooltip.install(output.getVisual(), t);
	   	addConnector(output);
	   	Main.graphics.onBuildGraphToCommand();
	}

	@Override
	protected void setupContextMenu() {
		menu = new ContextMenu();
		StackPane sp;
		for (Operation op : Operation.values()) {	
			sp = new StackPane();
			TooltipMenuItem item = new TooltipMenuItem(op.name(), getDescription(op),sp);
			item.setOnAction(new EventHandler<ActionEvent>() {
			    @Override
			    public void handle(ActionEvent event) {
			        changeToOperation(op);
			    }});
			menu.getItems().add(item);
		}
		visual.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
	        @Override
	        public void handle(ContextMenuEvent event) {
	            menu.show(visual, event.getScreenX(), event.getScreenY());
	        }
	    });
	}

	@Override
	public Tree toTree() {
		if (selected == null)
			return new Leaf("[Operator]");
		return new Branch(this);
	}

	@Override
	public String getArgumentName(int i) {
		return getInput();
	}

	@Override
	public Pin getPin(String id) {
		for (Pin t : arguments) {
			if (id.equals(t.getPinId()))
				return t;
		}
		if (id.equals(output.getPinId()))
			return output;
		return null;
	}

	
	@Override
	public Rectangle makeVisual() {
		StackPane sp = new StackPane();
		Rectangle header = new Rectangle(100, 30);
		header.setArcHeight(20);
		header.setArcWidth(20);
		header.setOpacity(0.3);
		header.setFill(Color.RED);
		text.setWrappingWidth(header.getWidth() - 40);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setMouseTransparent(true);
		text.textProperty().addListener((observable, oldValue, newValue) -> {
		    header.heightProperty().set(Math.max(35, text.getBoundsInLocal().getHeight()+6));
		    header.widthProperty().set(Math.max(60, text.getBoundsInLocal().getWidth()+10));
		});
		sp.getChildren().addAll(header, text);
		this.getChildren().addAll(sp);
		return header;
	}


	
	/**
	 * Checks if the node had its type selected
	 * @return Whether a type is selected
	 */
	public boolean isSelected() {
		return (selected != null);
	}


	@Override
	protected void setNodeType() {
		nodeType = NodeType.OperatorNode;
		
	}
}
