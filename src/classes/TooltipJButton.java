package classes;

import javax.swing.JButton;

/**
 * A JButton class which also automatically adds a tooltip and stores the index of the record it's linked with.
 * Used for loading history from the menu.
 * @author Daniel Annan -1569423
 *
 */
public class TooltipJButton extends JButton {
	int index;
	
	/**
	 * Class Constructor
	 * @param title The title of the button to display
	 * @param description The description of the record to be added as the tooltip 
	 * @param value The index of the record
	 */
	public TooltipJButton(String title, String description, int value) {
		setText(title);
		setToolTipText(description);
		index = value;
	}
	
	/**
	 * Gets the index of the record it relates to
	 * @return The index of the record.
	 */
	public int getIndex() {
		return index;
	}
}
