package app.codec.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import relish.abs.Abstractions.StringConstant;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.fta.ProgramTree;
import relish.learn.ComposedCFTALearner;
import relish.learn.RelationalExample;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;
import relish.util.PrintUtil;

public class CodecComposedCFTALearnerTest {

  private final DSLGrammarMap grammarMap = CodecTestUtils.buildGrammarMap();
  private FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
  private FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
  private FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
  private FunctionSymbol and = grammarMap.getFunctionSymbolByName("and");

  @Test
  public void testComposedCFTALearner() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildParametricExample1("Man"));
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples1() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildParametricExample1("Man"));
    examples.add(buildParametricExample1("M"));
    examples.add(buildParametricExample2("Man", "TWFu"));
    examples.add(buildParametricExample2("M", "TQ=="));
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("PadToMultiple(Id2(Enc64(Reshape(Id1(x1), 6))), 4, '=')"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Id2(LSBReshape(Id2(Dec64(RemovePad(Id1(x1), '='))), 6))"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples2() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildParametricExample1("Man"));
    examples.add(buildParametricExample2("Man", "JVQW4==="));
    examples.add(buildParametricExample2("M", "JU======"));
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("PadToMultiple(Id2(Enc32(Reshape(Id1(x1), 5))), 8, '=')"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Id2(LSBReshape(Id2(Dec32(RemovePad(Id1(x1), '='))), 5))"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  @Test
  public void testComposedCFTALearnerWithMultiExamples3() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildParametricExample1("Man"));
    examples.add(buildParametricExample2("Man", "4D616E"));
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = programTreesToTexts(funcSymbolToProgTrees);
    PrintUtil.printMap(funcSymbolToProgTexts);
    Assert.assertTrue(funcSymbolToProgTexts.size() == 3);
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id2(Enc16(Reshape(Id1(x1), 4)))"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Id2(LSBReshape(Id2(Dec16(Id1(x1))), 4))"));
    Assert.assertTrue(funcSymbolToProgTexts.get(eq).equals("Eq(x1, x2)"));
  }

  // g(f(x1)) = x1
  private RelationalExample buildParametricExample1(String x1) {
    ExampleConstant var1 = new ExampleConstant(new StringConstant(x1));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { var1 }));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 0), buildList(new ExampleTerm[] { f1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { g1, var1 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // f(x1) = x2
  private RelationalExample buildParametricExample2(String x1, String x2) {
    ExampleConstant var1 = new ExampleConstant(new StringConstant(x1));
    ExampleConstant var2 = new ExampleConstant(new StringConstant(x2));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { var1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { f1, var2 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // g(f(x1)) = x1 /\ f(x2) = x3
  public RelationalExample buildParametricExample3(String x1, String x2, String x3) {
    ExampleConstant var1 = new ExampleConstant(new StringConstant(x1));
    ExampleConstant var2 = new ExampleConstant(new StringConstant(x2));
    ExampleConstant var3 = new ExampleConstant(new StringConstant(x3));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { var1 }));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 0), buildList(new ExampleTerm[] { f1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { g1, var1 }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f, 0), buildList(new ExampleTerm[] { var2 }));
    ExampleFunction eq2 = new ExampleFunction(new FunctionOccurrence(eq, 0), buildList(new ExampleTerm[] { f2, var3 }));
    ExampleFunction and1 = new ExampleFunction(new FunctionOccurrence(and, 0), buildList(new ExampleTerm[] { eq1, eq2 }));
    RelationalExample example = new RelationalExample(and1);
    return example;
  }

  private <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

  private Map<FunctionSymbol, String> programTreesToTexts(Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees) {
    Map<FunctionSymbol, String> funcSymbolToProgTexts = new HashMap<>();
    for (FunctionSymbol funcSymbol : funcSymbolToProgTrees.keySet()) {
      ProgramTree progTree = funcSymbolToProgTrees.get(funcSymbol);
      funcSymbolToProgTexts.put(funcSymbol, progTree.translateToProgramText());
    }
    return funcSymbolToProgTexts;
  }

}
