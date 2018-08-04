package graphics;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import classes.Branch;
import classes.Command;
import classes.Option;
import classes.TooltipMenuItem;
import classes.Tree;
import graphics.Pin.PinType;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.util.Pair;
import windows.Main;

/**
 * Graphics Manager handles everything relating to the graphical interface used
 * for building commands.
 * 
 * @author Daniel Annan - 1569423
 *
 */
public class GraphicsManager extends Pane {
	private Scene scene;
	public static Group container;
	private ArrayList<Connector> connectors;
	private LinkedHashMap<String, GraphicalNode> nodes;
	private Node selected, hovering;
	private NodeType selectedNodeType;
	public Pin carrying = null, tempPin, selectedPin;
	public Connector connector;
	public final Color hoverColour = Color.BLUE;
	public Tree currentTree;
	public String currentTreeValue;
	private double x, y;
	private Text onBuild;
	private ContextMenu nodesMenu;
	private double orgSceneX;
	private double orgSceneY;
	private double orgTranslateX;
	private double orgTranslateY;
	private Timer timer;

	/**
	 *Class Constructor
	 */
	public GraphicsManager() {
		nodes = new LinkedHashMap<String, GraphicalNode>();
		connectors = new ArrayList<Connector>();
		container = new Group();
		scene = new Scene(this);

		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				Main.btnSlider.repaint();
				if (mouseEvent.isSecondaryButtonDown()) {
					Point2D p = container.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
					x = p.getX();
					y = p.getY();

				}
				if (isCarrying()) {
					bindNodes();
				}
			}
		});

		nodesMenu = new ContextMenu();
		StackPane sp = new StackPane();
		TooltipMenuItem op = new TooltipMenuItem("Operator", "Used to include operators", sp);

		op.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
			@Override
			public void handle(javafx.event.ActionEvent arg0) {
				addGraphicalNode(new OperatorNode(x, y), false);
			}
		});
		nodesMenu.getItems().add(op);
		sp = new StackPane();
		TooltipMenuItem cust = new TooltipMenuItem("Custom", "Used to include text or files", sp);
		cust.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
			@Override
			public void handle(javafx.event.ActionEvent arg0) {
				addGraphicalNode(new CustomNode(x, y), true);
			}
		});
		nodesMenu.getItems().add(cust);
		
		
				sp = new StackPane();
		TooltipMenuItem forNode = new TooltipMenuItem("For", "Use to repeat a commands for each input given", sp);
		forNode.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
			@Override
			public void handle(javafx.event.ActionEvent arg0) {
				addGraphicalNode(new ForNode(x, y), true);
			}
		});
		//nodesMenu.getItems().add(forNode);
		
		
		
		
		if (Main.commands != null)
			for (Command command : Main.commands.values()) {
				sp = new StackPane();
				TooltipMenuItem toAdd = new TooltipMenuItem(command.getCommand(), command.getDescription(), sp);
				toAdd.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
					@Override
					public void handle(javafx.event.ActionEvent arg0) {
						addGraphicalNode(new CommandNode(command, x, y), true);

					}
				});
				nodesMenu.getItems().add(toAdd);
			}

		Rectangle background = new Rectangle(0, 0, getWidth(), getHeight());
		background.widthProperty().bind(Bindings.createDoubleBinding(() -> getWidth(), widthProperty()));
		background.heightProperty().bind(Bindings.createDoubleBinding(() -> getHeight(), heightProperty()));
		background.setOnMousePressed(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				setSelected(null);
				orgSceneX = e.getSceneX();
				orgSceneY = e.getSceneY();
				orgTranslateX = container.getTranslateX();
				orgTranslateY = container.getTranslateY();
			}
		});

		this.styleProperty()
				.bind(Bindings.createStringBinding(() -> "-fx-background-image: url(\""
						+ GraphicsManager.class.getResource("/images/background.png") + "\");\n"
						+ "    -fx-background-position: " + container.translateXProperty().get() % 256 + "px "
						+ container.translateYProperty().get() % 256 + "px;" + "-fx-background-repeat: repeat;   \n",
						container.translateXProperty(), container.translateYProperty()));

		background.setOnMouseDragged(e -> {
			if (e.getButton() == MouseButton.PRIMARY) {
				double offsetX = e.getSceneX() - orgSceneX;
				double offsetY = e.getSceneY() - orgSceneY;
				double newTranslateX = orgTranslateX + offsetX;
				double newTranslateY = orgTranslateY + offsetY;
				container.setTranslateX(newTranslateX);
				container.setTranslateY(newTranslateY);
			}
		});
		background.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				nodesMenu.show(background, event.getScreenX(), event.getScreenY());
			}
		});
		background.setFill(javafx.scene.paint.Color.TRANSPARENT);

		getChildren().add(background);

		scene.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (selected != null && keyEvent.getCode() == KeyCode.DELETE)
					deleteSelected();
			}

		});
		ImageView imageView = new ImageView(new Image(GraphicsManager.class.getResourceAsStream("/images/eye.png")));
		double ratio = imageView.getImage().getWidth() / imageView.getImage().getHeight();
		imageView.setFitWidth(30);
		imageView.setFitHeight(imageView.getFitWidth());
		Button btnDisplay = new Button();
		btnDisplay.setPadding(new Insets(0, 0, 0, 0));
		btnDisplay.setGraphic(imageView);
		btnDisplay.setPrefHeight(imageView.getFitWidth());
		btnDisplay.setOnMouseClicked(e -> {
			String command = graphToCommand();
			Main.txtCommand.setText(command);
		});

		Button btnSave = new Button();
		imageView = new ImageView(new Image(GraphicsManager.class.getResourceAsStream("/images/save.png")));
		ratio = imageView.getImage().getWidth() / imageView.getImage().getHeight();
		imageView.setFitWidth(30);
		btnSave.setLayoutX(35);
		imageView.setFitHeight(imageView.getFitWidth() / ratio);
		btnSave.setPadding(new Insets(0, 0, 0, 0));
		btnSave.setGraphic(imageView);
		btnSave.setOnMouseClicked(e -> {
			if (!nodes.isEmpty()) {
				saveToHistory();
			} else {
				JOptionPane.showMessageDialog(null, "Failed to save to history. Please add some nodes.", "Fail",
						JOptionPane.ERROR_MESSAGE);

			}
		});

		Pane grpButtons = new Pane();
		grpButtons.layoutXProperty()
				.bind(Bindings.createDoubleBinding(() -> scene.getWidth() - 77, scene.widthProperty()));
		grpButtons.setTranslateY(12);
		grpButtons.getChildren().addAll(btnDisplay, btnSave);
		grpButtons.autosize();
		grpButtons.setOpacity(0.3);
		FadeTransition ft = new FadeTransition(Duration.millis(300), grpButtons);
		grpButtons.setOnMouseEntered(e -> {
			ft.stop();
			ft.setFromValue(0.3);
			ft.setToValue(1);
			ft.setCycleCount(1);
			ft.setAutoReverse(true);
			ft.play();
		});
		grpButtons.setOnMouseExited(e -> {
			ft.stop();
			ft.setFromValue(1);
			ft.setToValue(0.3);
			ft.setCycleCount(1);
			ft.setAutoReverse(true);
			ft.play();

		});
		getChildren().add(grpButtons);

		this.getChildren().add(container);
		onBuild = new Text();
		onBuild.setFill(javafx.scene.paint.Color.WHITE);
		TextFlow flow = new TextFlow();
		flow.setLayoutX(10);
		flow.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> this.getWidth() - 10, this.widthProperty()));
		flow.setLayoutY(this.getHeight() - flow.getHeight());
		flow.layoutYProperty().bind(Bindings.createDoubleBinding(() -> this.getHeight() - flow.getHeight() - 3,
				flow.heightProperty(), this.heightProperty()));
		flow.getChildren().add(onBuild);
		this.getChildren().add(flow);

		timer = new Timer();
		Button upArrow = new Button();
		upArrow.setStyle("-fx-background-image: url(\"" + GraphicsManager.class.getResource("/images/up.png") + "\");\n"
				+ "    -fx-background-color: rgb(178,181,186,0.5);   \n" + "    -fx-background-repeat: no-repeat;   \n"
				+ "    -fx-background-position: center center;   \n");
		upArrow.setOnMousePressed(e -> {
			TimerTask task = new TimerTask() {
				public void run() {
					Timer time = timer;
					timer = new Timer();
					if (upArrow.isPressed()) {
						container.setTranslateY(container.getTranslateY() + 4);
					} else {
						time.cancel();
					}
				}
			};
			timer.scheduleAtFixedRate(task, 0, 25);
		});
		upArrow.prefWidthProperty().bind(this.widthProperty());
		upArrow.setVisible(false);
		this.getChildren().add(upArrow);

		Button downArrow = new Button();
		downArrow.setOnMousePressed(e -> {
			TimerTask task = new TimerTask() {
				public void run() {
					Timer time = timer;
					timer = new Timer();
					if (downArrow.isPressed()) {
						container.setTranslateY(container.getTranslateY() - 4);
					} else {
						time.cancel();
					}
				}
			};
			timer.scheduleAtFixedRate(task, 0, 25);
		});
		downArrow.prefWidthProperty().bind(this.widthProperty());
		downArrow.setVisible(false);
		downArrow.layoutYProperty().bind(Bindings.createDoubleBinding(() -> this.getHeight() - downArrow.getHeight(),
				downArrow.heightProperty(), this.heightProperty()));
		downArrow.setStyle("-fx-background-image: url(\"" + GraphicsManager.class.getResource("/images/down.png")
				+ "\");\n" + "    -fx-background-color: rgb(178,181,186,0.5);   \n"
				+ "    -fx-background-repeat: no-repeat;   \n" + "    -fx-background-position: center center;   \n");
		this.getChildren().add(downArrow);

		Button leftArrow = new Button();
		leftArrow.setOnMousePressed(e -> {
			TimerTask task = new TimerTask() {
				public void run() {
					Timer time = timer;
					timer = new Timer();
					if (leftArrow.isPressed()) {
						container.setTranslateX(container.getTranslateX() + 4);
					} else {
						time.cancel();
					}
				}
			};
			timer.scheduleAtFixedRate(task, 0, 25);
		});
		leftArrow.prefHeightProperty().bind(this.heightProperty());
		leftArrow.setVisible(false);
		leftArrow.setStyle("-fx-background-image: url(\"" + GraphicsManager.class.getResource("/images/left.png")
				+ "\");\n" + "    -fx-background-color: rgb(178,181,186,0.5);   \n"
				+ "    -fx-background-repeat: no-repeat;   \n" + "    -fx-background-position: center center;   \n");
		this.getChildren().add(leftArrow);

		Button rightArrow = new Button();
		rightArrow.setOnMousePressed(e -> {
			TimerTask task = new TimerTask() {
				public void run() {
					Timer time = timer;
					timer = new Timer();
					if (rightArrow.isPressed()) {
						container.setTranslateX(container.getTranslateX() - 4);
					} else {
						time.cancel();
					}
				}
			};
			timer.scheduleAtFixedRate(task, 0, 25);
		});
		rightArrow.prefHeightProperty().bind(this.heightProperty());
		rightArrow.setVisible(false);
		rightArrow.layoutXProperty().bind(Bindings.createDoubleBinding(() -> this.getWidth() - rightArrow.getWidth(),
				rightArrow.widthProperty(), this.widthProperty()));
		rightArrow.setStyle("-fx-background-image: url(\"" + GraphicsManager.class.getResource("/images/right.png")
				+ "\");\n" + "    -fx-background-color: rgb(178,181,186,0.5);   \n"
				+ "    -fx-background-repeat: no-repeat;   \n" + "    -fx-background-position: center center;   \n");
		this.getChildren().add(rightArrow);

		leftArrow.setOpacity(0.3);
		upArrow.setOpacity(0.3);
		downArrow.setOpacity(0.3);
		rightArrow.setOpacity(0.3);

		container.boundsInParentProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<? extends Object> arg0, Object arg1, Object arg2) {
				if (container.getBoundsInParent().getMinX() < -50) {
					leftArrow.setVisible(true);
				} else {
					leftArrow.setVisible(false);
				}

				if (container.getBoundsInParent().getMinY() < -15) {
					upArrow.setVisible(true);
				} else {
					upArrow.setVisible(false);
				}

				if (container.getBoundsInParent().getMaxX() > scene.getWidth() + 50) {
					rightArrow.setVisible(true);
				} else {
					rightArrow.setVisible(false);
				}
				if (container.getBoundsInParent().getMaxY() > scene.getHeight() + 15) {
					downArrow.setVisible(true);
				} else {
					downArrow.setVisible(false);
				}

			}
		});

	}

	
	/**
	 * Resets the position of the all the nodes on the canvas while keeping
	 * their relative position to each other by
	 * resetting their container
	 */
	public void resetContainer() {
		container.setTranslateX(0);
		container.setTranslateX(0);
		container.setLayoutX(0);
		container.setLayoutY(0);
	}
	
	/**
	 * Splits a string into parts and puts them in a list
	 * @param str The string to split
	 * @return The list of strings
	 */
	private List<String> splitParts(String str) {

		List<String> list = new ArrayList<String>();
		Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(str);
		while (m.find())
			list.add(m.group(1));
		return list;
	}

	/**
	 * Saves the current scene to the history
	 */
	public void saveToHistory() {
			if (save(Main.txtCommand.getText())) {
				try {

					Main.refreshHistoryList();
				} catch (Exception e) {
					e.printStackTrace();
				}

		}
	}

	/**
	 * Shows the command on the scene as graphical nodes, if the command is valid
	 * @param text  The command to be displayed
	 */
	public void showCommand(String text) {
		text = cleanResult(text);
		String commandName;
		ArrayList<Pair<String, String>> opts;
		String[] arguments;
		try {
			text = text.trim();
			if (text.contains(" "))
				commandName = text.substring(0, text.indexOf(" "));
			else
				commandName = text;
			ResultSet command = Main.db.getSingleCommand(commandName);
			if (command == null) {
				System.err.println("Couldn't match command");
				return;
			}
			List<String> str = splitParts(text);
			str.remove(0);
			arguments = new String[command.getInt("Inputs")];
			int size = str.size();
			for (int i = 0; i < arguments.length; i++) {
				arguments[arguments.length - 1 - i] = str.remove(size - 1 - i);
			}
			opts = new ArrayList<Pair<String, String>>();
			while (!str.isEmpty()) {

				String optionName = str.remove(0);
				if (optionName.startsWith("-")) {
					ResultSet option = Main.db.getOptionRow(command.getInt("id"), optionName);
					if (option.next()) {
						String[] optionArgs = new String[option.getInt("Inputs")];
						for (int i = 0; i < option.getInt("Inputs"); i++) {
							optionArgs[i] = str.remove(0);
							if (optionArgs[i].startsWith("-")) {
								JOptionPane.showMessageDialog(null,
										"Option " + optionName + " requires " + option.getInt("Inputs")
										+ " arguments.",
									    "Error",
									    JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						for (int i = 0; i < optionArgs.length; i++) {
							int x = i + 1;
							String name = optionName + "," + x;
							Pair<String, String> opt = new Pair<String, String>(name, optionArgs[i]);
							opts.add(opt);
						}
						while (option.next()) {
							optionArgs = new String[option.getInt("Inputs")];
							for (int i = 0; i < option.getInt("Inputs"); i++) {
								optionArgs[i] = str.remove(0);
								if (optionArgs[i].startsWith("-")) {
									JOptionPane.showMessageDialog(null,
											"Option " + optionName + " requires " + option.getInt("Inputs")
											+ " arguments.",
										    "Error",
										    JOptionPane.ERROR_MESSAGE);
			
									return;
								}
							}
							for (int i = 0; i < optionArgs.length; i++) {
								String name = optionName + "," + i + 1;
								Pair<String, String> opt = new Pair<String, String>(name, optionArgs[i]);
								opts.add(opt);
							}
						}

					} else {
						JOptionPane.showMessageDialog(null,
								"Invalid Option",
							    "Error",
							    JOptionPane.ERROR_MESSAGE);
						System.err.println("Invalid Option");
						return;
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"Too many arguments",
						    "Error",
						    JOptionPane.ERROR_MESSAGE);
					System.err.print("Too many arguments");
					return;
				}
			}
		} catch (Exception e) {
		
			return;
		}
		// Adding Nodes
		ArrayList<GraphicalNode> children = new ArrayList<GraphicalNode>();
		Command command = Main.commands.get(commandName);
		CommandNode cNode = new CommandNode(command);
		ArrayList<Option> options = command.getOptionsAsList();
		Hashtable<String, Option> optionsTable = new Hashtable<String, Option>();
		for (Option o : options) {
			optionsTable.put(o.getOption(), o);
		}
		addGraphicalNode(cNode, false);
		// Adding Arguments to commandNode
		Pin[] cArgs = cNode.getArguments();
		for (int i = 0; i < arguments.length; i++) {
			CustomNode txtNode = new CustomNode(CustomNode.Custom.TEXT, arguments[i]);
			addGraphicalNode(txtNode, false);
			bindNodes(txtNode, cArgs[i]);
			children.add(txtNode);
		}
		// Adding Options with args to commandNode
		for (Pair<String, String> pair : opts) {
			String[] x = pair.getKey().split(",");
			OptionPin pins = cNode.addOption(optionsTable.get(x[0]), Integer.parseInt(x[1]));
			CustomNode txtNode = new CustomNode(CustomNode.Custom.TEXT, pair.getValue());
			addGraphicalNode(txtNode, false);
			bindNodes(txtNode, pins);
			children.add(txtNode);

		}

		for (int i = 0; i < children.size(); i++) {
			int row = i % 2;
			int column = i / 2;
			GraphicalNode node = children.get(i);
			if (row == 0) {
				node.setLayoutX(column * (node.visual.getWidth() + 10));
				node.setLayoutY(80);
			} else {
				node.setLayoutX(column * (node.visual.getWidth() + 10));
				node.setLayoutY(160);
			}
		}
		container.setTranslateX(15);
		container.setTranslateY(15);

	}

	/**
	 * Handles building the linux command/script as the user adds, deletes and
	 * connects nodes
	 * 
	 * @return The new script value
	 */
	public String onBuildGraphToCommand() {
		return onBuildGraphToCommand(null);
	}

	
	/**
	 * Inserts a string into the currentTreeValue at a given index
	 * @param str The string to be inserted
	 * @param index The index to insert the string at
	 */
	public void insertIntoCurrentTreeValue(String str, int index) {
		if (index == 0) {
			setCurrentTreeValue(str + currentTreeValue);
		} else if (index >= currentTreeValue.length()) {
			setCurrentTreeValue(currentTreeValue + str);
		} else {
			String newValue = currentTreeValue.substring(0, index);
			newValue += str;
			setCurrentTreeValue(newValue + currentTreeValue.substring(index));
		}

	}

	/**
	 * Converts the nodes on the scene into its text form. Only shows commands
	 * connected to the root tree selected
	 * @param tree the root
	 * @return The result of the conversion
	 */
	public String onBuildGraphToCommand(Tree tree) {
		if (!nodes.isEmpty()) {
			try {
				currentTree = null;
				if (tree == null) {
					ArrayList<GraphicalNode> n = new ArrayList<GraphicalNode>(nodes.values());
					Iterator<GraphicalNode> it = n.iterator();
					boolean next = true;
					GraphicalNode node = null;
					while (it.hasNext() && next) {
						node = it.next();
						next = false;
						//TODO FOR
						if (OperatorNode.class.isInstance(node) && !((OperatorNode) node).isSelected()
								|| CustomNode.class.isInstance(node) && !((CustomNode) node).isSelected()) {
							next = true;
						}
					}
					currentTree = node.toTree();
				} else {
					currentTree = tree;
				}
				if (currentTree.getData() != null) {
					while (currentTree.getData().getOutput().isBound()) {
						currentTree = currentTree.getData().getOutput().getConnect()
								.getOtherEnd(currentTree.getData().getOutput().getParent()).toTree();
					}
				}
				currentTree.build(null);
				convertTreeToString();
				cleanResult();

			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		} else {
			currentTree = null;
			currentTreeValue = "";
		}
		onBuild.setText(currentTreeValue.replaceAll(Character.toString((char)'\n'), ""));
		return currentTreeValue;
	}

	/**
	 * Gets a list of all the connectors
	 * @return The list of connectors
	 */
	public List<Connector> getConnectors() {
		return connectors;
	}

	/**
	 * Gets a collection of all the nodes on the scene
	 * @return The collection of the nodes
	 */
	public Collection<GraphicalNode> getNodes() {
		return nodes.values();
	}

	/**
	 * Saves the current scene to the history
	 * @param val The value of the nodes on the scene
	 * @return Whether the save was successful
	 */
	public boolean save(String val) {

		ArrayList<String> temp = new ArrayList<String>();
		ArrayList<GraphicalNode> nods = new ArrayList<GraphicalNode>(nodes.values());
		try {
			for (int i = 0; i < nodes.size(); i++) {
				String node = nods.get(i).toStorageValue();
				if (node != null)
					temp.add(node);
			}
			if (temp.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Failed to save to history. Please add some nodes.", "Fail",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream so = new ObjectOutputStream(bo);
			so.writeObject(temp);
			so.flush();
			byte[] obj = bo.toByteArray();

			// Create the custom dialog.
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Save");
			dialog.setHeaderText("What's the title and description?");

			ButtonType btnSave = new ButtonType("Save", ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			TextField title = new TextField();
			title.setPromptText("Title");
			TextField description = new TextField();
			description.setPromptText("Description");

			grid.add(new Label("Title:"), 0, 0);
			grid.add(title, 1, 0);
			grid.add(new Label("Description:"), 0, 1);
			grid.add(description, 1, 1);

			dialog.getDialogPane().setContent(grid);

			Platform.runLater(() -> title.requestFocus());

			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == btnSave) {
					return new Pair<>(title.getText(), description.getText());
				}
				return null;
			});

			Optional<Pair<String, String>> result = dialog.showAndWait();

			result.ifPresent(value -> {
				try {
					Main.db.startTransaction();
					int id = Main.db.addToHistory(value.getKey(), value.getValue(), val, obj);
					if (id >= 0)
						Main.db.addBindings(id, connectors.size(), connectors);
					Main.db.completeTransaction();
					JOptionPane.showMessageDialog(null, "Saved.", "Saved to history.", JOptionPane.PLAIN_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, "Error.", "Failed to save to history.",
							JOptionPane.ERROR_MESSAGE);
					Main.db.rollBack();
				}
			});
			return result.isPresent();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error.", "Failed to save to history.", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	/**
	 * Loads a record from the history into the scene
	 * @param id The ID of the record
	 * @param list  a list of all the nodes in the record
	 * @param clear Whether it should first clear the scene
	 */
	public void load(int id, ArrayList<String> list, boolean clear) {
		if (clear) {
			LinkedHashMap<String, GraphicalNode> x = (LinkedHashMap<String, GraphicalNode>) nodes.clone();
			for (GraphicalNode g : x.values()) {
				g.remove();
				removeGraphicalNode(g);
			}
			nodes.clear();
		}
		try {
			for (String node : list) {
				Hashtable<String, String> tab = Main.mapper.readValue(node, Hashtable.class);
				switch (Integer.parseInt(tab.get("type"))) {
				case 0:
					addGraphicalNode(new CommandNode(tab), false);
					break;
				case 1:
					addGraphicalNode(new CustomNode(tab), false);
					break;
				case 2:
					addGraphicalNode(new OperatorNode(tab), false);
					break;
				case 3:
					addGraphicalNode(new ForNode(tab), false);
					break;
				}
			}
			ResultSet r = Main.db.getBindings(id);
			while (r.next()) {
				bindNodes(r.getString("firstNode"), r.getString("lastNode"), r.getString("firstPin"),
						r.getString("secondPin"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	/**
	 * Gets the current tree of all the nodes
	 * @return the tree
	 */
	public Tree getCurrentTree() {
		return currentTree;
	}

	/**
	 * Sets the current tree
	 * @param currentTree the root of the tree
	 */
	public void setCurrentTree(Tree currentTree) {
		this.currentTree = currentTree;
	}

	/**
	 * Gets the currentTreeValue
	 * @return The currentTreeValue
	 */
	public String getCurrentTreeValue() {
		return currentTreeValue;
	}

	/**
	 * Sets the value of currentTreeValue
	 * @param currentTreeValue The new value for currentTreeValue
	 */
	public void setCurrentTreeValue(String currentTreeValue) {
		this.currentTreeValue = currentTreeValue;
	}

	/**
	 * Gets node being hovered
	 * @return The node
	 */
	public Node getHovering() {
		return hovering;
	}

	/**
	 * sets the hovering to the node being hovered over
	 * @param hovering The node
	 */
	public void setHovering(Node hovering) {
		this.hovering = hovering;
	}

	/**
	 * Gets the pin which is being carried
	 * @return The pin
	 */
	public Pin getCarrying() {
		return carrying;
	}

	/**
	 * Sets the pin which is being carried
	 * @param carrying the pin
	 */
	public void setCarrying(Pin carrying) {
		this.carrying = carrying;
	}

	/**
	 * Sets a temporary pin. Used as the pin which is
	 * being bound to
	 * @param tempPin the pin
	 */
	public void setTempPin(Pin tempPin) {
		this.tempPin = tempPin;
	}

	/**
	 * Binds the node being carried to another node. Carrying is the output node
	 * tempPin is the pin of the node being connected and Node is the parent of
	 * the tempPin
	 */
	void bindNodes() {
		Node inputPinVisual = hovering;
		Pin inputPin = tempPin;
		Pin outputPin = carrying;
		boolean binding = false;
		
		if (inputPinVisual != null && inputPin != null && !inputPin.isBound() // No Null Values
				&& !outputPin.getParent().equals(inputPin.getParent().getParent()) //Not binding to same node
				&& !outputPin.getParent().equals(inputPin.getParent()) //Not binding to same node
				&& inputPin.getCanBind()
				&& (!(outputPin.getType() == PinType.Next && inputPin.getParent().getNodeType() != NodeType.OperatorNode)
				|| !(outputPin.getType() == PinType.Next && inputPin.getType() != PinType.Infinite))
				&& !(outputPin.getType() == PinType.Output && inputPin.getType() != PinType.Input)
				&& !(outputPin.getType() == PinType.InfiniteOutput && inputPin.getType() != PinType.Input )) {
	
			//Remove old connector
			if (outputPin.getConnect() != null && outputPin.getType() != PinType.InfiniteOutput) {
				outputPin.getConnect().remove();
				removeConnector(outputPin.getConnect());
			}
			outputPin.setConnect(connector);
			inputPin.setBound(true);
			outputPin.setBound(true);
			
			connector.setLastNode(inputPin.getParent());

			connector.setFirstPin(outputPin);
			connector.setSecondPin(inputPin);
			connector.endXProperty()
					.bind(Bindings
							.createDoubleBinding(
									() -> GraphicsManager.container
											.sceneToLocal(
													inputPinVisual.localToScene(inputPinVisual.getBoundsInLocal()))
											.getMinX() + (Pin.size / 2),
									inputPinVisual.getParent().getParent().layoutXProperty(),
									inputPinVisual.getParent().layoutXProperty(), container.translateXProperty()));
			connector.endYProperty()
					.bind(Bindings
							.createDoubleBinding(
									() -> GraphicsManager.container
											.sceneToLocal(
													inputPinVisual.localToScene(inputPinVisual.getBoundsInLocal()))
											.getMinY() + (Pin.size / 2),
									inputPinVisual.getParent().getParent().layoutYProperty(),
									inputPinVisual.getParent().layoutYProperty(), container.translateYProperty()));
			
			outputPin.setConnect(connector);
			inputPin.setConnect(null, connector);
			connector.toBack();
			binding = true;
			onBuildGraphToCommand();
		}
		if (!binding) {
			connector.remove();
			if (outputPin.getConnect() != null && outputPin.getType() != PinType.InfiniteOutput) {
				outputPin.getConnect().remove();
				removeConnector(outputPin.getConnect());
			}
			onBuildGraphToCommand();
		}
		carrying = null;
		getScene().setOnMouseMoved(x -> {
		});
	}

	/**
	 * Binds the pins of 2 different nodes
	 * @param firstNodeId The ID of the first node to be bound
	 * @param secondNodeId The ID of the second node to be bound
	 * @param firstPinId The ID of the pin in the first node being bound
	 * @param secondPinId The ID of the pin in the second node being bound
	 */
	void bindNodes(String firstNodeId, String secondNodeId, String firstPinId, String secondPinId) {
		GraphicalNode firstNode = nodes.get(firstNodeId);
		Pin firstPin = firstNode.getPin(firstPinId);

		GraphicalNode lastNode = nodes.get(secondNodeId);
		Pin secondPin = lastNode.getPin(secondPinId);

		Connector c = new Connector();

		firstPin.setConnect(null, c);
		secondPin.setConnect(null, c);
		firstPin.setBound(true);
		secondPin.setBound(true);
		c.setFirstNode(firstNode);
		c.setLastNode(lastNode);
		c.setFirstPin(firstPin);
		c.setSecondPin(secondPin);
		c.startXProperty()
				.bind(Bindings.createDoubleBinding(() -> firstPin.getVisual().getParent()
						.localToParent(firstPin.getVisual().getBoundsInParent()).getMinX() + (Pin.size / 2),
						firstNode.layoutXProperty()));

		c.startYProperty()
				.bind(Bindings.createDoubleBinding(() -> firstPin.getVisual().getParent()
						.localToParent(firstPin.getVisual().getBoundsInParent()).getMinY() + (Pin.size / 2),
						firstNode.layoutYProperty()));

		c.endXProperty()
				.bind(Bindings.createDoubleBinding(
						() -> GraphicsManager.container.sceneToLocal(secondPin.getVisual().getParent()
								.localToScene(secondPin.getVisual().getBoundsInParent())).getMinX() + (Pin.size / 2),
						secondPin.getVisual().getParent().getParent().layoutXProperty(),
						secondPin.getVisual().getParent().layoutXProperty()));
		c.endYProperty().bind(Bindings.createDoubleBinding(
				() -> GraphicsManager.container.sceneToLocal(
						secondPin.getVisual().getParent().localToScene(secondPin.getVisual().getBoundsInParent()))
						.getMinY() + (secondPin.getVisual().getHeight() / 2),
				secondPin.getVisual().getParent().getParent().layoutYProperty()));

		addConnector(c);
		c.toBack();
		secondPin.getVisual().toFront();
		firstPin.getVisual().toFront();

	}

	/**
	 * Binds 2 different nodes
	 * @param txtNode The first node being bound
	 * @param pin The pin of the second node to be bound
	 */
	public void bindNodes(CustomNode txtNode, Pin pin) {
	
		GraphicalNode firstNode = txtNode;
		Pin firstPin = firstNode.getOutput();

		GraphicalNode lastNode = pin.getParent();
		Pin secondPin = pin;

		Connector c = new Connector();

		firstPin.setConnect(null, c);
		secondPin.setConnect(null, c);
		firstPin.setBound(true);
		secondPin.setBound(true);
		c.setFirstNode(firstNode);
		c.setLastNode(lastNode);
		c.setFirstPin(firstPin);
		c.setSecondPin(secondPin);
		c.startXProperty()
				.bind(Bindings.createDoubleBinding(() -> firstPin.getVisual().getParent()
						.localToParent(firstPin.getVisual().getBoundsInParent()).getMinX() + (Pin.size / 2),
						firstNode.layoutXProperty()));

		c.startYProperty()
				.bind(Bindings.createDoubleBinding(() -> firstPin.getVisual().getParent()
						.localToParent(firstPin.getVisual().getBoundsInParent()).getMinY() + (Pin.size / 2),
						firstNode.layoutYProperty()));
	
		c.endXProperty().bind(Bindings.createDoubleBinding(
				() -> GraphicsManager.container.sceneToLocal(
						secondPin.getVisual().getParent().localToScene(secondPin.getVisual().getBoundsInParent()))
						.getMinX() + (secondPin.getVisual().getWidth() / 2),
				secondPin.getVisual().getParent().getParent().layoutXProperty(),
				secondPin.getVisual().getParent().layoutXProperty()));
		c.endYProperty().bind(Bindings.createDoubleBinding(
				() -> GraphicsManager.container.sceneToLocal(
						secondPin.getVisual().getParent().localToScene(secondPin.getVisual().getBoundsInParent()))
						.getMinY() + (secondPin.getVisual().getHeight() / 2),
				secondPin.getVisual().getParent().getParent().layoutYProperty()));
		addConnector(c);
		c.toBack();
		secondPin.getVisual().toFront();
		firstPin.getVisual().toFront();

	}

	/**
	 * Adds a connector to the graphics interface
	 * 
	 * @param node
	 *            - The connector being added
	 */
	public void addConnector(Connector node) {
		connectors.add(node);
		container.getChildren().add(node);
	}

	/**
	 * Removes a connector from the graphics interface
	 * 
	 * @param node
	 *            - Connector being removed
	 */
	public void removeConnector(Connector node) {
		container.getChildren().remove(node);
		connectors.remove(node);

	}

	/**
	 * Moves the scene to center the node selected
	 * @param node the node to be centered
	 */
	private void goToNode(GraphicalNode node) {
		Point2D sceneMiddle = new Point2D(this.getWidth() / 2, this.getHeight() / 2);
		double vectorX = sceneMiddle.getX()
				- (container.getTranslateX() + node.getLayoutX() + (node.visual.getWidth() / 2));
		double vectorY = sceneMiddle.getY()
				- (container.getTranslateY() + node.getLayoutY() + (node.visual.getHeight() / 2));
		container.setTranslateX(container.getTranslateX() + vectorX);
		container.setTranslateY(container.getTranslateY() + vectorY);
	}

	/**
	 * Adds a graphical node to the graphics interface
	 * @param node the node being added
	 */
	public void addGraphicalNode(GraphicalNode node, boolean onBuild) {
		container.getChildren().add(node);
		nodes.put(node.getNodeId(), node);
		JButton btn = new JButton(node.text.getText());
		btn.setForeground(Color.WHITE);
		btn.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0, 128, 114)));
		btn.setFocusPainted(false);
		btn.setBackground(new Color(0, 68, 95));
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				goToNode(node);

			}
		});
		node.setMoveButton(btn);
		JButton x = btn;
		btn.addMouseListener(new java.awt.event.MouseListener() {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent arg0) {
				// TODO Auto-generated method stub
				x.setBackground(new Color(46, 102, 124));
			}

			@Override
			public void mouseEntered(java.awt.event.MouseEvent arg0) {
				x.setBackground(new Color(46, 102, 124));

			}

			@Override
			public void mouseExited(java.awt.event.MouseEvent arg0) {
				x.setBackground(new Color(0, 68, 95));

			}

			@Override
			public void mousePressed(java.awt.event.MouseEvent arg0) {
				// TODO Auto-generated method stub
				x.setBackground(new Color(0, 56, 78));
			}

			@Override
			public void mouseReleased(java.awt.event.MouseEvent arg0) {
				// TODO Auto-generated method stub
				x.setBackground(new Color(0, 68, 95));
			}

		});
		Main.pnl.add(x);

		if (onBuild)
			onBuildGraphToCommand();
	}

	/**
	 * Removes a node from the graphics interface
	 * 
	 * @param node
	 *            - The node to be removed
	 */
	public void removeGraphicalNode(GraphicalNode node) {
		container.getChildren().remove(node);
		nodes.remove(node.getNodeId());
		Main.pnl.remove(node.getMoveButton());
	}

	/**
	 * Checks if a node has been connect
	 * 
	 * @param node
	 *            the node being checked
	 * @return
	 */
	public boolean containsGraphicalNode(CommandNode node) {
		return getChildren().contains(node);
	}

	/**
	 * Checks whether a pin is being carried
	 * @return Whether a pin is being carried
	 */
	public boolean isCarrying() {
		if (carrying == null)
			return false;
		return true;
	}

	/**
	 * Checks if all the nodes currently on the graphics interface have been bound
	 * to another node.
	 * 
	 * @return
	 */
	private boolean areNodesConnected() {
		LinkedHashSet<GraphicalNode> set = new LinkedHashSet<GraphicalNode>();
		for (Connector conn : connectors) {
			set.add((GraphicalNode) conn.getFirstNode());
			set.add((GraphicalNode) conn.getLastNode());
		}
		HashSet<Node> fin = new HashSet<Node>();
		fin.addAll(getChildren());
		Predicate<Node> pred = n -> !GraphicalNode.class.isInstance(n);
		fin.removeIf(pred);
		fin.removeAll(set);
		if (!connectors.isEmpty() & !fin.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Converts all the nodes into a Linux command/script if they are all connected
	 * 
	 * @return
	 */
	public String graphToCommand() {
		if (areNodesConnected()) {
			try {
				currentTree = new ArrayList<GraphicalNode>(nodes.values()).get(0).toTree();
				currentTree.build(null);
				convertTreeToString();
				cleanResult();
			} catch (IndexOutOfBoundsException e) {

			}
		}
		return currentTreeValue;
	}

	/**
	 * Returns the selected Node
	 * 
	 * @return
	 */
	public Node getSelected() {
		return selected;
	}

	/**
	 * Sets the selected node
	 * 
	 * @param node The node that was selected
	 */
	public void setSelected(Node node) {
		if (selected != null) {
			if (selectedNodeType == NodeType.CommandNode ||
					selectedNodeType == NodeType.OperatorNode ||
					selectedNodeType == NodeType.CustomNode ||
					selectedNodeType == NodeType.ForNode) {
				((GraphicalNode) selected).getVisual().setStroke(null);
			} else if (selectedNodeType == NodeType.Pin){
				if (selectedPin != null)
				for (Connector con : selectedPin.getConnectors()) {
					if (con != null)
						con.setStroke(javafx.scene.paint.Color.GRAY);
					
				}
				
				((Rectangle) selected).setStroke(javafx.scene.paint.Color.GRAY);
			}
			selectedPin = null;
		}

		if (GraphicalNode.class.isInstance(node)) {
			GraphicalNode gNode = (GraphicalNode) node;
			gNode.getVisual().setStroke(javafx.scene.paint.Color.RED);
			selectedNodeType = gNode.getNodeType();
			onBuildGraphToCommand(gNode.toTree());
		} else if (Rectangle.class.isInstance(node)) {
			((Rectangle) node).setStroke(javafx.scene.paint.Color.RED);
			selectedPin = tempPin;
			for (Connector con : selectedPin.getConnectors()) {
				if (con != null)
				con.setStroke(javafx.scene.paint.Color.RED);
				
			}
			
			selectedNodeType = NodeType.Pin;
		}
		selected = node;

	}

	/**
	 * Deletes the selected node from the interface
	 */
	public void deleteSelected() {
		if (selectedNodeType == NodeType.CommandNode ||
				selectedNodeType == NodeType.OperatorNode ||
				selectedNodeType == NodeType.CustomNode ||
				selectedNodeType == NodeType.ForNode) {
			((GraphicalNode) selected).remove();
			removeGraphicalNode((GraphicalNode) selected);
		} else if (selectedNodeType == NodeType.Pin) {
			if (selected == tempPin.getVisual() && tempPin.getType() == PinType.Output ||
					tempPin.getType() == PinType.InfiniteOutput || tempPin.getType() == PinType.Next ||
					tempPin.getType() == PinType.OutputNext) {
				Pin pin = tempPin;
				ArrayList<Connector> list = (ArrayList<Connector>) tempPin.getConnectors().clone();
				for (Connector c : list) {
					c.remove();
				}
				
			} else {
			Node node = ((Rectangle) selected).getParent().getParent();
			GraphicalNode gNode = (GraphicalNode) node;
			if (gNode.getNodeType() == NodeType.CommandNode)
				((CommandNode)gNode).removeOption((Rectangle) selected);
			else
				((ForNode)gNode).removeInput((Rectangle) selected);
			}
		}
		onBuildGraphToCommand();
	}

	/**
	 * Cleans the currentTreeValue slightly by removing the all double spaces,
	 * trailing spaces and semi-colons
	 */
	private void cleanResult() {
		cleanResult(currentTreeValue);
	}

	/**
	 * Trims the trailing spaces and semi-colons from the text
	 * 
	 * @param text
	 * @return Trimmed text
	 */
	private String cleanResult(String text) {
		int len = text.length() - 1;
		int i = 0;
		text.replace("  ", " ");
		while (text.charAt(len) == ' ' || text.charAt(len) == ';') {
			i++;
			len--;
		}
		text = text.substring(0, text.length() - i);
		return text;
	}

	/**
	 * Builds the command from the Tree
	 */
	private void convertTreeToString() {
		currentTreeValue = "";
		currentTree.buildValue(0, true);
	}

}
