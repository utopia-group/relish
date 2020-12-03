package relish.dsl;

import java.util.ArrayList;
import java.util.List;

import org.javatuples.Pair;

public class FunctionSymbol {

  public final String functionName;
  public final String returnType;
  // parameter list, where each parameter is a pair of (type, name)
  public final List<Pair<String, String>> parameters;

  public FunctionSymbol(String functionName, String returnType) {
    this.functionName = functionName;
    this.returnType = returnType;
    parameters = new ArrayList<>();
  }

  public void addParameter(String type, String name) {
    parameters.add(new Pair<>(type, name));
  }

  @Override
  public int hashCode() {
    return functionName.hashCode();
  }

  // equals is purely based on function name
  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof FunctionSymbol)) return false;
    return functionName.equals(((FunctionSymbol) o).functionName);
  }

  @Override
  public String toString() {
    return functionName;
  }

}
