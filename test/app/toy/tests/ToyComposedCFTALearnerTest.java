package app.toy.tests;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.DSLGrammar;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.dsl.VariableTerminalSymbol;
import relish.fta.ComposedFTA;
import relish.fta.FTA;
import relish.fta.ProgramTree;
import relish.learn.ComposedCFTALearner;
import relish.learn.InputOutput;
import relish.learn.RelationalExample;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;
import relish.util.PrintUtil;
import relish.util.SetMultiMap;

public class ToyComposedCFTALearnerTest {

  private final DSLGrammarMap grammarMap = ToyTestUtils.buildGrammarMap();
  private FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
  private FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
  private FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
  private FunctionSymbol and = grammarMap.getFunctionSymbolByName("and");

  @Test
  public void testConstructFTA() {
    DSLGrammar grammar = grammarMap.get(f);
    SetMultiMap<VariableTerminalSymbol, Value[]> valuations = new SetMultiMap<>();
    VariableTerminalSymbol var = (VariableTerminalSymbol) grammar.getSymbolFromName("x1");
    valuations.put(var, new Value[] { new IntConstant(1) });
    valuations.put(var, new Value[] { new IntConstant(2) });
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    FTA fta = learner.constructFTA(grammar, valuations);
    // System.out.println(fta.dumpStates());
    // System.out.println(fta.dumpTransitions());
    Assert.assertTrue(fta.symbolToStates.size() == 8);
    Assert.assertTrue(fta.transitions.size() == 6);
  }

  @Test
  public void testConstructComposedCFTA1() {
    RelationalExample example = buildRelationalExample1();
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    long startTime = System.currentTimeMillis();
    ComposedFTA result = learner.constructComposedFTA(example);
    long endTime = System.currentTimeMillis();
    System.out.println("============Composed CFTA============");
    System.out.println(example);
    System.out.println(result);
    System.out.println("Construction Time: " + (endTime - startTime) + " ms");
    FunctionOccurrence f1 = new FunctionOccurrence(f, 1);
    FunctionOccurrence g1 = new FunctionOccurrence(g, 1);
    FunctionOccurrence eq1 = new FunctionOccurrence(eq, 1);
    FTA ftaG1 = result.getFTA(g1);
    FTA ftaF1 = result.getFTA(f1);
    FTA ftaEq1 = result.getFTA(eq1);
    Assert.assertTrue(result.numOfFTAs() == 3);
    Assert.assertTrue(result.finalOccurrence.equals(eq1));
    Assert.assertTrue(ftaG1.numOfStates() == 6);
    Assert.assertTrue(ftaG1.numOfTransitions() == 3);
    Assert.assertTrue(ftaG1.numOfProgs().equals(new BigInteger("3")));
    Assert.assertTrue(ftaF1.numOfStates() == 8);
    Assert.assertTrue(ftaF1.numOfTransitions() == 6);
    Assert.assertTrue(ftaF1.numOfProgs().equals(new BigInteger("4")));
    Assert.assertTrue(ftaEq1.numOfStates() == 3);
    Assert.assertTrue(ftaEq1.numOfTransitions() == 1);
    Assert.assertTrue(ftaEq1.numOfProgs().equals(new BigInteger("1")));
    Assert.assertTrue(result.numOfEpsilonTransitions() == 4);
  }

  @Test
  public void testConstructComposedCFTA2() {
    RelationalExample example = buildRelationalExample2();
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    long startTime = System.currentTimeMillis();
    ComposedFTA result = learner.constructComposedFTA(example);
    long endTime = System.currentTimeMillis();
    System.out.println("============Composed CFTA============");
    System.out.println(example);
    System.out.println(result);
    System.out.println("Construction Time: " + (endTime - startTime) + " ms");
    FunctionOccurrence eq1 = new FunctionOccurrence(eq, 1);
    FTA ftaG1 = result.getFTA(new FunctionOccurrence(g, 1));
    FTA ftaF1 = result.getFTA(new FunctionOccurrence(f, 1));
    FTA ftaF2 = result.getFTA(new FunctionOccurrence(f, 2));
    FTA ftaG2 = result.getFTA(new FunctionOccurrence(g, 2));
    FTA ftaEq1 = result.getFTA(eq1);
    Assert.assertTrue(result.numOfFTAs() == 5);
    Assert.assertTrue(result.finalOccurrence.equals(eq1));
    Assert.assertTrue(ftaG1.numOfStates() == 6);
    Assert.assertTrue(ftaG1.numOfTransitions() == 3);
    Assert.assertTrue(ftaG1.numOfProgs().equals(new BigInteger("3")));
    Assert.assertTrue(ftaF1.numOfStates() == 9);
    Assert.assertTrue(ftaF1.numOfTransitions() == 8);
    Assert.assertTrue(ftaF1.numOfProgs().equals(new BigInteger("13")));
    Assert.assertTrue(ftaF2.numOfStates() == 6);
    Assert.assertTrue(ftaF2.numOfTransitions() == 3);
    Assert.assertTrue(ftaF2.numOfProgs().equals(new BigInteger("3")));
    Assert.assertTrue(ftaG2.numOfStates() == 9);
    Assert.assertTrue(ftaG2.numOfTransitions() == 6);
    Assert.assertTrue(ftaG2.numOfProgs().equals(new BigInteger("7")));
    Assert.assertTrue(ftaEq1.numOfStates() == 9);
    Assert.assertTrue(ftaEq1.numOfTransitions() == 4);
    Assert.assertTrue(ftaEq1.numOfProgs().equals(new BigInteger("4")));
    Assert.assertTrue(result.numOfEpsilonTransitions() == 14);
  }

  @Test
  public void testProductFTAs() {
    RelationalExample example = buildRelationalExample3();
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    long startTime1 = System.currentTimeMillis();
    ComposedFTA result = learner.constructComposedFTA(example);
    long endTime1 = System.currentTimeMillis();
    FTA ftaF1 = result.getFTA(new FunctionOccurrence(f, 1));
    FTA ftaF2 = result.getFTA(new FunctionOccurrence(f, 2));
    long startTime2 = System.currentTimeMillis();
    List<FTA> ftas = new ArrayList<>(Arrays.asList(new FTA[] { ftaF1, ftaF2 }));
    FTA product = learner.productFTAs(grammarMap.get(f), ftas);
    long endTime2 = System.currentTimeMillis();
    System.out.println(result);
    System.out.println("Construction Time: " + (endTime1 - startTime1) + " ms");
    System.out.println("=========== f_2 f_1 Product FTA ===========");
    System.out.println(product);
    System.out.println("Product time: " + (endTime2 - startTime2) + " ms");
    Assert.assertTrue(product.numOfStates() == 11);
    Assert.assertTrue(product.numOfTransitions() == 6);
  }

  @Test
  public void testReachableInOutSets() {
    DSLGrammar grammar = grammarMap.get(eq);
    SetMultiMap<VariableTerminalSymbol, Value[]> valuations = new SetMultiMap<>();
    VariableTerminalSymbol x1 = (VariableTerminalSymbol) grammar.getSymbolFromName("x1");
    VariableTerminalSymbol x2 = (VariableTerminalSymbol) grammar.getSymbolFromName("x2");
    valuations.put(x1, new Value[] { new IntConstant(1) });
    valuations.put(x1, new Value[] { new IntConstant(2) });
    valuations.put(x2, new Value[] { new IntConstant(1) });
    valuations.put(x2, new Value[] { new IntConstant(2) });
    valuations.put(x2, new Value[] { new IntConstant(3) });
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    FTA fta = learner.constructFTA(grammar, valuations);
    System.out.println(fta.dumpStates());
    System.out.println(fta.dumpTransitions());
    Collection<InputOutput> inOutSets = learner.computeReachableInOutSets(grammar, fta);
    Assert.assertTrue(inOutSets.size() == 6);
    inOutSets.forEach((inout) -> System.out.println(inout));
  }

  @Test
  public void testComputeFeasiblePaths() {
    RelationalExample example = buildRelationalExample1();
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    List<ComposedFTA> composedFTAs = new ArrayList<>();
    composedFTAs.add(learner.constructComposedFTA(example));

    @SuppressWarnings("deprecation")
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgs = learner.computeFeasiblePaths(composedFTAs);

    if (funcSymbolToProgs.size() == 0) {
      System.out.println("No Solution");
    } else {
      for (FunctionSymbol funcSymbol : funcSymbolToProgs.keySet()) {
        System.out.print(funcSymbol + ": ");
        System.out.println(funcSymbolToProgs.get(funcSymbol).translateToProgramText());
      }
    }
    Assert.assertTrue(funcSymbolToProgs.size() == 3);
    boolean isFIdentity = funcSymbolToProgs.get(f).translateToProgramText().equals("Id(x1)");
    boolean isGIdentity = funcSymbolToProgs.get(g).translateToProgramText().equals("Id(x1)");
    Assert.assertTrue(isFIdentity || isGIdentity);
    Assert.assertTrue(funcSymbolToProgs.get(eq).translateToProgramText().equals("Eq(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearner1() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildRelationalExample4());
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = ToyTestUtils.programTreesToTexts(funcSymbolToProgTrees);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 4);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id(x1)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Multiply(Id(x1), 2)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(and).equals("And(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearner2() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildRelationalExample3());
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = ToyTestUtils.programTreesToTexts(funcSymbolToProgTrees);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 2);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id(x1)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples1() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildParametricExample1(1, 1));
    examples.add(buildParametricExample2(3, 6));
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = ToyTestUtils.programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id(x1)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Multiply(Id(x1), 2)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples2() {
    List<RelationalExample> examples = new ArrayList<>();
    // stress test
    int num = 100;
    for (int i = 1; i <= num; ++i) {
      examples.add(buildParametricExample1(i, i));
      examples.add(buildParametricExample2(i, 3 * i));
      examples.add(buildParametricExample3(2 * i, 6 * i));
    }
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = ToyTestUtils.programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id(x1)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Multiply(Id(x1), 3)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples3() {
    List<RelationalExample> examples = new ArrayList<>();
    int num = 100;
    for (int i = 1; i <= num; ++i) {
      examples.add(buildParametricExample1(i, i));
      examples.add(buildParametricExample2(i, 3 * i));
      examples.add(buildParametricExample3(2 * i, 5 * i));
    }
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = ToyTestUtils.programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 0);
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples4() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildParametricExample1(1, 1));
    examples.add(buildParametricExample1(2, 2));
    examples.add(buildParametricExample1(3, 3));
    examples.add(buildParametricExample2(3, 6));
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    // handle different number of examples for different properties
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = ToyTestUtils.programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id(x1)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Multiply(Id(x1), 2)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  // f_1(g_1(1)) = 3
  private RelationalExample buildRelationalExample1() {
    ExampleConstant one = new ExampleConstant(new IntConstant(1));
    ExampleConstant three = new ExampleConstant(new IntConstant(3));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 1), buildList(new ExampleTerm[] { one }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 1), buildList(new ExampleTerm[] { g1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 1), buildList(new ExampleTerm[] { f1, three }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // f_1(g_1(1)) = g_2(f_2(1))
  private RelationalExample buildRelationalExample2() {
    ExampleConstant one = new ExampleConstant(new IntConstant(1));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 1), buildList(new ExampleTerm[] { one }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 1), buildList(new ExampleTerm[] { g1 }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f, 2), buildList(new ExampleTerm[] { one }));
    ExampleFunction g2 = new ExampleFunction(new FunctionOccurrence(g, 2), buildList(new ExampleTerm[] { f2 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 1), buildList(new ExampleTerm[] { f1, g2 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // f_1(f_2(1)) = f_3(1)
  private RelationalExample buildRelationalExample3() {
    ExampleConstant one = new ExampleConstant(new IntConstant(1));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f, 2), buildList(new ExampleTerm[] { one }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 1), buildList(new ExampleTerm[] { f2 }));
    ExampleFunction f3 = new ExampleFunction(new FunctionOccurrence(f, 3), buildList(new ExampleTerm[] { one }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 1), buildList(new ExampleTerm[] { f1, f3 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // f_1(g_1(1)) = g_2(f_2(1)) /\ f_3(g_3(3)) = 6
  private RelationalExample buildRelationalExample4() {
    ExampleConstant one = new ExampleConstant(new IntConstant(1));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 1), buildList(new ExampleTerm[] { one }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 1), buildList(new ExampleTerm[] { g1 }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f, 2), buildList(new ExampleTerm[] { one }));
    ExampleFunction g2 = new ExampleFunction(new FunctionOccurrence(g, 2), buildList(new ExampleTerm[] { f2 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 1), buildList(new ExampleTerm[] { f1, g2 }));
    ExampleConstant three = new ExampleConstant(new IntConstant(3));
    ExampleConstant six = new ExampleConstant(new IntConstant(6));
    ExampleFunction g3 = new ExampleFunction(new FunctionOccurrence(g, 3), buildList(new ExampleTerm[] { three }));
    ExampleFunction f3 = new ExampleFunction(new FunctionOccurrence(f, 3), buildList(new ExampleTerm[] { g3 }));
    ExampleFunction eq2 = new ExampleFunction(new FunctionOccurrence(eq, 2), buildList(new ExampleTerm[] { f3, six }));
    ExampleFunction and1 = new ExampleFunction(new FunctionOccurrence(and, 1), buildList(new ExampleTerm[] { eq1, eq2 }));
    RelationalExample example = new RelationalExample(and1);
    return example;
  }

  // f(g(x1)) = g(f(x2))
  private RelationalExample buildParametricExample1(int x1, int x2) {
    ExampleConstant var1 = new ExampleConstant(new IntConstant(x1));
    ExampleConstant var2 = new ExampleConstant(new IntConstant(x2));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 0), buildList(new ExampleTerm[] { var1 }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { g1 }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { var2 }));
    ExampleFunction g2 = new ExampleFunction(new FunctionOccurrence(g, 0), buildList(new ExampleTerm[] { f2 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { f1, g2 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // f(g(x1)) = x2
  private RelationalExample buildParametricExample2(int x1, int x2) {
    ExampleConstant var1 = new ExampleConstant(new IntConstant(x1));
    ExampleConstant var2 = new ExampleConstant(new IntConstant(x2));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 0), buildList(new ExampleTerm[] { var1 }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { g1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { f1, var2 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // g(f(x1)) = x2
  private RelationalExample buildParametricExample3(int x1, int x2) {
    ExampleConstant var1 = new ExampleConstant(new IntConstant(x1));
    ExampleConstant var2 = new ExampleConstant(new IntConstant(x2));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { var1 }));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 0), buildList(new ExampleTerm[] { f1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { g1, var2 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  private <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

}
