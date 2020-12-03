package relish.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import relish.abs.Abstractions.BoolConstant;
import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.Value;
import relish.dsl.ConstantTerminalSymbol;
import relish.dsl.DSLGrammar;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.TerminalSymbol;
import relish.dsl.VariableTerminalSymbol;
import relish.fta.ComposedFTA;
import relish.fta.ComposedFTA.EpsilonTransition;
import relish.fta.FTA;
import relish.fta.ProgramTree;
import relish.fta.ProgramTree.Node;
import relish.fta.State;
import relish.fta.Transition;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;
import relish.util.EnumUtil;
import relish.util.ListMultiMap;
import relish.util.MultiMap;
import relish.util.PrintUtil;
import relish.util.SetMultiMap;

public class ComposedCFTALearner {

  // a map of grammars under consideration
  public final DSLGrammarMap grammarMap;
  // start indices of occurrence FTA in the product FTA, inclusive
  private final Map<FunctionOccurrence, Integer> startIndices = new HashMap<>();
  // end indices of occurrence FTA in the product FTA, exclusive
  private final Map<FunctionOccurrence, Integer> endIndices = new HashMap<>();
  // max number of input-outputs for backtrack search
  public static int K = 1;

  public ComposedCFTALearner(DSLGrammarMap grammarMap) {
    this.grammarMap = grammarMap;
  }

  // learn programs from a list of relational examples
  // return a map from function symbols to program trees
  public Map<FunctionSymbol, ProgramTree> learnMultiple(List<RelationalExample> examples) {
    // merge examples based on properties
    List<RelationalExample> mergedExamples = RelationalExample.mergeExamples(examples);

    PrintUtil.printlnIfVerbose("Constructing HFTA...");
    // build a composed FTA for each property
    List<ComposedFTA> composedFTAs = new ArrayList<>();
    for (RelationalExample example : mergedExamples) {
      ComposedFTA composedFTA = constructComposedFTA(example);
      composedFTAs.add(composedFTA);
    }

    // find a feasible path from all composed FTAs
    Map<FunctionSymbol, ProgramTree> ret = findPrograms(composedFTAs);
    return ret;
  }

  // core algorithm to find programs from list of composed FTAs
  public Map<FunctionSymbol, ProgramTree> findPrograms(List<ComposedFTA> composedFTAs) {

    Map<FunctionSymbol, ProgramTree> ret = new HashMap<>();

    // return "no solution" if any of the composed FTA is empty
    // because there is no program consistent with all examples of that property
    for (ComposedFTA composedFTA : composedFTAs) {
      PrintUtil.printlnIfVerbose("Num of states: " + composedFTA.numOfStates());
      if (composedFTA.isEmpty()) return ret;
    }

    PrintUtil.printlnIfVerbose("Computing FTA intersection...");
    // product occurrence FTAs
    Map<FunctionSymbol, FTA> funcSymbolToFTAs = productOccurrenceFTAs(composedFTAs);

    PrintUtil.printlnIfVerbose("Computing reachable inouts...");
    // compute reachable in-out sets
    List<FunctionSymbol> funcSymbols = new ArrayList<>(funcSymbolToFTAs.keySet());
    MultiMap<FunctionSymbol, InputOutput> funcSymbolToInOutSets = new ListMultiMap<>();
    for (FunctionSymbol funcSymbol : funcSymbols) {
      DSLGrammar grammar = grammarMap.get(funcSymbol);
      FTA fta = funcSymbolToFTAs.get(funcSymbol);
      List<InputOutput> inOutSet = computeReachableInOutSets(grammar, fta);
      // sort the inouts based on costs
      Collections.sort(inOutSet, (x, y) -> (x.cost - y.cost));
      funcSymbolToInOutSets.putAll(funcSymbol, inOutSet);
    }

    for (FunctionSymbol funcSymbol : funcSymbols) {
      PrintUtil.printlnIfVerbose(funcSymbol + ": " + funcSymbolToInOutSets.get(funcSymbol).size());
    }

    PrintUtil.printlnIfVerbose("Finding connected inouts...");
    // find a set of input-outputs that are connected
    Map<FunctionSymbol, InputOutput> funcSymbolToConnectedInOut = findConnectedInOuts(composedFTAs, funcSymbolToInOutSets);

    PrintUtil.printlnIfVerbose("Ranking...");
    // find program trees after finding a set of connected input-outputs
    if (funcSymbolToConnectedInOut != null) {
      for (FunctionSymbol funcSymbol : funcSymbols) {
        FTA fta = funcSymbolToFTAs.get(funcSymbol);
        InputOutput inout = funcSymbolToConnectedInOut.get(funcSymbol);
        ret.put(funcSymbol, rankFromInout(fta, inout));
      }
    }
    return ret;
  }

  // find connected input-outputs of each function
  public Map<FunctionSymbol, InputOutput> findConnectedInOuts(List<ComposedFTA> composedFTAs,
      MultiMap<FunctionSymbol, InputOutput> funcSymbolToInOutSets) {
    // pre-process
    List<FunctionSymbol> functionSymbols = new ArrayList<>(funcSymbolToInOutSets.keySet());
    // sort all function symbols by their number of input-outputs
    Collections.sort(functionSymbols, (x, y) -> (funcSymbolToInOutSets.get(x).size() - funcSymbolToInOutSets.get(y).size()));


    PrintUtil.printlnIfVerbose("ORDER: " + functionSymbols);

    // compute the parents and children map
    Map<FunctionOccurrence, FunctionOccurrence> parents = getOccurrenceParents(composedFTAs);
    ListMultiMap<FunctionOccurrence, FunctionOccurrence> children = getOccurrenceChildren(composedFTAs);
    MultiMap<FunctionSymbol, FunctionOccurrence> funcSymbolToOccurrences = getFuncSymbolOccurrences(composedFTAs);
    // backtrack search
    List<Map<FunctionSymbol, InputOutput>> topKInouts = new ArrayList<>();
    Map<FunctionSymbol, InputOutput> currInout = new HashMap<>();
    findTopKConnectedInOutsImpl(topKInouts, currInout, funcSymbolToInOutSets, parents, children, funcSymbolToOccurrences, functionSymbols, 0, K);
    if (topKInouts.isEmpty()) return null;
    Map<FunctionSymbol, InputOutput> ret = null;
    int minCost = Integer.MAX_VALUE;
    for (Map<FunctionSymbol, InputOutput> inout : topKInouts) {
      int cost = 0;
      for (FunctionSymbol funcSymbol : inout.keySet()) {
        cost += inout.get(funcSymbol).cost;
      }
      if (cost < minCost) {
        minCost = cost;
        ret = inout;
      }
    }
    assert ret != null;
    return ret;
  }

  // auxiliary function for finding connected input-outputs
  private void findTopKConnectedInOutsImpl(List<Map<FunctionSymbol, InputOutput>> ret, Map<FunctionSymbol, InputOutput> currInout,
      MultiMap<FunctionSymbol, InputOutput> funcSymbolToInOutSets, Map<FunctionOccurrence, FunctionOccurrence> parents,
      ListMultiMap<FunctionOccurrence, FunctionOccurrence> children, MultiMap<FunctionSymbol, FunctionOccurrence> funcSymbolToOccurrences,
      List<FunctionSymbol> funcSymbols, int index, int k) {

    if (index >= funcSymbols.size()) {
      // found a solution
      ret.add(new HashMap<>(currInout));
      return;
    }

    FunctionSymbol funcSymbol = funcSymbols.get(index);
    for (InputOutput inout : funcSymbolToInOutSets.get(funcSymbol)) {
      PrintUtil.printlnIfVerbose(funcSymbol + " " + inout);
      // propagate current input-output to prune input-outputs of other functions
      MultiMap<FunctionSymbol, InputOutput> updatedFuncSymbolToInOutSets = propagate(funcSymbolToInOutSets, parents, children,
          funcSymbolToOccurrences, funcSymbol, inout);
      // if there is no possible input-outputs for other functions, pick the next inout
      if (updatedFuncSymbolToInOutSets == null) continue;
      // otherwise store the current inout for current function
      currInout.put(funcSymbol, inout);
      // recursively get results of all other functions
      findTopKConnectedInOutsImpl(ret, currInout, updatedFuncSymbolToInOutSets, parents, children, funcSymbolToOccurrences, funcSymbols, index + 1,
          k);
      if (ret.size() >= k) return;
    }
  }

  // auxiliary function for finding connected input-outputs
  // deprecated because we have the top k version
  @Deprecated
  @SuppressWarnings("unused")
  private Map<FunctionSymbol, InputOutput> findConnectedInOutsImpl(MultiMap<FunctionSymbol, InputOutput> funcSymbolToInOutSets,
      Map<FunctionOccurrence, FunctionOccurrence> parents, ListMultiMap<FunctionOccurrence, FunctionOccurrence> children,
      MultiMap<FunctionSymbol, FunctionOccurrence> funcSymbolToOccurrences, List<FunctionSymbol> funcSymbols, int index) {
    Map<FunctionSymbol, InputOutput> ret = new HashMap<>();
    if (index >= funcSymbols.size()) return ret;
    FunctionSymbol funcSymbol = funcSymbols.get(index);
    for (InputOutput inout : funcSymbolToInOutSets.get(funcSymbol)) {
      System.out.println(funcSymbol + " " + inout);
      // propagate current input-output to prune input-outputs of other functions
      MultiMap<FunctionSymbol, InputOutput> updatedFuncSymbolToInOutSets = propagate(funcSymbolToInOutSets, parents, children,
          funcSymbolToOccurrences, funcSymbol, inout);
      // if there is no possible input-outputs for other functions, pick the next inout
      if (updatedFuncSymbolToInOutSets == null) continue;
      // otherwise store the current inout for current function
      ret.put(funcSymbol, inout);
      // recursively get results of all other functions
      Map<FunctionSymbol, InputOutput> result = findConnectedInOutsImpl(updatedFuncSymbolToInOutSets, parents, children, funcSymbolToOccurrences,
          funcSymbols, index + 1);
      if (result != null) {
        ret.putAll(result);
        return ret;
      }
    }
    return null;
  }

  // return a NEW map from function symbols to input-output sets, which prunes the unconnected input-output sets
  private MultiMap<FunctionSymbol, InputOutput> propagate(MultiMap<FunctionSymbol, InputOutput> funcSymbolToInOutSets,
      Map<FunctionOccurrence, FunctionOccurrence> parents, ListMultiMap<FunctionOccurrence, FunctionOccurrence> children,
      MultiMap<FunctionSymbol, FunctionOccurrence> funcSymbolToOccurrences, FunctionSymbol funcSymbol, InputOutput inout) {

    // start the new multimap by assigning the current function symbol to the provided inout
    MultiMap<FunctionSymbol, InputOutput> ret = new ListMultiMap<>();
    for (FunctionSymbol func : funcSymbolToInOutSets.keySet()) {
      if (func.equals(funcSymbol)) {
        ret.put(func, inout);
      } else {
        ret.putAll(func, funcSymbolToInOutSets.get(func));
      }
    }

    // worklist algorithm to compute fix-point
    Queue<FunctionOccurrence> worklist = new LinkedList<>();
    // add all occurrences of current function symbol to the worklist
    Collection<FunctionOccurrence> occurrences = funcSymbolToOccurrences.get(funcSymbol);
    worklist.addAll(occurrences);

    while (!worklist.isEmpty()) {
      FunctionOccurrence occurrence = worklist.poll();

      // handle the parent of current occurrence, if any
      if (parents.containsKey(occurrence)) {
        // get all possible outputs of current occurrence
        List<Value[]> currOutputs = new ArrayList<>();
        for (InputOutput currInout : ret.get(occurrence.symbol)) {
          Value[] output = Arrays.copyOfRange(currInout.out, startIndices.get(occurrence), endIndices.get(occurrence));
          currOutputs.add(output);
        }

        FunctionOccurrence parent = parents.get(occurrence);
        Collection<InputOutput> parentInouts = ret.get(parent.symbol);
        int delCounter = 0;
        for (Iterator<InputOutput> iter = parentInouts.iterator(); iter.hasNext();) {
          InputOutput parentInout = iter.next();
          // current occurrence corresponds to x_index in the parent occurrence
          int index = getIndexOfParentChildren(children, occurrence, parent);
          // remove the parent inout if its input is different from the output of current occurrence
          Value[] entireParentInput = getInputFromInout(parentInout, index);
          Value[] parentInput = Arrays.copyOfRange(entireParentInput, startIndices.get(parent), endIndices.get(parent));
          if (!isValueArrayInList(parentInput, currOutputs)) {
            iter.remove();
            ++delCounter;
          }
        }
        // if one function has no feasible input-output
        if (parentInouts.isEmpty()) return null;
        // if we have pruned anything using the parent occurrence, add the parent to the worklist
        if (delCounter > 0) worklist.add(parent);
      }

      // handle the children of current occurrence, if any
      if (children.containsKey(occurrence)) {
        for (FunctionOccurrence child : children.get(occurrence)) {
          // child is the index-th children of current occurrence
          int index = getIndexOfParentChildren(children, child, occurrence);
          // get all inputs corresponding to child
          List<Value[]> currInputs = new ArrayList<>();
          for (InputOutput currInout : ret.get(occurrence.symbol)) {
            Value[] entireInput = getInputFromInout(currInout, index);
            Value[] input = Arrays.copyOfRange(entireInput, startIndices.get(occurrence), endIndices.get(occurrence));
            currInputs.add(input);
          }

          Collection<InputOutput> childInouts = ret.get(child.symbol);
          int delCounter = 0;
          for (Iterator<InputOutput> iter = childInouts.iterator(); iter.hasNext();) {
            InputOutput childInout = iter.next();
            Value[] childOutput = Arrays.copyOfRange(childInout.out, startIndices.get(child), endIndices.get(child));
            if (!isValueArrayInList(childOutput, currInputs)) {
              iter.remove();
              ++delCounter;
            }
          }
          // if one function has no feasible input-output
          if (childInouts.isEmpty()) return null;
          // if we have pruned anything using the child occurrence, add the child to the worklist
          if (delCounter > 0) worklist.add(child);
        }
      }

    }

    return ret;
  }

  // return a program tree that has the specified input-output behavior in FTA
  private ProgramTree rankFromInout(FTA fta, InputOutput inout) {

    assert !fta.isEmpty();

    // initialization of costs of states (ONLY for non-terminal symbol states) 
    {
      MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof NonTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minCost = Integer.MAX_VALUE;
          }
        } else if (symbol instanceof VariableTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minCost = 0;
          }
        }
      }
    }

    // initialization of the number of unprocessed arguments for each transition 
    {
      for (Transition transition : fta.transitions) {
        transition.n = transition.production.rank;
      }
    }

    // map from each state to its in-transition in the final result 
    // map[s] represents the transition (along the minimum weighted route) that flows into state s 
    Map<State, Transition> prev = new HashMap<>();
    {

      // use a priority queue (heap) to achieve O(log n * size(fta)) running time complexity where n is the number of states in fta 
      PriorityQueue<State> wl = new PriorityQueue<>(fta.numOfStates(), (x, y) -> (x.minCost - y.minCost));
      // initialize the work-list to include all the states for terminal symbols 
      {
        MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
        for (Symbol symbol : symbolToStates.keySet()) {
          if (symbol instanceof VariableTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              Map<VariableTerminalSymbol, Value[]> inputs = inout.in;
              if (inputs.keySet().contains(symbol) && Arrays.equals(state.values, inputs.get(symbol))) {
                wl.add(state);
                // the marked bit is used for denoting if the state is in the current work-list 
                state.marked = true;
              } else {
                state.marked = false;
              }
            }
          } else if (symbol instanceof ConstantTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              wl.add(state);
              // the marked bit is used for denoting if the state is in the current work-list 
              state.marked = true;
            }
          } else if (symbol instanceof NonTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              state.marked = false;
            }
          } else {
            throw new RuntimeException();
          }
        }
      }

      MultiMap<State, Transition> stateToOutTransitions = fta.computeStateToOutTransitions();

      // a work-list algorithm 
      while (!wl.isEmpty()) {

        State state = wl.remove();
        state.marked = false;

        Collection<Transition> outTransitions = stateToOutTransitions.get(state);

        for (Transition outTransition : outTransitions) {
          int n = outTransition.n;
          n --;
          outTransition.n = n;
          if (n == 0) {
            State outState = outTransition.returnState;
            // NOTE: the additive weighting function (generalized Bellman's equations) 
            int cost = outTransition.cost + f(outTransition);
            int minCost = outState.minCost;
            if (cost < minCost) {
              if (!outState.marked) {
                // if outState is not in the current work-list, add it into the work-list 
                wl.add(outState);
                outState.marked = true;

                if (minCost < Integer.MAX_VALUE) {
                  // if outState has been visited before, then all of its out-transitions must be processed again 
                  for (Transition outTransition1 : stateToOutTransitions.get(outState)) {
                    int n1 = outTransition1.n;
                    n1 ++;
                    assert (n1 <= outTransition1.argumentStates.length);
                    outTransition1.n = n1;
                  }
                }
              }
              outState.minCost = cost;
              prev.put(outState, outTransition);
            }
          }
        }

      }
    }

    // construct the program tree for the minimum weighted route (encoded in prev) 
    ProgramTree ret = constructProgramTreeWithOutput(fta, prev, inout.out);

    return ret;
  }

  private int getIndexOfParentChildren(ListMultiMap<FunctionOccurrence, FunctionOccurrence> children, FunctionOccurrence curr,
      FunctionOccurrence parent) {
    int index = 0;
    for (FunctionOccurrence parentChild : children.get(parent)) {
      ++index;
      if (curr.equals(parentChild)) return index;
    }
    throw new RuntimeException("Unreachable");
  }

  private Value[] getInputFromInout(InputOutput inout, int index) {
    // this is a hack
    Map<VariableTerminalSymbol, Value[]> inputs = inout.in;
    for (VariableTerminalSymbol var : inputs.keySet()) {
      if (var.symbolName.equals("x" + index)) {
        return inputs.get(var);
      }
    }
    throw new RuntimeException("Unreachable for index: " + index);
  }

  private boolean isValueArrayInList(Value[] elem, List<Value[]> values) {
    for (Value[] value : values) {
      if (Arrays.equals(elem, value)) return true;
    }
    return false;
  }

  private ListMultiMap<FunctionSymbol, FunctionOccurrence> getFuncSymbolOccurrences(List<ComposedFTA> composedFTAs) {
    ListMultiMap<FunctionSymbol, FunctionOccurrence> ret = new ListMultiMap<>();
    for (ComposedFTA composedFTA : composedFTAs) {
      for (FunctionOccurrence occurrence : composedFTA.occurrenceToFTAs.keySet()) {
        ret.put(occurrence.symbol, occurrence);
      }
    }
    return ret;
  }

  // get parents of all function occurrences
  private Map<FunctionOccurrence, FunctionOccurrence> getOccurrenceParents(List<ComposedFTA> composedFTAs) {
    Map<FunctionOccurrence, FunctionOccurrence> ret = new HashMap<>();
    for (ComposedFTA composedFTA : composedFTAs) {
      for (EpsilonTransition tran : composedFTA.epsilonTransitions) {
        ret.put(tran.fromOccurrence, tran.toOccurrence);
      }
    }
    return ret;
  }

  // get children of all function occurrences
  private ListMultiMap<FunctionOccurrence, FunctionOccurrence> getOccurrenceChildren(List<ComposedFTA> composedFTAs) {
    Map<FunctionOccurrence, Map<Integer, FunctionOccurrence>> nestedMap = new HashMap<>();
    for (ComposedFTA composedFTA : composedFTAs) {
      for (EpsilonTransition tran : composedFTA.epsilonTransitions) {
        State toState = tran.toState;
        String symbolName = toState.symbol.symbolName;
        assert symbolName.startsWith("x") : symbolName;
        int index = Integer.parseInt(symbolName.substring(1));
        if (!nestedMap.containsKey(tran.toOccurrence)) {
          Map<Integer, FunctionOccurrence> map = new HashMap<>();
          map.put(index, tran.fromOccurrence);
          nestedMap.put(tran.toOccurrence, map);
        } else {
          Map<Integer, FunctionOccurrence> map = nestedMap.get(tran.toOccurrence);
          map.put(index, tran.fromOccurrence);
        }
      }
    }
    ListMultiMap<FunctionOccurrence, FunctionOccurrence> ret = new ListMultiMap<>();
    for (FunctionOccurrence occurrence : nestedMap.keySet()) {
      Map<Integer, FunctionOccurrence> map = nestedMap.get(occurrence);
      for (int i = 1; i <= map.size(); ++i) {
        assert map.containsKey(i) : i;
        ret.put(occurrence, map.get(i));
      }
    }
    return ret;
  }


  // learn programs from multiple composed FTAs
  // deprecated due to performance issue
  @Deprecated
  public Map<FunctionSymbol, ProgramTree> learnMultipleExplicitEnum(List<RelationalExample> examples) {
    // merge examples based on properties
    List<RelationalExample> mergedExamples = RelationalExample.mergeExamples(examples);

    // build a composed FTA for each property
    List<ComposedFTA> composedFTAs = new ArrayList<>();
    for (RelationalExample example : mergedExamples) {
      ComposedFTA composedFTA = constructComposedFTA(example);
      composedFTAs.add(composedFTA);
    }

    // find a feasible path from all composed FTAs
    Map<FunctionSymbol, ProgramTree> ret = computeFeasiblePaths(composedFTAs);
    return ret;
  }

  // compute the feasible path from multiple composed FTAs such that
  // paths in different occurrences of the same function are identical
  // paths would contribute to accepting paths in all composed FTAs
  @Deprecated
  public Map<FunctionSymbol, ProgramTree> computeFeasiblePaths(List<ComposedFTA> composedFTAs) {

    Map<FunctionSymbol, ProgramTree> ret = new HashMap<>();

    // return "no solution" if any of the composed FTA is empty
    // because there is no program consistent with all examples of that property
    for (ComposedFTA composedFTA : composedFTAs) {
      System.out.println("Num of states: " + composedFTA.numOfStates());
      if (composedFTA.isEmpty()) return ret;
    }

    // product occurrence FTAs
    Map<FunctionSymbol, FTA> funcSymbolToFTAs = productOccurrenceFTAs(composedFTAs);

    // compute reachable in-out sets
    List<FunctionSymbol> funcSymbols = new ArrayList<>(funcSymbolToFTAs.keySet());
    MultiMap<FunctionSymbol, InputOutput> funcSymbolToInOutSets = new ListMultiMap<>();
    for (FunctionSymbol funcSymbol : funcSymbols) {
      DSLGrammar grammar = grammarMap.get(funcSymbol);
      FTA fta = funcSymbolToFTAs.get(funcSymbol);
      Collection<InputOutput> inOutSet = computeReachableInOutSets(grammar, fta);
      funcSymbolToInOutSets.putAll(funcSymbol, inOutSet);
    }

    // compute the Cartesian product of in-outs from different functions
    List<List<InputOutput>> listOfInOuts = new ArrayList<>();
    System.out.println("Num of in-out sets:");
    for (FunctionSymbol funcSymbol : funcSymbols) {
      List<InputOutput> inOutList = new ArrayList<>(funcSymbolToInOutSets.get(funcSymbol));
      System.out.println(funcSymbol + ": " + inOutList.size());
      listOfInOuts.add(inOutList);
    }
    System.out.println("I know you'll get stuck here!!!");
    long startTime = System.currentTimeMillis();
    List<List<InputOutput>> products = EnumUtil.cartesianProduct(listOfInOuts);
    long endTime = System.currentTimeMillis();
    System.out.println("Nope. I'm done in " + (endTime - startTime) + " ms");
    PrintUtil.println("=========== Total In-outs: " + products.size());

    // sort all in-out sets by sum of costs
    class InputOutputListComparator implements Comparator<List<InputOutput>> {
      @Override
      public int compare(List<InputOutput> list1, List<InputOutput> list2) {
        int cost1 = 0;
        for (InputOutput inout : list1) {
          cost1 += inout.cost;
        }
        int cost2 = 0;
        for (InputOutput inout : list2) {
          cost2 += inout.cost;
        }
        return cost1 - cost2;
      }
    }
    Collections.sort(products, new InputOutputListComparator());

    // enumerate the product to search for a connected one
    Map<FunctionSymbol, InputOutput> funcSymbolToConnectedInOut = null;
    for (List<InputOutput> product : products) {
      Map<FunctionSymbol, InputOutput> funcSymbolToInOut = new HashMap<>();
      assert product.size() == funcSymbols.size();
      for (int i = 0; i < product.size(); ++i) {
        funcSymbolToInOut.put(funcSymbols.get(i), product.get(i));
      }
      if (checkConnectivity(composedFTAs, funcSymbolToInOut)) {
        funcSymbolToConnectedInOut = funcSymbolToInOut;
        System.out.println("Found one inout: ");
        PrintUtil.printMap(funcSymbolToInOut);
        break;
      }
    }

    // find program trees after finding a set of connected input-outputs
    if (funcSymbolToConnectedInOut != null) {
      for (FunctionSymbol funcSymbol : funcSymbols) {
        DSLGrammar grammar = grammarMap.get(funcSymbol);
        MultiMap<VariableTerminalSymbol, Value[]> valuation = new SetMultiMap<>();
        Map<VariableTerminalSymbol, Value[]> input = funcSymbolToConnectedInOut.get(funcSymbol).in;
        Value[] output = funcSymbolToConnectedInOut.get(funcSymbol).out;
        for (VariableTerminalSymbol var : input.keySet()) {
          valuation.put(var, input.get(var));
        }
        FTA fta = constructFTA(grammar, valuation);
        // set the real final states
        Iterator<State> iter = fta.finalStates.iterator();
        while (iter.hasNext()) {
          State state = iter.next();
          if (!Arrays.equals(output, state.values)) {
            iter.remove();
          }
        }
        fta.removeBackwardsUnreachable();
        ret.put(funcSymbol, rank(fta));
      }
    }
    return ret;
  }

  // check if a set of input-outputs is connected in all composedFTAs
  @Deprecated
  private boolean checkConnectivity(List<ComposedFTA> composedFTAs, Map<FunctionSymbol, InputOutput> funcSymbolToInOut) {
    for (ComposedFTA composedFTA : composedFTAs) {
      for (EpsilonTransition epsilonTran : composedFTA.epsilonTransitions) {
        // (f_k, q^c_r) -> (g_j, q^c_{x_i})
        InputOutput gInOut = funcSymbolToInOut.get(epsilonTran.toOccurrence.symbol);
        InputOutput fInOut = funcSymbolToInOut.get(epsilonTran.fromOccurrence.symbol);
        Value[] gValues = gInOut.in.get(epsilonTran.toState.symbol);
        Value[] fValues = fInOut.out;
        // check if values agree
        Value[] gValue = Arrays.copyOfRange(gValues, startIndices.get(epsilonTran.toOccurrence), endIndices.get(epsilonTran.toOccurrence));
        Value[] fValue = Arrays.copyOfRange(fValues, startIndices.get(epsilonTran.fromOccurrence), endIndices.get(epsilonTran.fromOccurrence));
        if (!Arrays.equals(gValue, fValue)) return false;
      }
    }
    return true;
  }

  //==========================================================

  // construct a composed FTA from a relational example
  public ComposedFTA constructComposedFTA(RelationalExample example) {
    ComposedFTA ret = new ComposedFTA();
    ExampleFunction rootFunction = example.rootFunction;
    constructComposedFTAImpl(ret, rootFunction);
    ret.finalOccurrence = rootFunction.occurrence;
    // set the real final states (true) of root function
    FTA finalFTA = ret.occurrenceToFTAs.get(ret.finalOccurrence);
    Iterator<State> finalStateIter = finalFTA.finalStates.iterator();
    while (finalStateIter.hasNext()) {
      State state = finalStateIter.next();
      // check if all values are true
      for (Value value : state.values) {
        if (!value.equals(new BoolConstant(true))) {
          finalStateIter.remove();
          break;
        }
      }
    }
    // remove all states and transitions that are not backwards reachable from final states
    ret.pruneBackwardsUnreachable();
    return ret;
  }

  // a helper function for composed FTA construction
  protected void constructComposedFTAImpl(ComposedFTA composedFTA, ExampleFunction func) {
    DSLGrammar grammar = grammarMap.get(func.occurrence.symbol);
    FTA fta = new FTA();
    MultiMap<Symbol, State> variableStates = new SetMultiMap<>();
    for (int i = 0; i < func.arity(); ++i) {
      ExampleTerm currArg = func.arguments.get(i);
      VariableTerminalSymbol var = (VariableTerminalSymbol) grammar.nameToSymbol.get("x" + (i + 1));
      if (currArg instanceof ExampleConstant) {
        // if this argument is a constant
        ExampleConstant argConst = (ExampleConstant) currArg;
        State argState = fta.mkState(var, argConst.values);
        variableStates.put(var, argState);
      } else if (currArg instanceof ExampleFunction) {
        // if this argument is another function term
        ExampleFunction argFunc = (ExampleFunction) currArg;
        // recursively construct FTA for the argument
        constructComposedFTAImpl(composedFTA, argFunc);
        FTA argFTA = composedFTA.getFTA(argFunc.occurrence);
        // take all final states of argument FTA as the initial variable states
        for (State finalState : argFTA.finalStates) {
          State argState = fta.mkState(var, finalState.values);
          variableStates.put(var, argState);
          // add epsilon transitions
          composedFTA.mkEpsilonTransition(argFunc.occurrence, finalState, func.occurrence, argState);
        }
        // add occurrence dependency
        composedFTA.mkDependency(argFunc.occurrence, func.occurrence, i + 1);
      } else {
        throw new RuntimeException("Unknown subtype of ExampleTerm");
      }
    }
    fta = constructFTAWithVariableStates(grammar, fta, variableStates);
    composedFTA.addFTA(func.occurrence, fta);
  }

  // for all function symbol, compute the product FTA of all its occurrences
  // assume occurrences are uniquely indexed among all composed FTAs
  private Map<FunctionSymbol, FTA> productOccurrenceFTAs(List<ComposedFTA> composedFTAs) {
    Map<FunctionSymbol, FTA> ret = new HashMap<>();
    ListMultiMap<FunctionSymbol, FunctionOccurrence> funcSymbolToOccurrences = getFuncSymbolOccurrences(composedFTAs);
    for (FunctionSymbol funcSymbol : funcSymbolToOccurrences.keySet()) {
      List<FunctionOccurrence> occurrences = funcSymbolToOccurrences.get(funcSymbol);
      // FTAs that correspond to all occurrences of this function symbol
      List<FTA> ftas = new ArrayList<>();
      int startIndex = 0;
      for (FunctionOccurrence occurrence : occurrences) {
        for (ComposedFTA composedFTA : composedFTAs) {
          if (composedFTA.occurrenceToFTAs.containsKey(occurrence)) {
            FTA fta = composedFTA.occurrenceToFTAs.get(occurrence);
            ftas.add(fta);
            // store start and end indices of this occurrence
            assert !startIndices.containsKey(occurrence) : occurrence;
            assert !endIndices.containsKey(occurrence) : occurrence;
            startIndices.put(occurrence, startIndex);
            endIndices.put(occurrence, startIndex + fta.numOfExamples);
            startIndex += fta.numOfExamples;
          }
        }
      }
      assert ftas.size() == occurrences.size();
      // compute the product
      DSLGrammar grammar = grammarMap.get(funcSymbol);
      FTA product = productFTAs(grammar, ftas);
      ret.put(funcSymbol, product);
    }
    return ret;
  }

  public MultiMap<ConstantTerminalSymbol, Value> computeCommonConstantValues(List<FTA> ftas) {

    // symbol -> list of set of values (one set for each FTA of the symbol) 
    ListMultiMap<ConstantTerminalSymbol, Set<Value>> map = new ListMultiMap<>();

    {

      for (FTA fta : ftas) {
        for (Symbol symbol : fta.symbolToStates.keySet()) {
          if (symbol instanceof ConstantTerminalSymbol) {
            Set<Value> set = new HashSet<>();
            for (State state : fta.symbolToStates.get(symbol)) {
              set.add(state.values[0]);
            }
            map.put((ConstantTerminalSymbol) symbol, set);
          }
        }
      }

    }

    MultiMap<ConstantTerminalSymbol, Value> ret = new SetMultiMap<>();

    {
      for (ConstantTerminalSymbol k : map.keySet()) {
        List<Set<Value>> list = map.get(k);
        Set<Value> set = new HashSet<>(list.get(0));

        for (int i = 1; i < list.size(); i ++) {
          set.retainAll(list.get(i));
        }

        ret.putAll(k, set);
      }
    }

    return ret;

  }

  // new one
  @Deprecated
  public FTA productFTAs1(DSLGrammar grammar, List<FTA> ftas) {

    assert ftas.size() > 0;
    if (ftas.size() == 1) return ftas.get(0);

    FTA ret = new FTA();

    List<Integer> startPos = new ArrayList<>();
    List<Integer> endPos = new ArrayList<>();
    int numOfExamples = 0;
    for (FTA fta : ftas) {
      startPos.add(numOfExamples);
      endPos.add(numOfExamples + fta.numOfExamples);
      numOfExamples += fta.numOfExamples;
    }

    SetMultiMap<Symbol, State> oldStates = new SetMultiMap<>();
    SetMultiMap<Symbol, State> newStates = new SetMultiMap<>();

    // initialize new states for constants and variables 
    {

      // constants 
      {
        MultiMap<ConstantTerminalSymbol, Value> map = this.computeCommonConstantValues(ftas);
        for (ConstantTerminalSymbol constantTerminalSymbol : map.keySet()) {
          Collection<Value> constantValues = map.get(constantTerminalSymbol);
          for (Value constantValue : constantValues) {
            Value[] values = new Value[numOfExamples];
            for (int i = 0; i < values.length; i ++) {
              values[i] = constantValue;
            }
            State state = ret.mkState(constantTerminalSymbol, values);
            state.minCost = constantTerminalSymbol.costs.get(constantValue);
            newStates.put(constantTerminalSymbol, state);
          }
        }
      }

      // variables 
      {
        MultiMap<VariableTerminalSymbol, Value[]> map = new ListMultiMap<>();

        // compute the Cartesian product of initial states
        for (VariableTerminalSymbol var : grammar.variableTerminalSymbols) {
          List<List<State>> ftaToInitStates = new ArrayList<>();
          for (FTA fta : ftas) {
            assert fta.symbolToStates.containsKey(var) : var;
            List<State> initStates = new ArrayList<>(fta.symbolToStates.get(var));
            ftaToInitStates.add(initStates);
          }

          List<List<State>> productStates = EnumUtil.cartesianProduct(ftaToInitStates);

          for (List<State> productState : productStates) {
            Value[] productValues = new Value[numOfExamples];
            for (int i = 0; i < productState.size(); ++i) {
              Value[] values = productState.get(i).values;
              System.arraycopy(values, 0, productValues, startPos.get(i), values.length);
            }
            map.put(var, productValues);
          }
        }

        for (VariableTerminalSymbol variableTerminalSymbol : map.keySet()) {
          for (Value[] values : map.get(variableTerminalSymbol)) {
            State state = ret.mkState(variableTerminalSymbol, values);
            state.minCost = 0;
            newStates.put(variableTerminalSymbol, state);
          }
        }
      }

    }

    // the transitions that have been created so far 
    Collection<Transition> transitions = new LinkedList<>();

    // a work-list algorithm 
    {

      int iter = 0;

      // work-list 
      while (!newStates.isEmpty()) {

        PrintUtil.println("*********************************************");
        PrintUtil.println("Start iteration " + ++iter);
        // PrintUtil.println("Number of transitions: " + transitions.size());
        // PrintUtil.println("Number of states: " + oldStates.size());

        // the old states before next iteration 
        SetMultiMap<Symbol, State> oldStates1 = SetMultiMap.unionSetMultiMaps(oldStates, newStates);
        // the new states before next iteration 
        SetMultiMap<Symbol, State> newStates1 = new SetMultiMap<>();
        // the productions that are activated by the new states (before this iteration) 
        Collection<Production> activatedProductions = computeActivatedProductions(grammar, oldStates1, newStates);

        for (Production activatedProduction : activatedProductions) {

          // 1. compute new states before next iteration and store them into "newStates1"
          // 2. compute new transitions that are created by the new states in this iteration and add them into "transitions" 
          handleActivatedProduction(ret, activatedProduction, oldStates, newStates, oldStates1, newStates1, transitions, numOfExamples);

        }
        oldStates = oldStates1;
        newStates = newStates1;
      }
    }

    // create the FTA A = (Q, F, Q_f, \Delta) 
    MultiMap<Symbol, State> symbolToStates = oldStates;
    Collection<Production> alphabet = new ArrayList<>(grammar.productions);
    Collection<State> finalStates = new HashSet<>();
    {
      // mark all states associated with the start symbol as final
      Collection<State> candidates = symbolToStates.get(grammar.startSymbol);
      finalStates.addAll(candidates);
    }

    ret.symbolToStates = symbolToStates;
    ret.alphabet = alphabet;
    ret.finalStates = finalStates;
    ret.transitions = transitions;
    ret.startSymbol = grammar.startSymbol;


    // collect all final values of FTAs
    List<List<Value[]>> ftaToFinalValues = new ArrayList<>();
    for (int i = 0; i < ftas.size(); ++i) {
      List<Value[]> finalValues = new ArrayList<>();
      for (State state : ftas.get(i).finalStates) {
        finalValues.add(state.values);
      }
      ftaToFinalValues.add(finalValues);
    }

    // remove all spurious final states
    for (Iterator<State> iter = ret.finalStates.iterator(); iter.hasNext();) {
      State state = iter.next();
      for (int i = 0; i < ftas.size(); ++i) {
        Value[] values = Arrays.copyOfRange(state.values, startPos.get(i), endPos.get(i));
        if (!isValueArrayInList(values, ftaToFinalValues.get(i))) {
          iter.remove();
          break;
        }
      }
    }

    ret.removeBackwardsUnreachable();

    return ret;

  }

  // compute the intersection of a list of FTAs, assuming they have the same grammar
  public FTA productFTAs(DSLGrammar grammar, List<FTA> ftas) {
    assert ftas.size() > 0;
    if (ftas.size() == 1) return ftas.get(0);

    // compute the start and end positions of each FTA value
    // also compute the length of value in the product FTA
    // the values of FTA(i) is stored at [startPos(i), endPos(i))
    List<Integer> startPos = new ArrayList<>();
    List<Integer> endPos = new ArrayList<>();
    int productValueLen = 0;
    for (FTA fta : ftas) {
      startPos.add(productValueLen);
      endPos.add(productValueLen + fta.numOfExamples);
      productValueLen += fta.numOfExamples;
    }

    MultiMap<VariableTerminalSymbol, Value[]> valuations = new SetMultiMap<>();

    // compute the Cartesian product of initial states
    for (VariableTerminalSymbol var : grammar.variableTerminalSymbols) {
      List<List<State>> ftaToInitStates = new ArrayList<>();
      for (FTA fta : ftas) {
        assert fta.symbolToStates.containsKey(var) : var;
        List<State> initStates = new ArrayList<>(fta.symbolToStates.get(var));
        ftaToInitStates.add(initStates);
      }

      List<List<State>> productStates = EnumUtil.cartesianProduct(ftaToInitStates);

      for (List<State> productState : productStates) {
        Value[] productValues = new Value[productValueLen];
        for (int i = 0; i < productState.size(); ++i) {
          Value[] values = productState.get(i).values;
          System.arraycopy(values, 0, productValues, startPos.get(i), values.length);
        }
        valuations.put(var, productValues);
      }
    }

    // build the new FTA using product initial states
    FTA ret = constructFTA(grammar, valuations);

    // collect all final values of FTAs
    List<List<Value[]>> ftaToFinalValues = new ArrayList<>();
    for (int i = 0; i < ftas.size(); ++i) {
      List<Value[]> finalValues = new ArrayList<>();
      for (State state : ftas.get(i).finalStates) {
        finalValues.add(state.values);
      }
      ftaToFinalValues.add(finalValues);
    }

    // remove all spurious final states
    for (Iterator<State> iter = ret.finalStates.iterator(); iter.hasNext();) {
      State state = iter.next();
      for (int i = 0; i < ftas.size(); ++i) {
        Value[] values = Arrays.copyOfRange(state.values, startPos.get(i), endPos.get(i));
        if (!isValueArrayInList(values, ftaToFinalValues.get(i))) {
          iter.remove();
          break;
        }
      }
    }

    // System.out.println("********** Product before pruning: " + ret.numOfTransitions());

    ret.removeBackwardsUnreachable();

    // System.out.println("********** Pruduct after pruning: " + ret.numOfTransitions());

    return ret;
  }

  /* FTA construction */

  // construct a single FTA given DSL grammar and variable valuations
  public FTA constructFTA(DSLGrammar grammar, MultiMap<VariableTerminalSymbol, Value[]> valuations) {
    FTA fta = new FTA();
    SetMultiMap<Symbol, State> variableStates = new SetMultiMap<>();
    Collection<VariableTerminalSymbol> variableTerminalSymbols = grammar.variableTerminalSymbols;
    // compute initial states for all variables
    for (VariableTerminalSymbol variableTerminalSymbol : variableTerminalSymbols) {
      assert valuations.get(variableTerminalSymbol) != null : variableTerminalSymbol;
      for (Value[] values : valuations.get(variableTerminalSymbol)) {
        State state = fta.mkState(variableTerminalSymbol, values);
        state.minCost = 0;
        variableStates.put(variableTerminalSymbol, state);
      }
    }
    return constructFTAWithVariableStates(grammar, fta, variableStates);
  }

  // construct a single FTA given DSL grammar and initial variable states
  public FTA constructFTAWithVariableStates(DSLGrammar grammar, FTA ret, MultiMap<Symbol, State> variableStates) {

    Symbol sym = variableStates.keySet().iterator().next();
    int numOfExamples = variableStates.get(sym).iterator().next().values.length;

    SetMultiMap<Symbol, State> oldStates = new SetMultiMap<>();
    SetMultiMap<Symbol, State> newStates = new SetMultiMap<>();

    // initialization of the new states before the first iteration 
    {
      // constants 
      {
        Collection<ConstantTerminalSymbol> constantTerminalSymbols = grammar.constantTerminalSymbols;
        for (ConstantTerminalSymbol constantTerminalSymbol : constantTerminalSymbols) {
          Collection<Value> constantValues = constantTerminalSymbol.values;
          assert (constantValues != null);
          for (Value constantValue : constantValues) {
            Value[] values = new Value[numOfExamples];
            for (int i = 0; i < values.length; i ++) {
              values[i] = constantValue;
            }
            State state = ret.mkState(constantTerminalSymbol, values);
            state.minCost = constantTerminalSymbol.costs.get(constantValue);
            newStates.put(constantTerminalSymbol, state);
          }
        }
      }
      // variables 
      {
        for (Symbol variableSymbol : variableStates.keySet()) {
          newStates.putAll(variableSymbol, variableStates.get(variableSymbol));
        }
      }
    }

    // the transitions that have been created so far 
    Collection<Transition> transitions = new LinkedList<>();

    // a work-list algorithm 
    {

      // int iter = 0;

      // work-list 
      while (!newStates.isEmpty()) {

        // PrintUtil.println("*********************************************");
        // PrintUtil.println("Start iteration " + ++iter);
        // PrintUtil.println("Number of transitions: " + transitions.size());
        // PrintUtil.println("Number of states: " + oldStates.size());

        // the old states before next iteration 
        SetMultiMap<Symbol, State> oldStates1 = SetMultiMap.unionSetMultiMaps(oldStates, newStates);
        // the new states before next iteration 
        SetMultiMap<Symbol, State> newStates1 = new SetMultiMap<>();
        // the productions that are activated by the new states (before this iteration) 
        Collection<Production> activatedProductions = computeActivatedProductions(grammar, oldStates1, newStates);

        for (Production activatedProduction : activatedProductions) {

          // 1. compute new states before next iteration and store them into "newStates1"
          // 2. compute new transitions that are created by the new states in this iteration and add them into "transitions" 
          handleActivatedProduction(ret, activatedProduction, oldStates, newStates, oldStates1, newStates1, transitions, numOfExamples);

        }
        oldStates = oldStates1;
        newStates = newStates1;
      }
    }

    // create the FTA A = (Q, F, Q_f, \Delta) 
    MultiMap<Symbol, State> symbolToStates = oldStates;
    Collection<Production> alphabet = new ArrayList<>(grammar.productions);
    Collection<State> finalStates = new HashSet<>();
    {
      // mark all states associated with the start symbol as final
      Collection<State> candidates = symbolToStates.get(grammar.startSymbol);
      finalStates.addAll(candidates);
    }

    ret.symbolToStates = symbolToStates;
    ret.alphabet = alphabet;
    ret.finalStates = finalStates;
    ret.transitions = transitions;
    ret.startSymbol = grammar.startSymbol;

    ret.removeBackwardsUnreachable();

    return ret;

  }

  // a production is activated iff 
  // 1. the production has at least one argument with new states being created 
  // 2. all of its argument symbols have non-empty sets of states that have been created (including this iteration) 
  protected Collection<Production> computeActivatedProductions(DSLGrammar grammar, MultiMap<Symbol, State> oldStates1,
      MultiMap<Symbol, State> newStates) {
    // the productions that satisfy the first requirement, i.e., at least one symbol has new states 
    Collection<Production> candidates = new HashSet<>();
    for (Symbol symbol : newStates.keySet()) {
      assert (!newStates.get(symbol).isEmpty());
      Collection<Production> productions = grammar.symbolToOutProductions.get(symbol);
      if (productions == null) continue;
      candidates.addAll(productions);
    }
    // filter out productions that do not satisfy the second requirement 
    Collection<Production> ret = new ArrayList<>();
    Filter: for (Production production : candidates) {
      Symbol[] argumentSymbols = production.argumentSymbols;
      for (Symbol argumentSymbol : argumentSymbols) {
        if (!oldStates1.containsKey(argumentSymbol)) continue Filter;
        assert (!oldStates1.get(argumentSymbol).isEmpty());
      }
      ret.add(production);
    }

    return ret;
  }

  //  (Q_1 + Q'_1) * (Q_2 + Q'_2) * .. * (Q_n + Q'_n) 
  // = [Q'_1 * (Q_2 + Q'_2) * .. * (Q_n + Q'_n)][computed] + Q_1 * (Q_2 + Q'_2) * .. * (Q_n + Q'_n) 
  // = [computed] + [Q_1 * Q'_2 * .. * (Q_n +Q'_n)][computed] + Q_1 * Q_2 * .. * (Q_n + Q'_n) 
  // = [computed] + [computed] + .. 
  protected void handleActivatedProduction(FTA fta, Production production, MultiMap<Symbol, State> oldStates, MultiMap<Symbol, State> newStates,
      MultiMap<Symbol, State> oldStates1, MultiMap<Symbol, State> newStates1, Collection<Transition> transitions, int numOfExamples) {

    int rank = production.rank;
    Symbol[] argumentSymbols = production.argumentSymbols;

    // states for arguments 
    List<Collection<State>> list = new ArrayList<>(rank);
    // set to oldStates1 (all current states) 
    for (Symbol argumentSymbol : argumentSymbols) {
      list.add(oldStates1.get(argumentSymbol));
    }

    for (int i = 0; i < rank; i ++) {

      // in the i-th iteration, we compute Q_1 * .. * Q_{i-1} * Q'_i * (Q_{i+1} + Q'_{i+1}) * .. * (Q'_n + Q_n) 

      Symbol symbol = argumentSymbols[i];
      // get Q'_i 
      Collection<State> currNewStates = newStates.get(symbol);

      // computation (in this iteration) is necessary iff Q'_i is not empty 
      // it seems we should do list.set(i, Q_i) before continue, however we do not do this 
      // since Q'_i is empty means (Q'_i + Q_i) = Q_i -- LOL 
      if (currNewStates == null || currNewStates.isEmpty()) continue;

      // set Q'_i 
      list.set(i, currNewStates);
      // compute Q_1 * .. * Q_{i-1} * Q'_i * (Q_{i+1} + Q'_{i+1}) * .. * (Q_n + Q'_n) 
      // by applying the abstract transformer on the Cartesian product of states for the arguments 
      cartesian(fta, production, list, newStates1, transitions, oldStates1, numOfExamples);
      // old states Q_i 
      Collection<State> currOldStates = oldStates.get(symbol);
      // if Q_i is empty, we can break the whole loop  
      if (currOldStates == null || currOldStates.isEmpty()) return;
      // set Q_i 
      list.set(i, currOldStates);

    }

  }

  // apply the abstract transformer on the Cartesian product of states for arguments 
  protected void cartesian(FTA fta, Production production, List<Collection<State>> list, MultiMap<Symbol, State> newStates1,
      Collection<Transition> transitions, MultiMap<Symbol, State> oldStates1, int numOfExamples) {

    cartesian(fta, production, list, list.size() - 1, new State[production.rank], newStates1, transitions, oldStates1, numOfExamples);

  }

  // the helper function 
  protected void cartesian(FTA fta, Production production, List<Collection<State>> list, int currIndex, State[] currStates,
      MultiMap<Symbol, State> newStates1, Collection<Transition> transitions, MultiMap<Symbol, State> oldStates1, int numOfExamples) {

    // base case: we find one combination of states for arguments 
    if (currIndex == -1) {

      // check against maximum recursion depth bound 
      int depth = 0;
      // compute the current minimum recursion depth 
      if (production.isRecursive) {
        for (State currState : currStates) {
          if (currState.symbol.equals(production.returnSymbol)) {
            depth += currState.minDepth;
          }
        }
        depth += 1;
      }
      if (depth > production.returnSymbol.maxDepth) return;

      Value[] returnValues = new Value[numOfExamples];
      {
        Value[][] argsForExamples = new Value[numOfExamples][production.rank];
        for (int i = 0; i < production.rank; i ++) {
          Value[] currValues = currStates[i].values;
          for (int j = 0; j < numOfExamples; j ++) {
            argsForExamples[j][i] = currValues[j];
          }
        }
        for (int i = 0; i < returnValues.length; i ++) {
          Value[] args = argsForExamples[i];
          Value returnValue = production.exec(args);
          returnValues[i] = returnValue;
        }

      }

      Symbol returnSymbol = production.returnSymbol;

      // NOTE: this is where new states are created (however the state might already exist) 
      State returnState = fta.mkState(returnSymbol, returnValues);
      // minDepth for a state is initialized to be Integer.MAX_VALUE 
      returnState.minDepth = Math.min(returnState.minDepth, depth);

      State[] argumentStates = new State[currStates.length];
      System.arraycopy(currStates, 0, argumentStates, 0, argumentStates.length);

      // NOTE: this is where new transitions are created 
      // NOTE: The transition is guaranteed to be a new one (!) since there is at least one new argument state 
      Transition transition = fta.mkTransition(production, argumentStates, returnState);
//     assert (!transitions.contains(transition));
      transitions.add(transition);

      // check if return state is new or not 
      if (!oldStates1.contains(returnSymbol, returnState)) {
        newStates1.put(returnSymbol, returnState);
      }

      return;
    }

    // recursive case 
    for (State state : list.get(currIndex)) {
      currStates[currIndex] = state;
      cartesian(fta, production, list, currIndex - 1, currStates, newStates1, transitions, oldStates1, numOfExamples);
    }

  }

  protected boolean equals(Value[] outputs, Value[] values) {
    assert (outputs.length == values.length);
    return Arrays.equals(outputs, values);
  }

  /* Ranking */

  // NOTE: a tunable function used in computing cost in the ranking algorithm 
  protected int f(Transition transition) {
    int ret = 0;
    for (State argumentState : transition.argumentStates) {
      ret += argumentState.minCost;
    }
    return ret;
  }

  //==========================================================

  @Deprecated
  protected ProgramTree rank(FTA fta) {

    // no program exists 
    if (fta.isEmpty()) return null;

    // initialization of costs of states (ONLY for non-terminal symbol states) 
    {
      MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof NonTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minCost = Integer.MAX_VALUE;
          }
        }
      }
    }

    // initialization of the number of unprocessed arguments for each transition 
    {
      for (Transition transition : fta.transitions) {
        transition.n = transition.production.rank;
      }
    }

    // map from each state to its in-transition in the final result 
    // map[s] represents the transition (along the minimum weighted route) that flows into state s 
    Map<State, Transition> prev = new HashMap<>();
    {

      class StateCostComparator implements Comparator<State> {
        @Override
        public int compare(State state1, State state2) {
          return state1.minCost - state2.minCost;
        }
      }

      Comparator<State> comparator = new StateCostComparator();

      // use a priority queue (heap) to achieve O(log n * size(fta)) running time complexity where n is the number of states in fta 
      PriorityQueue<State> wl = new PriorityQueue<>(fta.numOfStates(), comparator);
      // initialize the work-list to include all the states for terminal symbols 
      {
        MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
        for (Symbol symbol : symbolToStates.keySet()) {
          if (symbol instanceof TerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              wl.add(state);
              // the marked bit is used for denoting if the state is in the current work-list 
              state.marked = true;
            }
          } else if (symbol instanceof NonTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              state.marked = false;
            }
          } else {
            throw new RuntimeException();
          }
        }
      }

      MultiMap<State, Transition> stateToOutTransitions = fta.computeStateToOutTransitions();

      // a work-list algorithm 
      while (!wl.isEmpty()) {

        State state = wl.remove();
        state.marked = false;

        Collection<Transition> outTransitions = stateToOutTransitions.get(state);

        for (Transition outTransition : outTransitions) {
          int n = outTransition.n;
          n --;
          outTransition.n = n;
          if (n == 0) {
            State outState = outTransition.returnState;
            // NOTE: the additive weighting function (generalized Bellman's equations) 
            int cost = outTransition.cost + f(outTransition);
            int minCost = outState.minCost;
            if (cost < minCost) {
              if (!outState.marked) {
                // if outState is not in the current work-list, add it into the work-list 
                wl.add(outState);
                outState.marked = true;

                if (minCost < Integer.MAX_VALUE) {
                  // if outState has been visited before, then all of its out-transitions must be processed again 
                  for (Transition outTransition1 : stateToOutTransitions.get(outState)) {
                    int n1 = outTransition1.n;
                    n1 ++;
                    assert (n1 <= outTransition1.argumentStates.length);
                    outTransition1.n = n1;
                  }
                }
              }
              outState.minCost = cost;
              prev.put(outState, outTransition);
            }
          }
        }

      }
    }

    // construct the program tree for the minimum weighted route (encoded in prev) 
    ProgramTree ret = constructProgramTree(fta, prev);

    return ret;

  }

  // given the predecessor map "prev", construct a program tree which represents the best program 
  @Deprecated
  protected ProgramTree constructProgramTree(FTA fta, Map<State, Transition> prev) {

    // find the final state with the minimum cost 
    Collection<State> finalStates = fta.finalStates;
    State finalStateWithMinCost = null;
    for (State finalState : finalStates) {
      int minCost = finalState.minCost;
      if (finalStateWithMinCost == null || minCost < finalStateWithMinCost.minCost) {
        finalStateWithMinCost = finalState;
      }
    }
    assert (finalStateWithMinCost != null);

    // program tree construction 
    Node root = constructProgramTree(prev, finalStateWithMinCost);

    ProgramTree ret = new ProgramTree(root);
    return ret;

  }

  //==========================================================

  // given the predecessor map "prev" and output, construct a program tree which represents the best program 
  protected ProgramTree constructProgramTreeWithOutput(FTA fta, Map<State, Transition> prev, Value[] output) {

    // find the final state with the minimum cost 
    Collection<State> finalStates = fta.finalStates;
    State finalStateWithMinCost = null;
    for (State finalState : finalStates) {
      int minCost = finalState.minCost;
      if (!Arrays.equals(finalState.values, output)) continue;
      if (finalStateWithMinCost == null || minCost < finalStateWithMinCost.minCost) {
        finalStateWithMinCost = finalState;
      }
    }
    assert (finalStateWithMinCost != null);

    // program tree construction 
    Node root = constructProgramTree(prev, finalStateWithMinCost);

    ProgramTree ret = new ProgramTree(root);
    return ret;

  }

  // construct a program tree whose root node has rootState 
  protected Node constructProgramTree(Map<State, Transition> prev, State rootState) {

    Node ret = constructProgramTree(prev, rootState, new HashMap<State, Node>());

    return ret;

  }

  // a helper function 
  protected Node constructProgramTree(Map<State, Transition> prev, State rootState, Map<State, Node> constructed) {

    // first check if the program tree for rootState has been constructed 
    {
      Node ret = constructed.get(rootState);
      if (ret != null) return ret;
    }

    Transition transition = prev.get(rootState);

    // base case: rootState is a leaf node (terminal state) 
    if (transition == null) {
      assert (rootState.symbol instanceof TerminalSymbol);
      Node ret = new Node(rootState, null, null);
      ret.concreteValues = new ConcreteValue[rootState.values.length];
      for (int i = 0; i < ret.concreteValues.length; i ++) {
        ret.concreteValues[i] = (ConcreteValue) rootState.values[i];
      }
      return ret;
    }

    // recursive case: rootState is an internal node (non-terminal state) 
    Production production = transition.production;
    Node[] children = new Node[production.rank];
    State[] argumentStates = transition.argumentStates;
    for (int i = 0; i < argumentStates.length; i ++) {
      State argumentState = argumentStates[i];
      Node child = constructProgramTree(prev, argumentState, constructed);
      children[i] = child;
    }

    Node ret = new Node(rootState, children, production);

    // put into the constructed cache 
    assert (!constructed.containsKey(rootState)) : rootState;
    constructed.put(rootState, ret);

    return ret;
  }

  // compute start-symbol states that are reachable from initStates
  // mark the min-cost as side-effect
  private Set<State> computeForwardReachableWithCost(FTA fta, MultiMap<State, Transition> stateToOutTransitions, List<State> initStates) {

    // initialization of costs of states
    {
      MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof NonTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minCost = Integer.MAX_VALUE;
          }
        } else if (symbol instanceof VariableTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minCost = 0;
          }
        } else if (symbol instanceof ConstantTerminalSymbol) {
          ;
        } else {
          throw new RuntimeException("unreachable");
        }
      }
    }

    // initialization of the number of unprocessed arguments for each transition 
    {
      for (Transition transition : fta.transitions) {
        transition.n = transition.production.rank;
      }
    }

    // start-symbol states that are reachable from initial states
    Set<State> ret = new HashSet<>();
    {
      // use a priority queue (heap) to achieve O(log n * size(fta)) running time complexity where n is the number of states in fta 
      PriorityQueue<State> wl = new PriorityQueue<>(fta.numOfStates(), (x, y) -> (x.minCost - y.minCost));
      // initialize the work-list to include all the states for terminal symbols 
      {
        MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
        for (Symbol symbol : symbolToStates.keySet()) {
          if (symbol instanceof ConstantTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              wl.add(state);
              // the marked bit is used for denoting if the state is in the current work-list 
              state.marked = true;
            }
          } else if (symbol instanceof VariableTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              state.marked = false;
            }
          } else if (symbol instanceof NonTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              state.marked = false;
            }
          } else {
            throw new RuntimeException();
          }
        }

        for (State state : initStates) {
          wl.add(state);
          state.marked = true;
        }
      }

      // a work-list algorithm
      while (!wl.isEmpty()) {

        State state = wl.remove();
        state.marked = false;

        // add the final state to return set
        if (fta.finalStates.contains(state)) {
          ret.add(state);
        }

        Collection<Transition> outTransitions = stateToOutTransitions.get(state);

        for (Transition outTransition : outTransitions) {
          int n = outTransition.n;
          n --;
          outTransition.n = n;
          if (n == 0) {
            State outState = outTransition.returnState;
            // NOTE: the additive weighting function (generalized Bellman's equations)
            int cost = outTransition.cost + f(outTransition);
            int minCost = outState.minCost;
            if (cost < minCost) {
              if (!outState.marked) {
                // if outState is not in the current work-list, add it into the work-list
                wl.add(outState);
                outState.marked = true;

                if (minCost < Integer.MAX_VALUE) {
                  // if outState has been visited before, then all of its out-transitions must be processed again
                  for (Transition outTransition1 : stateToOutTransitions.get(outState)) {
                    int n1 = outTransition1.n;
                    n1 ++;
                    assert (n1 <= outTransition1.argumentStates.length);
                    outTransition1.n = n1;
                  }
                }
              }
              outState.minCost = cost;

            }
          }
        }

      }
    }

    return ret;
  }

  // compute reachable in-out sets
  public List<InputOutput> computeReachableInOutSets(DSLGrammar grammar, FTA fta) {

    List<InputOutput> ret = new ArrayList<>();

    if (fta.isEmpty()) return ret;

    // cache out-transitions for all states
    MultiMap<State, Transition> stateToOutTransitions = fta.computeStateToOutTransitions();

    // find all variable terminal symbols
    List<VariableTerminalSymbol> variableSymbols = new ArrayList<>();
    for (Symbol symbol : fta.symbolToStates.keySet()) {
      if (symbol instanceof VariableTerminalSymbol) {
        variableSymbols.add((VariableTerminalSymbol) symbol);
      }
    }

    // sort all variable symbols by name
    // not necessary -- just for pretty print
    Collections.sort(variableSymbols, ((s1, s2) -> s1.symbolName.compareTo(s2.symbolName)));

    // get the Cartesian product
    List<List<State>> varToInitStates = new ArrayList<>();
    for (VariableTerminalSymbol var : variableSymbols) {
      List<State> initStates = new ArrayList<>();
      for (State state : fta.symbolToStates.get(var)) {
        initStates.add(state);
      }
      varToInitStates.add(initStates);
    }

    List<List<State>> products = EnumUtil.cartesianProduct(varToInitStates);

    for (List<State> product : products) {

      assert product.size() > 0;

      // compute the valuation
      MultiMap<VariableTerminalSymbol, Value[]> valuation = new SetMultiMap<>();
      for (State varState : product) {
        assert varState.symbol instanceof VariableTerminalSymbol : varState;
        valuation.put((VariableTerminalSymbol) varState.symbol, varState.values);
      }

      // compute reachable states with cost
      Set<State> reachableFinalStates = computeForwardReachableWithCost(fta, stateToOutTransitions, product);

      // add the in-out set into ret
      for (State state : reachableFinalStates) {
        Map<VariableTerminalSymbol, Value[]> in = new HashMap<>();
        for (VariableTerminalSymbol var : valuation.keySet()) {
          Collection<Value[]> values = valuation.get(var);
          assert values.size() == 1;
          // assume the "values" set is singleton
          in.put(var, values.iterator().next());
        }
        Value[] out = state.values;
        ret.add(new InputOutput(in, out, state.minCost));
      }
    }

    return ret;
  }

  //==========================================================
  // compute the minimum cost of reaching each state in FTA
  // exactly the same as rank(FTA fta) *except* that it doesn't return a program tree
  // deprecated for performance issues
  @Deprecated
  private void computeCost(FTA fta) {

    assert !fta.isEmpty() : fta;

    // initialization of costs of states (ONLY for non-terminal symbol states)
    {
      MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
      for (Symbol symbol : symbolToStates.keySet()) {
        if (symbol instanceof NonTerminalSymbol) {
          for (State state : symbolToStates.get(symbol)) {
            state.minCost = Integer.MAX_VALUE;
          }
        }
      }
    }

    // initialization of the number of unprocessed arguments for each transition
    {
      for (Transition transition : fta.transitions) {
        transition.n = transition.production.rank;
      }
    }

    // map from each state to its in-transition in the final result
    // map[s] represents the transition (along the minimum weighted route) that flows into state s
    Map<State, Transition> prev = new HashMap<>();
    {

      class StateCostComparator implements Comparator<State> {
        @Override
        public int compare(State state1, State state2) {
          return state1.minCost - state2.minCost;
        }
      }

      Comparator<State> comparator = new StateCostComparator();

      // use a priority queue (heap) to achieve O(log n * size(fta)) running time complexity
      // where n is the number of states in fta
      PriorityQueue<State> wl = new PriorityQueue<>(fta.numOfStates(), comparator);
      // initialize the work-list to include all the states for terminal symbols
      {
        MultiMap<Symbol, State> symbolToStates = fta.symbolToStates;
        for (Symbol symbol : symbolToStates.keySet()) {
          if (symbol instanceof TerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              wl.add(state);
              // the marked bit is used for denoting if the state is in the current work-list
              state.marked = true;
            }
          } else if (symbol instanceof NonTerminalSymbol) {
            for (State state : symbolToStates.get(symbol)) {
              state.marked = false;
            }
          } else {
            throw new RuntimeException();
          }
        }
      }

      MultiMap<State, Transition> stateToOutTransitions = fta.computeStateToOutTransitions();

      // a work-list algorithm
      while (!wl.isEmpty()) {

        State state = wl.remove();
        state.marked = false;

        Collection<Transition> outTransitions = stateToOutTransitions.get(state);

        for (Transition outTransition : outTransitions) {
          int n = outTransition.n;
          n --;
          outTransition.n = n;
          if (n == 0) {
            State outState = outTransition.returnState;
            // NOTE: the additive weighting function (generalized Bellman's equations)
            int cost = outTransition.cost + f(outTransition);
            int minCost = outState.minCost;
            if (cost < minCost) {
              if (!outState.marked) {
                // if outState is not in the current work-list, add it into the work-list
                wl.add(outState);
                outState.marked = true;

                if (minCost < Integer.MAX_VALUE) {
                  // if outState has been visited before, then all of its out-transitions must be processed again
                  for (Transition outTransition1 : stateToOutTransitions.get(outState)) {
                    int n1 = outTransition1.n;
                    n1 ++;
                    assert (n1 <= outTransition1.argumentStates.length);
                    outTransition1.n = n1;
                  }
                }
              }
              outState.minCost = cost;
              prev.put(outState, outTransition);
            }
          }
        }

      }
    }

  }

  // compute reachable in-out sets
  // deprecated for performance issues
  @Deprecated
  public List<InputOutput> computeReachableInOutSetsByBuildFTA(DSLGrammar grammar, FTA fta) {

    List<InputOutput> ret = new ArrayList<>();

    // find all final values of provided FTA
    List<Value[]> finalValues = new ArrayList<>();
    for (State state : fta.finalStates) {
      finalValues.add(state.values);
    }

    // find all variable terminal symbols
    List<VariableTerminalSymbol> variableSymbols = new ArrayList<>();
    for (Symbol symbol : fta.symbolToStates.keySet()) {
      if (symbol instanceof VariableTerminalSymbol) {
        variableSymbols.add((VariableTerminalSymbol) symbol);
      }
    }

    // sort all variable symbols by name
    // not necessary -- just for pretty print
    Collections.sort(variableSymbols, ((s1, s2) -> s1.symbolName.compareTo(s2.symbolName)));

    // get the Cartesian product
    List<List<Value[]>> varToInitValues = new ArrayList<>();
    for (VariableTerminalSymbol var : variableSymbols) {
      List<Value[]> initValues = new ArrayList<>();
      for (State state : fta.symbolToStates.get(var)) {
        initValues.add(state.values);
      }
      varToInitValues.add(initValues);
    }

    List<List<Value[]>> products = EnumUtil.cartesianProduct(varToInitValues);

    for (List<Value[]> product : products) {
      // NOTE: empty product is mainly caused by empty FTA
      if (product.size() == 0) continue;
      assert product.size() > 0;
      // compute the valuation
      MultiMap<VariableTerminalSymbol, Value[]> valuation = new SetMultiMap<>();
      for (int i = 0; i < product.size(); ++i) {
        valuation.put(variableSymbols.get(i), product.get(i));
      }
      // compute reachable states by constructing the FTA again
      FTA partialFTA = constructFTA(grammar, valuation);
      // set the final states of partial FTA (only starts with specific valuation)
      // by removing states that are not associated with a final value
      for (Iterator<State> iter = partialFTA.finalStates.iterator(); iter.hasNext();) {
        State state = iter.next();
        if (!isValueArrayInList(state.values, finalValues)) {
          iter.remove();
        }
      }

      // compute the cost of reaching each final state
      computeCost(partialFTA);

      // add the in-out set into ret
      for (State finalState : partialFTA.finalStates) {
        Map<VariableTerminalSymbol, Value[]> in = new HashMap<>();
        for (VariableTerminalSymbol var : valuation.keySet()) {
          Collection<Value[]> values = valuation.get(var);
          assert values.size() == 1;
          // assume the "values" set is singleton
          in.put(var, values.iterator().next());
        }
        Value[] out = finalState.values;
        ret.add(new InputOutput(in, out, finalState.minCost));
      }
    }

    return ret;
  }

  //==========================================================

}
