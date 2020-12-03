package relish.util;

import java.util.Map;

import relish.Options;

public class PrintUtil {

  public static void printlnIfVerbose(Object obj) {
    if (Options.VERBOSE) {
      System.out.println(obj);
    }
  }

  public static void println(Object obj) {
    System.out.println(obj);
  }

  public static <K, V> void printMap(Map<K, V> map) {
    System.out.println("{");
    map.forEach((key, value) -> System.out.println(key + " -> " + value));
    System.out.println("}");
  }

}
