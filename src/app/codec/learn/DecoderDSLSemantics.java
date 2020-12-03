package app.codec.learn;

import java.util.ArrayList;
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

public class DecoderDSLSemantics {

  public static Map<String, Production> getDSLConstructMap() {
    Map<String, Production> map = new HashMap<>();
    map.put("Id2", new Id2());
    map.put("LSBReshape", new LSBReshape());
    map.put("AsUnicode", new AsUnicode());
    map.put("Dec64", new Dec64());
    map.put("Dec32", new Dec32());
    map.put("Dec16", new Dec16());
    map.put("Dec64XML", new Dec64XML());
    map.put("DecUU", new DecUU());
    map.put("Dec32Hex", new Dec32Hex());
    map.put("Id1", new Id1());
    map.put("RemovePad", new RemovePad());
    map.put("Substr", new Substr());
    map.put("DecUTF8", new DecUTF8());
    map.put("DecUTF16", new DecUTF16());
    map.put("DecUTF32", new DecUTF32());
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

  public static class LSBReshape extends Production {

    public LSBReshape() {
      super();
    }

    public LSBReshape(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue lsbReshape(StringConstant str, IntConstant radix) {
      String strValue = str.value;
      int radixValue = radix.value;
      if (radixValue <= 0 || radixValue > 8) return ErrConstant.v();
      assert radixValue >= 1 && radixValue <= 8 : radixValue;
      byte[] bytes = StringUtil.toByteArray(strValue);
      int mod = (bytes.length * radixValue) % 8;
      // valid length by trimming zeros
      int len = bytes.length * radixValue - mod;
      // reshape the bit stream
      ArrayList<Byte> list = new ArrayList<>();
      int tgtHigh = 7;
      int srcIndex = 0;
      int srcHigh = radixValue - 1;
      int srcLow = 0;
      byte tgt = 0x0;
      for (int bitsAssigned = 0; bitsAssigned < len;) {
        tgt = assignBits(tgt, tgtHigh, bytes[srcIndex], srcHigh, srcLow);
        int assignedLen = srcHigh - srcLow + 1;
        bitsAssigned += assignedLen;
        // compute the new high index for target
        if (bitsAssigned % 8 == 0) {
          list.add(tgt);
          tgt = 0x0;
          tgtHigh = 7;
        } else {
          tgtHigh -= assignedLen;
        }
        // compute the new srcIndex and high index for source
        int numOfBitsToProvide = srcHigh + 1 - assignedLen;
        if (numOfBitsToProvide == 0) {
          ++srcIndex;
          srcHigh = radixValue - 1;
          numOfBitsToProvide = radixValue;
        } else {
          srcHigh -= assignedLen;
        }
        // compute the new low index for source
        int numOfBitsToFill = tgtHigh + 1;
        if (numOfBitsToProvide <= numOfBitsToFill) {
          srcLow = 0;
        } else {
          srcLow = srcHigh - numOfBitsToFill + 1;
        }
      }
      // convert list of bytes to byte array
      byte[] ret = new byte[len / 8];
      for (int i = 0; i < ret.length; ++i) {
        ret[i] = list.get(i);
      }
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    // assign src[srcHigh..srcLow] to tgt[tgtHigh..]
    private byte assignBits(byte tgt, int tgtHigh, byte src, int srcHigh, int srcLow) {
      for (int i = srcHigh; i >= srcLow; --i) {
        if (((src >> i) & 1) == 1) {
          tgt |= 1 << i + tgtHigh - srcHigh;
        }
      }
      return tgt;
    }

    @Override
    public String translateToProgramText() {
      return "LSBReshape";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;
      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      assert (args[1] instanceof IntConstant) : args[1];
      return lsbReshape((StringConstant) args[0], ((IntConstant) args[1]));
    }

  }

  public static class AsUnicode extends Production {

    public AsUnicode() {
      super();
    }

    public AsUnicode(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue asUnicode(IntArrayConstant str) {
      int[] codepoints = str.value;
      List<Character> unicodes = new ArrayList<>();
      for (int i = 0; i < codepoints.length; ++i) {
        int codepoint = codepoints[i];
        if (codepoint < 0) {
          return ErrConstant.v();
        } else if (codepoint <= 0xffff) {
          unicodes.add((char) codepoint);
        } else if (codepoint <= 0x10ffff) {
          char[] chars = Character.toChars(codepoint);
          assert chars.length == 2;
          unicodes.add(chars[0]);
          unicodes.add(chars[1]);
        } else {
          return ErrConstant.v();
        }
      }
      StringBuilder ret = new StringBuilder();
      for (char unicode : unicodes) {
        ret.append(unicode);
      }
      return new StringConstant(ret.toString());
    }

    @Override
    public String translateToProgramText() {
      return "AsUnicode";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof IntArrayConstant) : args[0];
      return asUnicode((IntArrayConstant) args[0]);
    }

  }

  public static class Dec64 extends Production {

    public Dec64() {
      super();
    }

    public Dec64(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue dec64(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b >= 0x41 && b <= 0x5a) { // A - Z
          ret[i] = (byte) (b - 0x41);
        } else if (b >= 0x61 && b <= 0x7a) { // a - z
          ret[i] = (byte) (b - 0x47);
        } else if (b >= 0x30 && b <= 0x39) { // 0 - 9
          ret[i] = (byte) (b + 0x4);
        } else if (b == 0x2b) { // +
          ret[i] = 0x3e;
        } else if (b == 0x2f) { // /
          ret[i] = 0x3f;
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Dec64";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return dec64((StringConstant) args[0]);
    }

  }

  public static class Dec32 extends Production {

    public Dec32() {
      super();
    }

    public Dec32(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue dec32(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b >= 0x41 && b <= 0x5a) { // A - Z
          ret[i] = (byte) (b - 0x41);
        } else if (b >= 0x32 && b <= 0x37) { // 2 - 7
          ret[i] = (byte) (b - 0x18);
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Dec32";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return dec32((StringConstant) args[0]);
    }

  }

  public static class Dec16 extends Production {

    public Dec16() {
      super();
    }

    public Dec16(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue dec16(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b >= 0x30 && b <= 0x39) { // 0 - 9
          ret[i] = (byte) (b - 0x30);
        } else if (b >= 0x41 && b <= 0x46) { // A - F
          ret[i] = (byte) (b - 0x37);
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Dec16";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return dec16((StringConstant) args[0]);
    }

  }

  public static class Dec64XML extends Production {

    public Dec64XML() {
      super();
    }

    public Dec64XML(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue dec64XML(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b >= 0x41 && b <= 0x5a) { // A - Z
          ret[i] = (byte) (b - 0x41);
        } else if (b >= 0x61 && b <= 0x7a) { // a - z
          ret[i] = (byte) (b - 0x47);
        } else if (b >= 0x30 && b <= 0x39) { // 0 - 9
          ret[i] = (byte) (b + 0x4);
        } else if (b == 0x2e) { // .
          ret[i] = 0x3e;
        } else if (b == 0x2d) { // -
          ret[i] = 0x3f;
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Dec64XML";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return dec64XML((StringConstant) args[0]);
    }

  }

  public static class DecUU extends Production {

    public DecUU() {
      super();
    }

    public DecUU(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue decUU(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b >= 0x20 && b <= 0x5f) { // SP - _
          ret[i] = (byte) (b - 0x20);
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "DecUU";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return decUU((StringConstant) args[0]);
    }

  }

  public static class Dec32Hex extends Production {

    public Dec32Hex() {
      super();
    }

    public Dec32Hex(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue dec32Hex(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      byte[] ret = new byte[bytes.length];
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (b >= 0x30 && b <= 0x39) { // 0 - 9
          ret[i] = (byte) (b - 0x30);
        } else if (b >= 0x41 && b <= 0x56) { // A - V
          ret[i] = (byte) (b - 0x37);
        } else {
          return ErrConstant.v();
        }
      }
      // covert it to String
      return new StringConstant(StringUtil.byteArrayToString(ret));
    }

    @Override
    public String translateToProgramText() {
      return "Dec32Hex";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return dec32Hex((StringConstant) args[0]);
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

  public static class RemovePad extends Production {

    public RemovePad() {
      super();
    }

    public RemovePad(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue removePad(StringConstant str, CharConstant ch) {
      StringBuilder builder = new StringBuilder(str.value);
      char charValue = ch.value;
      int startIndex = builder.length();
      for (int i = startIndex - 1; i >= 0; --i) {
        if (builder.charAt(i) == charValue) {
          --startIndex;
        } else {
          break;
        }
      }
      builder.delete(startIndex, builder.length());
      return new StringConstant(builder.toString());
    }

    @Override
    public String translateToProgramText() {
      return "RemovePad";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;
      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      assert (args[1] instanceof CharConstant) : args[1];
      return removePad((StringConstant) args[0], (CharConstant) args[1]);
    }

  }

  public static class Substr extends Production {

    public Substr() {
      super();
    }

    public Substr(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue substr(StringConstant str, IntConstant startIndex) {
      String strValue = str.value;
      int index = startIndex.value;
      if (index >= strValue.length()) return ErrConstant.v();
      return new StringConstant(strValue.substring(index));
    }

    @Override
    public String translateToProgramText() {
      return "Substr";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;
      if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      assert (args[1] instanceof IntConstant) : args[1];
      return substr((StringConstant) args[0], (IntConstant) args[1]);
    }

  }

  public static class DecUTF8 extends Production {

    public DecUTF8() {
      super();
    }

    public DecUTF8(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue decUTF8(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      List<Integer> codepoints = new ArrayList<>();
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        if (((b & 0xff) >>> 7) == 0x00) { // U+0000 - U+007F
          int codepoint = (int) b;
          codepoints.add(codepoint);
        } else if (((b & 0xff) >>> 5) == 0x06) { // U+0080 - U+07FF
          if (i + 1 >= bytes.length) return ErrConstant.v();
          byte b1 = bytes[++i];
          if (((b1 & 0xff) >>> 6) != 0x02) {
            return ErrConstant.v();
          }
          int codepoint = (int) (((b & 0x1f) << 6) | (b1 & 0x3f));
          codepoints.add(codepoint);
        } else if (((b & 0xff) >>> 4) == 0x0e) { // U+0800 - U+FFFF
          if (i + 2 >= bytes.length) return ErrConstant.v();
          byte b1 = bytes[++i];
          byte b2 = bytes[++i];
          if (((b1 & 0xff) >>> 6) != 0x02 || ((b2 & 0xff) >>> 6) != 0x02) {
            return ErrConstant.v();
          }
          int codepoint = (int) (((b & 0x0f) << 12) | ((b1 & 0x3f) << 6) | (b2 & 0x3f));
          codepoints.add(codepoint);
        } else if (((b & 0xff) >>> 3) == 0x1e) { // U+10000 - U+10FFFF
          if (i + 3 >= bytes.length) return ErrConstant.v();
          byte b1 = bytes[++i];
          byte b2 = bytes[++i];
          byte b3 = bytes[++i];
          if (((b1 & 0xff) >>> 6) != 0x02 || ((b2 & 0xff) >>> 6) != 0x02 || ((b3 & 0xff) >>> 6) != 0x02) {
            return ErrConstant.v();
          }
          int codepoint = (int) (((b & 0x07) << 18) | ((b1 & 0x3f) << 12) | ((b2 & 0x3f) << 6) | (b3 & 0x3f));
          codepoints.add(codepoint);
        } else {
          return ErrConstant.v();
        }
      }
      return new IntArrayConstant(codepoints.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public String translateToProgramText() {
      return "DecUTF8";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return decUTF8((StringConstant) args[0]);
    }

  }

  public static class DecUTF16 extends Production {

    public DecUTF16() {
      super();
    }

    public DecUTF16(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue decUTF16(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      if (bytes.length % 2 != 0) return ErrConstant.v();
      List<Integer> codepoints = new ArrayList<>();
      for (int i = 0; i < bytes.length; ++i) {
        byte b = bytes[i];
        byte b1 = bytes[++i];
        if (((b & 0xff) >>> 2) != 0x36) { // U+0000 - U+FFFF
          int codepoint = (int) (((b & 0xff) << 8) | (b1 & 0xff));
          codepoints.add(codepoint);
        } else if (((b & 0xff) >>> 2) == 0x36) { // U+10000 - U+10FFFF
          if (i + 2 >= bytes.length) return ErrConstant.v();
          byte b2 = bytes[++i];
          byte b3 = bytes[++i];
          if (((b2 & 0xff) >>> 2) != 0x37) return ErrConstant.v();
          int codepoint = ((int) (((b & 0x03) << 18) | ((b1 & 0xff) << 10) | ((b2 & 0x03) << 8) | (b3 & 0xff))) + 0x10000;
          codepoints.add(codepoint);
        } else {
          return ErrConstant.v();
        }
      }
      return new IntArrayConstant(codepoints.stream().mapToInt(Integer::intValue).toArray());
    }

    @Override
    public String translateToProgramText() {
      return "DecUTF16";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return decUTF16((StringConstant) args[0]);
    }

  }

  public static class DecUTF32 extends Production {

    public DecUTF32() {
      super();
    }

    public DecUTF32(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue decUTF32(StringConstant str) {
      byte[] bytes = StringUtil.toByteArray(str.value);
      if (bytes.length % 4 != 0) return ErrConstant.v();
      int[] codepoints = new int[bytes.length / 4];
      for (int i = 0; i < bytes.length; i += 4) {
        byte b = bytes[i];
        byte b1 = bytes[i + 1];
        byte b2 = bytes[i + 2];
        byte b3 = bytes[i + 3];
        codepoints[i / 4] = (int) (((b & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff));
      }
      return new IntArrayConstant(codepoints);
    }

    @Override
    public String translateToProgramText() {
      return "DecUTF32";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }
      assert (args[0] instanceof StringConstant) : args[0];
      return decUTF32((StringConstant) args[0]);
    }

  }

}
