package app.comparator.exp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import app.Benchmark;
import app.comparator.learn.ComparatorDSLGrammar;
import app.comparator.learn.ComparatorDSLSemantics;
import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.dsl.Production;
import relish.eval.DSLAstNode;
import relish.eval.DSLAstNode.CharLiteralNode;
import relish.eval.DSLAstNode.FunctionNode;
import relish.eval.DSLAstNode.IntLiteralNode;
import relish.eval.DSLAstNode.StringLiteralNode;
import relish.eval.DSLAstNode.VariableNode;
import relish.eval.DSLInterpreter;
import relish.fta.ProgramTree;
import relish.learn.AndDSLGrammar;
import relish.learn.ComposedCFTALearner;
import relish.learn.EqDSLGrammar;
import relish.learn.ImplyDSLGrammar;
import relish.learn.MinusDSLGrammar;
import relish.learn.RelationalExample;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;
import relish.verify.RelationalProperty;

public class ComparatorBenchmark extends Benchmark {

  // each example is of the form (x, y, return) 
  public List<Triplet<String, String, Integer>> allIOExamples = new ArrayList<>();

  // map from function names to function symbols
  public Map<String, FunctionSymbol> funcNameToFuncSymbols;

  // map from DSL construct names to objects, for interpreter
  public Map<String, Production> nameToDslConstructs;

  // relational specification 
  public RelationalProperty property;

  // path to .c file, for CBMC  
  public String cFile;

  //
  public DSLGrammarMap grammarMap;

  // 
  // logging information 
  // 

  public String learnt_program;

  //
  //
  //

  public ComparatorBenchmark(String benchmarkName, String benchmarkFilePath, String logFilePath) {
    super(benchmarkName, logFilePath);
    parseAllIOExamples(benchmarkFilePath);
    this.funcNameToFuncSymbols = buildFunctionSymbolMap();
    this.nameToDslConstructs = ComparatorDSLSemantics.getDSLConstructMap();
    this.property = null;
    this.cFile = null;
    this.grammarMap = buildGrammarMap();
  }

  @Override
  public Set<RelationalExample> getFirstExample() {
    Triplet<String, String, Integer> e = this.allIOExamples.get(0);
    Set<RelationalExample> ret = new HashSet<>();
    ret.add(buildIOExample(e));
    return ret;
  }

  @Override
  public void succ(Map<FunctionSymbol, String> funcSymbolToProgTexts) {
    this.learnt_program = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("f"));
  }

  public void parseAllIOExamples(String benchmarkFilePath) {

    try (BufferedReader br = new BufferedReader(new FileReader(new File(benchmarkFilePath)))) {

      // Parse I/O examples 
      {

        String line = br.readLine();
        for (; line.isEmpty() || line.startsWith("//"); line = br.readLine())
          ;
        for (; line != null && !line.isEmpty(); line = br.readLine()) {
          if (line.startsWith("//")) continue;
          String[] ss = line.split("\"");
          String x1 = ss[1];
          String x2 = ss[3];
          int v = Integer.parseInt(ss[5]);
          Triplet<String, String, Integer> e = Triplet.with(x1, x2, v);
          this.allIOExamples.add(e);
        }

      }

    } catch (Exception exception) {

      exception.printStackTrace();

    }

  }

  // An I/O example has to be of the form: f(x1, x2) = v 
  public RelationalExample buildIOExample(Triplet<String, String, Integer> e) {

    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");

    ExampleConstant x1 = new ExampleConstant(new StringConstant(e.getValue0()));
    ExampleConstant x2 = new ExampleConstant(new StringConstant(e.getValue1()));
    ExampleConstant v = new ExampleConstant(new IntConstant(e.getValue2()));

    ExampleFunction f = new ExampleFunction(new FunctionOccurrence(f_symbol, 0), buildList(new ExampleTerm[] { x1, x2, }));
    ExampleFunction eq = new ExampleFunction(new FunctionOccurrence(eq_symbol, 0), buildList(new ExampleTerm[] { f, v, }));

    RelationalExample ret = new RelationalExample(eq);

    return ret;
  }

  public Map<String, FunctionSymbol> buildFunctionSymbolMap() {
    Map<String, FunctionSymbol> funcNameToFuncSymbols = new HashMap<>();
    {
      FunctionSymbol f = new FunctionSymbol("f", "String");
      f.addParameter("String", "x1");
      funcNameToFuncSymbols.put("f", f);
    }
    {
      FunctionSymbol minus = new FunctionSymbol("minus", "int");
      minus.addParameter("int", "x1");
      funcNameToFuncSymbols.put("minus", minus);
    }
    {
      FunctionSymbol eq = new FunctionSymbol("eq", "bool");
      eq.addParameter("Poly", "x1");
      eq.addParameter("Poly", "x2");
      funcNameToFuncSymbols.put("eq", eq);
    }
    {
      FunctionSymbol and = new FunctionSymbol("and", "bool");
      and.addParameter("bool", "x1");
      and.addParameter("bool", "x2");
      funcNameToFuncSymbols.put("and", and);
    }
    {
      FunctionSymbol imply = new FunctionSymbol("imply", "bool");
      imply.addParameter("bool", "x1");
      imply.addParameter("bool", "x2");
      funcNameToFuncSymbols.put("imply", imply);
    }
    return funcNameToFuncSymbols;
  }

  public DSLGrammarMap buildGrammarMap() {

    DSLGrammarMap ret = new DSLGrammarMap();

    {
      FunctionSymbol f_symbol = ret.mkFunctionSymbol("f", "int");
      f_symbol.addParameter("String", "x1");
      f_symbol.addParameter("String", "x2");
      ret.put(f_symbol, new ComparatorDSLGrammar(null));
    }

    {
      FunctionSymbol minus_symbol = ret.mkFunctionSymbol("minus", "int");
      minus_symbol.addParameter("int", "x1");
      ret.put(minus_symbol, new MinusDSLGrammar());
    }

    {
      FunctionSymbol eq_symbol = ret.mkFunctionSymbol("eq", "bool");
      eq_symbol.addParameter("Poly", "x1");
      eq_symbol.addParameter("Poly", "x2");
      ret.put(eq_symbol, new EqDSLGrammar());
    }

    {
      FunctionSymbol and_symbol = ret.mkFunctionSymbol("and", "bool");
      and_symbol.addParameter("bool", "x1");
      and_symbol.addParameter("bool", "x2");
      ret.put(and_symbol, new AndDSLGrammar());
    }

    {
      FunctionSymbol imply_symbol = ret.mkFunctionSymbol("imply", "bool");
      imply_symbol.addParameter("bool", "x1");
      imply_symbol.addParameter("bool", "x2");
      ret.put(imply_symbol, new ImplyDSLGrammar());
    }

    return ret;
  }

  @Override
  public Map<FunctionSymbol, String> learn(List<RelationalExample> ioExamples, List<RelationalExample> relationalExamples) {
    ComposedCFTALearner learner = new ComposedCFTALearner(this.grammarMap);

    List<RelationalExample> examples = new ArrayList<>(ioExamples);
    examples.addAll(relationalExamples);

    Map<FunctionSymbol, ProgramTree> map = learner.learnMultiple(examples);

    Map<FunctionSymbol, String> ret = new HashMap<>();
    for (FunctionSymbol k : map.keySet()) {
      ret.put(k, map.get(k).translateToProgramText());
    }

    return ret;
  }

  @Override
  public RelationalExample verifyByTesting1(Map<FunctionSymbol, String> funcSymbolToProgTexts) {

    String f = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("f"));

    DSLInterpreter interpreter = new DSLInterpreter(this.nameToDslConstructs);

    DSLAstNode fRoot = interpreter.parseProgram(f);

    // check relational property 1 
    // f(x1, x2) = -f(x2, x1) 
    {
      System.out.println("Testing property 1");
      System.out.println("Number of test input pairs: " + this.testInputPairs.size());
      for (Pair<String, String> e : this.testInputPairs) {
        String x1 = e.getValue0();
        String x2 = e.getValue1();
        Map<String, Value> valuation1 = new HashMap<>();
        valuation1.put("x1", new StringConstant(x1));
        valuation1.put("x2", new StringConstant(x2));
        Value v1 = interpreter.evaluate(fRoot, valuation1);
        Map<String, Value> valuation2 = new HashMap<>();
        valuation2.put("x1", new StringConstant(x2));
        valuation2.put("x2", new StringConstant(x1));
        Value v2 = interpreter.evaluate(fRoot, valuation2);
        if (v1 instanceof ErrConstant || v2 instanceof ErrConstant) {
//          System.out.println(v1 + " ----- " + v2);
//          return generateRelationalExample1(x1, x2);
        } else {
          assert (v1 instanceof IntConstant) : v1;
          assert (v2 instanceof IntConstant) : v2;
          int v1val = ((IntConstant) v1).value;
          int v2val = ((IntConstant) v2).value;
          if (v1val != -v2val) {
            System.out.println("Property 1 violated!");
            System.out.println("Counterexample: x1 = " + x1 + ", x2 = " + x2);
            return buildRelationalExample1(x1, x2);
          }
        }
      }
      System.out.println("Property 1 checked!");
    }

    // check relational property 2 
    // ( f(x1, x2) = 1 & f(x2, x3) = 1 ) => f(x1, x3) = 1 
    {
      System.out.println("Testing property 2");
      System.out.println("Number of test input triplets: " + this.testInputTriplets.size());
      for (Triplet<String, String, String> e : this.testInputTriplets) {
        String x1 = e.getValue0();
        String x2 = e.getValue1();
        String x3 = e.getValue2();
        Map<String, Value> valuation1 = new HashMap<>();
        valuation1.put("x1", new StringConstant(x1));
        valuation1.put("x2", new StringConstant(x2));
        Map<String, Value> valuation2 = new HashMap<>();
        valuation2.put("x1", new StringConstant(x2));
        valuation2.put("x2", new StringConstant(x3));
        Map<String, Value> valuation3 = new HashMap<>();
        valuation3.put("x1", new StringConstant(x1));
        valuation3.put("x2", new StringConstant(x3));
        Value v1 = interpreter.evaluate(fRoot, valuation1);
        Value v2 = interpreter.evaluate(fRoot, valuation2);
        Value v3 = interpreter.evaluate(fRoot, valuation3);
        if (v1 instanceof ErrConstant || v2 instanceof ErrConstant || v3 instanceof ErrConstant) {
//          return generateRelationalExample2(x1, x2, x3);
        } else {
          assert (v1 instanceof IntConstant) : v1;
          assert (v2 instanceof IntConstant) : v2;
          assert (v3 instanceof IntConstant) : v3;
          int val1 = ((IntConstant) v1).value;
          int val2 = ((IntConstant) v2).value;
          int val3 = ((IntConstant) v3).value;
          if (val1 == 1 && val2 == 1 && val3 != 1) {
            System.out.println("Property 2 violated!");
            System.out.println("Counterexample: x1 = " + x1 + ", x2 = " + x2 + ", x3 = " + x3);
            return buildRelationalExample2(x1, x2, x3);
          }
        }
      }
      System.out.println("Property 2 checked!");
    }

    // check relational property 3 
    // f(x1, x2) = 0 => f(x1, x3) = f(x2, x3) 
    {
      System.out.println("Testing property 3");
      System.out.println("Number of test input triplets: " + this.testInputTriplets.size());
      for (Triplet<String, String, String> e : this.testInputTriplets) {
        String x1 = e.getValue0();
        String x2 = e.getValue1();
        String x3 = e.getValue2();
        Map<String, Value> valuation1 = new HashMap<>();
        valuation1.put("x1", new StringConstant(x1));
        valuation1.put("x2", new StringConstant(x2));
        Map<String, Value> valuation2 = new HashMap<>();
        valuation2.put("x1", new StringConstant(x1));
        valuation2.put("x2", new StringConstant(x3));
        Map<String, Value> valuation3 = new HashMap<>();
        valuation3.put("x1", new StringConstant(x2));
        valuation3.put("x2", new StringConstant(x3));
        Value v1 = interpreter.evaluate(fRoot, valuation1);
        Value v2 = interpreter.evaluate(fRoot, valuation2);
        Value v3 = interpreter.evaluate(fRoot, valuation3);
        if (v1 instanceof ErrConstant || v2 instanceof ErrConstant || v3 instanceof ErrConstant) {
//          return generateRelationalExample2(x1, x2, x3);
        } else {
          assert (v1 instanceof IntConstant) : v1;
          assert (v2 instanceof IntConstant) : v2;
          assert (v3 instanceof IntConstant) : v3;
          int val1 = ((IntConstant) v1).value;
          int val2 = ((IntConstant) v2).value;
          int val3 = ((IntConstant) v3).value;
          if (val1 == 0 && val2 != val3) {
            System.out.println("Property 3 violated!");
            System.out.println("Counterexample: x1 = " + x1 + ", x2 = " + x2 + ", x3 = " + x3);
            return buildRelationalExample3(x1, x2, x3);
          }
        }
      }
      System.out.println("Property 3 checked!");
    }

    return null;

  }

  // f(x1, x2) = -f(x2, x1) 
  public RelationalExample buildRelationalExample1(String x1, String x2) {

    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol minus_symbol = grammarMap.getFunctionSymbolByName("minus");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");

    ExampleConstant x1val = new ExampleConstant(new StringConstant(x1));
    ExampleConstant x2val = new ExampleConstant(new StringConstant(x2));

    ExampleFunction f0 = new ExampleFunction(new FunctionOccurrence(f_symbol, 0), buildList(new ExampleTerm[] { x1val, x2val, }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f_symbol, 1), buildList(new ExampleTerm[] { x2val, x1val, }));
    ExampleFunction minusf1 = new ExampleFunction(new FunctionOccurrence(minus_symbol, 0), buildList(new ExampleTerm[] { f1, }));
    ExampleFunction f0eqminusf1 = new ExampleFunction(new FunctionOccurrence(eq_symbol, 0), buildList(new ExampleTerm[] { f0, minusf1, }));

    RelationalExample ret = new RelationalExample(f0eqminusf1);

    return ret;
  }

  // ( f(x1, x2) = 1 & f(x2, x3) = 1 ) => f(x1, x3) = 1
  public RelationalExample buildRelationalExample2(String x1, String x2, String x3) {

    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");
    FunctionSymbol and_symbol = this.grammarMap.getFunctionSymbolByName("and");
    FunctionSymbol imply_symbol = this.grammarMap.getFunctionSymbolByName("imply");

    ExampleConstant x1val = new ExampleConstant(new StringConstant(x1));
    ExampleConstant x2val = new ExampleConstant(new StringConstant(x2));
    ExampleConstant x3val = new ExampleConstant(new StringConstant(x3));

    ExampleConstant one = new ExampleConstant(new IntConstant(1));

    ExampleFunction f0 = new ExampleFunction(new FunctionOccurrence(f_symbol, 0), buildList(new ExampleTerm[] { x1val, x2val, }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f_symbol, 1), buildList(new ExampleTerm[] { x2val, x3val, }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f_symbol, 2), buildList(new ExampleTerm[] { x1val, x3val, }));

    ExampleFunction f0eqone = new ExampleFunction(new FunctionOccurrence(eq_symbol, 0), buildList(new ExampleTerm[] { f0, one, }));
    ExampleFunction f1eqone = new ExampleFunction(new FunctionOccurrence(eq_symbol, 1), buildList(new ExampleTerm[] { f1, one, }));
    ExampleFunction f2eqone = new ExampleFunction(new FunctionOccurrence(eq_symbol, 2), buildList(new ExampleTerm[] { f2, one, }));

    ExampleFunction f0eqoneANDf1eqone = new ExampleFunction(new FunctionOccurrence(and_symbol, 0),
        buildList(new ExampleTerm[] { f0eqone, f1eqone, }));

    ExampleFunction f0eqoneANDf1eqoneIMPLYf2eqone = new ExampleFunction(new FunctionOccurrence(imply_symbol, 0),
        buildList(new ExampleTerm[] { f0eqoneANDf1eqone, f2eqone, }));

    RelationalExample ret = new RelationalExample(f0eqoneANDf1eqoneIMPLYf2eqone);

    return ret;
  }

  // f(x1, x2) = 0 => f(x1, x3) = f(x2, x3) 
  public RelationalExample buildRelationalExample3(String x1, String x2, String x3) {

    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");
    FunctionSymbol imply_symbol = this.grammarMap.getFunctionSymbolByName("imply");

    ExampleConstant x1val = new ExampleConstant(new StringConstant(x1));
    ExampleConstant x2val = new ExampleConstant(new StringConstant(x2));
    ExampleConstant x3val = new ExampleConstant(new StringConstant(x3));

    ExampleConstant zero = new ExampleConstant(new IntConstant(0));

    ExampleFunction f0 = new ExampleFunction(new FunctionOccurrence(f_symbol, 0), buildList(new ExampleTerm[] { x1val, x2val, }));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f_symbol, 1), buildList(new ExampleTerm[] { x1val, x3val, }));
    ExampleFunction f2 = new ExampleFunction(new FunctionOccurrence(f_symbol, 2), buildList(new ExampleTerm[] { x2val, x3val, }));

    ExampleFunction f0eqzero = new ExampleFunction(new FunctionOccurrence(eq_symbol, 0), buildList(new ExampleTerm[] { f0, zero, }));

    ExampleFunction f1eqf2 = new ExampleFunction(new FunctionOccurrence(eq_symbol, 0), buildList(new ExampleTerm[] { f1, f2, }));

    ExampleFunction f0eqzeroIMPLYf1eqf2 = new ExampleFunction(new FunctionOccurrence(imply_symbol, 0),
        buildList(new ExampleTerm[] { f0eqzero, f1eqf2, }));

    RelationalExample ret = new RelationalExample(f0eqzeroIMPLYf1eqf2);

    return ret;
  }

  // 
  // Test input generation 
  // 

  public List<Pair<String, String>> testInputPairs;
  public List<Triplet<String, String, String>> testInputTriplets;

  @Override
  public void generateTestInputs(Map<FunctionSymbol, String> funcSymbolToProgTexts) {

    List<Character> chars = generateChars(funcSymbolToProgTexts);

    List<String> strings = null;
    List<String> strings1 = null;

    if ("c18".equals(Benchmark.benchmarkName)) {
      strings = generateStrings(chars, 5);
      strings1 = generateStrings(chars, 3);
    } else if ("c11".equals(Benchmark.benchmarkName)) {
      strings = generateStrings(chars, 5);
      strings1 = generateStrings(chars, 3);
    } else if ("c19".equals(Benchmark.benchmarkName)) {
      strings = generateStrings(chars, 5);
      strings1 = generateStrings(chars, 3);
    }
    //
    else {
      strings = generateStrings(chars, 5);
      strings1 = generateStrings(chars, 2);
    }

    this.testInputPairs = generateStringPairs(strings);
    this.testInputTriplets = generateStringTriplets(strings1);

  }

  public List<Pair<String, String>> generateStringPairs(List<String> strings) {
    List<Pair<String, String>> ret = new ArrayList<>();
    for (String x1 : strings) {
      for (String x2 : strings) {
        ret.add(Pair.with(x1, x2));
      }
    }
    return ret;
  }

  public List<Triplet<String, String, String>> generateStringTriplets(List<String> strings) {
    List<Triplet<String, String, String>> ret = new ArrayList<>();
    for (String x1 : strings) {
      for (String x2 : strings) {
        for (String x3 : strings) {
          ret.add(Triplet.with(x1, x2, x3));
        }
      }
    }
    return ret;
  }

  public List<String> generateStrings(List<Character> chars, int maxLen) {
    List<String> ret = new ArrayList<>();
    for (int len = 1; len <= maxLen; len ++) {
      char[] cur = new char[len];
      generate(chars, len, 0, cur, ret);
    }
    return ret;
  }

  public void generate(List<Character> chars, int len, int curIndex, char[] cur, List<String> res) {
    if (curIndex == len) {
      String s = String.valueOf(cur);
      res.add(s);
    } else {
      for (char c : chars) {
        cur[curIndex] = c;
        generate(chars, len, curIndex + 1, cur, res);
      }
    }
  }

  public List<Character> generateChars(Map<FunctionSymbol, String> funcSymbolToProgTexts) {

    String f = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("f"));

    DSLInterpreter interpreter = new DSLInterpreter(this.nameToDslConstructs);
    DSLAstNode fRoot = interpreter.parseProgram(f);

    List<Character> ret = new ArrayList<>();

    // program-driven test input generation 
    Set<Character> chars = new HashSet<>();
    extractCharConstants(fRoot, chars);

    // add up to 2 digits and 2 letters 

    int totalNumOfDigits = 2;
    int totalNumOfLetters = 2;

    {
      int numOfDigits = 0;
      int numOfLetters = 0;
      for (char c : chars) {
        if (c >= '0' && c <= '9') {
          numOfDigits ++;
        } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
          numOfLetters ++;
        } else {
          throw new RuntimeException();
        }
      }
      for (char c = '1'; c <= '9' && numOfDigits < totalNumOfDigits; c ++) {
        if (!chars.contains(c)) {
          chars.add(c);
          numOfDigits ++;
        }
      }
      for (char c = 'a'; c <= 'z' && numOfLetters < totalNumOfLetters; c ++) {
        if (!chars.contains(c)) {
          chars.add(c);
          numOfLetters ++;
        }
      }
    }

    ret.addAll(chars);

    System.out.println("Chars: " + ret);

    return ret;
  }

  public void extractCharConstants(DSLAstNode root, Set<Character> chars) {
    if (root instanceof IntLiteralNode) {
      return;
    } else if (root instanceof StringLiteralNode) {
      return;
    } else if (root instanceof CharLiteralNode) {
      chars.add(((CharLiteralNode) root).value.value);
    } else if (root instanceof VariableNode) {
      return;
    } else if (root instanceof FunctionNode) {
      FunctionNode funcNode = (FunctionNode) root;
      Value[] args = new Value[funcNode.arguments.size()];
      for (int i = 0; i < args.length; ++i) {
        extractCharConstants(funcNode.arguments.get(i), chars);
      }
      return;
    } else {
      throw new RuntimeException("Unknown subtype of DSLAstNode: " + root);
    }
  }

  @Override
  public RelationalExample verifyByIO1(Map<FunctionSymbol, String> funcSymbolToProgTexts) {

    FunctionSymbol f = this.funcNameToFuncSymbols.get("f");
    String program = funcSymbolToProgTexts.get(f);
    DSLInterpreter interpreter = new DSLInterpreter(this.nameToDslConstructs);
    DSLAstNode progRoot = interpreter.parseProgram(program);

    for (Triplet<String, String, Integer> e : this.allIOExamples) {

      StringConstant x1 = new StringConstant(e.getValue0());
      StringConstant x2 = new StringConstant(e.getValue1());
      IntConstant output = new IntConstant(e.getValue2());

      Map<String, Value> valuation = new HashMap<>();
      valuation.put("x1", x1);
      valuation.put("x2", x2);

      Value result = interpreter.evaluate(progRoot, valuation);

      boolean matched = output.equals(result);

      if (!matched) {
        RelationalExample ioCounterexample = buildIOExample(e);
        return ioCounterexample;
      }
    }

    return null;
  }

  @Override
  public RelationalExample verifyByCBMC1(Map<FunctionSymbol, String> funcSymbolToProgTexts) {
    return null;
  }

  //
  // logging 
  //

  @Override
  public void log_after() {

    this.total_time = this.synthesis_time + this.verification_time;
    this.number_of_iterations = this.number_of_io_examples + this.number_of_relational_examples;

    try {

      FileWriter fw = new FileWriter(this.logFilePath);
      BufferedWriter bw = new BufferedWriter(fw);

      if (learnt_program == null) {

        bw.write("Status: FAILED\n");
        bw.write("Number of examples: " + this.allIOExamples.size() + "\n");

        bw.write("Number of iterations: " + this.number_of_iterations + "\n");
        bw.write("Number of I/O examples: " + this.number_of_io_examples + "\n");
        bw.write("Number of relational examples: " + this.number_of_relational_examples + "\n");

      } else {

        bw.write("Status: SUCCESSFUL\n");
        bw.write("Number of examples: " + this.allIOExamples.size() + "\n");

        bw.write("Number of iterations: " + this.number_of_iterations + "\n");
        bw.write("Number of I/O examples: " + this.number_of_io_examples + "\n");
        bw.write("Number of relational examples: " + this.number_of_relational_examples + "\n");

        bw.write("Total time: " + (this.total_time / 1000.0) + " sec\n");
        bw.write("Synthesis time: " + (this.synthesis_time / 1000.0) + " sec\n");
        bw.write("Verification time: " + (this.verification_time / 1000.0) + " sec\n");

        bw.write("Learnt program: " + this.learnt_program + "\n");

      }

      bw.close();

    } catch (IOException e) {

      e.printStackTrace();

    }
  }

  @Override
  public void log_before() {
    try {

      FileWriter fw = new FileWriter(this.logFilePath);
      BufferedWriter bw = new BufferedWriter(fw);

      bw.write("Status: TO\n");
      bw.write("Number of I/O examples: " + this.allIOExamples.size() + "\n");

      bw.close();

    } catch (IOException e) {

      e.printStackTrace();

    }
  }

}