package relish.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
import relish.verify.RelationalProperty;
import relish.verify.RelationalProperty.PropertyFunction;
import relish.verify.RelationalProperty.PropertyTerm;
import relish.verify.RelationalProperty.PropertyVariable;

public class RelationalPropertyTests {

  @Test
  public void testPropertyToCBMCText() {
    DSLGrammarMap grammarMap = buildDummyGrammarMap();
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    PropertyVariable var1 = new PropertyVariable("int", "var1");
    PropertyFunction g1 = new PropertyFunction(g, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { g1 }));
    PropertyFunction f2 = new PropertyFunction(f, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction g2 = new PropertyFunction(g, buildList(new PropertyTerm[] { f2 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f1, g2 }));
    RelationalProperty prop = new RelationalProperty(eq1);
    String propCBMCText = "(f(g(var1)) == g(f(var1)))";
    System.out.println(prop.toCMBCText());
    Assert.assertTrue(prop.toCMBCText().equals(propCBMCText));
  }

  // build a dummy grammar map that only contains function symbols
  private DSLGrammarMap buildDummyGrammarMap() {
    DSLGrammarMap grammarMap = new DSLGrammarMap();
    {
      FunctionSymbol f = grammarMap.mkFunctionSymbol("f", "int");
      f.addParameter("int", "x1");
    }
    {
      FunctionSymbol g = grammarMap.mkFunctionSymbol("g", "int");
      g.addParameter("int", "x1");
    }
    {
      FunctionSymbol eq = grammarMap.mkFunctionSymbol("eq", "bool");
      eq.addParameter("Poly", "x1");
      eq.addParameter("Poly", "x2");
    }
    return grammarMap;
  }

  private List<PropertyTerm> buildList(PropertyTerm[] array) {
    return Arrays.asList(array);
  }

}
