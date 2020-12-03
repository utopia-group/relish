package relish.fta;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.Value;
import relish.dsl.Symbol;

// A state for the form q_{symbol}^{value} 
public class State {

  public final int id;

  public final Symbol symbol;

  public final int numOfExamples;
  public final Value[] values;

  // an integer used in the FTA construction algorithm 
  // it records the minimum recursion depth (for the symbol this state belongs to) 
  // of programs that reach this state 
  public int minDepth = Integer.MAX_VALUE;

  // an integer used in the ranking algorithm 
  // it records the minimum cost among routes that reach this state 
  public int minCost = Integer.MAX_VALUE;

  // a bit for mark-and-sweep in various algorithms 
  public boolean marked;

  // for experiment
  public BigInteger numOfProgs = BigInteger.ONE;
  public int times = 0;

  public Map<List<ConcreteValue>, String> map = new HashMap<>();

  protected State(Symbol symbol, Value[] values, int id) {
    this.symbol = symbol;
    this.values = values;
    this.id = id;
    this.numOfExamples = values.length;
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "q(" + symbol + "," + Arrays.asList(values) + ")";
  }

}
