package relish.fta;

import relish.dsl.Production;

// A transition of the form f(q_1, .., q_n) -> q_0 
public class Transition {

  // f
  public final Production production;
  // q_1, .., q_n 
  public final State[] argumentStates;
  // q_0 
  // NOTE: return state is final since in the FTA reconstruction this might be adjusted (due to the refinement) 
  public State returnState;

  // this integer is used in various algorithms 
  public int n;

  // a bit for mark-and-sweep 
  public boolean marked;

  // the cost of this transition used in the ranking algorithm 
  public int cost;

  public Transition(Production production, State[] argumentStates, State returnState) {
    this.production = production;
    this.argumentStates = argumentStates;
    this.returnState = returnState;
    this.cost = production.cost;
  }

  @Override
  public boolean equals(Object o) {
    assert false;
    if (o == null) return false;
    if (o == this) return true;
    if (!(o instanceof Transition)) return false;
    Transition other = (Transition) o;
    if (!other.production.equals(this.production)) return false;
    if (!other.returnState.equals(this.returnState)) return false;
    if (other.argumentStates.length != this.argumentStates.length) return false;
    for (int i = 0; i < this.argumentStates.length; i ++) {
      if (!other.argumentStates[i].equals(this.argumentStates[i])) return false;
    }
    return true;
  }

  private Integer hash = null;

  @Override
  public int hashCode() {
    assert false;
    if (hash != null) return hash.intValue();
    int ret = production.hashCode() << 10;
    ret = ret + ((returnState.hashCode() << 10) % 1000);
    for (int i = 0; i < argumentStates.length; i ++) {
      ret = ret + argumentStates[i].hashCode();
    }
    hash = ret;
    return ret;
  }

  @Override
  public String toString() {
    String ret = production.operatorName + "(";
    for (int i = 0; i < argumentStates.length; i ++) {
      if (i > 0) ret += ",";
      ret += argumentStates[i];
    }
    ret += ") -> " + returnState;
    return ret;
  }

}