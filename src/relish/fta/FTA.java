package relish.fta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import relish.abs.Abstractions.Value;
import relish.dsl.ConstantTerminalSymbol;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.TerminalSymbol;
import relish.dsl.VariableTerminalSymbol;
import relish.util.ListMultiMap;
import relish.util.MultiMap;
import relish.util.SetMultiMap;

// An FTA A = (Q, F, Q_f, \Delta) 
public class FTA {

  // Q: symbol -> a set of states 
  public MultiMap<Symbol, State> symbolToStates;
  // F: a list of operators (i.e., productions in the grammar) 
  public Collection<Production> alphabet;
  // Q_f: a set of final states 
  public Collection<State> finalStates;
  public NonTerminalSymbol startSymbol;
  // \Delta: a set of transitions
  public Collection<Transition> transitions;

  public int numOfExamples = -1;

  protected Map<Pair<Symbol, List<Value>>, State> factory = new HashMap<>(5000);
  protected int maxStateId = -1;

  public State mkState(Symbol symbol, Value[] values) {
    setNumOfExamples(values);
    List<Value> list = Arrays.asList(values);
    Pair<Symbol, List<Value>> key = new Pair<>(symbol, list);
    State ret = factory.get(key);
    if (ret == null) {
      maxStateId ++;
      ret = new State(symbol, values, maxStateId);
      factory.put(key, ret);
    }
    return ret;
  }

  public State mkFreshState(Symbol symbol, Value[] values) {
    setNumOfExamples(values);
    maxStateId ++;
    State ret = new State(symbol, values, maxStateId);
    return ret;
  }

  private void setNumOfExamples(final Value[] values) {
    if (numOfExamples == -1) {
      numOfExamples = values.length;
    }
    assert numOfExamples == values.length : numOfExamples + " " + Arrays.toString(values);
  }

  public Transition mkTransition(Production production, State[] argumentStates, State returnState) {
    return new Transition(production, argumentStates, returnState);
  }

  public boolean isEmpty() {
    return transitions.isEmpty();
  }

  public int numOfStates() {
    return symbolToStates.size();
  }

  public int numOfTransitions() {
    return transitions.size();
  }

  public void minimize() {
    removeForwardsUnreachable();
    removeBackwardsUnreachable();
  }

  public MultiMap<State, Transition> computeStateToOutTransitions() {
    MultiMap<State, Transition> ret = new ListMultiMap<>();
    for (Transition transition : transitions) {
      for (State argumentState : transition.argumentStates) {
        ret.put(argumentState, transition);
      }
    }
    for (Symbol symbol : symbolToStates.keySet()) {
      for (State state : symbolToStates.get(symbol)) {
        if (!ret.containsKey(state)) {
          Collection<Transition> transitions = new ArrayList<>();
          ret.putAll(state, transitions);
        }
      }
    }
    return ret;
  }

  protected MultiMap<State, Transition> computeStateToInTransitions() {
    MultiMap<State, Transition> ret = new ListMultiMap<>();
    for (Transition transition : transitions) {
      ret.put(transition.returnState, transition);
    }
    return ret;
  }

  public void depthPrune() {

    // reset markers   
    {
      for (Transition transition : transitions) {
        transition.n = transition.argumentStates.length;
      }

      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof ConstantTerminalSymbol || symbol instanceof VariableTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minDepth = 0;
          }
        } else {
          for (State state : symbolToStates.get(symbol)) {
            state.minDepth = Integer.MAX_VALUE;
          }
        }
      }
    }

    MultiMap<State, Transition> stateToOutTransitions = computeStateToOutTransitions();

    LinkedList<State> wl = new LinkedList<>();
    MultiMap<Symbol, State> symbolToReachableStates = new SetMultiMap<>();
    Collection<Transition> reachableTransitions = new LinkedList<>();

    // initialization 
    {
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof ConstantTerminalSymbol || symbol instanceof VariableTerminalSymbol) {
          // constant and variable terminal symbols are initially reachable states 
          wl.addAll(symbolToStates.get(symbol));
          for (State state : symbolToStates.get(symbol)) {
            symbolToReachableStates.put(symbol, state);
            // the marked bit is used to denote if a state is reachable (i.e., it has been visited) 
            state.marked = true;
          }
        } else {
          // all other states are not reachable initially 
          for (State state : symbolToStates.get(symbol)) {
            state.marked = false;
          }
        }
      }
    }

    // a work-list algorithm 
    while (!wl.isEmpty()) {
      State state = wl.removeFirst();

      Collection<Transition> outTransitions = stateToOutTransitions.get(state);
      for (Transition outTransition : outTransitions) {
        int n = outTransition.n;
        n --;
        outTransition.n = n;
        State outState = outTransition.returnState;
        if (n == 0) {
          Production production = outTransition.production;
          // check the depth 
          int depth = 0;
          if (production.isRecursive) {
            State[] argumentStates = outTransition.argumentStates;
            for (State argumentState : argumentStates) {
              if (argumentState.symbol.equals(production.returnSymbol)) {
                depth += argumentState.minDepth;
              }
            }
            depth += 1;
          }
          // NOTE: if the depth exceeds the bound 
          // then neither the out-state nor the out-transition is reachable 
          if (depth > production.returnSymbol.maxDepth) continue;
          outState.minDepth = Math.min(depth, outState.minDepth);

          // add the out-transition
          reachableTransitions.add(outTransition);

          // populate work-list 
          if (!outState.marked) {
            wl.addLast(outState);
            outState.marked = true;
            symbolToReachableStates.put(outState.symbol, outState);
          }
        }
      }
    }

    // re-construct Q and Q_f 
    {
      this.symbolToStates = symbolToReachableStates;
      Collection<State> reachableFinalStates = symbolToReachableStates.get(startSymbol);
      this.finalStates.retainAll(reachableFinalStates);
    }

    // re-construct \Delta 
    {
      this.transitions = reachableTransitions;
    }

  }

  protected void removeForwardsUnreachable() {

    // reset markers 
    for (Transition transition : transitions) {
      transition.n = transition.argumentStates.length;
    }

    MultiMap<State, Transition> stateToOutTransitions = computeStateToOutTransitions();

    LinkedList<State> wl = new LinkedList<>();
    MultiMap<Symbol, State> symbolToReachableStates = new SetMultiMap<>();
    Collection<Transition> reachableTransitions = new LinkedList<>();

    // initialization 
    {
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof ConstantTerminalSymbol || symbol instanceof VariableTerminalSymbol) {
          // constant and variable terminal symbols are initially reachable states 
          wl.addAll(symbolToStates.get(symbol));
          for (State state : symbolToStates.get(symbol)) {
            symbolToReachableStates.put(symbol, state);
            // the marked bit is used to denote if a state has been visited (in the work-list) 
            // or in another word, if a state is reachable 
            state.marked = true;
          }
        } else {
          // all other states are not reachable initially 
          for (State state : symbolToStates.get(symbol)) {
            state.marked = false;
          }
        }
      }
    }

    // a work-list algorithm 
    while (!wl.isEmpty()) {

      State state = wl.removeFirst();

      Collection<Transition> outTransitions = stateToOutTransitions.get(state);

      for (Transition outTransition : outTransitions) {

        int n = outTransition.n;
        n --;
        outTransition.n = n;

        State outState = outTransition.returnState;

        if (n == 0) {
          reachableTransitions.add(outTransition);
          if (!outState.marked) {
            wl.addLast(outState);
            outState.marked = true;
            symbolToReachableStates.put(outState.symbol, outState);
          }
        }
      }

    }

    // re-construct Q and Q_f 
    {
      this.symbolToStates = symbolToReachableStates;
      Collection<State> states = symbolToReachableStates.get(startSymbol);
      this.finalStates.retainAll(states);
    }

    // re-construct \Delta 
    {
      this.transitions = reachableTransitions;
    }

  }

  public void removeBackwardsUnreachable() {

    MultiMap<State, Transition> stateToInTransitions = computeStateToInTransitions();

    LinkedList<State> wl = new LinkedList<>();
    MultiMap<Symbol, State> symbolToReachableStates = new SetMultiMap<>();
    Collection<Transition> reachableTransitions = new LinkedList<>();

    // initialization 
    {
      // the marked bit denotes if a state is reachable 
      // mark all bits as false first 
      for (Symbol symbol : symbolToStates.keySet()) {
        for (State state : symbolToStates.get(symbol)) {
          state.marked = false;
        }
      }
      // initially all final states are reachable states and added into the work-list 
      for (State finalState : finalStates) {
        wl.add(finalState);
        symbolToReachableStates.put(finalState.symbol, finalState);
        finalState.marked = true;
      }
    }

    // a work-list algorithm to compute the reachable states 
    while (!wl.isEmpty()) {

      State state = wl.removeFirst();

      Collection<Transition> inTransitions = stateToInTransitions.get(state);

      if (inTransitions == null) {
        assert (state.symbol instanceof TerminalSymbol) : state.symbol;
        continue;
      }

      for (Transition inTransition : inTransitions) {

        reachableTransitions.add(inTransition);

        State[] argumentStates = inTransition.argumentStates;

        for (State argumentState : argumentStates) {
          if (!argumentState.marked) {
            wl.addLast(argumentState);
            argumentState.marked = true;
            symbolToReachableStates.put(argumentState.symbol, argumentState);
          }
        }

      }

    }

    // re-construct Q and Q_f 
    {
      this.symbolToStates = symbolToReachableStates;
      // Q_f is the same as before, so no need to update Q_f 
    }

    // re-construct \Delta 
    {
      this.transitions = reachableTransitions;
    }

  }

  public String dumpStates() {
    StringBuilder sb = new StringBuilder();
    for (Symbol symbol : symbolToStates.keySet()) {
      Collection<State> states = symbolToStates.get(symbol);
      for (State state : states) {
        sb.append(state + "\n");
      }
    }
    return sb.toString();
  }

  public String dumpTransitions() {
    StringBuilder sb = new StringBuilder();
    for (Transition transition : transitions) {
      sb.append(transition + "\n");
    }
    return sb.toString();
  }

  public BigInteger numOfProgs() {

    // reset markers 
    for (Transition transition : transitions) {
      transition.n = transition.argumentStates.length;
    }

    MultiMap<State, Transition> stateToOutTransitions = computeStateToOutTransitions();

    LinkedList<State> wl = new LinkedList<>();

    // initialization 
    {
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof ConstantTerminalSymbol || symbol instanceof VariableTerminalSymbol) {
          wl.addAll(symbolToStates.get(symbol));
          for (State state : symbolToStates.get(symbol)) {
            state.marked = true;
            state.numOfProgs = BigInteger.ONE;
            state.times = 0;
          }
        } else {
          for (State state : symbolToStates.get(symbol)) {
            state.marked = false;
            state.numOfProgs = BigInteger.ZERO;
            state.times = 0;
          }
        }
      }
    }

    // a work-list algorithm 
    while (!wl.isEmpty()) {

      State state = wl.removeFirst();

      Collection<Transition> outTransitions = stateToOutTransitions.get(state);

      for (Transition outTransition : outTransitions) {

        int n = outTransition.n;
        n --;
        outTransition.n = n;

        State outState = outTransition.returnState;

        if (n == 0) {

          BigInteger prod = BigInteger.ONE;
          for (State inState : outTransition.argumentStates) {
            prod = prod.multiply(inState.numOfProgs);
          }
          outState.numOfProgs = outState.numOfProgs.add(prod);

          if (!outState.marked) {
            wl.addLast(outState);
            outState.marked = true;
          }
        }
      }

    }

    BigInteger ret = BigInteger.ZERO;
    for (State finalState : finalStates) {
      ret = ret.add(finalState.numOfProgs);
    }

    return ret;
  }

  public int size() {
    int ret = 0;
    for (Transition t : transitions) {
      ret += t.argumentStates.length;
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("States:\n");
    builder.append(dumpStates());
    builder.append("Transitions:\n");
    builder.append(dumpTransitions());
    builder.append("Final States:\n");
    builder.append(finalStates);
    return builder.toString();
  }

}
