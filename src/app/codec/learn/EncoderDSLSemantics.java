package app.codec.learn;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.IntArrayConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.util.StringUtil;

public class EncoderDSLSemantics {

  public static Map<String, Production> getDSLConstructMap() {
    Map<String, Production> map = new HashMap<>();
    map.put("Id2", new Id2());
    map.put("PadToMultiple", new PadToMultiple());
    map.put("HeaderUU", new HeaderUU());
    map.put("Enc64", new Enc64());
    map.put("Enc32", new Enc32());
    map.put("Enc16", new Enc16());
    map.put("Enc64XML", new Enc64XML());
    map.put("EncUU", new EncUU());
    map.put("Enc32Hex", new Enc32Hex());
    map.put("Id1", new Id1());
    map.put("Reshape", new Reshape());
    map.put("EncUTF8", new EncUTF8());
    map.put("EncUTF16", new EncUTF16());
    map.put("EncUTF32", new EncUTF32());
    map.put("CodePoint", new CodePoint());
    return map;
  }

  public static class Id2 extends Production {

    public Id2() {
      super();
    }

    public Id2(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public String translateToProgramText() {
      return "Id2";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      return args[0];
    }

  }

  public static class PadToMultiple extends Production {

    public PadToMultiple() {
      super();
    }

    public PadToMultiple(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue padToMultiple(StringConstant str, IntConstant num, CharConstant ch) {
      String strValue = str.value;
      int mod = num.value;
      char charValue = ch.value;
      int len = strValue.length();
      int padLen = (len % mod == 0) ? 0 : (mod - len % mod);
      StringBuilder builder = new StringBuilder(strValue);
      for (int i = 0; i < padLen; ++i) {
        builder.append(charValue);
      }
      return new StringConstant(builder.toString());
    }

    @Override
    public String translateToProgramText() {
      return "PadToMultiple";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 3) : args.length;
      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant || args[2] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      assert (args[1] instanceof IntConstant) : args[1];
      assert (args[2] instanceof CharConstant) : args[1];
      return padToMultiple((StringConstant) args[0], (IntConstant) args[1], (CharConstant) args[2]);
    }

  }

  public static class HeaderUU extends Production {

    public HeaderUU() {
      super();
    }

    public HeaderUU(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue headerUU(StringConstant str) {
      int len = str.value.length();
      if (len % 4 != 0) return ErrConstant.v();
      int padLen = 0;
      if (str.value.charAt(len - 1) == '`') ++padLen;
      if (str.value.charAt(len - 2) == '`') ++padLen;
      String header = String.valueOf((char) (len / 4 * 3 - padLen + 32));
      return new StringConstant(header + str.value);
    }

    @Override
    public String translateToProgramText() {
      return "HeaderUU";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return headerUU((StringConstant) args[0]);
    }

  }

  public static class Enc64 extends Production {

    public Enc64() {
      super();
    }

    public Enc64(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue enc64(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b < 0) {
          return ErrConstant.v();
        } else if (b <= 0x19) {
          ret[i] = (byte) (b + 0x41); // A - Z
        } else if (b <= 0x33) {
          ret[i] = (byte) (b + 0x47); // a - z
        } else if (b <= 0x3d) {
          ret[i] = (byte) (b - 0x04); // 0 - 9
        } else if (b == 0x3e) {
          ret[i] = 0x2b; // +
        } else if (b == 0x3f) {
          ret[i] = 0x2f; // /
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Enc64";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return enc64((StringConstant) args[0]);
    }

  }

  public static class Enc32 extends Production {

    public Enc32() {
      super();
    }

    public Enc32(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue enc32(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b < 0) {
          return ErrConstant.v();
        } else if (b <= 0x19) {
          ret[i] = (byte) (b + 0x41); // A - Z
        } else if (b <= 0x1f) {
          ret[i] = (byte) (b + 0x18); // 2 - 7
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Enc32";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return enc32((StringConstant) args[0]);
    }

  }

  public static class Enc16 extends Production {

    public Enc16() {
      super();
    }

    public Enc16(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue enc16(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b < 0) {
          return ErrConstant.v();
        } else if (b <= 0x9) { // 0 - 9
          ret[i] = (byte) (b + 0x30);
        } else if (b <= 0xf) { // A - F
          ret[i] = (byte) (b + 0x37);
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Enc16";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return enc16((StringConstant) args[0]);
    }

  }

  public static class Enc64XML extends Production {

    public Enc64XML() {
      super();
    }

    public Enc64XML(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue enc64XML(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b < 0) {
          return ErrConstant.v();
        } else if (b <= 0x19) {
          ret[i] = (byte) (b + 0x41); // A - Z
        } else if (b <= 0x33) {
          ret[i] = (byte) (b + 0x47); // a - z
        } else if (b <= 0x3d) {
          ret[i] = (byte) (b - 0x04); // 0 - 9
        } else if (b == 0x3e) {
          ret[i] = 0x2e; // .
        } else if (b == 0x3f) {
          ret[i] = 0x2d; // -
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Enc64XML";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return enc64XML((StringConstant) args[0]);
    }

  }

  public static class EncUU extends Production {

    public EncUU() {
      super();
    }

    public EncUU(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue encUU(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b < 0) {
          return ErrConstant.v();
        } else if (b <= 0x3f) {
          ret[i] = (byte) (b + 0x20); // SP - _
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "EncUU";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return encUU((StringConstant) args[0]);
    }

  }

  public static class Enc32Hex extends Production {

    public Enc32Hex() {
      super();
    }

    public Enc32Hex(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue enc32Hex(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b < 0) {
          return ErrConstant.v();
        } else if (b <= 0x09) {
          ret[i] = (byte) (b + 0x30); // 0 - 9
        } else if (b <= 0x1f) {
          ret[i] = (byte) (b + 0x37); // A - V
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Enc32Hex";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return enc32Hex((StringConstant) args[0]);
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
    public String translateToProgramText() {
      return "Id1";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      return args[0];
    }

  }

  public static class Reshape extends Production {

    public Reshape() {
      super();
    }

    public Reshape(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue reshape(StringConstant str, IntConstant radix) {
      String strValue = str.value;
      int radixValue = radix.value;
      if (radixValue <= 0 || radixValue > 8) return ErrConstant.v();
      assert radixValue > 0 && radixValue <= 8 : radixValue;
      BitSet bitSet = BitSet.valueOf(StringUtil.toByteArray(reverseString(strValue)));
      // left shift the bits for alignment, with 0 for padding
      int numOfBits = strValue.length() * 8;
      int amountOfShift = (numOfBits % radixValue == 0) ? 0 : (radixValue - numOfBits % radixValue);
      bitSet = leftShift(bitSet, amountOfShift);
      // reshape the byte array
      int len = numOfBits + amountOfShift;
      assert len % radixValue == 0;
      byte[] ret = new byte[len / radixValue];
      for (int high = len; high > 0; high -= radixValue) {
        int low = high - radixValue;
        byte[] singleton = bitSet.get(low, high).toByteArray();
        int index = ret.length - 1 - low / radixValue;
        if (singleton.length == 0) {
          // handle all 0 bits
          ret[index] = 0x0;
        } else {
          assert singleton.length == 1 : low + " to " + high;
          ret[index] = singleton[0];
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    // reverse a string
    private String reverseString(String src) {
      int i, len = src.length();
      StringBuilder dest = new StringBuilder(len);
      for (i = (len - 1); i >= 0; i --) {
        dest.append(src.charAt(i));
      }
      return dest.toString();
    }

    // left shift a bit vector by num bits
    private BitSet leftShift(BitSet bitSet, int num) {
      assert num >= 0 : num;
      if (num == 0) return bitSet;
      BitSet ret = new BitSet();
      int index = bitSet.length() - 1;
      while (index >= 0) {
        ret.set(index + num);
        index = bitSet.previousSetBit(index - 1);
      }
      return ret;
    }

    @Override
    public String translateToProgramText() {
      return "Reshape";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;
      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      assert (args[1] instanceof IntConstant) : args[1];
      return reshape((StringConstant) args[0], ((IntConstant) args[1]));
    }

  }

  public static class EncUTF8 extends Production {

    public EncUTF8() {
      super();
    }

    public EncUTF8(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue encUTF8(IntArrayConstant array) {
      int[] codepoints = array.value;
      List<Byte> ret = new ArrayList<>();
      for (int codepoint : codepoints) {
        if (codepoint < 0) {
          return ErrConstant.v();
        } else if (codepoint <= 0x007f) {
          ret.add((byte) (codepoint & 0x007f));
        } else if (codepoint <= 0x07ff) {
          ret.add((byte) (((codepoint & 0x07c0) >>> 6) | 0xc0));
          ret.add((byte) ((codepoint & 0x003f) | 0x80));
        } else if (codepoint <= 0xffff) {
          ret.add((byte) (((codepoint & 0xf000) >>> 12) | 0xe0));
          ret.add((byte) (((codepoint & 0x0fc0) >>> 6) | 0x80));
          ret.add((byte) ((codepoint & 0x003f) | 0x80));
        } else if (codepoint <= 0x10ffff) {
          ret.add((byte) (((codepoint & 0x1c0000) >>> 18) | 0xf0));
          ret.add((byte) (((codepoint & 0x3f000) >>> 12) | 0x80));
          ret.add((byte) (((codepoint & 0x0fc0) >>> 6) | 0x80));
          ret.add((byte) ((codepoint & 0x003f) | 0x80));
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteListToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "EncUTF8";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof IntArrayConstant) : args[0];
      return encUTF8((IntArrayConstant) args[0]);
    }

  }

  public static class EncUTF16 extends Production {

    public EncUTF16() {
      super();
    }

    public EncUTF16(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue encUTF16(IntArrayConstant array) {
      int[] codepoints = array.value;
      List<Byte> ret = new ArrayList<>();
      for (int codepoint : codepoints) {
        if (codepoint < 0) {
          return ErrConstant.v();
        } else if (codepoint <= 0xffff) {
          ret.add((byte) ((codepoint & 0xff00) >>> 8));
          ret.add((byte) (codepoint & 0x00ff));
        } else if (codepoint <= 0x10ffff) {
          int diff = codepoint - 0x10000;
          ret.add((byte) (((diff & 0xc0000) >>> 18) | 0xd8));
          ret.add((byte) ((diff & 0x3fc00) >>> 10));
          ret.add((byte) (((diff & 0x00300) >>> 8) | 0xdc));
          ret.add((byte) (diff & 0x000ff));
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteListToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "EncUTF16";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof IntArrayConstant) : args[0];
      return encUTF16((IntArrayConstant) args[0]);
    }

  }

  public static class EncUTF32 extends Production {

    public EncUTF32() {
      super();
    }

    public EncUTF32(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue encUTF32(IntArrayConstant array) {
      int[] codepoints = array.value;
      List<Byte> ret = new ArrayList<>();
      for (int codepoint : codepoints) {
        if (codepoint < 0) {
          return ErrConstant.v();
        } else {
          ret.add((byte) ((codepoint & 0x7f000000) >>> 24));
          ret.add((byte) ((codepoint & 0x00ff0000) >>> 16));
          ret.add((byte) ((codepoint & 0x0000ff00) >>> 8));
          ret.add((byte) (codepoint & 0x000000ff));
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteListToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "EncUTF32";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof IntArrayConstant) : args[0];
      return encUTF32((IntArrayConstant) args[0]);
    }

  }

  public static class CodePoint extends Production {

    public CodePoint() {
      super();
    }

    public CodePoint(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue codePoint(StringConstant str) {
      String strValue = str.value;
      int[] ret = new int[strValue.length()];
      for (int i = 0; i < strValue.length(); ++i) {
        char ch = strValue.charAt(i);
        // TODO: only works for Unicode less than U+FFFF
        ret[i] = (int) ch;
      }
      return new IntArrayConstant(ret);
    }

    @Override
    public String translateToProgramText() {
      return "CodePoint";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return codePoint((StringConstant) args[0]);
    }

  }

}
