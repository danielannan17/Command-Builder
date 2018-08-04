package classes;

import java.util.ArrayList;

/**
 * Stores everything related to an option of a command along with an identifying number
 * @author Daniel Annan - 1569423
 *
 */
public class Option {
	private String option, description;	
	private int numOfArguments;
	private ArrayList<String> arguments;
	private int id;
	
	/**
	 * Class Constructor
	 * @param id The identifier of the option
	 * @param option The name of the option
	 * @param description The description of what the option does
	 * @param numOfArguments The number of arguments the option can take
	 */
	public Option (int id, String option, String description, int numOfArguments) {
		this.option = option;
		this.id = id;
		this.description = description;
		arguments = new ArrayList<String>();
        this.numOfArguments = numOfArguments;	        		
     
	}
	
	/**
	 * Returns the ID of the option
	 * @return The identifying number
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Adds an argument to the list of possible arguments.
	 * @param argument The argument to be added
	 */
	public void addArgumentDescription(String argument) {
		if (arguments.size() < numOfArguments) {
			arguments.add(argument);
		}
	}
	
	/**
	 * Returns the name of the  option
	 * @return The name of the option
	 */
	public String getOption() {
		return option;
	}

	/**
	 * Returns the description of the argument chosen
	 * @param i the index of the argument
	 * @return the description of the argument at index i
	 */
	public String getArgument(int i) {
		return arguments.get(i);
	}
	
	/**
	 * Returns the description of what the option does
	 * @return The description of the option
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the number of arguments the option has
	 * @return The number of arguments
	 */
	public int getNumOfArguments() {
		return numOfArguments;
	}
	

}
