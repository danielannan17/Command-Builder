package database;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import graphics.Connector;
import graphics.OptionPin;


/**
 * This class handles everything related to the MYSQL database. This includes creating the connection to the 
 * database, updating the tables and filling the software commands from the database on start up.
 * @author Daniel Annan -1569423
 *
 */
public class DatabaseManager {
	private static Connection localConnection;
	static String localUsername;
	static String localPassword = "";
	static String localUrl = "jdbc:mysql://localhost";

	private Connection onlineConnection;
	String onlineUsername = "bodilywe_User";
	String onlinePassword = "password1";
	String onlineURL = "jdbc:mysql://uk35.siteground.eu:3306/bodilywe_builder";

	/**
	 * Class Constructor
	 */
	public DatabaseManager() {
		localUsername = System.getProperty("user.name");
		updateLocalDatabase();
		getHistory();

		try {
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Creates a connection to a MYSQL database;
	 * @param url The URL of the database
	 * @param username The username to log into the database
	 * @param pass The password for the database
	 * @param local Whether it's connecting to a local database or not
	 * @return
	 */
	public static Connection connectToDatabase(String url, String username, String pass, boolean local) {
		try {
			// Load the PostgreSQL JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException ex) {
			System.exit(1);
		}

		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url, username, pass);
		} catch (SQLException ex) {
			if (local) {
				JPanel panel = new JPanel(new BorderLayout(5, 5));
				panel.add(new JLabel("Please enter your MYSQL Login. "
						+ "Setting up a user with the username \"" + System.getProperty("user.name") 
						+ "\" and no password will allow automatic login."), BorderLayout.NORTH);
			    JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
			    label.add(new JLabel("Username", SwingConstants.RIGHT));
			    label.add(new JLabel("Password", SwingConstants.RIGHT));
			    panel.add(label, BorderLayout.WEST);

			    JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
			    JTextField txtUsername = new JTextField();
			    controls.add(txtUsername);
			    JPasswordField password = new JPasswordField();
			    controls.add(password);
			    panel.add(controls, BorderLayout.CENTER);

			    int val = JOptionPane.showConfirmDialog(
			            null, panel, "login", JOptionPane.OK_CANCEL_OPTION);
			    if (val == JOptionPane.OK_OPTION) {
			    	localUsername = txtUsername.getText();
			    	localPassword = new String(password.getPassword());
			    	conn = connectToDatabase(localUrl,localUsername, localPassword, true);
			    } else {
			    	System.exit(0);
			    }
				return conn;
			}
		}

		if (conn != null) {
			System.err.println("Database accessed!");
		} else {
			System.err.println("Failed to make connection");
	
		}

		return conn;
	}

	/**
	 * Creates a table from a result set and also copies the data into the table
	 * @param rs The result set to create the table from
	 * @throws SQLException
	 */
	private void resultSetToTable(ResultSet rs) throws SQLException {
		try {
			makeTable(rs);
			ResultSetMetaData meta = rs.getMetaData();
			String table = meta.getTableName(1);

			List<String> columns = new ArrayList<>();
			for (int i = 1; i <= meta.getColumnCount(); i++)
				columns.add(meta.getColumnName(i));

			try (PreparedStatement s2 = localConnection
					.prepareStatement("INSERT INTO " + table + " (" + columns.stream().collect(Collectors.joining(", "))
							+ ") VALUES (" + columns.stream().map(c -> "?").collect(Collectors.joining(", ")) + ")")) {

				while (rs.next()) {
					for (int i = 1; i <= meta.getColumnCount(); i++)
						s2.setObject(i, rs.getObject(i));

					s2.addBatch();
				}

				s2.executeBatch();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Creates a table from a result set without filling it
	 * @param set The result set to create the fable from
	 * @throws SQLException
	 */
	private void makeTable(ResultSet set) throws SQLException {
		ResultSetMetaData rsmd = set.getMetaData();
		int columnCount = rsmd.getColumnCount();
		StringBuilder sb = new StringBuilder(1024);
		if (columnCount > 0) {
			sb.append("Create table ").append(rsmd.getTableName(1)).append(" ( ");
		}
		for (int i = 1; i <= columnCount; i++) {
			if (i > 1)
				sb.append(", ");
			String columnName = rsmd.getColumnLabel(i);
			String columnType = rsmd.getColumnTypeName(i);

			sb.append(columnName).append(" ").append(columnType);

			int precision = rsmd.getPrecision(i);
			if (precision != 0) {
				sb.append("( ").append(precision).append(" )");
			}
		} // for columns
		sb.append(" );");

		localConnection.prepareStatement(sb.toString()).execute();
	}

	/**
	 * Gets the commands from the local database. If the table does not exist
	 * it returns null.
	 * @return ResultSet all the commands from the Commands table
	 */
	public ResultSet getCommands() {
		try {
			return localConnection.prepareStatement("SELECT * FROM Commands;", PreparedStatement.RETURN_GENERATED_KEYS)
					.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}

	
	/**
	 * Gets the options related to a given command from the local database. If the table does not exist
	 * it returns null.
	 * @param cid The id of the command in the Commands table
	 * @return ResultSet of all the options for that command
	 */
	public ResultSet getOptions(int cid) {
		PreparedStatement p;
		try {
			p = localConnection.prepareStatement("SELECT * FROM Options WHERE cid = ? ORDER BY id;",
					PreparedStatement.RETURN_GENERATED_KEYS);
			p.setInt(1, cid);
			return p.executeQuery();
		} catch (SQLException e) {
			return null;
		}
	}
	
	/**
	 * Gets the arguments related to a given command from the local database.
	 * @param cid The id of the command in the Commands table
	 * @return ResultSet of all the arguments for that option
	 * @throws SQLException
	 */
	public ResultSet getCommandInputs(int cid) throws SQLException {
		PreparedStatement p = localConnection
				.prepareStatement("SELECT * FROM CommandInputs WHERE cid = ? ORDER BY id;");
		p.setInt(1, cid);
		return p.executeQuery();
	}

	
	/**
	 * Gets the arguments related to a given option from the local database.
	 * @param oid The id of the option in the Options table
	 * @return ResultSet of all the arguments for that option
	 * @throws SQLException
	 */
	public ResultSet getOptionInputs(int oid) throws SQLException {
		PreparedStatement p = localConnection.prepareStatement("SELECT * FROM OptionInputs WHERE oid = ? ORDER BY id;");
		p.setInt(1, oid);
		return p.executeQuery();
	}
	
	/**
	 * Drops a table from the local database
	 * @param table The name of the table to be dropped
	 * @throws SQLException 
	 */
	private static void dropTable(String table) throws SQLException {
		
			PreparedStatement p = localConnection.prepareStatement("drop table IF EXISTS " + table + ";");
			p.execute();
	
	}
	
	
	/**
	 * Returns a table from the local database
	 * @param table The table to be returned
	 * @return ResultSet of the table
	 */
	private ResultSet getTable(String table) {
		try {
			PreparedStatement p = localConnection.prepareStatement("select * from " + table + ";");
			return p.executeQuery();
		} catch (SQLException e) {
			return null;
		}

	}
	
	/**
	 * Updates the local database from the online database. If a connection cannot be made to the online database,
	 * it fills the software with commands from the local CSV file.
	 */
	private void updateLocalDatabase() {
		boolean started = false;
		try {
			localConnection = connectToDatabase(localUrl, localUsername, localPassword, true);
			localConnection.prepareStatement("Create database IF NOT EXISTS builder;").execute();
			localConnection.prepareStatement("Use builder;").execute();
			makeHistoryTables();

			onlineConnection = connectToDatabase(onlineURL, onlineUsername, onlinePassword, false);
			ResultSet commandRS = onlineConnection.prepareStatement("select * from Commands;").executeQuery();
			ResultSet optionRS = onlineConnection.prepareStatement("select * from Options;").executeQuery();
			ResultSet cInputsRS = onlineConnection.prepareStatement("select * from CommandInputs;").executeQuery();
			ResultSet InputsRS = onlineConnection.prepareStatement("select * from OptionInputs;").executeQuery();

			if (!commandRS.equals(getTable("Commands")) || !optionRS.equals(getTable("Options"))
					|| !cInputsRS.equals(getTable("CommandInputs")) || !InputsRS.equals(getTable("OptionInputs")))
				startTransaction();
			started = true;
			dropTable("CommandInputs");
			dropTable("OptionInputs");
			dropTable("Options");
			dropTable("Commands");
			resultSetToTable(commandRS);
			resultSetToTable(optionRS);
			resultSetToTable(cInputsRS);
			resultSetToTable(InputsRS);
			completeTransaction();

		} catch (Exception e) {
			if (started)
				rollBack();
			System.err.println("Cant connect to Online Database");
			e.printStackTrace();
			if (getCommands() == null) {
				setUpTables(localConnection);
			}
			try {
				if (getCommands() == null || !getCommands().next())
					fillCommands(localConnection);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}

	}
	

	/**
	 * Creates the tables needed to  keep a history of scripts
	 */
	private static void makeHistoryTables() {
		try {
			PreparedStatement historyQuery = localConnection
					.prepareStatement("CREATE TABLE IF NOT EXISTS history(" 
							+ "id INT AUTO_INCREMENT,"
							+ "title VARCHAR(255) NOT NULL,"
							+ "description TEXT," 
							+ "value MEDIUMTEXT," 
							+ "nodes MEDIUMBLOB NOT NULL," 
							+ "PRIMARY KEY(id));");
			historyQuery.execute();

			PreparedStatement bindingsQuery = localConnection.prepareStatement("CREATE TABLE IF NOT EXISTS bindings("
					+ "id INT AUTO_INCREMENT," + "hid INT NOT NULL, " + "firstNode VARCHAR(37) NOT NULL,"
					+ "lastNode VARCHAR(37) NOT NULL," + "firstPin VARCHAR(37) NOT NULL,"
					+ "secondPin VARCHAR(37) NOT NULL," + "oid INT," + "optionArg INT," + "PRIMARY KEY (id,hid),"
					+ "FOREIGN KEY (hid) REFERENCES history(id));");
			bindingsQuery.execute();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	
	/**
	 * Creates the tables needed to store the commands in a database
	 * @param connection The database to create the tables for
	 */
	public static void setUpTables(Connection connection) {
		try {
			if (connection == localConnection) {
			connection.prepareStatement("CREATE Database IF NOT EXISTS builder").execute();
			connection.prepareStatement("Use builder;").execute();
				makeHistoryTables();
		}
			PreparedStatement commandsQuery = connection
					.prepareStatement("CREATE TABLE IF NOT EXISTS Commands(" + "id INT AUTO_INCREMENT,"
							+ "Name VARCHAR(30) NOT NULL UNIQUE," + "Description VARCHAR(255) NOT NULL,"
							+ "Inputs INT NOT NULL," + "OutputType INT NOT NULL,"
							+ "PRIMARY KEY(id));");
			commandsQuery.execute();

			PreparedStatement optionsQuery = connection
					.prepareStatement("CREATE TABLE IF NOT EXISTS Options(" + "id INT AUTO_INCREMENT UNIQUE,"
							+ "cid INT NOT NULL," + "Name VARCHAR(30) NOT NULL," + "Description VARCHAR(255) NOT NULL,"
							+ "Inputs INT NOT NULL," + "OutputType INT NOT NULL,"
							+ "PRIMARY KEY(cid,Name)," + "FOREIGN KEY (cid) REFERENCES Commands(id));");
			optionsQuery.execute();

			PreparedStatement commandInputsQuery = connection
					.prepareStatement("CREATE TABLE IF NOT EXISTS CommandInputs(" + "id INT AUTO_INCREMENT UNIQUE,"
							+ "cid INT NOT NULL," + "Description VARCHAR(255) NOT NULL,"
							+ "PRIMARY KEY(cid,Description)," + "FOREIGN KEY (cid) REFERENCES Commands(id));");
			commandInputsQuery.execute();

			PreparedStatement optionsInputsQuery = connection
					.prepareStatement("CREATE TABLE IF NOT EXISTS OptionInputs(" + "id INT AUTO_INCREMENT UNIQUE,"
							+ "oid INT NOT NULL," + "Description VARCHAR(255) NOT NULL,"
							+ "PRIMARY KEY(oid,Description)," + "FOREIGN KEY (oid) REFERENCES Options(id));");
			optionsInputsQuery.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	/**
	 * Returns the history table from local database.
	 * @return ResultSet of the history table
	 */
	public ResultSet getHistory() {
		ResultSet result = null;
		try {
			result = localConnection.prepareStatement("SELECT * from history ORDER BY id DESC;").executeQuery();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Deletes a record from the history table;
	 * @param id The id of the record
	 */
	public void deleteHistoryRecord(int id) {
		try {
		PreparedStatement statement = localConnection.prepareStatement("DELETE FROM bindings WHERE hid = ?");
		statement.setInt(1, id);
		statement.execute();
		statement = localConnection.prepareStatement("DELETE FROM history WHERE id = ?");
		statement.setInt(1, id);
		statement.execute();
		} catch (Exception e) {}
	}
	
	/**
	 * Begins a transaction on the local database.
	 * @throws SQLException
	 */
	public void startTransaction() throws SQLException {
		localConnection.prepareStatement("start transaction;").execute();
	}

	
	/**
	 * Ends a transaction on the local database
	 * @throws SQLException
	 */
	public void completeTransaction() throws SQLException {
		localConnection.prepareStatement("commit;").execute();
	}
	
	/**
	 * Rolls back all changes made during the transaction on the local database
	 */
	public void rollBack() {
		try {
			localConnection.prepareStatement("rollback;").execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Adds a record to the history
	 * @param title The title of the record, displayed in the menu
	 * @param description The description of the record, displayed in the menu
	 * @param value The script created from the nodes being saved.
	 * @param obj An array of bytes of all the nodes being saved
	 * @return The ID of the record created
	 * @throws SQLException
	 */
	public int addToHistory(String title, String description, String value, byte[] obj) throws SQLException {
		PreparedStatement p = localConnection.prepareStatement(
				"INSERT INTO history (title, description, value, nodes)" + "VALUES(?,?,?,?)",
				PreparedStatement.RETURN_GENERATED_KEYS);

		p.setString(1, title);
		p.setString(2, description);
		p.setString(3, value);
		p.setBytes(4, obj);
		p.executeUpdate();
		ResultSet id = p.getGeneratedKeys();
		int i = -1;
		if (id.next()) {
			i = id.getInt(1);
		}
		return i;
	}
	
	/**
	 * Saves all the bindings of the connectors for the current script being saved
	 * @param id The id of the record created in the history table for which the binding relates to
	 * @param values The number of bindings being saved, must be less than 1000
	 * @param connectors A list of all the connectors used in this script
	 * @throws SQLException
	 */
	
	public void addBindings(int id, int values, ArrayList<Connector> connectors) throws SQLException {
		if (values > 0 && values < 1000) {
			String query;
			for (int i = 0; i < connectors.size() - 1; i++) {
				Connector c = connectors.get(i);
				if (OptionPin.class.isInstance(c.getSecondPin())
						|| OptionPin.class.isInstance(c.getFirstPin())) {
					int oid;
					int optionArg;
					if (OptionPin.class.isInstance(c.getSecondPin())) {
						oid = ((OptionPin)c.getSecondPin()).getOption().getId();
						optionArg = ((OptionPin)c.getSecondPin()).getArgNum();
					} else {
						oid = ((OptionPin)c.getSecondPin()).getOption().getId();
						optionArg = ((OptionPin)c.getSecondPin()).getArgNum();
					}
					query = "INSERT INTO bindings (hid, firstNode, lastNode, firstPin, secondPin, oid, optionArg) VALUES ";
					query += "('" + id + "'," + "'" + c.getFirstNode().getNodeId() + "'," + "'"
							+ c.getLastNode().getNodeId() + "'," + "'" + c.getFirstPin().getPinId() + "'," + "'"
							+ c.getSecondPin().getPinId() +"'," +oid +","+ optionArg+");";
				} else {
					query = "INSERT INTO bindings (hid, firstNode, lastNode, firstPin, secondPin) VALUES ";
					query += "('" + id + "'," + "'" + c.getFirstNode().getNodeId() + "'," + "'"
							+ c.getLastNode().getNodeId() + "'," + "'" + c.getFirstPin().getPinId() + "'," + "'"
							+ c.getSecondPin().getPinId() + "');";	
				}
				localConnection.prepareStatement(query).executeUpdate();
			}
			
			Connector c = connectors.get(connectors.size() - 1);
			if (OptionPin.class.isInstance(c.getSecondPin())
					|| OptionPin.class.isInstance(c.getFirstPin())) {
				int oid;
				int optionArg;
				if (OptionPin.class.isInstance(c.getSecondPin())) {
					oid = ((OptionPin)c.getSecondPin()).getOption().getId();
					optionArg = ((OptionPin)c.getSecondPin()).getArgNum();
				} else {
					oid = ((OptionPin)c.getSecondPin()).getOption().getId();
					optionArg = ((OptionPin)c.getSecondPin()).getArgNum();
				}
				query = "INSERT INTO bindings (hid, firstNode, lastNode, firstPin, secondPin, oid, optionArg) VALUES ";
				query += "('" + id + "'," + "'" + c.getFirstNode().getNodeId() + "'," + "'"
						+ c.getLastNode().getNodeId() + "'," + "'" + c.getFirstPin().getPinId() + "'," + "'"
						+ c.getSecondPin().getPinId() +"'," +oid +","+ optionArg+");";
			} else {
				query = "INSERT INTO bindings (hid, firstNode, lastNode, firstPin, secondPin) VALUES ";
				query += "('" + id + "'," + "'" + c.getFirstNode().getNodeId() + "'," + "'"
						+ c.getLastNode().getNodeId() + "'," + "'" + c.getFirstPin().getPinId() + "'," + "'"
						+ c.getSecondPin().getPinId() + "');";	
			}
			localConnection.prepareStatement(query).executeUpdate();

		}
	}

	
	/**
	 * Returns all the records from the bindings table for a given history record
	 * @param id The ID of the record in the history table
	 * @return ResultSet containing all the bindings
	 * @throws SQLException
	 */
	public ResultSet getBindings(int id) throws SQLException {
		return localConnection.prepareStatement("SELECT * FROM bindings WHERE hid = " + id + ";").executeQuery();
	}

	/**
	 * Fills the commands table for the given database.
	 * @param connection The connection to the database to be updated
	 */
	public static void fillCommands(Connection connection) {
		try {
			int currentId = -1;
			BufferedReader reader = new BufferedReader(new FileReader("src/resources/commands.csv"));
			String currentLine;
			reader.readLine();
			while ((currentLine = reader.readLine()) != null) {
				// Add Commands
				if (currentLine.startsWith("Command")) {
					String[] commandArray = currentLine.split(",");
					PreparedStatement p = connection.prepareStatement(
							"INSERT INTO Commands (Name, description, inputs, outputtype)" + "VALUES(?,?,?,?);",
							PreparedStatement.RETURN_GENERATED_KEYS);
					p.setString(1, commandArray[1]);
					p.setString(2, commandArray[2]);
					p.setInt(3, Integer.parseInt(commandArray[4]));
					p.setInt(4, Integer.parseInt(commandArray[5]));
					p.executeUpdate();

					ResultSet id = p.getGeneratedKeys();
					currentId = -1;
					if (id.next()) {
						currentId = id.getInt(1);
					}

					// Add command Inputs
					int inputs = commandArray.length - 8;
					if (Integer.parseInt(commandArray[4]) != 0) {
						for (int i = 0; i < inputs; i++) {
							PreparedStatement x = connection.prepareStatement(
									"INSERT IGNORE INTO CommandInputs (cid, description)" + "Values (?,?);");
							x.setInt(1, currentId);
							x.setString(2, commandArray[8 + i]);
							x.executeUpdate();
						}
					}

				} else if (currentLine.startsWith("Option")) {
					// Add options to last command Added
					String[] commandArray = currentLine.split(",");
					PreparedStatement p = connection
							.prepareStatement("INSERT INTO Options (cid, Name, description, inputs, outputtype)"
									+ "VALUES(?,?,?,?,?);", PreparedStatement.RETURN_GENERATED_KEYS);
					p.setInt(1, currentId);
					p.setString(2, commandArray[1]);
					p.setString(3, commandArray[2]);
					p.setInt(4, Integer.parseInt(commandArray[4]));
					p.setInt(5, Integer.parseInt(commandArray[5]));
					p.executeUpdate();
					ResultSet id = p.getGeneratedKeys();
					int optId = -1;
					if (id.next()) {
						optId = id.getInt(1);
					}
					
					// Add Option Inputs
					int inputs = commandArray.length - 8;
					if (Integer.parseInt(commandArray[4]) != 0) {
						for (int i = 0; i < inputs; i++) {
							PreparedStatement x = connection.prepareStatement(
									"INSERT IGNORE INTO OptionInputs (oid, description)" + "Values (?,?);");
							x.setInt(1, optId);
							x.setString(2, commandArray[8 + i]);
							x.executeUpdate();

						}
					}

				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	/**
	 * Gets the option record related to the command and option name given.
	 * @param id The ID of the command in the Commands table
	 * @param optionName The name of the option
	 * @return ResultSet containing the record for the option
	 * @throws SQLException
	 */
	public ResultSet getOptionRow(int id, String optionName) throws SQLException {
		return localConnection
				.prepareStatement("SELECT * from Options WHERE cid = " + id + " and Name = '" + optionName + "';")
				.executeQuery();
	}
	
	/**
	 * Returns the option of the given ID
	 * @param id The ID of the option to be returned
	 * @return ResultSet containing a single record for the option
	 * @throws SQLException
	 */

	public ResultSet getOptionRow(int id) throws SQLException {
		return localConnection
				.prepareStatement("SELECT * from Options WHERE id = " + id + ";")
				.executeQuery();
	}

	
	/**
	 * Returns the record for the command given
	 * @param commandName The name of the command to be returned
	 * @return ResultSet of the Command
	 * @throws SQLException
	 */
	public ResultSet getSingleCommand(String commandName) throws SQLException {
		ResultSet x = localConnection.prepareStatement("SELECT * from Commands WHERE Name = '" + commandName + "';")
				.executeQuery();
		if (x.next())
			return x;
		return null;
	}

}
