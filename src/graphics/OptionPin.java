package graphics;

import classes.Option;
import javafx.scene.control.Tooltip;

/**
 * An extension of the Pin class for holding extra information about
 * options.
 * @author Daniel Annan - 1569423
 *
 */
public class OptionPin extends Pin {
	Option option;
	int argNum;
	/**
	 * Class Constructor
	 * @param parent The graphical node this object is part of
	 * @param option The option related to this object
	 * @param description The description to be displayed as the tooltip
	 * @param argNum The argument of the option this object is for
	 * @param type The type of this object
	 */
	public OptionPin(GraphicalNode holder, Option option,String description, int argNum, PinType type) {
		super(holder, type);
		this.option = option;
		this.argNum = argNum;
		Tooltip t = new Tooltip(option.getOption() + ": " + description);
		Tooltip.install(getVisual(), t);
	}

	
	/**
	 * Returns the option for this object
	 * @return The option being returned
	 */
	public Option getOption() {
		return option;
	}
	
	/**
	 * Returns the argument number for this object
	 * @return The argument number
	 */
	public int getArgNum() {
		return argNum;
	}
	

}
