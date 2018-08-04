package classes;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import graphics.CommandNode;
import graphics.Connector;
import graphics.OptionPin;

/**
 * This class stores everything needed about a given command
 * @author Daniel Annan - 1569423
 *
 */
public class Command {
		private String command, description;
		LinkedHashMap<String,Option> options;
		private int numOfArguments;
		private int outputType;
		private ArrayList<String> arguments;
	    
		/**
		 * Class Constructor
		 * @param command The name of the command
		 * @param description The description of what the command does
		 * @param numOfArguments The number of arguments the command takes
		 * @param outputType The output number for the command.
		 */
		public Command (String command,String description, int numOfArguments,
				int outputType) {
	        this.command = command;
	        this.description = description;
	        arguments = new ArrayList<String>();
	        options = new LinkedHashMap<String,Option>();
	        this.outputType = outputType;
	        this.numOfArguments = numOfArguments;
	    }
	    
		/**
		 * Adds an argument to the list of possible arguments.
		 * @param argument The argument to be added
		 */
		public void addArgumentDescription(String argument) {
			if (arguments.size() < numOfArguments || numOfArguments == -1) {
				arguments.add(argument);
			}
		}
		
		
		/**
		 * Returns the list of arguments
		 * @return The list of arguments
		 */
	    public ArrayList<String> getArguments() {
			return arguments;
		}

	    /**
	     * Returns the number of arguments
	     * @return the number of arguments
	     */
		public int getNumOfArguments() {
			return numOfArguments;
		}

		/**
		 * Returns the name of the command
		 * @return the command
		 */
		public String getCommand() {
	        return command;
	    }
	    
		/**
		 * Returns the output type
		 * @return the output type
		 */
		public int getOutputType() {
			return outputType;
		}
		
		
		/**
		 * Returns the description for the command
		 * @return The description of what the command does
		 */
	    public String getDescription() {
	    	return description;
	    }
	    
	    
	    /**
	     * Adds an option to the Hashmap of options
	     * @param option The option to be added
	     */
	    public void addOption(Option option) {
	    	options.put(option.getOption(), option);
	    }
	  
	    
	    /**
	     * Returns an option object for the given option name
	     * @param option The name of the option to be returned
	     * @return The option
	     */
	    public Option getOption(String option) {
	    	return options.get(option);
	    }
	    
	    /**
	     * Converts the linkedHashmap to a list and returns the list
	     * @return The list of options
	     */
	    public ArrayList<Option> getOptionsAsList() {
	    	return new ArrayList<Option>(options.values());
	    }
	     
}
