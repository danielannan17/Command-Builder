package classes;

import javafx.geometry.Pos;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * Class for creating a menu option with a tooltip
 * @author Daniel Annan - 1569423
 *
 */
public class TooltipMenuItem extends CustomMenuItem {
	/**
	 * Class constructor
	 * @param name The name of the option to be displayed
	 * @param tooltip The description to be used as the tooltip
	 * @param sp An empty stackpane to hold the JavaFX components
	 */
	public TooltipMenuItem(String name, String tooltip, StackPane sp) {
		super(sp);
		Text t = new Text(name);
		sp.setAlignment(Pos.BASELINE_LEFT);
		t.setMouseTransparent(true);
		Tooltip tt = new Tooltip(tooltip);
		Rectangle r = new Rectangle();
		r.setMouseTransparent(true);
		r.setFill(null);
		sp.setPrefWidth(100);
		Tooltip.install(sp, tt);
		sp.getChildren().add(r);
		sp.getChildren().add(t);
	}
	
	
}
