package relish.eval;

import java.util.List;

import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.dsl.Production;

public abstract class DSLAstNode {

  @Override
  public int hashCode() {
    throw new RuntimeException("Unreachable");
  }

  @Override
  public boolean equals(Object o) {
    throw new RuntimeException("Unreachable");
  }

  public static class FunctionNode extends DSLAstNode {

    public final String name;
    public final Production func;
    public final List<DSLAstNode> arguments;

    public FunctionNode(String name, Production func, List<DSLAstNode> arguments) {
      this.name = name;
      this.func = func;
      this.arguments = arguments;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(name).append("(");
      for (DSLAstNode argument : arguments) {
        builder.append(argument).append(", ");
      }
      builder.delete(builder.length() - 2, builder.length());
      builder.append(")");
      return builder.toString();
    }

  }

  public static class IntLiteralNode extends DSLAstNode {

    public final IntConstant value;

    public IntLiteralNode(IntConstant value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value.translateToProgramText();
    }

  }

  public static class StringLiteralNode extends DSLAstNode {

    public final StringConstant value;

    public StringLiteralNode(StringConstant value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value.translateToProgramText();
    }

  }

  public static class CharLiteralNode extends DSLAstNode {

    public final CharConstant value;

    public CharLiteralNode(CharConstant value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return value.translateToProgramText();
    }

  }

  public static class VariableNode extends DSLAstNode {

    public final String name;

    public VariableNode(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }

  }

}
