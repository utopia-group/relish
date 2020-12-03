package app.comparator.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;

import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;

public class ComparatorDSLSemantics {

  public static Map<String, Production> getDSLConstructMap() {
    Map<String, Production> map = new HashMap<>();
    map.put("Id", new Id());
    map.put("Conditional", new Conditional());
    map.put("IntLt", new IntLt());
    map.put("StrLt", new StrLt());
    map.put("CountChar", new CountChar());
    map.put("Len", new Len());
    map.put("ToInt", new ToInt());
    map.put("SubStr", new SubStr());
    map.put("Id3", new Id3());
    map.put("StartPos", new StartPos());
    map.put("EndPos", new EndPos());
    map.put("End", new End());
    map.put("ConstPos", new ConstPos());
    map.put("Id1", new Id1());
    map.put("Id2", new Id2());
    return map;
  }

  public static int compareString(StringConstant first, StringConstant second) {
    int i = 0;
    for (; i < first.value.length() && i < second.value.length() && first.value.charAt(i) == second.value.charAt(i); i ++)
      ;
    if (i == first.value.length() && i == second.value.length()) {
      return 0;
    } else if (i == first.value.length()) {
      return -1;
    } else if (i == second.value.length()) {
      return 1;
    } else if (first.value.charAt(i) > second.value.charAt(i)) {
      return 1;
    } else if (first.value.charAt(i) < second.value.charAt(i)) {
      return -1;
    } else {
      throw new RuntimeException();
    }
  }

  public static class Id extends Production {

    public Id() {
      super();
    }

    public Id(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      return args[0];
    }

    @Override
    public String translateToProgramText() {
      return "Id";
    }

  }

  public static class Conditional extends Production {

    public Conditional() {
      super();
    }

    public Conditional(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;

      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      IntConstant arg0 = (IntConstant) args[0];

      if (arg0.value == 0) {
        return args[1];
      } else {
        return arg0;
      }
    }

    @Override
    public String translateToProgramText() {
      return "Conditional";
    }

  }

  public static class IntLt extends Production {

    public IntLt() {
      super();
    }

    public IntLt(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;

      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      IntConstant arg0 = (IntConstant) args[0];
      IntConstant arg1 = (IntConstant) args[1];

      if (arg0.value < arg1.value) return new IntConstant(1);
      if (arg0.value > arg1.value) return new IntConstant(-1);
      return new IntConstant(0);
    }

    @Override
    public String translateToProgramText() {
      return "IntLt";
    }

  }

  public static class IntGt extends Production {

    public IntGt(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;

      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      IntConstant arg0 = (IntConstant) args[0];
      IntConstant arg1 = (IntConstant) args[1];

      if (arg0.value > arg1.value) return new IntConstant(1);
      if (arg0.value < arg1.value) return new IntConstant(-1);
      return new IntConstant(0);
    }

    @Override
    public String translateToProgramText() {
      return "IntGt";
    }

  }

  public static class StrLt extends Production {

    public StrLt() {
      super();
    }

    public StrLt(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;

      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      StringConstant arg0 = (StringConstant) args[0];
      StringConstant arg1 = (StringConstant) args[1];

//      System.out.println("arg0: " + arg0.value);
//      System.out.println("arg1: " + arg1.value);

      int comp = compareString(arg0, arg1);

      IntConstant ret = new IntConstant(-comp);
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "StrLt";
    }

  }

  public static class StrGt extends Production {

    public StrGt(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;

      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      StringConstant arg0 = (StringConstant) args[0];
      StringConstant arg1 = (StringConstant) args[1];

      int comp = compareString(arg0, arg1);

      IntConstant ret = new IntConstant(comp);
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "StrGt";
    }

  }

  public static class CountChar extends Production {

    public CountChar() {
      super();
    }

    public CountChar(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;

      StringConstant arg0 = (StringConstant) args[0];
      CharConstant arg1 = (CharConstant) args[1];

      String str = arg0.value;
      char c = arg1.value;

      int count = 0;
      for (int i = 0; i < str.length(); i ++) {
        if (str.charAt(i) == c) count ++;
      }

      IntConstant ret = new IntConstant(count);
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "CountChar";
    }

  }

  public static class Len extends Production {

    public Len() {
      super();
    }

    public Len(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;

      StringConstant arg0 = (StringConstant) args[0];

      IntConstant ret = new IntConstant(arg0.value.length());
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "Len";
    }

  }

  public static class ToInt extends Production {

    public ToInt() {
      super();
    }

    public ToInt(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;

      if (args[0] instanceof ErrConstant) return ErrConstant.v();

      StringConstant arg0 = (StringConstant) args[0];
      String str = arg0.value;

      // NOTE: assume no integer overflow 
      int num = 0;
      for (int i = 0; i < str.length(); i ++) {
        char c = str.charAt(i);
        if (c >= '0' && c <= '9') {
          num = num * 10 + (c - '0');
        } else {
          return ErrConstant.v();
        }
      }

      IntConstant ret = new IntConstant(num);
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "ToInt";
    }

  }

  public static class SubStr extends Production {

    public SubStr() {
      super();
    }

    public SubStr(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {

      assert (args.length == 3) : args.length;

      if (args[1] instanceof ErrConstant || args[2] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      StringConstant arg0 = (StringConstant) args[0];
      IntConstant arg1 = (IntConstant) args[1];
      IntConstant arg2 = (IntConstant) args[2];

      int p1 = arg1.value;
      int p2 = arg2.value;
      String str = arg0.value;

      if (p1 >= 0 && p2 >= 0 && p1 < p2 && p2 <= str.length()) {
        String substr = str.substring(p1, p2);
        return new StringConstant(substr);
      } else {
        return ErrConstant.v();
      }
    }

    @Override
    public String translateToProgramText() {
      return "SubStr";
    }

  }

  public static class Id3 extends Production {

    public Id3() {
      super();
    }

    public Id3(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      return args[0];
    }

    @Override
    public String translateToProgramText() {
      return "Id3";
    }

  }

  public static class StartPos extends Production {

    public StartPos() {
      super();
    }

    public StartPos(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 3) : args.length;

      StringConstant x = (StringConstant) args[0];
      IntConstant t = (IntConstant) args[1];
      IntConstant k = (IntConstant) args[2];

      Pattern pattern = Tokens.tokenToPattern.get(t.value);
      Matcher matcher = pattern.matcher(x.value);
      List<Pair<Integer, Integer>> list = new ArrayList<>();
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        Pair<Integer, Integer> pair = Pair.with(start, end);
        list.add(pair);
      }

      int kval = k.value;
      if (kval > 0) {
        kval --;
      } else {
        kval = list.size() + kval;
      }
      if (kval >= 0 && kval < list.size()) {
        int start = list.get(kval).getValue0();
        return new IntConstant(start);
      } else {
        return ErrConstant.v();
      }
    }

    @Override
    public String translateToProgramText() {
      return "StartPos";
    }

  }

  public static class EndPos extends Production {

    public EndPos() {
      super();
    }

    public EndPos(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 3) : args.length;

      StringConstant x = (StringConstant) args[0];
      IntConstant t = (IntConstant) args[1];
      IntConstant k = (IntConstant) args[2];

      Pattern pattern = Tokens.tokenToPattern.get(t.value);
      Matcher matcher = pattern.matcher(x.value);
      List<Pair<Integer, Integer>> list = new ArrayList<>();
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        Pair<Integer, Integer> pair = Pair.with(start, end);
        list.add(pair);
      }

      int kval = k.value;
      if (kval > 0) {
        kval --;
      } else {
        kval = list.size() + kval;
      }
      if (kval >= 0 && kval < list.size()) {
        int end = list.get(kval).getValue1();
        return new IntConstant(end);
      } else {
        return ErrConstant.v();
      }
    }

    @Override
    public String translateToProgramText() {
      return "EndPos";
    }

  }

  public static class End extends Production {

    public End() {
      super();
    }

    public End(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;

      StringConstant x = (StringConstant) args[0];

      IntConstant ret = new IntConstant(x.value.length());
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "End";
    }

  }

  public static class Tokens {

    public static Map<Integer, Pattern> tokenToPattern = new HashMap<>();
    public static Map<Integer, Integer> tokenToCost = new HashMap<>();

    static {
      // tokens in BlinkFill 

      Pattern Digits = Pattern.compile("\\d+");
      Pattern Words = Pattern.compile("[a-zA-Z]+");

//      Pattern Alphanumeric = Pattern.compile("[\\w]+");
//      Pattern ProperCase = Pattern.compile("[A-Z][a-z]+");
//      Pattern CAPS = Pattern.compile("[A-Z]+");
//      Pattern lowercase = Pattern.compile("[a-z]+");
//      Pattern Alphabets = Pattern.compile("[\\w&&\\D]+");
//      Pattern Whitespace = Pattern.compile("\\s+");
//      Pattern StartT = Pattern.compile("^");
//      Pattern EndT = Pattern.compile("$");
//      Pattern ProperCaseWSpaces = Pattern.compile("([A-Z][a-z]+)((\\s+)([A-Z][a-z]+))*");
//      Pattern CAPSWSpaces = Pattern.compile("([A-Z]+)((\\s+)([A-Z]+))*");
//      Pattern lowercaseWSpaces = Pattern.compile("([a-z]+)((\\s+)([a-z]+))*");
//      Pattern AlphabetWSpaces = Pattern.compile("([\\w&&\\D]+)((\\s+)([\\w&&\\D]+))*");

      tokenToPattern.put(0, Digits);
      tokenToCost.put(0, 10);

      tokenToPattern.put(1, Words);
      tokenToCost.put(1, 10);

    }

  }

  public static class ConstPos extends Production {

    public ConstPos() {
      super();
    }

    public ConstPos(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      return args[0];
    }

    @Override
    public String translateToProgramText() {
      return "ConstPos";
    }

  }

  public static class Id1 extends Production {

    public Id1() {
      super();
    }

    public Id1(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      return args[0];
    }

    @Override
    public String translateToProgramText() {
      return "Id1";
    }

  }

  public static class Id2 extends Production {

    public Id2() {
      super();
    }

    public Id2(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public Value exec(Value... args) {
      return args[0];
    }

    @Override
    public String translateToProgramText() {
      return "Id2";
    }

  }

}
