package relish.learn;

import java.util.Arrays;
import java.util.Map;

import relish.abs.Abstractions.Value;
import relish.dsl.VariableTerminalSymbol;

public class InputOutput {

  public final Map<VariableTerminalSymbol, Value[]> in;
  public final Value[] out;
  public final int cost;

  public InputOutput(Map<VariableTerminalSymbol, Value[]> in, Value[] out, int cost) {
    this.in = in;
    this.out = out;
    this.cost = cost;
  }

  @Override
  public int hashCode() {
    throw new RuntimeException("Unreachable");
  }

  @Override
  public boolean equals(Object o) {
    throw new RuntimeException("Unreachable");
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("cost: ").append(cost).append(", ");
    builder.append("{");
    for (VariableTerminalSymbol var : in.keySet()) {
      builder.append(var).append("=");
      builder.append(Arrays.toString(in.get(var))).append(", ");
    }
    builder.delete(builder.length() - 2, builder.length());
    builder.append("} --> ");
    builder.append(Arrays.toString(out));
    return builder.toString();
  }

}
