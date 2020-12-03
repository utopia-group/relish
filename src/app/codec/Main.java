package app.codec;

import app.codec.exp.CodecBenchmark;

public class Main {

  public static void main(String[] args) {

    String benchmarkName = args[0];
    String benchmarkFilePath = args[1];
    String logFilePath = args[2];

    CodecBenchmark benchmark = new CodecBenchmark(benchmarkName, benchmarkFilePath, logFilePath);

    benchmark.log_before();
    
    benchmark.run();
    
    benchmark.log_after();

  }

}
