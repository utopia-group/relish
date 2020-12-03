package relish.dsl;

import java.util.Collection;
import java.util.Map;

import relish.abs.Abstractions.Value;

public class ConstantTerminalSymbol extends TerminalSymbol {

  // the concrete values this constant terminal symbol can take
  public final Collection<Value> values;

  // a map from each concrete value to its cost used in ranking algorithm
  public final Map<Value, Integer> costs;

  public ConstantTerminalSymbol(int id, String symbolName, Collection<Value> values, Map<Value, Integer> costs) {
    super(id, symbolName);
    this.values = values;
    this.costs = costs;
  }

}
