package classes;

import java.util.ArrayList;

import graphics.CustomNode;
import graphics.NodeType;
import windows.Main;

/**
 * An extention of the tree class which only stores a value and cannot have
 * children.
 * 
 * @author Daniel Annan - 1569423
 *
 */
public class Leaf extends Tree {

	/**
	 * Class constructor for a leaf without a graphical node, such as for options
	 * 
	 * @param val
	 *            The value of the leaf
	 */
	public Leaf(String val) {
		value = val;
	}

	/**
	 * Class Constructor for leaf built from a custom node.
	 * 
	 * @param customNode
	 *            The custom node of the leaf
	 * @param val
	 *            The value of the leaf.
	 */
	public Leaf(CustomNode customNode, String val) {
		value = val;
		data = customNode;
	}

	@Override
	public String buildValue(int parentIndex, boolean getOutput) {
		if (preData != null) {
			value = preData + " " + value;
		}
		value += " ";

		if (parent != null) {
			if (parent.getNodeType() == NodeType.CommandNode) {
				Main.graphics.insertIntoCurrentTreeValue(value, parentIndex + parent.getValue().length());
		
			} else if (parent.getNodeType() == NodeType.ForNode && getOutput) {
				Main.graphics.insertIntoCurrentTreeValue(value, parentIndex);
			} else {
				Main.graphics.insertIntoCurrentTreeValue(value, parentIndex);
				parent.addToIndex(value.length());
			}
		} else {
			Main.graphics.insertIntoCurrentTreeValue(value, parentIndex);

		}
		return value;
	}

	@Override
	public void build(Branch parent) {
	}

	@Override
	public boolean contains(Tree o) {
		return this.equals(o);
	}

	@Override
	public ArrayList<Tree> getChildren() {
		return null;
	}

	@Override
	protected void addChild(Tree child) {
	}
}
