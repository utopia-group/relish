package relish.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import relish.util.EnumUtil;

public class EnumUtilTests {

  @Test
  public void testCartesianProduct() {
    List<List<Integer>> listOfValues = new ArrayList<>();
    listOfValues.add(new ArrayList<>(Arrays.asList(new Integer[] { 1, 2 })));
    listOfValues.add(new ArrayList<>(Arrays.asList(new Integer[] { 3, 4 })));
    listOfValues.add(new ArrayList<>(Arrays.asList(new Integer[] { 5, 6 })));
    List<List<Integer>> result = EnumUtil.cartesianProduct(listOfValues);
    List<List<Integer>> expected = new LinkedList<>();
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 1, 3, 5 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 1, 3, 6 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 1, 4, 5 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 1, 4, 6 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 2, 3, 5 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 2, 3, 6 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 2, 4, 5 })));
    expected.add(new LinkedList<>(Arrays.asList(new Integer[] { 2, 4, 6 })));
    Assert.assertTrue(result.equals(expected));
  }

}
