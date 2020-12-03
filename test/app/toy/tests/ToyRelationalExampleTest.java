package app.toy.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import relish.abs.Abstractions.IntConstant;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.learn.RelationalExample;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;

public class ToyRelationalExampleTest {

  private final DSLGrammarMap grammarMap = ToyTestUtils.buildGrammarMap();
  private FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
  private FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
  private FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");

  @Test
  public void testRelationalExampleMerge() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildRelationalExample1(1, 1));
    examples.add(buildRelationalExample1(1, 0));
    examples.add(buildRelationalExample1(0, 1));
    examples.add(buildRelationalExample1(0, 0));
    List<RelationalExample> mergedExamples = RelationalExample.mergeExamples(examples);
    Assert.assertTrue(mergedExamples.size() == 1);
    String result = "eq_1(f_1(g_1([1, 1, 0, 0])), g_2(f_2([1, 0, 1, 0]))) = AllTrue";
    Assert.assertTrue(mergedExamples.get(0).toString().equals(result));
  }

  @Test
  public void testRelationalExampleRenameOccurrence() {
    List<RelationalExample> examples = new ArrayList<>();
    examples.add(buildRelationalExample1(1, 1));
    examples.add(buildRelationalExample2(1, 0));
    examples.add(buildRelationalExample1(0, 1));
    examples.add(buildRelationalExample2(0, 0));
    List<RelationalExample> mergedExamples = RelationalExample.mergeExamples(examples);
    Assert.assertTrue(mergedExamples.size() == 1);
    String result = "eq_1(f_1(g_1([1, 1, 0, 0])), g_2(f_2([1, 0, 1, 0]))) = AllTrue";
    Assert.assertTrue(mergedExamples.get(0).toString().equals(result));
  }

  @Test
  public void testRelationalExampleEquals() {
    RelationalExample example1 = buildRelationalExample1(1, 1);
    RelationalExample example2 = buildRelationalExample1(1, 1);
    Assert.assertTrue(example1.equals(example2));
  }

  // f_1(g_1(x1)) = g_2(f_2(x2))
  private RelationalExample buildRelationalExample1(int var1, int var2) {
    ExampleConstant x1 = new ExampleConstant(new IntConstant(var1));
    ExampleConstant x2 = new ExampleConstant(new IntConstant(var2));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g, 1), buildList(new ExampleTerm[] { x1 }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f, 1), buildList(new ExampleTerm[] { g1 }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f, 2), buildList(new ExampleTerm[] { x2 }));
    ExampleFunction g2 = new ExampleFunction(new FunctionOccurrence(g, 2), buildList(new ExampleTerm[] { f2 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq, 1), buildList(new ExampleTerm[] { f1, g2 }));
    RelationalExample example = new RelationalExample(eq1);
    return example;
  }

  // f_3(g_4(x1)) = g_3(f_4(x2))
  private RelationalExample buildRelationalExample2(int var1, int var2) {
    ExampleConstant x1 = new ExampleConstant(new IntConstant(var1));
    ExampleConstant x2 = new ExampleConstant(new IntConstant(var2));
    ExampleFunction g3 = new ExampleFunction(new FunctionOccurrence(g, 3), buildList(new ExampleTerm[] { x1 }));
    ExampleFunction f3 = new ExampleFunction(new FunctionOccurrence(f, 3), buildList(new ExampleTerm[] { g3 }));
    ExampleFunction f4 = new ExampleFunction(new FunctionOccurrence(f, 4), buildList(new ExampleTerm[] { x2 }));
    ExampleFunction g4 = new ExampleFunction(new FunctionOccurrence(g, 4), buildList(new ExampleTerm[] { f4 }));
    ExampleFunction eq9 = new ExampleFunction(new FunctionOccurrence(eq, 9), buildList(new ExampleTerm[] { f3, g4 }));
    RelationalExample example = new RelationalExample(eq9);
    return example;
  }

  private <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

}
