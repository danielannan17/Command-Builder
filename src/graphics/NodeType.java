package graphics;

/**
 * An enumeration of the different components used for the Graphical interface
 * @author Daniel Annan - 1569423
 *
 */
public enum NodeType {
	CommandNode(0), CustomNode(1),OperatorNode(2), ForNode(3), Pin(4);
	public final int val;
	/**
	 * Constructor
	 * @param v
	 */
	private NodeType(int v) {
		this.val = v;
	}
	
	/**
	 * Gets the integer related to the type
	 * @return the integer
	 */
	public int getType() {
		return val;
	}

}
