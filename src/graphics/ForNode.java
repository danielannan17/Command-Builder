package graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import classes.Branch;
import classes.Option;
import classes.Tree;
import graphics.Pin.PinType;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import windows.Main;

public class ForNode extends GraphicalNode {
	private Pin queue, varOutput, doStart;
	private ArrayList<Pin> inputList;
	private String description = "For Loop";
	private FlowPane inputListPane;

	public ForNode(double x, double y) {
		super("For loop");
		inputList = new ArrayList<Pin>();
		output = new Pin(this, PinType.Next);
		// Set up Output
		output.getVisual().xProperty()
				.bind(Bindings.createDoubleBinding(
						() -> (visual.getWidth() / 2) + (text.getBoundsInLocal().getWidth() / 2) + 10,
						visual.widthProperty(), text.textProperty()));
		this.getChildren().add(output.getVisual());
		output.getVisual().yProperty()
				.bind(Bindings
						.createDoubleBinding(
								() -> (output.getVisual().getHeight() / 2) + text.getY()
										+ (text.getBoundsInLocal().getHeight() / 2) - 2.5,
								text.boundsInLocalProperty()));
		addConnector(output);
		
		// Set up Queue
		queue = new Pin(this, PinType.Input);
		queue.getVisual().setX((visual.getWidth() / 2) - (text.getBoundsInLocal().getWidth() / 2) - 20
				- (queue.getVisual().getWidth() / 2));
		queue.getVisual().yProperty()
				.bind(Bindings
						.createDoubleBinding(
								() -> (queue.getVisual().getHeight() / 2) + text.getY()
										+ (text.getBoundsInLocal().getHeight() / 2) - 2.5,
								text.boundsInLocalProperty()));
		Pane pane = new Pane();
		Tooltip t = new Tooltip("queue");
		Tooltip.install(queue.getVisual(), t);
		pane.getChildren().add(queue.getVisual());
		this.getChildren().add(pane);

		t = new Tooltip(description);
		Tooltip.install(visual, t);
		
		// Set up Var Input
		arguments = new Pin[1];
		FlowPane p = makeFlowPane(Orientation.VERTICAL);
		arguments[0] = new Pin(this, PinType.Input);
		arguments[0].getVisual().setX(text.getX() + text.getBoundsInLocal().getWidth() - Pin.size - 10);
		arguments[0].getVisual().setY(text.getBoundsInLocal().getHeight() / 2 - Pin.size / 2);
		t = new Tooltip("Variable Name");
		Tooltip.install(arguments[0].getVisual(), t);
		p.setTranslateX(12);
		p.setTranslateY(30);
		p.getChildren().add(arguments[0].getVisual());
		this.getChildren().add(p);

		// Set up VarOutput
		varOutput = new Pin(this, PinType.InfiniteOutput);
		varOutput.getVisual().setX(visual.getWidth()-12-Pin.size);
		varOutput.getVisual().setY(42 + Pin.size);
		t = new Tooltip("Variable name output");
		Tooltip.install(varOutput.getVisual(), t);
		this.getChildren().add(varOutput.getVisual());
		addConnector(varOutput);
		

		// Set up do
		doStart = new Pin(this, PinType.Input);
		// TODO FOR Position
		t = new Tooltip("start loop");
		Tooltip.install(doStart.getVisual(), t);
		FlowPane temp = new FlowPane();
		temp.setLayoutY(36);
		temp.setLayoutX(visual.getWidth()-12-Pin.size);
		temp.getChildren().add(doStart.getVisual());
		this.getChildren().add(temp);

		inputListPane = makeFlowPane(Orientation.HORIZONTAL);
		inputListPane.setTranslateX(30);
		inputListPane.setPrefWrapLength(visual.getWidth() - 30 - 36);
		inputListPane.setTranslateY(35);
		visual.heightProperty().bind(Bindings.createDoubleBinding(
				() -> Math.max(72, 30 + 12 + inputListPane.getHeight()), inputListPane.heightProperty()));

		this.getChildren().add(inputListPane);
		addInput();
		
	}

	/**
	 * Class Constructor used for loading.
	 * 
	 * @param tab
	 *            The table to create the node from
	 */
	public ForNode(Hashtable<String, String> table) {
		this(Double.parseDouble(table.get("x")), Double.parseDouble(table.get("y")));
		this.setNodeId(table.get("id"));
		arguments[0].setPinId(table.get("varName"));
		output.setPinId(table.get("output"));
		queue.setPinId(table.get("queue"));
		doStart.setPinId(table.get("do"));
		varOutput.setPinId(table.get("varOutput"));
		String args = table.get("inputs");
		String[] argArray = args.split(",");
		for (int i = 0; i < argArray.length; i++) {
			addInput(argArray[i]);
		}

	}
	
	public void removeInput(Rectangle rect) {
		ArrayList<Pin> temp = (ArrayList<Pin>) inputList.clone();
		for (Pin opt : temp) {
			if (opt.getVisual() == rect) {
				opt.setBound(false);
			}
		}
	}

	public Pin getQueue() {
		return queue;
	}

	public Pin getVarOutput() {
		return varOutput;
	}

	public Pin getDoStart() {
		return doStart;
	}

	public ArrayList<Pin> getInputList() {
		return inputList;
	}

	public String getDescription() {
		return description;
	}

	public FlowPane getInputListPane() {
		return inputListPane;
	}

	private Pin createInputPin() {
		Pin opt = new Pin(this,PinType.Input);

		opt.getBoundProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (opt.getBoundProperty().get()) {
					addInput();
				} else {
					if (opt.getConnect() != null) {
						opt.getConnect().remove();
					}
					inputList.remove(opt);
					inputListPane.getChildren().remove(opt.getVisual());
				}
				
			}
			
		});
		
		Tooltip t = new Tooltip("Input");
		Tooltip.install(opt.getVisual(), t);
		inputList.add(opt);
		inputListPane.getChildren().add(opt.getVisual());
		return opt;
	}
	
	private void addInput(String string) {
		Pin pin =createInputPin();
		pin.setPinId(string);
		

	}
	
	
	/**
	 * Deletes this node from the scene
	 */
	public void remove() {
	super.remove();
	for (Pin arg : inputList) {
		if (arg.isBound())
			arg.getConnect().remove();
	}
	for (Connector arg : queue.getConnectors()) { 
		arg.remove(this);
	}
	for (Connector arg : varOutput.getConnectors()) { 
		arg.remove(this);
	}
	if (doStart.isBound())
		doStart.getConnect().remove();
	
	}
	
	private void addInput() {
		createInputPin();
	}

	@Override
	public String toStorageValue() throws Exception {
		Hashtable<String, String> table = new Hashtable<String, String>();
		table.put("id", this.getNodeId());
		table.put("x", "" + this.getLayoutX());
		table.put("y", "" + this.getLayoutY());
		table.put("output", output.getPinId());
		table.put("type", "" + NodeType.ForNode.getType());
		table.put("do", doStart.getPinId());
		table.put("queue", queue.getPinId());
		table.put("varOutput", varOutput.getPinId());
		table.put("varName", arguments[0].getPinId());
		String inputs = "";
		List<Pin> temp = (List<Pin>) inputList.clone();
		Collections.reverse(temp);
		for (Pin pin : temp) {
			inputs += pin.getPinId() + ",";
		}
		inputs = inputs.substring(0, inputs.length() - 1);

		table.put("inputs", inputs);
		return Main.mapper.writeValueAsString(table);
	}

	@Override
	public Pin getPin(String id) {
		for (Pin t : inputList) {
			if (id.equals(t.getPinId()))
				return t;
		}
		if (id.equals(arguments[0].getPinId()))
			return arguments[0];
		if (id.equals(output.getPinId()))
			return output;
		if (id.equals(queue.getPinId()))
			return queue;
		if (id.equals(varOutput.getPinId()))
			return varOutput;
		if (id.equals(doStart.getPinId()))
			return doStart;

		return null;
	}

	@Override
	protected void setNodeType() {
		nodeType = NodeType.ForNode;
	}

	@Override
	public String getArgumentName(int i) {
		return "Variable Name";
	}

	@Override
	protected void setupContextMenu() {

	}

	@Override
	public Tree toTree() {
		return new Branch(this);
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

}
