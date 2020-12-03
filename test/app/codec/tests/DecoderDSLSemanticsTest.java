package app.codec.tests;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import app.codec.learn.DecoderDSLSemantics;
import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.util.StringUtil;

public class DecoderDSLSemanticsTest {

  @Test
  public void testRemovePad1() {
    StringConstant str = new StringConstant("TWFu");
    CharConstant ch = new CharConstant('=');
    Production production = new DecoderDSLSemantics.RemovePad(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, ch);
    StringConstant result = new StringConstant("TWFu");
    Assert.assertTrue(result.equals(ret));
  }

  @Test
  public void testRemovePad2() {
    StringConstant str = new StringConstant("TQ==");
    CharConstant ch = new CharConstant('=');
    Production production = new DecoderDSLSemantics.RemovePad(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, ch);
    StringConstant result = new StringConstant("TQ");
    Assert.assertTrue(result.equals(ret));
  }

  @Test
  public void testRemovePad3() {
    StringConstant str = new StringConstant("TWE=");
    CharConstant ch = new CharConstant('=');
    Production production = new DecoderDSLSemantics.RemovePad(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, ch);
    StringConstant result = new StringConstant("TWE");
    Assert.assertTrue(result.equals(ret));
  }

  @Test
  public void testLSBReshape1() {
    byte[] bytes = new byte[] { 19, 22, 5, 46 };
    StringConstant str = new StringConstant(StringUtil.byteArrayToString(bytes));
    IntConstant radix = new IntConstant(6);
    Production production = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, radix);
    byte[] result = new byte[] { 77, 97, 110 };
    Assert.assertTrue(Arrays.equals(ret.value.getBytes(), result));
  }

  @Test
  public void testLSBReshape2() {
    byte[] bytes = new byte[] { 19, 16 };
    StringConstant str = new StringConstant(StringUtil.byteArrayToString(bytes));
    IntConstant radix = new IntConstant(6);
    Production production = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, radix);
    byte[] result = new byte[] { 77 };
    Assert.assertTrue(Arrays.equals(ret.value.getBytes(), result));
  }

  @Test
  public void testLSBReshape3() {
    byte[] bytes = new byte[] { 19, 22, 4 };
    StringConstant str = new StringConstant(StringUtil.byteArrayToString(bytes));
    IntConstant radix = new IntConstant(6);
    Production production = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, radix);
    byte[] result = new byte[] { 77, 97 };
    Assert.assertTrue(Arrays.equals(ret.value.getBytes(), result));
  }

  @Test
  public void testDec64() {
    StringConstant str = new StringConstant("TWFu");
    IntConstant radix = new IntConstant(6);
    Production dec64 = new DecoderDSLSemantics.Dec64(0, null, null, new Symbol[] {}, 0);
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    StringConstant decoded = (StringConstant) dec64.exec(str);
    StringConstant ret = (StringConstant) lsbReshape.exec(decoded, radix);
    StringConstant result = new StringConstant("Man");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testDec32() {
    StringConstant str = new StringConstant("JVQW4");
    IntConstant radix = new IntConstant(5);
    Production dec32 = new DecoderDSLSemantics.Dec32(0, null, null, new Symbol[] {}, 0);
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    StringConstant decoded = (StringConstant) dec32.exec(str);
    StringConstant ret = (StringConstant) lsbReshape.exec(decoded, radix);
    StringConstant result = new StringConstant("Man");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testDec16() {
    StringConstant str = new StringConstant("4D616E");
    IntConstant radix = new IntConstant(4);
    Production dec16 = new DecoderDSLSemantics.Dec16(0, null, null, new Symbol[] {}, 0);
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    StringConstant decoded = (StringConstant) dec16.exec(str);
    StringConstant ret = (StringConstant) lsbReshape.exec(decoded, radix);
    StringConstant result = new StringConstant("Man");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Dec1() {
    StringConstant str = new StringConstant("TWFu");
    StringConstant ret = base64Dec(str);
    StringConstant result = new StringConstant("Man");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Dec2() {
    StringConstant str = new StringConstant("TQ==");
    StringConstant ret = base64Dec(str);
    StringConstant result = new StringConstant("M");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Dec3() {
    StringConstant str = new StringConstant("TWE=");
    StringConstant ret = base64Dec(str);
    StringConstant result = new StringConstant("Ma");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Dec4() {
    StringConstant str = new StringConstant("IA/5");
    StringConstant ret = base64Dec(str);
    char[] resultCharArray = new char[] { 32, 15, 249 };
    StringConstant result = new StringConstant(new String(resultCharArray));
    Assert.assertTrue(ret.equals(result));
  }

  private StringConstant base64Dec(StringConstant x) {
    Production removePad = new DecoderDSLSemantics.RemovePad(0, null, null, new Symbol[] {}, 0);
    Production dec64 = new DecoderDSLSemantics.Dec64(0, null, null, new Symbol[] {}, 0);
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    IntConstant six = new IntConstant(6);
    CharConstant eq = new CharConstant('=');
    return (StringConstant) lsbReshape.exec((StringConstant) dec64.exec(removePad.exec(x, eq)), six);
  }

  @Test
  public void testBase32Dec1() {
    StringConstant str = new StringConstant("JVQW4===");
    StringConstant ret = base32Dec(str);
    StringConstant result = new StringConstant("Man");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase32Dec2() {
    StringConstant str = new StringConstant("JU======");
    StringConstant ret = base32Dec(str);
    StringConstant result = new StringConstant("M");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase32Enc3() {
    StringConstant str = new StringConstant("JVQQ====");
    StringConstant ret = base32Dec(str);
    StringConstant result = new StringConstant("Ma");
    Assert.assertTrue(ret.equals(result));
  }

  private StringConstant base32Dec(StringConstant x) {
    Production removePad = new DecoderDSLSemantics.RemovePad(0, null, null, new Symbol[] {}, 0);
    Production dec32 = new DecoderDSLSemantics.Dec32(0, null, null, new Symbol[] {}, 0);
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    IntConstant five = new IntConstant(5);
    CharConstant eq = new CharConstant('=');
    return (StringConstant) lsbReshape.exec((StringConstant) dec32.exec(removePad.exec(x, eq)), five);
  }

  @Test
  public void testBase16Dec() {
    StringConstant str = new StringConstant("4D616E");
    StringConstant ret = base16Dec(str);
    StringConstant result = new StringConstant("Man");
    Assert.assertTrue(ret.equals(result));
  }

  private StringConstant base16Dec(StringConstant x) {
    Production dec16 = new DecoderDSLSemantics.Dec16(0, null, null, new Symbol[] {}, 0);
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape(0, null, null, new Symbol[] {}, 0);
    IntConstant four = new IntConstant(4);
    return (StringConstant) lsbReshape.exec(dec16.exec(x), four);
  }

  @Test
  public void testUUDec1() {
    StringConstant str = new StringConstant("#0V%T");
    Value ret = uuDec(str);
    Value result = new StringConstant("Cat");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testUUDec2() {
    StringConstant str = new StringConstant("!0P``");
    Value ret = uuDec(str);
    Value result = new StringConstant("C");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testUUDec3() {
    StringConstant str = new StringConstant("%36]U<V4`");
    Value ret = uuDec(str);
    Value result = new StringConstant("Mouse");
    Assert.assertTrue(ret.equals(result));
  }

  private Value uuDec(StringConstant x) {
    Production decUU = new DecoderDSLSemantics.DecUU();
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape();
    Production removePad = new DecoderDSLSemantics.RemovePad();
    Production substr = new DecoderDSLSemantics.Substr();
    IntConstant one = new IntConstant(1);
    IntConstant six = new IntConstant(6);
    CharConstant ch = new CharConstant('`');
    return lsbReshape.exec(decUU.exec(removePad.exec(substr.exec(x, one), ch)), six);
  }

  @Test
  public void testUTF16Dec() {
    StringConstant str = new StringConstant("20A1");
    Value ret = utf16Dec(str);
    Value result = new StringConstant("₡");
    Assert.assertTrue(ret.equals(result));
  }

  private Value utf16Dec(StringConstant x) {
    Production dec16 = new DecoderDSLSemantics.Dec16();
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape();
    Production decUTF16 = new DecoderDSLSemantics.DecUTF16();
    Production asUnicode = new DecoderDSLSemantics.AsUnicode();
    IntConstant four = new IntConstant(4);
    return asUnicode.exec(decUTF16.exec(lsbReshape.exec(dec16.exec(x), four)));
  }

  @Test
  public void testUTF32Dec() {
    StringConstant str = new StringConstant("000020A1");
    Value ret = utf32Dec(str);
    Value result = new StringConstant("₡");
    Assert.assertTrue(ret.equals(result));
  }

  private Value utf32Dec(StringConstant x) {
    Production dec16 = new DecoderDSLSemantics.Dec16();
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape();
    Production decUTF32 = new DecoderDSLSemantics.DecUTF32();
    Production asUnicode = new DecoderDSLSemantics.AsUnicode();
    IntConstant four = new IntConstant(4);
    return asUnicode.exec(decUTF32.exec(lsbReshape.exec(dec16.exec(x), four)));
  }

  @Test
  public void testUTF8Dec() {
    StringConstant str = new StringConstant("E282A1");
    Value ret = utf8Dec(str);
    Value result = new StringConstant("₡");
    Assert.assertTrue(ret.equals(result));
  }

  private Value utf8Dec(StringConstant x) {
    Production dec16 = new DecoderDSLSemantics.Dec16();
    Production lsbReshape = new DecoderDSLSemantics.LSBReshape();
    Production decUTF8 = new DecoderDSLSemantics.DecUTF8();
    Production asUnicode = new DecoderDSLSemantics.AsUnicode();
    IntConstant four = new IntConstant(4);
    return asUnicode.exec(decUTF8.exec(lsbReshape.exec(dec16.exec(x), four)));
  }
}
