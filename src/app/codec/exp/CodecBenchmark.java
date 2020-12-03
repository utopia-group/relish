package app.codec.exp;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javatuples.Pair;

import app.Benchmark;
import app.codec.learn.DecoderDSLGrammar;
import app.codec.learn.DecoderDSLSemantics;
import app.codec.learn.EncoderDSLGrammar;
import app.codec.learn.EncoderDSLSemantics;
import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.dsl.Production;
import relish.eval.DSLAstNode;
import relish.eval.DSLInterpreter;
import relish.fta.ProgramTree;
import relish.learn.AndDSLGrammar;
import relish.learn.ComposedCFTALearner;
import relish.learn.EqDSLGrammar;
import relish.learn.ImplyDSLGrammar;
import relish.learn.NotDSLGrammar;
import relish.learn.OrDSLGrammar;
import relish.learn.RelationalExample;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;
import relish.util.FileUtil;
import relish.verify.RelationalProperty;
import relish.verify.RelationalProperty.PropertyFunction;
import relish.verify.RelationalProperty.PropertyTerm;
import relish.verify.RelationalProperty.PropertyVariable;
import relish.verify.RelationalVerifier;

public class CodecBenchmark extends Benchmark {

  // each example is of the form (x1, return) 
  public List<Pair<String, String>> allIOExamples = new ArrayList<>();

  // map from function names to function symbols
  public Map<String, FunctionSymbol> funcNameToFuncSymbols;

  // map from DSL construct names to objects, for interpreter
  // f is encoder 
  public Map<String, Production> decoderNameToDslConstructs;
  // g is decoder 
  public Map<String, Production> encoderNameToDslConstructs;

  // g(f(x1)) = x1
  public RelationalProperty property;

  // path to comparator.c 
  public String cFile;

  //
  public DSLGrammarMap grammarMap;

  //
  //
  //
  public String learnt_f;
  public String learnt_g;

  public CodecBenchmark(String benchmarkName, String benchmarkFilePath, String logFilePath) {
    super(benchmarkName, logFilePath);
    parseAllIOExamples(benchmarkFilePath);
    this.funcNameToFuncSymbols = buildFunctionSymbolMap();
    this.decoderNameToDslConstructs = DecoderDSLSemantics.getDSLConstructMap();
    this.encoderNameToDslConstructs = EncoderDSLSemantics.getDSLConstructMap();
    this.grammarMap = buildGrammarMap();
    this.property = buildRelationalProperty();
    this.cFile = "specs/codec.c";
  }

  // parse the benchmark file 
  public void parseAllIOExamples(String filename) {
    List<String> lines = FileUtil.readFromFile(filename);
    for (String line : lines) {
      if (line.isEmpty() || line.startsWith("//")) continue;
      String[] tokens = line.split("\"");
      String x1 = tokens[1];
      String x2 = tokens[3];
      Pair<String, String> e = Pair.with(x1, x2);
      this.allIOExamples.add(e);
    }
  }

  public Map<String, FunctionSymbol> buildFunctionSymbolMap() {
    Map<String, FunctionSymbol> funcNameToFuncSymbols = new HashMap<>();
    {
      FunctionSymbol f_symbol = new FunctionSymbol("f", "String");
      f_symbol.addParameter("String", "x1");
      funcNameToFuncSymbols.put("f", f_symbol);
    }

    {
      FunctionSymbol g_symbol = new FunctionSymbol("g", "String");
      g_symbol.addParameter("String", "x1");
      funcNameToFuncSymbols.put("g", g_symbol);
    }

    {
      FunctionSymbol eq_symbol = new FunctionSymbol("eq", "bool");
      eq_symbol.addParameter("Poly", "x1");
      eq_symbol.addParameter("Poly", "x2");
      funcNameToFuncSymbols.put("eq", eq_symbol);
    }

    {
      FunctionSymbol and_symbol = new FunctionSymbol("and", "bool");
      and_symbol.addParameter("bool", "x1");
      and_symbol.addParameter("bool", "x2");
      funcNameToFuncSymbols.put("and", and_symbol);
    }

    {
      FunctionSymbol or_symbol = new FunctionSymbol("or", "bool");
      or_symbol.addParameter("bool", "x1");
      or_symbol.addParameter("bool", "x2");
      funcNameToFuncSymbols.put("or", or_symbol);
    }

    {
      FunctionSymbol imply_symbol = new FunctionSymbol("imply", "bool");
      imply_symbol.addParameter("bool", "x1");
      imply_symbol.addParameter("bool", "x2");
      funcNameToFuncSymbols.put("imply", imply_symbol);
    }

    {
      FunctionSymbol not_symbol = new FunctionSymbol("not", "bool");
      not_symbol.addParameter("bool", "x1");
      funcNameToFuncSymbols.put("not", not_symbol);
    }

    return funcNameToFuncSymbols;
  }

  public DSLGrammarMap buildGrammarMap() {

    DSLGrammarMap ret = new DSLGrammarMap();

    {
      FunctionSymbol f_symbol = ret.mkFunctionSymbol("f", "String");
      f_symbol.addParameter("String", "x1");
      ret.put(f_symbol, new EncoderDSLGrammar(null));
    }

    {
      FunctionSymbol g_symbol = ret.mkFunctionSymbol("g", "String");
      g_symbol.addParameter("String", "x1");
      ret.put(g_symbol, new DecoderDSLGrammar(null));
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
      FunctionSymbol or_symbol = ret.mkFunctionSymbol("or", "bool");
      or_symbol.addParameter("bool", "x1");
      or_symbol.addParameter("bool", "x2");
      ret.put(or_symbol, new OrDSLGrammar());
    }

    {
      FunctionSymbol imply_symbol = ret.mkFunctionSymbol("imply", "bool");
      imply_symbol.addParameter("bool", "x1");
      imply_symbol.addParameter("bool", "x2");
      ret.put(imply_symbol, new ImplyDSLGrammar());
    }

    {
      FunctionSymbol not_symbol = ret.mkFunctionSymbol("not", "bool");
      not_symbol.addParameter("bool", "x1");
      ret.put(not_symbol, new NotDSLGrammar());
    }

    return ret;
  }

  public RelationalProperty buildRelationalProperty() {
    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g_symbol = this.grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");

    // g(f(x1)) = x1
    PropertyFunction p1 = null;
    {
      PropertyVariable x1 = new PropertyVariable("String", "x1");

      PropertyFunction f1 = new PropertyFunction(f_symbol, buildList(new PropertyTerm[] { x1 }));
      PropertyFunction g1 = new PropertyFunction(g_symbol, buildList(new PropertyTerm[] { f1, }));
      PropertyFunction eq1 = new PropertyFunction(eq_symbol, buildList(new PropertyTerm[] { g1, x1 }));

      p1 = eq1;
    }

    RelationalProperty prop = new RelationalProperty(p1);
    return prop;
  }

  @Override
  public Set<RelationalExample> getFirstExample() {
    Pair<String, String> e = this.allIOExamples.get(0);
    Set<RelationalExample> ret = new HashSet<>();
    ret.add(buildIOExample(e));
    return ret;
  }

  // An I/O example has to be of the form: f(x1) = v 
  public RelationalExample buildIOExample(Pair<String, String> e) {

    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");

    ExampleConstant x1 = new ExampleConstant(new StringConstant(e.getValue0()));
    ExampleConstant v = new ExampleConstant(new StringConstant(e.getValue1()));

    ExampleFunction f = new ExampleFunction(new FunctionOccurrence(f_symbol, 0), buildList(new ExampleTerm[] { x1, }));
    ExampleFunction eq = new ExampleFunction(new FunctionOccurrence(eq_symbol, 0), buildList(new ExampleTerm[] { f, v }));

    RelationalExample ret = new RelationalExample(eq);

    return ret;
  }

  @Override
  public void succ(Map<FunctionSymbol, String> funcSymbolToProgTexts) {
    this.learnt_f = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("f"));
    this.learnt_g = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("g"));
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

  //
  // Test input generation 
  // 

  public List<String> testInputStrings;

  public List<Character> generateChars() {
    List<Character> ret = new ArrayList<>();
    // digits
    for (char c = '0'; c <= '9'; c ++) {
      ret.add(c);
    }
    // other visible ascii characters
    for (char c = ':'; c <= '~'; c ++) {
      ret.add(c);
    }
    for (char c = ' '; c <= '/'; c ++) {
      ret.add(c);
    }
    // unicode
    {
      ret.add('£'); // U+00A3
      ret.add('₡'); // U+20A1
      ret.add('€'); // U+20AC
      ret.add('¢'); // U+00A2
      ret.add('¥'); // U+00A5
      ret.add('α'); // U+03B1
      ret.add('₵'); // U+20B5
      ret.add('☀'); // U+2600
      ret.add('♥'); // U+2665
      ret.add('☺'); // U+263A
    }
    return ret;
  }

  @Override
  public void generateTestInputs(Map<FunctionSymbol, String> funcSymbolToProgTexts) {
    List<Character> chars = generateChars();
    List<String> strings = new ArrayList<>();
    for (int len = 1; len <= 3; len ++) {
      char[] cur = new char[len];
      generate(chars, len, 0, cur, strings);
    }
    this.testInputStrings = strings;
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

  @Override
  public RelationalExample verifyByTesting1(Map<FunctionSymbol, String> funcSymbolToProgTexts) {
    String f = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("f"));
    String g = funcSymbolToProgTexts.get(this.funcNameToFuncSymbols.get("g"));

    DSLInterpreter fInterpreter = new DSLInterpreter(this.encoderNameToDslConstructs);

    DSLAstNode fRoot = fInterpreter.parseProgram(f);

    // check relational property 1
    // g(f(x1)) = x1
    {
      System.out.println("Testing property ...");
      System.out.println("Number of test inputs: " + this.testInputStrings.size());

      for (String x1 : this.testInputStrings) {
        Map<String, Value> fValuation = new HashMap<>();
        fValuation.put("x1", new StringConstant(x1));
        Value v1 = fInterpreter.evaluate(fRoot, fValuation);

        if (v1 instanceof ErrConstant) return generateRelationalExample(x1);
        assert (v1 instanceof StringConstant) : v1;

        if (g == null) return generateRelationalExample(x1);

        DSLInterpreter gInterpreter = new DSLInterpreter(this.decoderNameToDslConstructs);
        DSLAstNode gRoot = gInterpreter.parseProgram(g);

        Map<String, Value> gValuation = new HashMap<>();
        gValuation.put("x1", v1);
        Value v2 = gInterpreter.evaluate(gRoot, gValuation);

        if (v2 instanceof ErrConstant) return generateRelationalExample(x1);
        assert (v2 instanceof StringConstant) : v2;
        String v2val = ((StringConstant) v2).value;

        if (!v2val.equals(x1)) {
          return generateRelationalExample(x1);
        }
      }
    }

    // passed all tests
    return null;
  }

  // g(f(x1)) = x1
  public RelationalExample generateRelationalExample(String x1) {

    FunctionSymbol f_symbol = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g_symbol = this.grammarMap.getFunctionSymbolByName("g");
    FunctionSymbol eq_symbol = this.grammarMap.getFunctionSymbolByName("eq");

    ExampleConstant x1val = new ExampleConstant(new StringConstant(x1));
    ExampleFunction f1 = new ExampleFunction(new FunctionOccurrence(f_symbol, 1), buildList(new ExampleTerm[] { x1val }));
    ExampleFunction g1 = new ExampleFunction(new FunctionOccurrence(g_symbol, 1), buildList(new ExampleTerm[] { f1 }));
    ExampleFunction eq1 = new ExampleFunction(new FunctionOccurrence(eq_symbol, 1), buildList(new ExampleTerm[] { g1, x1val }));
    RelationalExample ret = new RelationalExample(eq1);

    return ret;
  }

  @Override
  public RelationalExample verifyByIO1(Map<FunctionSymbol, String> funcSymbolToProgTexts) {

    FunctionSymbol f = this.funcNameToFuncSymbols.get("f");
    String program = funcSymbolToProgTexts.get(f);
    DSLInterpreter fInterpreter = new DSLInterpreter(this.encoderNameToDslConstructs);
    DSLAstNode progRoot = fInterpreter.parseProgram(program);

    for (Pair<String, String> e : this.allIOExamples) {

      StringConstant x1 = new StringConstant(e.getValue0());
      StringConstant output = new StringConstant(e.getValue1());

      Map<String, Value> valuation = new HashMap<>();
      valuation.put("x1", x1);

      Value result = fInterpreter.evaluate(progRoot, valuation);
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
    // temporarily disable the CBMC verification
    return null;
  }

  public RelationalExample verifyByCBMC2(Map<FunctionSymbol, String> funcSymbolToProgTexts) {
    FunctionSymbol f = this.grammarMap.getFunctionSymbolByName("f");
    FunctionSymbol g = this.grammarMap.getFunctionSymbolByName("g");
    funcSymbolToProgTexts.put(f, funcSymbolToProgTexts.get(f));
    if (!funcSymbolToProgTexts.containsKey(g)) {
      funcSymbolToProgTexts.put(g, "#String _var; return _var;");
    }
    String cbmc_in_file = "exp/tmp/codec/" + benchmarkName + "-cbmc-check.c";
    String cbmc_out_file = "exp/tmp/codec/" + benchmarkName + "-cbmc-result.xml";
    RelationalVerifier verifier = new RelationalVerifier(this.grammarMap, this.cFile, cbmc_in_file, cbmc_out_file);
    RelationalExample counterexample = verifier.verify(funcSymbolToProgTexts, this.property);
    return counterexample;
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

  @Override
  public void log_after() {

    this.total_time = this.synthesis_time + this.verification_time;
    this.number_of_iterations = this.number_of_io_examples + this.number_of_relational_examples;

    try {

      FileWriter fw = new FileWriter(this.logFilePath);
      BufferedWriter bw = new BufferedWriter(fw);

      if (learnt_f == null || learnt_g == null) {

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

        bw.write("Learnt encoder program: " + this.learnt_f + "\n");
        bw.write("Learnt decoder program: " + this.learnt_g + "\n");

      }

      bw.close();

    } catch (IOException e) {

      e.printStackTrace();

    }

  }

}