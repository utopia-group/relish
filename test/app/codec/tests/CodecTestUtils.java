package app.codec.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.codec.learn.DecoderDSLGrammar;
import app.codec.learn.EncoderDSLGrammar;
import relish.abs.Abstractions.StringConstant;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
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

public class CodecTestUtils {

  public static final String path = "src/app/codec/";

  public static DSLGrammarMap buildGrammarMap() {
    DSLGrammarMap grammarMap = new DSLGrammarMap();
    {
      FunctionSymbol f = grammarMap.mkFunctionSymbol("f", "String");
      f.addParameter("String", "x1");
      grammarMap.put(f, new EncoderDSLGrammar(path + "EncoderGrammar.dsl"));
    }
    {
      FunctionSymbol g = grammarMap.mkFunctionSymbol("g", "String");
      g.addParameter("String", "x1");
      grammarMap.put(g, new DecoderDSLGrammar(path + "DecoderGrammar.dsl"));
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

  // g(f(var1)) == var1
  public static RelationalProperty buildRelationalProperty1(DSLGrammarMap grammarMap) {
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    PropertyVariable var1 = new PropertyVariable("String", "var1");
    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction g1 = new PropertyFunction(g, buildList(new PropertyTerm[] { f1 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { g1, var1 }));
    RelationalProperty prop = new RelationalProperty(eq1);
    return prop;
  }

  // g(f(var1)) == var1 /\ f("Man") == "TWFu" /\ f("Ma") == "TWE=" /\ f("M") == "TQ=="
  public static RelationalProperty buildRelationalProperty2(DSLGrammarMap grammarMap) {
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    FunctionSymbol and = grammarMap.getFunctionSymbolByName("and");
    PropertyVariable var1 = new PropertyVariable("String", "var1");
    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction g1 = new PropertyFunction(g, buildList(new PropertyTerm[] { f1 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { g1, var1 }));
    PropertyConstant c1 = new PropertyConstant(new StringConstant("Man"));
    PropertyConstant c2 = new PropertyConstant(new StringConstant("TWFu"));
    PropertyFunction f2 = new PropertyFunction(f, buildList(new PropertyTerm[] { c1 }));
    PropertyFunction eq2 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f2, c2 }));
    PropertyConstant c3 = new PropertyConstant(new StringConstant("Ma"));
    PropertyConstant c4 = new PropertyConstant(new StringConstant("TWE="));
    PropertyFunction f3 = new PropertyFunction(f, buildList(new PropertyTerm[] { c3 }));
    PropertyFunction eq3 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f3, c4 }));
    PropertyConstant c5 = new PropertyConstant(new StringConstant("M"));
    PropertyConstant c6 = new PropertyConstant(new StringConstant("TQ=="));
    PropertyFunction f4 = new PropertyFunction(f, buildList(new PropertyTerm[] { c5 }));
    PropertyFunction eq4 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f4, c6 }));
    PropertyFunction and1 = new PropertyFunction(and, buildList(new PropertyTerm[] { eq1, eq2 }));
    PropertyFunction and2 = new PropertyFunction(and, buildList(new PropertyTerm[] { and1, eq3 }));
    PropertyFunction and3 = new PropertyFunction(and, buildList(new PropertyTerm[] { and2, eq4 }));
    RelationalProperty prop = new RelationalProperty(and3);
    return prop;
  }

  // g(f(var1)) == var1 /\ f("M") == "TQ=="
  public static RelationalProperty buildRelationalProperty3(DSLGrammarMap grammarMap) {
    FunctionSymbol f = grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq = grammarMap.getFunctionSymbolByName("eq");
    FunctionSymbol and = grammarMap.getFunctionSymbolByName("and");
    PropertyVariable var1 = new PropertyVariable("String", "var1");
    PropertyFunction f1 = new PropertyFunction(f, buildList(new PropertyTerm[] { var1 }));
    PropertyFunction g1 = new PropertyFunction(g, buildList(new PropertyTerm[] { f1 }));
    PropertyFunction eq1 = new PropertyFunction(eq, buildList(new PropertyTerm[] { g1, var1 }));
    PropertyConstant c1 = new PropertyConstant(new StringConstant("M"));
    PropertyConstant c2 = new PropertyConstant(new StringConstant("TQ=="));
    PropertyFunction f2 = new PropertyFunction(f, buildList(new PropertyTerm[] { c1 }));
    PropertyFunction eq2 = new PropertyFunction(eq, buildList(new PropertyTerm[] { f2, c2 }));
    PropertyFunction and1 = new PropertyFunction(and, buildList(new PropertyTerm[] { eq1, eq2 }));
    RelationalProperty prop = new RelationalProperty(and1);
    return prop;
  }

  public static <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }

}
