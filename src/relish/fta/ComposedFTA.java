package relish.fta;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import relish.abs.Abstractions.Value;
import relish.dsl.FunctionOccurrence;
import relish.dsl.Symbol;
import relish.dsl.VariableTerminalSymbol;

// Composed FTA = (A-bar, a_0, \Delta_\epsilon)
public class ComposedFTA {

  // A-bar: function occurrence -> component FTA
  public final Map<FunctionOccurrence, FTA> occurrenceToFTAs;
  // a_0: final function occurrence
  public FunctionOccurrence finalOccurrence;
  // \Delta_\epsilon: a set of inter-component epsilon transitions
  // each transition is of the form: (fromOccur, fromState, toOccur, toState)
  public final Set<EpsilonTransition> epsilonTransitions;

  // look-up table for final states to their corresponding epsilon transitions
  private final Map<State, EpsilonTransition> finalStateToEpsilonTransitions;

  // occurrence adjacency/dependency map
  // for example, term h_i(f_j, g_k) should have a record h_i -> {(1 -> f_j), (2, g_k)} in the map
  private final Map<FunctionOccurrence, Map<Integer, FunctionOccurrence>> dependencyMap;

  public ComposedFTA() {
    occurrenceToFTAs = new HashMap<>();
    epsilonTransitions = new HashSet<>();
    finalStateToEpsilonTransitions = new HashMap<>();
    dependencyMap = new HashMap<>();
  }

  public void addFTA(FunctionOccurrence occur, FTA fta) {
    assert (!occurrenceToFTAs.containsKey(occur));
    occurrenceToFTAs.put(occur, fta);
  }

  public void mkEpsilonTransition(FunctionOccurrence fromOccur, State fromState, FunctionOccurrence toOccur, State toState) {
    EpsilonTransition epsilonTran = new EpsilonTransition(fromOccur, fromState, toOccur, toState);
    epsilonTransitions.add(epsilonTran);
    assert !finalStateToEpsilonTransitions.containsKey(fromState) : fromState;
    finalStateToEpsilonTransitions.put(fromState, epsilonTran);
  }

  public void mkDependency(FunctionOccurrence from, FunctionOccurrence to, int argIndex) {
    if (dependencyMap.containsKey(to)) {
      Map<Integer, FunctionOccurrence> map = dependencyMap.get(to);
      assert !map.containsKey(argIndex) : argIndex;
      map.put(argIndex, from);
    } else {
      Map<Integer, FunctionOccurrence> map = new HashMap<>();
      map.put(argIndex, from);
      dependencyMap.put(to, map);
    }
  }

  public FTA getFTA(FunctionOccurrence occurrence) {
    assert occurrenceToFTAs.containsKey(occurrence) : occurrence;
    return occurrenceToFTAs.get(occurrence);
  }

  public int numOfFTAs() {
    return occurrenceToFTAs.size();
  }

  public int numOfEpsilonTransitions() {
    return epsilonTransitions.size();
  }

  public boolean isEmpty() {
    return numOfStates() == 0;
  }

  public int numOfStates() {
    int sum = 0;
    for (FunctionOccurrence occurrence : occurrenceToFTAs.keySet()) {
      FTA fta = occurrenceToFTAs.get(occurrence);
      sum += fta.numOfStates();
    }
    return sum;
  }

  public int numOfTransitions() {
    int sum = 0;
    for (FunctionOccurrence occurrence : occurrenceToFTAs.keySet()) {
      FTA fta = occurrenceToFTAs.get(occurrence);
      sum += fta.numOfTransitions();
    }
    sum += numOfEpsilonTransitions();
    return sum;
  }

  // TODO: should do topological sort if the dependency graph is a DAG
  // current just assume the dependency graph is a tree
  // which is correct even if the dependency is indeed a DAG, but not optimal
  public void pruneBackwardsUnreachable() {
    Set<EpsilonTransition> epsilonTransToRemove = new HashSet<>();
    Queue<FunctionOccurrence> wl = new LinkedList<>();
    wl.add(finalOccurrence);
    while (!wl.isEmpty()) {
      FunctionOccurrence toOccurrence = wl.poll();
      FTA toFTA = occurrenceToFTAs.get(toOccurrence);
      // prune toFTA
      toFTA.removeBackwardsUnreachable();
      // if there is no fromOccurrences, there is no function arguments
      if (!dependencyMap.containsKey(toOccurrence)) continue;
      // work on all dependent fromFTAs
      Map<Integer, FunctionOccurrence> fromOccurrences = dependencyMap.get(toOccurrence);
      for (int argIndex : fromOccurrences.keySet()) {
        FunctionOccurrence fromOccurrence = fromOccurrences.get(argIndex);
        FTA fromFTA = occurrenceToFTAs.get(fromOccurrence);
        // compute all possible values of x_argIndex for toFTA
        Set<Value[]> valuesSet = new HashSet<>();
        for (Symbol symbol : toFTA.symbolToStates.keySet()) {
          if (symbol instanceof VariableTerminalSymbol && symbol.symbolName.equals("x" + argIndex)) {
            for (State state : toFTA.symbolToStates.get(symbol)) {
              valuesSet.add(state.values);
            }
          }
        }
        // remove final states of fromFTA that are not in valuesSet
        for (Iterator<State> iter = fromFTA.finalStates.iterator(); iter.hasNext();) {
          State state = iter.next();
          if (!valuesSet.contains(state.values)) {
            assert finalStateToEpsilonTransitions.containsKey(state) : state;
            // mark the corresponding epsilon transitions
            epsilonTransToRemove.add(finalStateToEpsilonTransitions.get(state));
            // remove this spurious final state
            iter.remove();
          }
        }
        // add this fromOccurrence to the work list
        wl.add(fromOccurrence);
      }
    }
    // remove all marked epsilon transitions
    epsilonTransitions.removeAll(epsilonTransToRemove);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (FunctionOccurrence occur : occurrenceToFTAs.keySet()) {
      builder.append("=========== " + occur + " ===========\n");
      FTA fta = occurrenceToFTAs.get(occur);
      builder.append(fta);
      builder.append("\n");
    }
    builder.append("============\n");
    builder.append("Final occurrence: " + finalOccurrence).append("\n");
    builder.append("Final states: " + occurrenceToFTAs.get(finalOccurrence).finalStates).append("\n");
    builder.append("Epsilon Transition Number: " + epsilonTransitions.size());
    return builder.toString();
  }

  public static class EpsilonTransition {
    public final FunctionOccurrence fromOccurrence;
    public final State fromState;
    public final FunctionOccurrence toOccurrence;
    public final State toState;

    public EpsilonTransition(FunctionOccurrence fromOccurrence, State fromState, FunctionOccurrence toOccurrence, State toState) {
      this.fromOccurrence = fromOccurrence;
      this.fromState = fromState;
      this.toOccurrence = toOccurrence;
      this.toState = toState;
    }

    @Override
    public int hashCode() {
      return fromOccurrence.hashCode() + fromState.hashCode() + toOccurrence.hashCode() + toState.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof EpsilonTransition)) return false;
      EpsilonTransition tran = (EpsilonTransition) o;
      return fromOccurrence.equals(tran.fromOccurrence) && fromState.equals(tran.fromState) && toOccurrence.equals(tran.toOccurrence)
          && toState.equals(tran.toState);
    }

    @Override
    public String toString() {
      return "(" + fromOccurrence + ", " + fromState + ") -> (" + toOccurrence + ", " + toState + ")";
    }

  }

}
