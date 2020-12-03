package app.comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;

public class Test {

  public static void main(String[] args) {
    Test t = new Test();
    t.test1();
//    t.test2();
//    t.test3();
  }

  public void test1() {

    String benchmarkName = "c10";

    String[] args = new String[] {
        // benchmark name 
        benchmarkName,
        // benchmark file path 
        "exp/benchmark/comparator/" + benchmarkName,
        // log file path 
        "exp/log/comparator/" + benchmarkName,
        // 
    };

    long start = System.currentTimeMillis();

    Main.main(args);

    long end = System.currentTimeMillis();

    double time = (end - start) / 1000.0;

    System.out.println("Time : " + time + " sec");

  }

  public void test2() {
    Pattern Alphanumeric = Pattern.compile("[\\w]+");
    Matcher matcher = Alphanumeric.matcher("a.txt");
    List<Pair<Integer, Integer>> list = new ArrayList<>();
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      Pair<Integer, Integer> pair = Pair.with(start, end);
      list.add(pair);
    }
    System.out.println(list);
  }

  public void test3() {
    String s1 = "ab";
    String s2 = "a";
    System.out.println(s1.compareTo(s2));
  }

}
