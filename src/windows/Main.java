package windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;

import com.fasterxml.jackson.databind.ObjectMapper;

import classes.Command;
import classes.Option;
import classes.TooltipJButton;
import database.DatabaseManager;
import graphics.CommandNode;
import graphics.CustomNode;
import graphics.ForNode;
import graphics.GraphicsManager;
import graphics.OperatorNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
/**
 * The Main class
 * @author Daniel Annan - 1569423
 *
 */
public class Main {

	private JFrame frmCommandBuilder;
	public static JTextField txtCommand;
	private JTabbedPane tpnlSlider;
	private boolean sliderShow = false;
	private JPanel pnlGraphics;
	private JButton btnDisplay;
	private JFXPanel fxPanel;
	private Panel panel;
	public static ObjectMapper mapper;
	public static DatabaseManager db;
	public static Hashtable<String, Command> commands;
	public static GraphicsManager graphics;
	private PrintWriter toServer;
	private BufferedReader fromServer;
	public static ResultSet history;
	private JButton btnConvert;
	private JButton btnRun;
	public static JButton btnSlider;
	public static JPanel pnl;
	static String output;
	boolean showHistory;
	static String errors;
	private static JPanel pnlHistory;
	private JPanel pnlHistoryParent;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmCommandBuilder.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Moves to the next record in history
	 * @return whether there was a next record
	 */
	private boolean nextHistory() {
		try {
			if (!history.isLast() && history.next()) {
				btnDisplay.setEnabled(true);
				return true;
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Displays the history record on the scene
	 * @param clear Whether the scene should be cleared
	 */
	private static void displayHistoricCommand(boolean clear) {
		try {
			txtCommand.setText(history.getString("value"));

			byte b[] = history.getBytes("nodes");
			ByteArrayInputStream bi = new ByteArrayInputStream(b);
			ObjectInputStream si = new ObjectInputStream(bi);
			ArrayList<String> obj = (ArrayList<String>) si.readObject();
			Task<Void> task = new Task<Void>() {
				@Override
				public void run() {
					try {
						graphics.load(history.getInt("id"), obj, clear);
						graphics.resetContainer();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				protected Void call() throws Exception {
					// TODO Auto-generated method stub
					return null;
				}
			};
			Platform.runLater(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Moves to the previous record in history
	 * @return whether there was a previous record
	 */
	private boolean previousHistory() {
		try {
			if (!history.isFirst() && history.previous()) {		
				btnDisplay.setEnabled(true);
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Executes the command on the computer and displays any output it may have
	 * @param command The command to be executed
	 */
	private void executeCommand(String command) { 
		try {
			Thread commandThread = new Thread() {
				public void run() {
					String s = null;
					output = "";
					errors = "";
					Process p = null;
					try {
						p = Runtime.getRuntime().exec("mktemp");
						BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						String name = stdInput.readLine();
						String scriptName = name + ".sh";
						String toExecute = "mv " + name + " " + scriptName;
						p = Runtime.getRuntime().exec(toExecute);
						PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(scriptName)));
						String[] newCommand = command.split(Character.toString((char)'\n'));
						for (String com : newCommand) {
							writer.println(com);
							writer.flush();
						}
					    writer.close();
						p = Runtime.getRuntime().exec("sh " + scriptName);
						stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
						boolean running = true;
						boolean gotInfo = true;
						while (running && gotInfo) {
						gotInfo = false;
						while (stdInput.ready() && (s = stdInput.readLine()) != null) {
							output += s + System.lineSeparator();
							gotInfo = true;
						}
						
						while (stdError.ready() && (s = stdError.readLine()) != null) {
							errors += s + System.lineSeparator();
							gotInfo = true;
						}
						}
						ArrayList<String> list = new ArrayList<String>();
						if (!output.trim().equals("") || !output.matches(" *" )) {
							list.add("Show Output");
						}
						if (!errors.equals("")) {
							list.add("Show Errors");
						}

						if (!list.isEmpty()) {
							list.add("Close");
							Task<Void> task = new Task<Void>() {
								private void showAlert() {
									Alert alert = new Alert(AlertType.CONFIRMATION);
									alert.setTitle("Confirmation Dialog with Custom Actions");
									alert.setHeaderText("Look, a Confirmation Dialog with Custom Actions");
									alert.setContentText("Choose your option.");
									ArrayList<ButtonType> li = new ArrayList<ButtonType>();
									for (String s : list) {
										ButtonType btn = new ButtonType(s);
										li.add(btn);
									}
									ButtonType btn = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
									li.add(btn);
									alert.getButtonTypes().setAll(li);
									Optional<ButtonType> result = alert.showAndWait();
									if (result.isPresent()) {
										for (int i = 0; i < li.size(); i++) {
											if (result.get() == li.get(i)) {
												if (result.get().getText().equals("Show Output")) {
													showInfo(true, output);
												} else if (result.get().getText().equals("Show Error")) {
													showInfo(false, errors);
												} else {

												}
											}
										}
									}
								}

								private void showInfo(boolean output, String info) {
									Alert alert;
									Label label;
									TextArea textArea = new TextArea(info);
									if (output) {
										alert = new Alert(AlertType.INFORMATION);
										alert.setTitle("Standard Output");
										alert.setHeaderText("Standard Output");
										label = new Label("The output was:");
										textArea.setText(Main.output);
									} else {
										alert = new Alert(AlertType.ERROR);
										alert.setTitle("Error Messages");
										alert.setHeaderText("Error Messages");
										label = new Label("The error messages were:");
										textArea.setText(Main.errors);
									}

									textArea.setEditable(false);
									textArea.setWrapText(true);

									textArea.setMaxWidth(Double.MAX_VALUE);
									textArea.setMaxHeight(Double.MAX_VALUE);
									GridPane.setVgrow(textArea, Priority.ALWAYS);
									GridPane.setHgrow(textArea, Priority.ALWAYS);

									GridPane expContent = new GridPane();
									expContent.setMaxWidth(Double.MAX_VALUE);
									expContent.add(label, 0, 0);
									expContent.add(textArea, 0, 1);

									// Set expandable Exception into the dialog pane.
									alert.getDialogPane().setExpandableContent(expContent);

									alert.showAndWait();
									showAlert();
								}

								@Override
								public void run() {

									showAlert();

								}

								@Override
								protected Void call() throws Exception {
									// TODO Auto-generated method stub
									return null;
								}
							};
							Platform.runLater(task);

						}

					} catch (Exception e) {
					}
				}
			};
			commandThread.start();
			JOptionPane.showMessageDialog(null,
				    "Command Executed",
				    "Finished",
				    JOptionPane.PLAIN_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the connection to the database
	 */
	private void setUpDatabase() {
		db = new DatabaseManager();
		try {
			history = db.getHistory();
			history.afterLast();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Refreshes the history list
	 */
	public static void refreshHistoryList() {
		try {
			int row = history.getRow();
			history = db.getHistory();
			pnlHistory.removeAll();
			history.beforeFirst();
			while (history.next()) {
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				int id = history.getInt("id");
				TooltipJButton b = new TooltipJButton(history.getString("title"), history.getString("description"),history.getRow());
				b.setForeground(Color.WHITE);
				b.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,128,114)));
				b.setFocusPainted(false);
				b.setBackground(new Color(0,68,95));
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Task<Void> task = new Task<Void>() {
							@Override
							public void run() {
								try {
									history.absolute(b.getIndex());
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								displayHistoricCommand(false);
							}

							@Override
							protected Void call() throws Exception {
								// TODO Auto-generated method stub
								return null;
							}
						};
						Platform.runLater(task);
					}
				});
				JButton g = b;
				b.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						// TODO Auto-generated method stub
						g.setBackground(new Color(46, 102, 124));
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
						g.setBackground(new Color(46, 102, 124));
						
					}

					@Override
					public void mouseExited(MouseEvent arg0) {
						g.setBackground(new Color(0,68,95));
						
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
						// TODO Auto-generated method stub
						g.setBackground(new Color(0, 56, 78));
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {
						// TODO Auto-generated method stub
						g.setBackground(new Color(0,68,95));
					}
					
				});
				
				JButton del = new JButton("X");
				del.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						
							db.deleteHistoryRecord(id);
							refreshHistoryList();
					
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
						del.setBackground(new Color(46,151,139));
					}

					@Override
					public void mouseExited(MouseEvent arg0) {
						del.setBackground(new Color(0,128,144));
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
						del.setBackground(new Color(0,105,94));
						
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {
						del.setBackground(new Color(0,128,144));
						
					}


					
				});
				del.setBorderPainted(false);
				del.setFocusPainted(false);
				del.setBackground(new Color(0,128,144));
				panel.add(g,BorderLayout.CENTER);
				panel.add(del,BorderLayout.EAST);
				pnlHistory.add(panel);
			
			}
			history.absolute(row);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
	
	/**
	 * Starts the python server used for conversion
	 */
	protected void executePython() {
		try {
			Thread server = new Thread() {
				public void run() {
					String s = null;
					Process p = null;
					try {
						p = Runtime.getRuntime().exec("python python-server/server.py");
						BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
						BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					
						s = stdInput.readLine();
						
						int port = Integer.parseInt(s);
						boolean retry = true;
						while (retry) {
							try {
								retry = false;
								connect(port);
							} catch (Exception e) {
								retry = true;
								sleep(1000);
							}
						}

						while (true) {
							while ((s = stdInput.readLine()) != null) {
								if (s.equals("Ready"))
									btnConvert.setEnabled(true);

							}


						}
					} catch (Exception e) {
						if (btnConvert != null)
							btnConvert.setEnabled(false);
						p.destroy();
						executePython();
					}
				}
			};
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Toggles the sliding menu
	 */
	protected void toggleSlider() {
		if (sliderShow) {
			tpnlSlider.setLocation(-tpnlSlider.getWidth(), 0);
			btnSlider.setIcon(new ImageIcon(Main.class.getResource("/images/slider-button-open.png")));
			tpnlSlider.setVisible(false);
			sliderShow = false;
		} else {
			tpnlSlider.setLocation(0, 0);
			btnSlider.setIcon(new ImageIcon(Main.class.getResource("/images/slider-button-close.png")));
			tpnlSlider.setVisible(true);
			sliderShow = true;
		}
	}

	/**
	 * Connects to the python server
	 * @param port The port of the server
	 * @throws IOException
	 */
	private void connect(int port) throws IOException {
		Thread client = null;
		final InetAddress host = InetAddress.getLocalHost();
		Socket sock = new Socket(host, port);
		client = new Thread() {

			public void run() {
				try {
					toServer = new PrintWriter(sock.getOutputStream(), true);
					fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				
					while (true) {
						if (fromServer.ready()) {
							String pythonResult;
							pythonResult = fromServer.readLine();
							if (pythonResult.startsWith("-1:")) {
								pythonResult = pythonResult.substring(3);
								JOptionPane.showMessageDialog(null, "Couldn't get an answer","Failed",
										JOptionPane.ERROR_MESSAGE);
							} else if (pythonResult.startsWith("0:")) {
								JOptionPane.showMessageDialog(null, "We got an answer","That's a match",
										JOptionPane.INFORMATION_MESSAGE);
								pythonResult = pythonResult.substring(2);
								txtCommand.setText(pythonResult);
							}
							btnConvert.setEnabled(true);

						}
					}
				} catch (Exception e) {
				}
			}
		};
		client.setDaemon(true);
		client.start();

	}

	/**
	 * Loads all the commands from the database into the server
	 */
	private void setUpCommands() {
		try {
			commands = new Hashtable<String, Command>();
			Command command;
			Option option;
			ResultSet commandSet = db.getCommands();
			while (commandSet.next()) {
				int cid = commandSet.getInt(1);
				command = new Command(commandSet.getString("Name"), commandSet.getString("Description"),
						commandSet.getInt("Inputs"), commandSet.getInt("OutputType"));
				ResultSet argSet = db.getCommandInputs(cid);
				while (argSet.next()) {
					command.addArgumentDescription(argSet.getString("Description"));
				}
				ResultSet optionSet = db.getOptions(cid);
				while (optionSet.next()) {
					int oid = optionSet.getInt(1);
					option = new Option(optionSet.getInt("id"), optionSet.getString("Name"),
							optionSet.getString("description"), optionSet.getInt("Inputs"));
					ResultSet oInputSet = db.getOptionInputs(oid);
					while (oInputSet.next()) {
						option.addArgumentDescription(oInputSet.getString("Description"));
					}
					command.addOption(option);

				}
				commands.put(command.getCommand(), command);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Sends the string in the txtCommand to the python server for it to try convert the string
	 */
	void convertNaturalToCommand() {
		if (!txtCommand.getText().trim().equals("")) {
		toServer.println(txtCommand.getText());
		toServer.flush();
		btnConvert.setEnabled(false);
		}
		}

	/**
	 * Create the application.
	 */
	public Main() {
		setUpDatabase();
		executePython();
		setUpCommands();
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		mapper = new ObjectMapper();
		frmCommandBuilder = new JFrame();
		frmCommandBuilder.getContentPane().setBackground(new Color(0,68,95));
		frmCommandBuilder.setTitle("Command Builder");
		frmCommandBuilder.setBounds(100, 100, 450, 300);
		frmCommandBuilder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmCommandBuilder.getContentPane().setLayout(null);
		frmCommandBuilder.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void componentResized(ComponentEvent e) {

				tpnlSlider.setSize(Math.min(300, frmCommandBuilder.getContentPane().getWidth() / 3),
						frmCommandBuilder.getContentPane().getHeight());
				if (tpnlSlider.getWidth() < 200) {
					tpnlSlider.setSize(200, tpnlSlider.getHeight());
				}
				if (!sliderShow) {
					tpnlSlider.setLocation(-tpnlSlider.getWidth(), 0);
				}
				panel.setSize(frmCommandBuilder.getContentPane().getWidth(),
						frmCommandBuilder.getContentPane().getHeight());
				pnlGraphics.setSize(panel.getWidth() - 24, panel.getHeight() - 95);
				txtCommand.setSize(
						panel.getWidth() - 42 - btnDisplay.getWidth() - btnConvert.getWidth() - btnRun.getWidth(),
						txtCommand.getHeight());
				btnDisplay.setLocation(txtCommand.getWidth() + 6 + txtCommand.getX(), btnDisplay.getY());
				btnConvert.setLocation(txtCommand.getWidth() + btnDisplay.getWidth() + 12 + txtCommand.getX(),
						btnConvert.getY());
				btnRun.setLocation(
						txtCommand.getWidth() + btnConvert.getWidth() + btnDisplay.getWidth() + 18 + txtCommand.getX(),
						btnRun.getY());

			}

			@Override
			public void componentShown(ComponentEvent e) {

			}
		});

		panel = new Panel();
		panel.setBackground(Color.DARK_GRAY);
		panel.setBounds(0, 0, 432, 253);
		panel.setLayout(null);
		panel.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {

			}

			@Override
			public void componentMoved(ComponentEvent e) {

			}

			@Override
			public void componentResized(ComponentEvent e) {
				pnlGraphics.setSize(panel.getWidth() - 22, panel.getHeight() - 67);
				fxPanel.setSize(pnlGraphics.getWidth(), pnlGraphics.getHeight());
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub

			}
		});

		txtCommand = new JTextField();
		txtCommand.setBackground(Color.LIGHT_GRAY);
		txtCommand.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_UP) {
					if (nextHistory()) {
						displayHistoricCommand(true);
						showHistory = true;
					}

				} else if (arg0.getKeyCode() == KeyEvent.VK_DOWN) {
					
					if (previousHistory()) {
						displayHistoricCommand(true);
						showHistory = true;
					}
				} else {
					showHistory = false;
				}
			}
		});
		txtCommand.setBounds(12, 14, 294, 22);
		panel.add(txtCommand);
		txtCommand.setColumns(10);

		btnSlider = new JButton(new ImageIcon(Main.class.getResource("/images/slider-button-open.png")));
		btnSlider.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				toggleSlider();
			}
		});
		btnSlider.setSize(30, 63);
		btnSlider.setBackground(new Color(0, 0, 0, 0));
		btnSlider.setBorderPainted(false);
		btnSlider.setFocusPainted(false);
		btnSlider.setContentAreaFilled(false);
		
		panel.add(btnSlider);

		UIManager.put("TabbedPane.contentAreaColor", new Color(0,68,95));
		UIManager.put("TabbedPane.highlight", new Color(0,68,95));
		UIManager.put("TabbedPane.selectHighlight", new Color(0,68,95));
		UIManager.put("TabbedPane.borderHightlightColor", new Color(0,68,95));
		UIManager.put("TabbedPane.background", new Color(0,68,95));
		UIManager.put("TabbedPane.darkShadow", new Color(0,68,95));
		UIManager.put("TabbedPane.tabsOverlapBorder",false);
		UIManager.put("TabbedPane.unselectedBackground", new Color(0,128,114));

		tpnlSlider = new JTabbedPane();
		tpnlSlider.setBackground(new Color(0,128,114));
		ScrollPane spnlCommands = new ScrollPane(ScrollPane.SCROLLBARS_NEVER);
		tpnlSlider.addTab("Commands", spnlCommands);
		pnl = new JPanel(new GridLayout(0, 1));
		JPanel pnlParent = new JPanel(new BorderLayout());
		pnlParent.add(pnl, BorderLayout.PAGE_START);
		

		spnlCommands.add(pnlParent);
		spnlCommands.setSize(200, 200);
		spnlCommands.setPreferredSize(new Dimension(100, 100));
		JButton b = new JButton("Operator");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task<Void> task = new Task<Void>() {
					@Override
					public void run() {
						toggleSlider();
						graphics.addGraphicalNode(new OperatorNode(0, 0), true);
					}

					@Override
					protected Void call() throws Exception {
						// TODO Auto-generated method stub
						return null;
					}
				};
				Platform.runLater(task);
			}
		});
		b.setSize(100, 0);
		b.setForeground(Color.WHITE);
		b.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,128,114)));
		b.setFocusPainted(false);
		b.setBackground(new Color(0,68,95));
		JButton x = b;
		b.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				x.setBackground(new Color(46, 102, 124));
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				x.setBackground(new Color(46, 102, 124));
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				x.setBackground(new Color(0,68,95));
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				x.setBackground(new Color(0, 56, 78));
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				x.setBackground(new Color(0,68,95));
			}
			
		});
		pnl.add(x);
		
		b = new JButton("Custom");
		b.setForeground(Color.WHITE);
		b.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,128,114)));
		b.setFocusPainted(false);
		b.setBackground(new Color(0,68,95));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task<Void> task = new Task<Void>() {
					@Override
					public void run() {
						toggleSlider();
						graphics.addGraphicalNode(new CustomNode(0, 0), true);
					}

					@Override
					protected Void call() throws Exception {
						// TODO Auto-generated method stub
						return null;
					}
				};
				Platform.runLater(task);
			}
		});
		JButton y = b;
		b.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				y.setBackground(new Color(46, 102, 124));
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				y.setBackground(new Color(46, 102, 124));
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				y.setBackground(new Color(0,68,95));
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				y.setBackground(new Color(0, 56, 78));
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				y.setBackground(new Color(0,68,95));
			}
			
		});
		pnl.add(y);
		
		
		
		b = new JButton("For");
		b.setForeground(Color.WHITE);
		b.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,128,114)));
		b.setFocusPainted(false);
		b.setBackground(new Color(0,68,95));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Task<Void> task = new Task<Void>() {
					@Override
					public void run() {
						toggleSlider();
						graphics.addGraphicalNode(new ForNode(0, 0), true);
					}

					@Override
					protected Void call() throws Exception {
						// TODO Auto-generated method stub
						return null;
					}
				};
				Platform.runLater(task);
			}
		});
		JButton z = b;
		b.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
				z.setBackground(new Color(46, 102, 124));
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				z.setBackground(new Color(46, 102, 124));
				
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				z.setBackground(new Color(0,68,95));
				
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				z.setBackground(new Color(0, 56, 78));
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub
				z.setBackground(new Color(0,68,95));
			}
			
		});
		//pnl.add(z);
		
		
		
		
		
		if (commands != null)
			for (String c : commands.keySet()) {
				b = new JButton(c);
				b.setForeground(Color.WHITE);
				b.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0,128,114)));
				b.setFocusPainted(false);
				b.setBackground(new Color(0,68,95));
				b.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Task<Void> task = new Task<Void>() {
							@Override
							public void run() {
								toggleSlider();
								graphics.addGraphicalNode(new CommandNode(commands.get(c)), true);
							}

							@Override
							protected Void call() throws Exception {
								// TODO Auto-generated method stub
								return null;
							}
						};
						Platform.runLater(task);
					}
				});
				JButton p = b;
				b.addMouseListener(new MouseListener() {

					@Override
					public void mouseClicked(MouseEvent arg0) {
						// TODO Auto-generated method stub
						p.setBackground(new Color(46, 102, 124));
					}

					@Override
					public void mouseEntered(MouseEvent arg0) {
						p.setBackground(new Color(46, 102, 124));
						
					}

					@Override
					public void mouseExited(MouseEvent arg0) {
						p.setBackground(new Color(0,68,95));
						
					}

					@Override
					public void mousePressed(MouseEvent arg0) {
						// TODO Auto-generated method stub
						p.setBackground(new Color(0, 56, 78));
					}

					@Override
					public void mouseReleased(MouseEvent arg0) {
						// TODO Auto-generated method stub
						p.setBackground(new Color(0,68,95));
					}
					
				});
				
				pnl.add(p);
			}
		ScrollPane spnlNodes = new ScrollPane();
		pnl = new JPanel(new GridLayout(0, 1));
		pnlParent.setBackground(new Color(0,68,95));
		pnl.setBorder(null);
		pnlParent = new JPanel(new BorderLayout());
		pnlParent.setBackground(new Color(0,68,95));
		pnlParent.add(pnl, BorderLayout.PAGE_START);
		spnlNodes.add(pnlParent);
		tpnlSlider.addTab("Nodes", spnlNodes);
		
		
		ScrollPane spnlHistory = new ScrollPane();
		pnlHistory = new JPanel(new GridLayout(0, 1));
		pnlHistoryParent = new JPanel(new BorderLayout());
		pnlHistoryParent.setBackground(new Color(0,68,95));
		pnlHistory.setBorder(null);
		pnlHistoryParent.setBackground(new Color(0,68,95));
		pnlHistoryParent.add(pnlHistory, BorderLayout.PAGE_START);
		spnlHistory.add(pnlHistoryParent);
		tpnlSlider.addTab("History", spnlHistory);
		Main.refreshHistoryList();
		
		tpnlSlider.addComponentListener(new ComponentListener() {
			@Override
			public void componentHidden(ComponentEvent e) {
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				if (sliderShow) {
					btnSlider.setLocation(tpnlSlider.getWidth(),
							(tpnlSlider.getHeight() / 2) - btnSlider.getHeight() / 2);
				} else {
					btnSlider.setLocation(0, (tpnlSlider.getHeight() / 2) - btnSlider.getHeight() / 2);
				}
			}

			@Override
			public void componentResized(ComponentEvent e) {
				if (sliderShow) {
					btnSlider.setLocation(tpnlSlider.getWidth(),
							(tpnlSlider.getHeight() / 2) - btnSlider.getHeight() / 2);
				} else {
					btnSlider.setLocation(0, (tpnlSlider.getHeight() / 2) - btnSlider.getHeight() / 2);
				}
			}

			@Override
			public void componentShown(ComponentEvent e) {
			}
		});
		frmCommandBuilder.getContentPane().add(tpnlSlider);
		frmCommandBuilder.getContentPane().add(panel);

		pnlGraphics = new JPanel();
		pnlGraphics.setBorder(null);
		pnlGraphics.setBackground(Color.WHITE);
		pnlGraphics.setBounds(12, 48, 408, 192);
		panel.add(pnlGraphics);
	
		fxPanel = new JFXPanel();
		fxPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, Color.GRAY, new Color(64, 64, 64)));
		fxPanel.setBounds(0, 0, 408, 192);
		fxPanel.setBackground(Color.LIGHT_GRAY);
		fxPanel.setInheritsPopupMenu(false);
		
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				initFX(fxPanel);
			}

			private void initFX(JFXPanel panel) {
				graphics = new GraphicsManager();
				panel.setScene(graphics.getScene());

			}
		});
		pnlGraphics.setLayout(null);


		pnlGraphics.add(fxPanel);

		btnConvert = new JButton(new ImageIcon(Main.class.getResource("/images/001-arrows.png")));
		btnConvert.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				convertNaturalToCommand();
			}
		});
		btnConvert.setBounds(354, 13, 30, 30);
		btnConvert.setEnabled(false);
		btnConvert.setBackground(new Color(0, 0, 0, 0));
		btnConvert.setBorderPainted(false);
		btnConvert.setFocusPainted(false);
		btnConvert.setContentAreaFilled(false);
		panel.add(btnConvert);

		btnRun = new JButton(new ImageIcon(Main.class.getResource("/images/002-play-button.png")));
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				executeCommand(txtCommand.getText());
			}
		});
		btnRun.setBounds(390, 13, 30, 30);
		btnRun.setBackground(new Color(0, 0, 0, 0));
		btnRun.setBorderPainted(false);
		btnRun.setFocusPainted(false);
		btnRun.setContentAreaFilled(false);
		panel.add(btnRun);

		btnDisplay = new JButton(new ImageIcon(Main.class.getResource("/images/004-interface.png")));
		btnDisplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (showHistory) {
					displayHistoricCommand(true);
				} else {
					Task<Void> task = new Task<Void>() {
						@Override
						public void run() {
							graphics.showCommand(txtCommand.getText());
						}

						@Override
						protected Void call() throws Exception {
							// TODO Auto-generated method stub
							return null;
						}
					};
					Platform.runLater(task);
				}
			}
		});
		
		btnDisplay.setBounds(318, 12, 30, 30);
		btnDisplay.setEnabled(true);
		btnDisplay.setBackground(new Color(0, 0, 0, 0));
		btnDisplay.setBorderPainted(false);
		btnDisplay.setFocusPainted(false);
		btnDisplay.setContentAreaFilled(false);
		panel.add(btnDisplay);

	}
}
