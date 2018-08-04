package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Class used to populate the online table.
 * @author Daniel Annan -1569423
 *
 */
public class DatabasePopulator {
	private static Connection onlineConnection;
	static String onlineUsername = "bodilywe_Filler";
	static String onlinePassword = "X_lZkd%BJxk2";
	static String onlineURL = "jdbc:mysql://uk35.siteground.eu:3306/bodilywe_builder";

	
	/**
	 * Drops table with the given name in the online database
	 * @param table The name of the table to be dropped
	 */
	private static void dropTable(String table) {
		try {
			PreparedStatement p = onlineConnection.prepareStatement("drop table IF EXISTS " + table + ";");
			p.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void main(String[] args) {
		onlineConnection = DatabaseManager.connectToDatabase(onlineURL, onlineUsername, onlinePassword, false);
		dropTable("CommandInputs");
		dropTable("OptionInputs");
		dropTable("Options");
		dropTable("Commands");
		DatabaseManager.setUpTables(onlineConnection);
		DatabaseManager.fillCommands(onlineConnection);
		System.out.println("Done");
	}
	
	
	
	
	
	
	
	
	
}
