package app.toy.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
import relish.learn.RelationalExample;
import relish.verify.RelationalProperty;
import relish.verify.RelationalVerifier;

public class ToyRelationalVerifierTest {

  private final static String IN_FILE = "cbmc-check.c";
  private final static String OUT_FILE = "cbmc-result.xml";

  @Test
  public void testCBMC1() {
    String specFile = "specs/toy.c";
    DSLGrammarMap grammarMap = ToyTestUtils.buildGrammarMap();
    RelationalProperty property = ToyTestUtils.buildRelationalProperty1(grammarMap);
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    RelationalVerifier verifier = new RelationalVerifier(grammarMap, specFile, IN_FILE, OUT_FILE);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = new HashMap<>();
    funcSymbolToProgTexts.put(f, "Id(x1)");
    funcSymbolToProgTexts.put(g, "Multiply(Id(x1), 3)");
    funcSymbolToProgTexts.put(eq, "Eq(x1, x2)");
    long startTime = System.currentTimeMillis();
    RelationalExample counterExample = verifier.verify(funcSymbolToProgTexts, property);
    long endTime = System.currentTimeMillis();
    System.out.println("=========== Verification Time: " + (endTime - startTime) + " ms");
    Assert.assertTrue(counterExample == null);
  }

  @Test
  public void testCBMC2() {
    String specFile = "specs/toy.c";
    DSLGrammarMap grammarMap = ToyTestUtils.buildGrammarMap();
    RelationalProperty property = ToyTestUtils.buildRelationalProperty1(grammarMap);
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    RelationalVerifier verifier = new RelationalVerifier(grammarMap, specFile, IN_FILE, OUT_FILE);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = new HashMap<>();
    funcSymbolToProgTexts.put(f, "Plus(Id(x1), 1)");
    funcSymbolToProgTexts.put(g, "Multiply(Id(x1), 3)");
    funcSymbolToProgTexts.put(eq, "Eq(x1, x2)");
    long startTime = System.currentTimeMillis();
    RelationalExample counterExample = verifier.verify(funcSymbolToProgTexts, property);
    long endTime = System.currentTimeMillis();
    System.out.println("=========== Verification Time: " + (endTime - startTime) + " ms");
    System.out.println(counterExample);
    String result = "eq_0(f_0(g_0([0])), g_0(f_0([0]))) = AllTrue";
    Assert.assertTrue(counterExample.toString().equals(result));
  }

}
