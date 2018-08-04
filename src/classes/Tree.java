package classes;

import java.util.ArrayList;

import graphics.GraphicalNode;
import graphics.NodeType;
/**
 * An abstract class to be used as the base super class for Branch and Leaf.
 * Used for getting the value of the graphical node selected in the interface.
 * @author Daniel Annan -1569423
 *
 */
public abstract class Tree {
	GraphicalNode data;
	String value = "";
	String preData;
	Branch parent;
	int startIndex = 0;
	NodeType nodeType = null;
	
	/**
	 * Returns the nodeType for this Tree
	 * @return the nodeType
	 */
	protected NodeType getNodeType() {
		return nodeType;
	}

	/**
	 * Adds its value to the value of its tree and updates its parent as necessary
	 * @param parentIndex The index in the main tree of this instance's parent
	 * @return Any output value it may return
	 */
	public abstract String buildValue(int parentIndex, boolean getOutput);

	/**
	 * Adds itself to the main tree
	 * @param parent The parent of this instance
	 */
	public abstract void build(Branch parent);
	
	/**
	 * Checks if this tree contains the tree given
	 * @param tree The tree that is being checked
	 * @return Whether it contains the tree
	 */
	public abstract boolean contains(Tree tree);
	
	/**
	 * Adds text that should be put before the value
	 * @param data The text to be added
	 */
	protected void setPreData(String data) {
		preData = data;
	}

	
	/**
	 * Sets the parent for this tree
	 * @param parent The parent of this tree
	 */
	protected void setParent(Branch parent) {
		this.parent = parent;
	}

	/**
	 * Adds the value given to this objects index updates the parent accordingly.
	 * @param val The value to be added
	 */
	protected void addToIndex(int val) {
		startIndex += val;
		if (parent != null) {
			parent.addToIndex(val);
		}
	}

	/**
	 * Returns the predata for this tree
	 * @return The predata
	 */
	public String getPreData() {
		return preData;
	}

	@Override
	public boolean equals(Object o) {
		if (data == null)
			return value.equals(((Tree) o).getValue());
		else
			return data == ((Tree) o).getData();
	}
	
	/**
	 * Returns the graphical node of this tree
	 * @return The node to be returned
	 */
	public GraphicalNode getData() {
		return data;
	}

	/**
	 * Returns the value for this tree
	 * @return The value of this tree
	 */
	protected String getValue() {
		return value;
	}
	
	/**
	 * If this is a branch, it returns a list of the children
	 * otherwise it returns null.
	 * @return The list of children
	 */
	public abstract ArrayList<Tree> getChildren();

	/**
	 * If this is a branch, it adds the child to the list
	 * @param child The child to be added
	 */
	protected abstract void addChild(Tree child);

	
}
