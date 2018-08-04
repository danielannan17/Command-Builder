package classes;

import java.util.ArrayList;

import graphics.CommandNode;
import graphics.Connector;
import graphics.ForNode;
import graphics.GraphicalNode;
import graphics.NodeType;
import graphics.OperatorNode;
import graphics.OptionPin;
import graphics.Pin;
import graphics.Pin.PinType;
import windows.Main;

/**
 * A subclass for the Tree class made for graphical nodes which can have
 * children.
 * 
 * @author Daniel Annan -1569423
 *
 */
public class Branch extends Tree {
	String delimiter = "";
	ArrayList<Tree> children;
	ArrayList<Tree> queue;
	Tree doList, var;
	String varName = null;

	/**
	 * Class Constructor
	 */
	public Branch() {
		children = new ArrayList<Tree>();
	}

	/**
	 * Class constructor for building a tree from a graphical node
	 * 
	 * @param node
	 *            The graphical node for this tree
	 * @param nodeType
	 *            the type of the graphical node
	 */
	public Branch(GraphicalNode node) {
		data = node;
		this.nodeType = node.getNodeType();
		children = new ArrayList<Tree>();
		if (nodeType == NodeType.CommandNode)
			queue = new ArrayList<Tree>();
		else if (nodeType == NodeType.ForNode) {
			queue = new ArrayList<Tree>();

		}
	}

	@Override
	protected void addChild(Tree child) {
		children.add(child);
		child.setParent(this);
	}

	@Override
	public boolean contains(Tree node) {
		if (this.equals(node)) {
			return true;
		} else {
			for (Tree child : children) {
				if (child.contains(node))
					return true;
			}
			return false;
		}
	}

	@Override
	public String buildValue(int parentIndex, boolean getOutput) {
		switch (nodeType) {
		case CommandNode:
			startIndex = parentIndex;
			CommandNode data = (CommandNode) this.data;
			int outputType = data.getOutputType();
			String output = "";
			if (preData != null) {
				value += preData + " ";
			}

			value = data.getCommandValue() + " ";
			Main.graphics.insertIntoCurrentTreeValue(value, startIndex);
			if (parent != null && getOutput) {
				parent.addToIndex(value.length());
			}
			for (int i = 0; i < children.size(); i++) {
				if (outputType < 0 && children.get(i).getPreData().equals(data.getOutputOption())) {
					String temp = children.get(i).buildValue(startIndex, true);
					output = temp;
					value += temp;
					if (parent != null && getOutput) {
						parent.addToIndex(temp.length());
					}
					if (parent != null && data.getOutput().getConnect().getOtherPin(data).getType() != PinType.Infinite
							&& parent.getNodeType() != NodeType.OperatorNode && getOutput) {
						Main.graphics.insertIntoCurrentTreeValue(temp,
								startIndex + value.length() + parent.getValue().length());
					}

				} else if (outputType > 0 && i + 1 == outputType + data.getOptions().size()) {
					String temp = children.get(i).buildValue(startIndex, true);
					output = temp;
					value += temp;
					if (parent != null && getOutput) {
						parent.addToIndex(temp.length());
					}
					if (parent != null && data.getOutput().getConnect().getOtherPin(data).getType() != PinType.Infinite
							&& parent.getNodeType() != NodeType.OperatorNode && getOutput) {
						Main.graphics.insertIntoCurrentTreeValue(temp,
								startIndex + value.length() + parent.getValue().length());
					}
				} else {
					String temp = children.get(i).buildValue(startIndex, true);

					value += temp;
					if (parent != null && getOutput) {
						parent.addToIndex(temp.length());
					} else if (parent != null) {
						Main.graphics.insertIntoCurrentTreeValue(temp, startIndex + value.length());
					}

				}
			}

			for (Tree t : queue) {
				String temp = t.buildValue(startIndex, true);

			}
			if (parent != null && parent.getNodeType() != NodeType.OperatorNode) {
				Main.graphics.insertIntoCurrentTreeValue("; " + delimiter, startIndex + value.length());
				value += "; " + delimiter;
				parent.addToIndex(2 + delimiter.length());
			}
			if (getOutput)
				return output;
			else
				return value;

		case OperatorNode:
			startIndex = parentIndex;
			OperatorNode oData = (OperatorNode) this.data;
			value = "";
			if (children.get(0) != null)
				children.get(0).buildValue(startIndex, true);
			if (oData.getSymbol() == ";") {
				value += oData.getSymbol() + " " + delimiter;
			} else {
				value += oData.getSymbol() + " ";
			}
			Main.graphics.insertIntoCurrentTreeValue(value, startIndex);
			if (parent != null && getOutput)
				parent.addToIndex(value.length());
			return value;
		case ForNode:
			String temp;
			if (varName == null) {
				for (Tree t : queue) {
					temp = t.buildValue(startIndex, false);
				}

				value = "for ";
				Main.graphics.insertIntoCurrentTreeValue(value, startIndex);
				varName = var.buildValue(startIndex + value.length(), true);

				if (varName.startsWith("\"")) {
					varName = varName.substring(1);
				}
				if (varName.endsWith("\"")) {
					varName = varName.substring(0, varName.length() - 1);
				}

				value += varName;
				temp = "in ";
				Main.graphics.insertIntoCurrentTreeValue(temp, startIndex + value.length());
				value += temp;
				for (Tree input : children) {
					if (Leaf.class.isInstance(input)) {
						temp = input.buildValue(startIndex + value.length(), true);
					} else {
						temp = input.buildValue(startIndex, true);
					}
					value += temp;
				}
				temp = delimiter + "do " + delimiter;
				Main.graphics.insertIntoCurrentTreeValue(temp, startIndex + value.length());
				value += temp;
				temp = doList.buildValue(startIndex + value.length(), false);
				value += temp;
				temp = delimiter + "done";
				value += temp;
				Main.graphics.insertIntoCurrentTreeValue(temp, startIndex + value.length());
				if (parent != null) {
					parent.addToIndex(value.length());
				}
			}
			return "${" + varName.trim() + "} ";
		}
		System.err.println("Missed Case With Command Node");
		return null;
	}

	@Override
	public void build(Branch parent) {

		if (Main.graphics.getCurrentTree().equals(data.toTree())) {
			while (Main.graphics.getCurrentTree().getData().getOutput().isBound()) {
				Main.graphics.setCurrentTree(
						Main.graphics.getCurrentTree().data.getOutput().getConnect().getOtherEnd(data).toTree());
				Main.graphics.currentTree.build(null);
				return;
			}
		}
		if (nodeType == NodeType.ForNode) {
			ForNode fNode = (ForNode) this.data;
			// Sort out Queue
			if (!fNode.getQueue().getConnectors().isEmpty()) {
				for (Connector conn : fNode.getQueue().getConnectors()) {
					if (conn != null) {
						Tree child = conn.getOtherEnd(fNode).toTree();
						child.build(this);
						if (child != null) {
							child.setParent(this);
							queue.add(child);
						}
					}
				}
			}

			// Sort out inputs
			if (fNode.getInputList().size() == 1 && !fNode.getInputList().get(0).isBound()) {
				this.addChild(new Leaf("[Input List]"));
			} else {
				for (Pin input : fNode.getInputList()) {
					Tree child = null;
					if (input.isBound()) {
						child = input.getConnect().getOtherEnd(data).toTree();
						child.build(this);
					}
					if (child != null)
						this.addChild(child);
				}
			}

			// Sort out Var Input
			if (fNode.getArguments()[0].isBound()) {
				var = fNode.getArguments()[0].getConnect().getOtherEnd(data).toTree();
				var.setParent(this);
				var.build(this);
			} else {
				var = new Leaf("[Variable]");
				var.setParent(this);
			}

			// Sort out doList
			if (fNode.getDoStart().isBound()) {
				doList = fNode.getDoStart().getConnect().getOtherEnd(data).toTree();
				doList.setParent(this);
				doList.build(this);
			} else {
				doList = new Leaf("[Loop]");
				doList.setParent(this);
			}
			return;
		}

		if (nodeType == NodeType.CommandNode) {
			CommandNode cData = (CommandNode) data;
			ArrayList<OptionPin> options = cData.getOptions();
			for (OptionPin option : options) {
				Tree child = null;
				if (option.isBound()) {
					child = option.getConnect().getOtherEnd(data).toTree();
					if (option.getArgNum() == 1) {
						child.setPreData(option.getOption().getOption());
					}
					child.build(this);
				} else if (option.getOption().getNumOfArguments() == 0) {
					child = new Leaf(option.getOption().getOption());
				} else {
					child = new Leaf("[" + option.getOption().getArgument(option.getArgNum() - 1) + "]");
					if (option.getArgNum() == 1) {
						child.setPreData(option.getOption().getOption());
					}
				}
				if (child != null)
					this.addChild(child);
			}

			if (!cData.getQueue().getConnectors().isEmpty()) {
				for (Connector conn : cData.getQueue().getConnectors()) {
					Tree child = conn.getOtherEnd(cData).toTree();
					child.build(this);
					child.setParent(this);
					queue.add(child);
				}
			}

		}

		Pin[] arguments = data.getArguments();
		if (arguments != null && arguments.length > 0) {
			for (int i = 0; i < arguments.length; i++) {
				Tree child;
				if (arguments[i].isBound()) {
					child = arguments[i].getConnect().getOtherEnd(data).toTree();
				} else {
					child = new Leaf("[" + data.getArgumentName(i) + "]");
				}
				child.build(this);
				this.addChild(child);

			}

		}
	}

	@Override
	public ArrayList<Tree> getChildren() {
		return children;
	}

}
