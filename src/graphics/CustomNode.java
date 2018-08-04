package graphics;

import java.io.File;
import java.util.Hashtable;
import java.util.Optional;

import javax.swing.JFileChooser;

import com.fasterxml.jackson.core.JsonProcessingException;

import classes.Leaf;
import classes.TooltipMenuItem;
import classes.Tree;
import graphics.Pin.PinType;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import windows.Main;

/**
 * A node used for adding custom information such as text
 * @author Daniel Annan - 1569423
 *
 */
public class CustomNode extends GraphicalNode {
	ContextMenu menu;
	Custom selected = null;
	String value = null;
	boolean open = true;
	public static enum Custom {
		TEXT, FILE, DIRECTORY
	}

	/**
	 * Class Constructor for creating a custom node at a given location
	 * on the scene
	 * @param x The x location of the node
	 * @param y The y location of the node
	 */
	public CustomNode(double x, double y) {
		super("Custom");
		output = new Pin(this,PinType.Output);
		
		output.getVisual().xProperty().bind(Bindings.createDoubleBinding(() -> 
		(visual.getWidth()/2) + (text.getBoundsInLocal().getWidth()/2) +5, 
		visual.widthProperty(),text.textProperty()));
		
		
		
		this.getChildren().add(output.getVisual());
		
		output.getVisual().yProperty().bind(Bindings.createDoubleBinding(() -> 
		(output.getVisual().getHeight()/2) + text.getY() + (text.getBoundsInLocal().getHeight()/2), 
		text.boundsInLocalProperty())); 
	
		this.relocate(x, y);
		setupContextMenu();
		output.getVisual().setVisible(false);
		Tooltip t = new Tooltip(getDescription());
		Tooltip.install(visual, t);
		addConnector(output);
	}


	@Override
	public String toStorageValue() throws JsonProcessingException {
		if (selected == null)
			return null;
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("id", this.getNodeId());
		table.put("type", "" + NodeType.CustomNode.getType());
		table.put("custom", selected.name());
		if (value != null) {
			table.put("text", value);
		}
		table.put("x", "" + this.getLayoutX());
		table.put("y", "" + this.getLayoutY());

	
		table.put("output", output.getPinId());
		return Main.mapper.writeValueAsString(table);
	}
	

	/**
	 * Gets the description for this node
	 * @return
	 */
	public String getDescription() {
		return getDescription(selected);
	}
	
	/**
	 * Gets the description based on the given type
	 * @param op The type to get the description for
	 * @return The description
	 */
	public String getDescription(Custom op) {
		if (op == null) {
			return "A node for adding custom inputs";
		}
		switch (op) {
		case TEXT:
			return "Used to add custom text";
		case DIRECTORY:
			return "Used to add directory names";
		case FILE:
			return "Used to add file names";
		
		}
		return null;
	}

	/**
	 * Class Constructor - Used for loading
	 * @param table the table to construct the node from
	 */
	public CustomNode(Hashtable<String, String> table) {
		this(Double.parseDouble(table.get("x")),Double.parseDouble(table.get("y")));
		changeType(Custom.valueOf(table.get("custom")),true);
		value = table.get("text");
		if (selected == Custom.FILE || selected == Custom.DIRECTORY) {
			String[] l = value.split("/");
			text.setText(l[l.length-1]);
		} else {
		text.setText(table.get("text"));
		}
		this.setNodeId(table.get("id"));
		
			output.setPinId(table.get("output"));

	}

	/**
	 * Class constructor used during conversion of text to nodes
	 * @param type The type of the custom node
	 * @param string The string to be the value of the custom node
	 */
	public CustomNode(Custom type, String string) {
		this(0,0);
		changeType(type, false);
		value = string;
		if (selected == Custom.FILE || selected == Custom.DIRECTORY) {
			String[] l = value.split("/");
			text.setText(l[l.length-1]);
		} else {
		text.setText(string);
		}
	}


	/**
	 * Changes the type of this node
	 * @param op The type to change the node to
	 * @param onBuild whether command should be displayed
	 */
	private void changeType(Custom op, boolean onBuild) {
		selected = op;
		text.setText(op.name());
		output.getVisual().setVisible(true);
		value = null;
		Tooltip t = new Tooltip(getDescription());
		Tooltip.install(visual, t);
		switch (op) {
		case TEXT:
			visual.setOnMouseClicked(e -> {
				if (e.getButton() == MouseButton.PRIMARY && clicked) {
					TextInputDialog dialog = new TextInputDialog("");
					dialog.setTitle("Custom Input");
					dialog.setContentText("Please enter your text");
					Optional<String> result = dialog.showAndWait();
					if (result.isPresent()) {
						text.setText(result.get());
						value = result.get();
					   		Main.graphics.onBuildGraphToCommand(this.toTree());
					}
				}
			});
			break;
		case FILE:
			visual.setOnMouseClicked(e -> {
				if (e.getButton() == MouseButton.PRIMARY && clicked) {
					final JFileChooser fc = new JFileChooser();
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						TextInputDialog dialog = new TextInputDialog(file.getAbsolutePath());
						dialog.setTitle("Custom Input");
						dialog.setContentText("You can change your file name here.");

			
						value = file.getAbsolutePath();
						Optional<String> result = dialog.showAndWait();
						if (result.isPresent()) {
							value = result.get();
						}
						String[] l = value.split("/");
						text.setText(l[l.length-1]);
					   		Main.graphics.onBuildGraphToCommand();
					}
				}
			});
			break;
		case DIRECTORY:
			visual.setOnMouseClicked(e -> {
				if (e.getButton() == MouseButton.PRIMARY && clicked) {
					final JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fc.showOpenDialog(null);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						TextInputDialog dialog = new TextInputDialog(file.getAbsolutePath());
						dialog.setTitle("Custom input");
						dialog.setContentText("You can change your file name here.");

						value = file.getAbsolutePath();
						Optional<String> result = dialog.showAndWait();
						if (result.isPresent()) {
							value = result.get();
				
						}
						String[] l = value.split("/");
						text.setText(l[l.length-1]);
					   		Main.graphics.onBuildGraphToCommand();
					}
				}
			});
			break;
		}
		
		if (onBuild)
	   		Main.graphics.onBuildGraphToCommand(this.toTree());
		addConnector(output);
	}

	@Override
	protected void setupContextMenu() {
		menu = new ContextMenu();
		for (Custom op : Custom.values()) {
			StackPane sp = new StackPane();
			TooltipMenuItem item = new TooltipMenuItem(op.name(),getDescription(op), sp);
			item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					changeType(op,true);
				}
			});
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
			if (value == null)
				return new Leaf(this, "[Custom]");
			switch (selected) {
			case TEXT:
				String temp = value;
				return new Leaf(this,temp);
			case FILE:
				return new Leaf(this,value);
			case DIRECTORY:
				return new Leaf(this,value);
			}
		return new Leaf(this, "[Custom]");
			
			
	
	}

	@Override
	public String getArgumentName(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pin getPin(String id) {
		if (id.equals(output.getPinId()))
			return output;
	
		return null;
	}
	
	@Override
	public Rectangle makeVisual() {
		StackPane sp = new StackPane();
		Rectangle header = new Rectangle(120, 30);
		header.setArcHeight(20);
		header.setArcWidth(20);
		header.setOpacity(0.3);
		header.setFill(Color.RED);
		header.minWidth(200);
		text.setWrappingWidth(header.getWidth() - 40);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setMouseTransparent(true);
		text.textProperty().addListener((observable, oldValue, newValue) -> {
		    header.setHeight(Math.max(35, text.getBoundsInLocal().getHeight()+6));
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
		nodeType = NodeType.CustomNode;
		
	}
}
