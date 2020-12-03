package app.comparator;

import app.comparator.exp.ComparatorBenchmark;

public class Main {

  public static void main(String[] args) {

    String benchmarkName = args[0];
    String benchmarkFilePath = args[1];
    String logFilePath = args[2];

    ComparatorBenchmark benchmark = new ComparatorBenchmark(benchmarkName, benchmarkFilePath, logFilePath);

    benchmark.log_before();

    benchmark.run();

    benchmark.log_after();

  }

}
