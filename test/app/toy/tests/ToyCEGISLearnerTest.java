package app.toy.tests;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
import relish.learn.CEGISLearner;
import relish.util.PrintUtil;
import relish.verify.RelationalProperty;
import relish.verify.RelationalVerifier;

public class ToyCEGISLearnerTest {

  private final static String IN_FILE = "cbmc-check.c";
  private final static String OUT_FILE = "cbmc-result.xml";

  @Test
  public void testToyCEGISLearner1() {
    String specFile = "specs/toy.c";
    DSLGrammarMap grammarMap = ToyTestUtils.buildGrammarMap();
    RelationalProperty property = ToyTestUtils.buildRelationalProperty1(grammarMap);
    RelationalVerifier verifier = new RelationalVerifier(grammarMap, specFile, IN_FILE, OUT_FILE);
    CEGISLearner learner = new CEGISLearner(grammarMap, verifier);
    Map<FunctionSymbol, String> funcSymbolToProgTexts = learner.cegisLearn(property);
    PrintUtil.printMap(funcSymbolToProgTexts);
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    Assert.assertTrue(funcSymbolToProgTexts.get(f).equals("Id(x1)"));
    Assert.assertTrue(funcSymbolToProgTexts.get(g).equals("Id(x1)"));
  }

}
