package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import relish.dsl.FunctionSymbol;
import relish.learn.RelationalExample;
import relish.util.PrintUtil;

public abstract class Benchmark {

  // a unique name for each benchmark 
  public static String benchmarkName;

  // path to log file 
  public String logFilePath;

  public Benchmark(String benchmarkName, String logFilePath) {

    Benchmark.benchmarkName = benchmarkName;

    this.logFilePath = logFilePath;

  }

  // learn a program that satisfies 
  // (1) all I/O examples (modulo DSL semantics) 
  // (2) relational property (modulo the verifier) 
  public void run() {

    System.out.println("Starts benchmark " + benchmarkName);

    List<RelationalExample> usedIOExamples = new ArrayList<>();
    List<RelationalExample> usedRelationalExamples = new ArrayList<>();

    Map<FunctionSymbol, String> funcSymbolToProgTexts = null;

    int iter = 0;

    // 
    // Initialize 
    // 
    {

      // the first example is always an IO example 
      Set<RelationalExample> firstExamples = getFirstExample();
      usedIOExamples.addAll(firstExamples);

      this.number_of_io_examples ++;

      {

        System.out.println("========================= " + (++iter) + " =========================");

        System.out.println("IO Examples:");

        System.out.println("{");
        for (RelationalExample ex : usedIOExamples) {
          System.out.println("  " + ex);
        }
        System.out.println("}");

        System.out.println("Relational Examples:");

        System.out.println("{");
        for (RelationalExample ex : usedRelationalExamples) {
          System.out.println("  " + ex);
        }
        System.out.println("}");

      }

      {

        System.out.println("Synthesizing...");

        long start = System.currentTimeMillis();

        funcSymbolToProgTexts = learn(usedIOExamples, usedRelationalExamples);

        long end = System.currentTimeMillis();

        this.synthesis_time += (end - start);

        System.out.println("Synthesis time so far: " + this.synthesis_time / 1000.0 + " sec");

        PrintUtil.printMap(funcSymbolToProgTexts);

      }

    }

    // CEGIS loop 
    while (true) {

      if (funcSymbolToProgTexts == null) {
        // no correct program exists in the program space
        System.out.println("FAILURE");
        break;
      }

      // Verification 
      {

        boolean succ = this.verify(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);

        System.out.println("Verification time so far: " + this.verification_time / 1000.0 + " sec");

        if (succ) {

          // learnt a correct program 
          System.out.println("SUCCESS");

          succ(funcSymbolToProgTexts);

          break;
        }

      }

      {

        System.out.println("========================= " + (++iter) + " =========================");

        System.out.println("IO Examples:");

        System.out.println("{");
        for (RelationalExample e : usedIOExamples) {
          System.out.println("  " + e);
        }
        System.out.println("}");

        System.out.println("Relational Examples:");

        System.out.println("{");
        for (RelationalExample e : usedRelationalExamples) {
          System.out.println("  " + e);
        }
        System.out.println("}");

      }

      {

        System.out.println("Synthesizing...");

        long start = System.currentTimeMillis();

        // Learn a program that satisfies 
        // (1) I/O examples in usedIOExamples 
        // (2) relational examples in usedRelationalExamples
        funcSymbolToProgTexts = learn(usedIOExamples, usedRelationalExamples);

        long end = System.currentTimeMillis();

        this.synthesis_time += (end - start);

        System.out.println("Synthesis time so far: " + this.synthesis_time / 1000.0 + " sec");

        PrintUtil.printMap(funcSymbolToProgTexts);

      }

    }

  }

  // 
  public abstract Set<RelationalExample> getFirstExample();

  // record the correct program upon success 
  public abstract void succ(Map<FunctionSymbol, String> funcSymbolToProgTexts);

  //
  // Synthesis 
  // 

  public abstract Map<FunctionSymbol, String> learn(List<RelationalExample> ioExamples, List<RelationalExample> relationalExamples);

  //
  // Verification 
  //

  public boolean verify(Map<FunctionSymbol, String> funcSymbolToProgTexts, List<RelationalExample> usedIOExamples,
      List<RelationalExample> usedRelationalExamples) {

//    return verifyByTestingIOCBMC(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
    return verifyByIOTestingCBMC(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);

  }

  public boolean verifyByTestingIOCBMC(Map<FunctionSymbol, String> funcSymbolToProgTexts, List<RelationalExample> usedIOExamples,
      List<RelationalExample> usedRelationalExamples) {

    {
      boolean succ = verifyByTesting(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
      if (!succ) return false;
    }

    {
      boolean succ = verifyByIO(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
      if (!succ) return false;
    }

    {
      boolean succ = verifyByCBMC(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
      return succ;
    }

  }

  public boolean verifyByIOTestingCBMC(Map<FunctionSymbol, String> funcSymbolToProgTexts, List<RelationalExample> usedIOExamples,
      List<RelationalExample> usedRelationalExamples) {

    {
      boolean succ = verifyByIO(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
      if (!succ) return false;
    }

    {
      boolean succ = verifyByTesting(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
      if (!succ) return false;
    }

    {
      boolean succ = verifyByCBMC(funcSymbolToProgTexts, usedIOExamples, usedRelationalExamples);
      return succ;
    }

  }

  public boolean verifyByTesting(Map<FunctionSymbol, String> funcSymbolToProgTexts, List<RelationalExample> usedIOExamples,
      List<RelationalExample> usedRelationalExamples) {

    generateTestInputs(funcSymbolToProgTexts);

    long start = System.currentTimeMillis();

    RelationalExample relationalCounterexample = verifyByTesting1(funcSymbolToProgTexts);

    long end = System.currentTimeMillis();

    this.verification_time += (end - start);

    if (relationalCounterexample != null) {

      assert (!usedRelationalExamples.contains(relationalCounterexample));

      usedRelationalExamples.add(relationalCounterexample);

      this.number_of_relational_examples ++;

      return false;
    }

    return true;

  }

  public abstract void generateTestInputs(Map<FunctionSymbol, String> funcSymbolToProgTexts);

  public boolean verifyByIO(Map<FunctionSymbol, String> funcSymbolToProgTexts, List<RelationalExample> usedIOExamples,
      List<RelationalExample> usedRelationalExamples) {

    long start = System.currentTimeMillis();

    RelationalExample ioCounterexample = verifyByIO1(funcSymbolToProgTexts);

    long end = System.currentTimeMillis();

    this.verification_time += (end - start);

    if (ioCounterexample != null) {

      assert (!usedIOExamples.contains(ioCounterexample));

      usedIOExamples.add(ioCounterexample);

      this.number_of_io_examples ++;

      return false;
    }

    return true;

  }

  public boolean verifyByCBMC(Map<FunctionSymbol, String> funcSymbolToProgTexts, List<RelationalExample> usedIOExamples,
      List<RelationalExample> usedRelationalExamples) {

    long start = System.currentTimeMillis();

    RelationalExample relationalCounterexample = verifyByCBMC1(funcSymbolToProgTexts);

    long end = System.currentTimeMillis();

    this.verification_time += (end - start);

    if (relationalCounterexample != null) {

      assert false;

      assert (!usedRelationalExamples.contains(relationalCounterexample));

      usedRelationalExamples.add(relationalCounterexample);

      this.number_of_relational_examples ++;

      return false;
    }

    return true;

  }

  public abstract RelationalExample verifyByTesting1(Map<FunctionSymbol, String> funcSymbolToProgTexts);

  public abstract RelationalExample verifyByIO1(Map<FunctionSymbol, String> funcSymbolToProgTexts);

  public abstract RelationalExample verifyByCBMC1(Map<FunctionSymbol, String> funcSymbolToProgTexts);

  //
  // logging information 
  //

  public long number_of_io_examples;
  public long number_of_relational_examples;
  public long synthesis_time;
  public long verification_time;

  public long total_time;
  public long number_of_iterations;

  // 
  public abstract void log_before();

  // 
  public abstract void log_after();

  public <T> List<T> buildList(T[] array) {
    return new ArrayList<>(Arrays.asList(array));
  }
}
