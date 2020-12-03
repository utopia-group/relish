package app.codec;

public class Test {

  public static void main(String[] args) {
    Test t = new Test();
    t.test1();
  }

  public void test1() {

    String benchmarkName = "b2";

    String[] args = new String[] {
        // 
        benchmarkName,
        // benchmark file path 
        "exp/benchmark/codec/" + benchmarkName,
        // log file path 
        "exp/log/codec/" + benchmarkName,
        //    
    };

    long start = System.currentTimeMillis();

    Main.main(args);

    long end = System.currentTimeMillis();

    double time = (end - start) / 1000.0;

    System.out.println("Time : " + time + " sec");

  }

}
