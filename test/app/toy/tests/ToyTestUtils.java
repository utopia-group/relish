package app.toy.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.toy.learn.ToyMultiplyDSLGrammar;
import app.toy.learn.ToyPlusDSLGrammar;
import relish.abs.Abstractions.IntConstant;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
import relish.fta.ProgramTree;
import relish.learn.AndDSLGrammar;
import relish.learn.EqDSLGrammar;
import relish.learn.ImplyDSLGrammar;
import relish.learn.NotDSLGrammar;
import relish.learn.OrDSLGrammar;
import relish.verify.RelationalProperty;
import relish.verify.RelationalProperty.PropertyConstant;
import relish.verify.RelationalProperty.PropertyFunction;
import relish.verify.RelationalProperty.PropertyTerm;
import relish.verify.RelationalProperty.PropertyVariable;

public class ToyTestUtils {

  public static final String path = "src/app/toy/";

  public static DSLGrammarMap buildGrammarMap() {
    DSLGrammarMap grammarMap = new DSLGrammarMap();
    {
      FunctionSymbol f = grammarMap.mkFunctionSymbol("f", "int");
      f.addParameter("int", "x1");
      grammarMap.put(f, new ToyPlusDSLGrammar(path + "PlusGrammar.dsl"));
    }
    {
      FunctionSymbol g = grammarMap.mkFunctionSymbol("g", "int");
      g.addParameter("int", "x1");
      grammarMap.put(g, new ToyMultiplyDSLGrammar(path + "MultiplyGrammar.dsl"));
    }
    {
      FunctionSymbol eq = grammarMap.mkFunctionSymbol("eq", "bool");
      eq.addParameter("Poly", "x1");
      eq.addParameter("Poly", "x2");
      grammarMap.put(eq, new EqDSLGrammar());
    }
    {
      FunctionSymbol and = grammarMap.mkFunctionSymbol("and", "bool");
      and.addParameter("bool", "x1");
      and.addParameter("bool", "x2");
      grammarMap.put(and, new AndDSLGrammar());
    }
    {
      FunctionSymbol or = grammarMap.mkFunctionSymbol("or", "bool");
      or.addParameter("bool", "x1");
      or.addParameter("bool", "x2");
      grammarMap.put(or, new OrDSLGrammar());
    }
    {
      FunctionSymbol imply = grammarMap.mkFunctionSymbol("imply", "bool");
      imply.addParameter("bool", "x1");
      imply.addParameter("bool", "x2");
      grammarMap.put(imply, new ImplyDSLGrammar());
    }
    {
      FunctionSymbol not = grammarMap.mkFunctionSymbol("not", "bool");
      not.addParameter("bool", "x1");
      grammarMap.put(not, new NotDSLGrammar());
    }
    return grammarMap;
  }

  // f(g(var1)) == g(f(var1))
  public static RelationalProperty buildRelationalProperty1(DSLGrammarMap grammarMap) {
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
    return prop;
  }

  // f(g(var1)) == g(f(var1)) && f(g(1)) = 72
  public static RelationalProperty buildRelationalProperty2(DSLGrammarMap grammarMap) {
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    FunctionSymbol and = grammarMap.getFunctionSymbolByName("and");
    PropertyVariable var1 = new PropertyVariable("int", "var1");
    PropertyConstant one = new PropertyConstant(new IntConstant(1));
    PropertyConstant c72 = new PropertyConstant(new IntConstant(72));
    PropertyFunction g1 = new PropertyFunction(g, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { g1 }));
    PropertyFunction f2 = new PropertyFunction(f, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction g2 = new PropertyFunction(g, buildList(new PropertyTerm[] { f2 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f1, g2 }));
    PropertyFunction g3 = new PropertyFunction(g, buildList(new PropertyTerm[] { one }));
    PropertyFunction f3 = new PropertyFunction(f, buildList(new PropertyTerm[] { g3 }));
    PropertyFunction eq2 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f3, c72 }));
    PropertyFunction and1 = new PropertyFunction(and, buildList(new PropertyTerm[] { eq1, eq2 }));
    RelationalProperty prop = new RelationalProperty(and1);
    return prop;
  }

  public static <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

  public static Map<FunctionSymbol, String> programTreesToTexts(Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees) {
    Map<FunctionSymbol, String> funcSymbolToProgTexts = new HashMap<>();
    for (FunctionSymbol funcSymbol : funcSymbolToProgTrees.keySet()) {
      ProgramTree progTree = funcSymbolToProgTrees.get(funcSymbol);
      funcSymbolToProgTexts.put(funcSymbol, progTree.translateToProgramText());
    }
    return funcSymbolToProgTexts;
  }

}
