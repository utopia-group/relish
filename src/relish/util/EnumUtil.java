package relish.util;

import java.util.LinkedList;
import java.util.List;

public class EnumUtil {

  // n-ary Cartesian product, return a list of n-tuple (stored as a list of size n)
  // e.g. [[1, 2], [3], [4, 5]] -> [[1, 3, 4], [1, 3, 5], [2, 3, 4], [2, 3, 5]]
  public static <T> List<List<T>> cartesianProduct(List<List<T>> listOfValues) {
    assert listOfValues != null;
    List<List<T>> ret = cartesianProductImpl(listOfValues, 0);
    return ret;
  }

  // real implementation of n-ary Cartesian product
  private static <T> List<List<T>> cartesianProductImpl(List<List<T>> listOfValues, int currIndex) {
    List<List<T>> ret = new LinkedList<>();
    int arity = listOfValues.size();
    if (currIndex >= arity) {
      ret.add(new LinkedList<>());
    } else {
      List<T> currList = listOfValues.get(currIndex);
      List<List<T>> tails = cartesianProductImpl(listOfValues, currIndex + 1);
      for (T elem : currList) {
        for (List<T> tail : tails) {
          LinkedList<T> currCat = new LinkedList<>(tail);
          currCat.addFirst(elem);
          ret.add(currCat);
        }
      }
    }
    return ret;
  }

}
