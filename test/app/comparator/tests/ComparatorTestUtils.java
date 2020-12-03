package app.comparator.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.comparator.learn.ComparatorDSLGrammar;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
import relish.learn.AndDSLGrammar;
import relish.learn.EqDSLGrammar;
import relish.learn.ImplyDSLGrammar;
import relish.learn.MinusDSLGrammar;
import relish.learn.NotDSLGrammar;
import relish.learn.OrDSLGrammar;
import relish.verify.RelationalProperty;
import relish.verify.RelationalProperty.PropertyConstant;
import relish.verify.RelationalProperty.PropertyFunction;
import relish.verify.RelationalProperty.PropertyTerm;
import relish.verify.RelationalProperty.PropertyVariable;

public class ComparatorTestUtils {

  public static final String path = "src/app/comparator/";

  public static DSLGrammarMap buildGrammarMap() {

    DSLGrammarMap grammarMap = new DSLGrammarMap();

    {
      FunctionSymbol f = grammarMap.mkFunctionSymbol("f", "int");
      f.addParameter("String", "x1");
      f.addParameter("String", "x2");
      grammarMap.put(f, new ComparatorDSLGrammar(path));
    }

    {
      FunctionSymbol minus = grammarMap.mkFunctionSymbol("minus", "int");
      minus.addParameter("int", "x1");
      grammarMap.put(minus, new MinusDSLGrammar());
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

  // f(x1, x2) = -f(x2, x1) 
  public static RelationalProperty buildRelationalProperty1(DSLGrammarMap grammarMap) {

    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol minus = grammarMap.getFunctionSymbolByName("minus");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");

    PropertyVariable x1 = new PropertyVariable("String", "x1");
    PropertyVariable x2 = new PropertyVariable("String", "x2");

    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { x1, x2, }));
    PropertyFunction f2 = new PropertyFunction(f, buildList(new PropertyTerm[] { x2, x1, }));
    PropertyFunction minusf2 = new PropertyFunction(minus, buildList(new PropertyTerm[] { f2 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f1, minusf2, }));

    RelationalProperty prop = new RelationalProperty(eq1);
    return prop;

  }

  // IntLt(CountChar(x1, 'A'), CountChar(x2, 'A')) 

  // f("A", "BC") = -1 
  // f("A", "AA") = 1 

  public static RelationalProperty buildRelationalProperty2(DSLGrammarMap grammarMap) {

    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol minus = grammarMap.getFunctionSymbolByName("minus");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    FunctionSymbol and = grammarMap.getFunctionSymbolByName("and");

    PropertyVariable x1 = new PropertyVariable("String", "x1");
    PropertyVariable x2 = new PropertyVariable("String", "x2");

    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { x1, x2, }));
    PropertyFunction f2 = new PropertyFunction(f, buildList(new PropertyTerm[] { x2, x1, }));
    PropertyFunction minusf2 = new PropertyFunction(minus, buildList(new PropertyTerm[] { f2 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f1, minusf2, }));

    PropertyFunction and1 = null;
    {
      PropertyFunction f3 = new PropertyFunction(f,
          buildList(new PropertyTerm[] { new PropertyConstant(new StringConstant("A")), new PropertyConstant(new StringConstant("BC")), }));
      PropertyFunction eq2 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f3, new PropertyConstant(new IntConstant(-1)), }));
      and1 = new PropertyFunction(and, buildList(new PropertyTerm[] { eq1, eq2 }));
    }

    PropertyFunction and2 = null;
    {
      and2 = new PropertyFunction(and, buildList(new PropertyTerm[] { and1, eq1 }));
    }

    PropertyFunction and3 = null;
    {
      PropertyFunction f3 = new PropertyFunction(f,
          buildList(new PropertyTerm[] { new PropertyConstant(new StringConstant("A")), new PropertyConstant(new StringConstant("AA")), }));
      PropertyFunction eq2 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f3, new PropertyConstant(new IntConstant(1)), }));
      and3 = new PropertyFunction(and, buildList(new PropertyTerm[] { and2, eq2 }));
    }

    RelationalProperty prop = new RelationalProperty(and3);

    return prop;

  }

  public static <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

}
