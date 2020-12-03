package relish.fta;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.Value;
import relish.dsl.ConstantTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.TerminalSymbol;
import relish.dsl.VariableTerminalSymbol;

// The program tree (derivation tree) corresponding to an FTA. 
public class ProgramTree {

  public final Node root;
  public final Collection<Node> nodes;
  public final int numOfExamples;
  public int counterexample;

  public ProgramTree(Node root) {
    this.root = root;
    this.nodes = new HashSet<>();
    this.numOfExamples = root.state.numOfExamples;
    populateNodes();
  }

  private void populateNodes() {
    LinkedList<Node> wl = new LinkedList<>();
    wl.addLast(root);
    while (!wl.isEmpty()) {
      Node node = wl.removeFirst();
      nodes.add(node);
      Node[] children = node.children;
      if (children == null) continue;
      for (Node child : children) {
        if (!nodes.contains(child)) {
          wl.addLast(child);
        }
      }
    }
  }

  // translate a program tree into the program text in the given DSL 
  public String translateToProgramText() {
    return translateToProgramText(root);
  }

  private String translateToProgramText(Node root) {

    // base case: if root is a leaf node 
    if (root.children == null) {
      State state = root.state;
      Symbol symbol = state.symbol;
      Value[] values = state.values;
      assert (symbol instanceof TerminalSymbol) : symbol;
      if (symbol instanceof ConstantTerminalSymbol) {
        assert (values[0] instanceof ConcreteValue) : values[0];
        return ((ConcreteValue) values[0]).translateToProgramText();
      } else if (symbol instanceof VariableTerminalSymbol) {
        return ((VariableTerminalSymbol) symbol).translateToProgramText();
      } else {
        throw new RuntimeException();
      }
    }

    // recursive case: if root is an internal node 
    StringBuilder sb = new StringBuilder();
    sb.append(root.production.translateToProgramText() + "(");
    Node[] children = root.children;
    for (int i = 0; i < children.length; i ++) {
      if (i > 0) sb.append(", ");
      sb.append(translateToProgramText(children[i]));
    }
    sb.append(")");

    return sb.toString();

  }

  public ConcreteValue[] eval() {
    return eval(root);
  }

  private ConcreteValue[] eval(Node root) {

    // base case: if root has been visited, or root is a leaf node in the program tree 
    if (root.concreteValues != null) return root.concreteValues;

    // recursive case: otherwise root must be an internal node that has not been visited 
    Node[] children = root.children;
    ConcreteValue[][] args = new ConcreteValue[numOfExamples][children.length];
    for (int i = 0; i < children.length; i ++) {
      Node child = children[i];
      ConcreteValue[] values = eval(child);
      assert (values.length == numOfExamples);
      for (int j = 0; j < numOfExamples; j ++) {
        args[j][i] = values[j];
      }
    }
    Production production = root.production;
    ConcreteValue[] ret = new ConcreteValue[numOfExamples];
    for (int i = 0; i < numOfExamples; i ++) {
      ret[i] = (ConcreteValue) production.exec(args[i]);
    }

    // record the concrete value for this node 
    root.concreteValues = ret;

    return ret;

  }

  public int size() {
    return size(root);
  }

  private int size(Node root) {
    if (root.children == null) {
      return 1;
    } else {
      int ret = 1;
      for (Node child : root.children) {
        ret += size(child);
      }
      return ret;
    }
  }

  public String toDot() {
    StringBuilder sb = new StringBuilder("digraph ProgramTree {\n");
    for (Node node : nodes) {
      if (node == root) {
        sb.append("  ");
        sb.append("\"" + node.state.id + "\"");
        sb.append(" [shape=doublecircle,label=\"" + "{" + node.state.id + "} -> {" + node.concreteValues + "}" + "\"];\n");
      } else {
        sb.append("  ");
        sb.append("\"" + node.state.id + "\"");
        sb.append(" [shape=circle,label=\"" + "{" + node.state.id + "} -> {" + node.concreteValues + "}" + "\"];\n");
      }
    }
    for (Node node : nodes) {
      Node[] children = node.children;
      if (children == null) continue;
      for (Node child : children) {
        sb.append("  ");
        sb.append("\"" + node.state.id + "\"");
        sb.append(" -> ");
        sb.append("\"" + child.state.id + "\"");
        sb.append(" [label=\"");
        sb.append(node.production.operatorName);
        sb.append("\"];\n");
      }
    }
    sb.append("}\n");
    return sb.toString();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (Node node : nodes) {
      Node[] children = node.children;
      if (children == null) continue;
      sb.append(node.production.operatorName + "(");
      for (int i = 0; i < children.length; i ++) {
        if (i > 0) sb.append(",");
        sb.append(children[i] + " (" + Arrays.toString(children[i].concreteValues) + ")");
      }
      sb.append(")-> ");
      sb.append(node.state + "(" + Arrays.toString(node.concreteValues) + ")" + "\n");
    }
    return sb.toString();
  }

  public static class Node {

    public final State state;
    // if the node is a leaf node, we have production is NULL and children is NULL
    public final Production production;
    public final Node[] children;

    public ConcreteValue[] concreteValues;

    public Value strengthenedValue;

    // an integer used for efficient implementation 
    public int n;

    public Node(State state, Node[] children, Production production) {
      this.state = state;
      this.children = children;
      this.production = production;
    }

    @Override
    public String toString() {
      return state + "";
    }

  }

}
