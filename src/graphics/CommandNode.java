package graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import classes.Branch;
import classes.Command;
import classes.Option;
import classes.TooltipMenuItem;
import graphics.Pin.PinType;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import windows.Main;

/**
 * The node used for adding commands to the script
 * @author Daniel Annan - 1569423
 *
 */
public class CommandNode extends GraphicalNode {
	Command command;
	private ContextMenu menu;
	private ArrayList<OptionPin> options;
	private FlowPane optionsPane;
	private Pin queue;
	private int outputNumber;
	private String outputOption;

	
	/**
	 * Class Constructor
	 * @param command
	 */
	public CommandNode(Command command) {
		super(command.getCommand());
		if (command.getOutputType() == 0) {
			output = new Pin(this,PinType.Next);
		} else {
			output = new Pin(this,PinType.OutputNext);
		}
		
		
		output.getVisual().xProperty().bind(Bindings.createDoubleBinding(() -> 
		(visual.getWidth()/2) + (text.getBoundsInLocal().getWidth()/2) +5, 
		visual.widthProperty(),text.textProperty()));
		this.getChildren().add(output.getVisual());	
		output.getVisual().yProperty()
				.bind(Bindings
						.createDoubleBinding(
								() -> (output.getVisual().getHeight() / 2) + text.getY()
										+ (text.getBoundsInLocal().getHeight() / 2) - 2.5,
								text.boundsInLocalProperty()));

		queue = new Pin(this, PinType.Infinite);
		queue.getVisual().setX((visual.getWidth() / 2) - (text.getBoundsInLocal().getWidth() / 2) - 20
				- (queue.getVisual().getWidth() / 2));
		queue.getVisual().yProperty().bind(Bindings.createDoubleBinding(
				() -> (queue.getVisual().getHeight() / 2) + text.getY() + (text.getBoundsInLocal().getHeight() / 2) - 2.5,
				text.boundsInLocalProperty()));

		Pane pane = new Pane();
		Tooltip t = new Tooltip("Connect for queueing");
		Tooltip.install(queue.getVisual(), t);
		pane.getChildren().add(queue.getVisual());
		this.getChildren().add(pane);
		options = new ArrayList<OptionPin>();
		this.command = command;
		setupContextMenu();
		t = new Tooltip(command.getDescription());
		Tooltip.install(visual, t);
		outputNumber = command.getOutputType();
		if (outputNumber < 0) {
			outputOption = command.getOptionsAsList().get(outputNumber*-1).getOption();
		}
		FlowPane p = null;
		if (command.getNumOfArguments() == 1 || command.getNumOfArguments() == -1) {
			arguments = new Pin[1];
			p = makeFlowPane(Orientation.VERTICAL);
			arguments[0] = new Pin(this, PinType.Input);
			arguments[0].getVisual().setX(text.getX() + text.getBoundsInLocal().getWidth() - Pin.size - 10);
			arguments[0].getVisual().setY(text.getBoundsInLocal().getHeight() / 2 - Pin.size / 2);
			t = new Tooltip(command.getArguments().get(0));
			Tooltip.install(arguments[0].getVisual(), t);
			p.getChildren().add(arguments[0].getVisual());
		} else if (command.getNumOfArguments() > 0) {
			arguments = new Pin[command.getNumOfArguments()];
			p = makeFlowPane(Orientation.VERTICAL);
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = new Pin(this, PinType.Input);
				
				t = new Tooltip(command.getArguments().get(i));
				Tooltip.install(arguments[i].getVisual(), t);
				p.getChildren().add(arguments[i].getVisual());
			}

		}
		addConnector(output);
		optionsPane = makeFlowPane(Orientation.HORIZONTAL);
		optionsPane.setTranslateY(35);

		this.getChildren().add(optionsPane);
		if (p != null) {
			p.setTranslateX(12);
			p.setTranslateY(30);
			this.getChildren().add(p);
			visual.setHeight(visual.getHeight() + p.getBoundsInLocal().getHeight() + 25);

			optionsPane.setTranslateX(p.getTranslateX() + p.getBoundsInLocal().getWidth());
			optionsPane.setPrefWrapLength(visual.getWidth() - p.getTranslateX() - p.getBoundsInLocal().getWidth() - 12);

		} else {
			optionsPane.setTranslateX(12);
			optionsPane.setPrefWrapLength(visual.getWidth() - 24);

		}
		double height = visual.getHeight();
		visual.heightProperty().bind(Bindings.createDoubleBinding(
				() -> Math.max(height, 30 + 12 + optionsPane.getHeight()), optionsPane.heightProperty()));

	}

	/**
	 * Class Constructor for creating node at a certain position
	 * @param command
	 * @param x
	 * @param y
	 */
	public CommandNode(Command command, double x, double y) {
		this(command);
		this.relocate(x - this.visual.getWidth() / 2, y - this.visual.getHeight() / 2);
	}

	/**
	 * Constructor - Used to load from history
	 * 
	 * @param command
	 * @param x
	 * @param y
	 * @param options
	 */
	public CommandNode(Hashtable<String, String> table) {
		this(Main.commands.get(table.get("command")), Double.parseDouble(table.get("x")),
				Double.parseDouble(table.get("y")));
		this.setNodeId(table.get("id"));
		try {
			if (arguments != null) {
				String args = table.get("args");
				String[] argArray = args.split(",");
				for (int i = 0; i < arguments.length; i++) {
					arguments[i].setPinId(argArray[i]);
				}
			}
			ArrayList<String> opts = Main.mapper.readValue(table.get("options"), ArrayList.class);
		
			if (!opts.isEmpty()) {
				for (String opt : opts) {
					String[] x = opt.split(",");
					OptionPin pin = addOption(command.getOption(x[0]), Integer.parseInt(x[3]));
					pin.setPinId(x[1]);
				}
			}
			output.setPinId(table.get("output"));
			queue.setPinId(table.get("queue"));
		} catch (Exception e) {

		}
	}

	/**
	 * Gets the name of the option used for outputting data
	 * @return The option name
	 */
	public String getOutputOption() {
		return outputOption;
	}

	
	/**
	 * Gets the queue;
	 * @return the queue
	 */
	public Pin getQueue() {
		return queue;
	}

	/**
	 * Sets the queue
	 * @param queue The queue
	 */
	public void setQueue(Pin queue) {
		this.queue = queue;
	}

	/**
	 * Deletes this node from the scene
	 */
	public void remove() {
	super.remove();
	for (OptionPin arg : this.getOptions()) {
		if (arg.isBound())
			arg.getConnect().remove();
	}
	for (Connector arg : this.getQueue().getConnectors()) { 
		arg.remove(this);
	}
	}
	
	@Override
	public String toStorageValue() throws Exception {
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("id", this.getNodeId());
		table.put("type", "" + NodeType.CommandNode.getType());
		table.put("command", command.getCommand());
		table.put("x", "" + this.getLayoutX());
		table.put("y", "" + this.getLayoutY());
		table.put("queue", queue.getPinId());
		String args = "";
		if (arguments != null) {
			List<Pin> temp = Arrays.asList(arguments);
			//Collections.reverse(temp);
			for (Pin pin : temp) {
				args += pin.getPinId() + ",";
			}
			args = args.substring(0, args.length() - 1);
		}
		table.put("args", args);
		ArrayList<String> opts = new ArrayList<String>();

		if (!options.isEmpty()) {
			for (OptionPin pin : options) {
				String val;
				if (pin.getOption().getNumOfArguments() == 0) {
					val = pin.getOption().getOption() + ",";
					val += pin.getPinId() + ","
					+ pin.getOption().getId() + ","
					+ pin.getArgNum();
					opts.add(val);
				} else {
					int argNum = pin.getArgNum();
					val = pin.getOption().getOption() + ",";
					val += pin.getPinId() + ","
					+ pin.getOption().getId() + ","
					+ argNum;
					opts.add(val);
				}
				
		
			}
		}
		table.put("options", Main.mapper.writeValueAsString(opts));
		table.put("output", output.getPinId());
		return Main.mapper.writeValueAsString(table);
	}


	/**
	 * Counts the number of bound options
	 * @return
	 */
	public int countBoundOptions() {
		int sum = 0;
		for (OptionPin opt : options) {
			if (opt.isBound() || opt.getOption().getNumOfArguments() == 0) {
				sum++;
			}
		}
		return sum;
	}
	
	
	/**
	 * Returns the output type
	 * @return the output type
	 */
	public int getOutputType() {
		return outputNumber;
	}

	
	/**
	 * Adds a single argument of an option to be displayed on the graphical node
	 * @param option The option whose argument should be displayed
	 * @param argNum Which argument of the option it is
	 * @return The graphical representation of the argument
	 */
	public OptionPin addOption(Option option, int argNum) {
		OptionPin opt;
		if (option.getNumOfArguments() == 0) {
			opt = new OptionPin(this, option, option.getDescription(), 0, PinType.Disabled);
			
		} else {
			opt = new OptionPin(this, option, option.getArgument(argNum-1), argNum, PinType.Input);
		}
		optionsPane.getChildren().add(opt.getVisual());
		options.add(opt);
		return opt;
	}

	/**
	 * Adds all the arguments of an option to be displayed on the graphical Node
	 * @param option THe option to be displayed
	 * @return All of the arguments of the option to be displayed on the node
	 */
	public ArrayList<OptionPin> addMultiOption(Option option) {
		ArrayList<OptionPin> opts = new ArrayList<OptionPin>();
		if (option.getNumOfArguments() == 0) {
			OptionPin opt = new OptionPin(this, option, option.getDescription(), 0, PinType.Disabled);
			opts.add(opt);
			optionsPane.getChildren().add(opt.getVisual());
			options.add(opt);
		} else {
			for (int i = 0; i < option.getNumOfArguments(); i++) {
				OptionPin opt = new OptionPin(this, option, option.getArgument(i), i+1, PinType.Input);
				optionsPane.getChildren().add(opt.getVisual());
				options.add(opt);
				opts.add(opt);
			}
		}
		return opts;
	}

	
	/**
	 * Removes an option and all of its arguments from the graphical node
	 * @param rect The rectangle of the argument that was selected to be deleted
	 */
	public void removeOption(Rectangle rect) {
		Option tempOption = null;
		for (OptionPin opt : options) {
			if (opt.getVisual() == rect) {
				tempOption = opt.getOption();
			}
		}
		if (tempOption != null) {
			Option option = tempOption;
			ArrayList<OptionPin> tempOptions = (ArrayList<OptionPin>) options.clone();
			for (OptionPin opt : tempOptions) {
				if (opt.getOption() == option) {
					optionsPane.getChildren().remove(opt.getVisual());
					options.remove(opt);
					if (opt.isBound())
						opt.getConnect().remove();

				}
			}
			StackPane sp = new StackPane();
			TooltipMenuItem item = new TooltipMenuItem(option.getOption(), option.getDescription(), sp);
			item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					addMultiOption(option);
					menu.getItems().remove(item);
				}
			});
			menu.getItems().add(item);
			return;
		}

	}

	/**
	 * Returns a list of all the pins used for an option
	 * @return the list of pins
	 */
	public ArrayList<OptionPin> getOptions() {
		return options;
	}

	@Override
	protected void setupContextMenu() {
		menu = new ContextMenu();
		if (command.getOptionsAsList() == null)
			return;
		StackPane sp;
		for (Option option : command.getOptionsAsList()) {
			sp = new StackPane();
			TooltipMenuItem item = new TooltipMenuItem(option.getOption(), option.getDescription(), sp);
			item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					addMultiOption(option);
					menu.getItems().remove(item);
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
	public String getArgumentName(int i) {
		return command.getArguments().get(i);
	}

	
	@Override
	public Branch toTree() {
		return new Branch(this);
	}

	@Override
	public Pin getPin(String id) {
		for (Pin t : options) {
			if (id.equals(t.getPinId()))
				return t;
		}
		for (Pin t : arguments) {
			if (id.equals(t.getPinId()))
				return t;
		}
		if (id.equals(output.getPinId()))
			return output;
		if (id.equals(queue.getPinId()))
			return queue;
		return null;
	}

	/**
	 * Returns value of the command
	 * @return The command name
	 */
	public String getCommandValue() {
		return command.getCommand();
	}

	
	@Override
	public Rectangle makeVisual() {
		visual = new Rectangle(120, 30);
		visual.setStroke(Color.GREEN);
		visual.setStrokeWidth(4);
		visual.setArcHeight(20.0);
		visual.setArcWidth(20.0);
		visual.setOpacity(0.3);
		this.getChildren().add(visual);
		StackPane sp = new StackPane();
		Rectangle header = new Rectangle(visual.getWidth(), 30);
		Rectangle hBot = new Rectangle(0, header.getHeight() / 2, visual.getWidth(), header.getHeight() / 2);
		header.setArcHeight(visual.getArcHeight());
		header.setArcWidth(visual.getArcWidth());
		header.setOpacity(0.3);
		header.setMouseTransparent(true);
		hBot.setMouseTransparent(true);
		Shape x = Shape.subtract(hBot, header);
		x.setVisible(false);
		visual.heightProperty().addListener(new ChangeListener<Object>() {

			@Override
			public void changed(ObservableValue arg0, Object arg1, Object arg2) {
				// TODO Auto-generated method stub
				if (visual.getHeight() > 30)
					x.setVisible(true);
			}

		});
		x.setOpacity(0.3);
		x.setFill(Color.RED);
		header.setFill(Color.RED);
		sp.getChildren().addAll(header, text);
		sp.setMouseTransparent(true);
		x.setMouseTransparent(true);
		this.getChildren().addAll(sp);
		this.getChildren().add(x);
		return visual;
	}

	
	@Override
	protected void setNodeType() {
		nodeType = NodeType.CommandNode;
		
	}

}
