package relish.dsl;

import relish.abs.Abstractions.Value;

// A production in the grammar: s_0 -> f(s_1, .., s_n)
// Each production has an operator with a unique name
public abstract class Production {

  public final int id;

  // s_0
  public final NonTerminalSymbol returnSymbol;
  // f
  public final String operatorName;
  // s_1, .., s_n
  public final Symbol[] argumentSymbols;
  // n
  public final int rank;
  // bit for whether LHS appears in RHS
  public final boolean isRecursive;

  // the cost of this production that is used for computing the cost of a transition
  public final int cost;

  public Production() {
    this.id = -1;
    this.returnSymbol = null;
    this.operatorName = null;
    this.argumentSymbols = null;
    this.rank = -1;
    this.isRecursive = false;
    this.cost = -1;
  }

  public Production(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
    this.id = id;
    this.returnSymbol = returnSymbol;
    this.operatorName = operatorName;
    this.argumentSymbols = argumentSymbols;
    this.rank = argumentSymbols.length;
    boolean isRecursive = false;
    for (Symbol argumentSymbol : argumentSymbols) {
      if (returnSymbol.equals(argumentSymbol)) {
        isRecursive = true;
        break;
      }
    }
    this.isRecursive = isRecursive;
    this.cost = cost;
  }

  // evaluation function
  public abstract Value exec(Value... args);

  // return the operator name used in the DSL
  public abstract String translateToProgramText();

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public String toString() {
    String ret = returnSymbol.toString() + " := " + operatorName + "(";
    for (int i = 0; i < argumentSymbols.length; i ++) {
      if (i > 0) ret += ",";
      ret += argumentSymbols[i].toString();
    }
    ret += ")";
    return ret;
  }

}
