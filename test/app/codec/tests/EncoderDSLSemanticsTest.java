package app.codec.tests;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import app.codec.learn.EncoderDSLSemantics;
import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.util.StringUtil;

public class EncoderDSLSemanticsTest {

  @Test
  public void testReshape1() {
    StringConstant str = new StringConstant("Man");
    IntConstant radix = new IntConstant(6);
    Production production = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, radix);
    byte[] result = new byte[] { 19, 22, 5, 46 };
    Assert.assertTrue(Arrays.equals(result, StringUtil.toByteArray(ret.value)));
  }

  @Test
  public void testReshape2() {
    StringConstant str = new StringConstant("M");
    IntConstant radix = new IntConstant(6);
    Production production = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, radix);
    byte[] result = new byte[] { 19, 16 };
    Assert.assertTrue(Arrays.equals(result, StringUtil.toByteArray(ret.value)));
  }

  @Test
  public void testReshape3() {
    StringConstant str = new StringConstant("Ma");
    IntConstant radix = new IntConstant(6);
    Production production = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    StringConstant ret = (StringConstant) production.exec(str, radix);
    byte[] result = new byte[] { 19, 22, 4 };
    Assert.assertTrue(Arrays.equals(result, StringUtil.toByteArray(ret.value)));
  }

  @Test
  public void testEnc64() {
    StringConstant str = new StringConstant("Man");
    IntConstant radix = new IntConstant(6);
    Production reshape = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    Production enc64 = new EncoderDSLSemantics.Enc64(0, null, null, new Symbol[] {}, 0);
    StringConstant reshaped = (StringConstant) reshape.exec(str, radix);
    StringConstant ret = (StringConstant) enc64.exec(reshaped);
    StringConstant result = new StringConstant("TWFu");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testEnc32() {
    StringConstant str = new StringConstant("Man");
    IntConstant radix = new IntConstant(5);
    Production reshape = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    Production enc32 = new EncoderDSLSemantics.Enc32(0, null, null, new Symbol[] {}, 0);
    StringConstant reshaped = (StringConstant) reshape.exec(str, radix);
    StringConstant ret = (StringConstant) enc32.exec(reshaped);
    StringConstant result = new StringConstant("JVQW4");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testEnc16() {
    StringConstant str = new StringConstant("Man");
    IntConstant radix = new IntConstant(4);
    Production reshape = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    Production enc16 = new EncoderDSLSemantics.Enc16(0, null, null, new Symbol[] {}, 0);
    StringConstant reshaped = (StringConstant) reshape.exec(str, radix);
    StringConstant ret = (StringConstant) enc16.exec(reshaped);
    StringConstant result = new StringConstant("4D616E");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Enc1() {
    StringConstant str = new StringConstant("Man");
    StringConstant ret = base64Enc(str);
    StringConstant result = new StringConstant("TWFu");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Enc2() {
    StringConstant str = new StringConstant("M");
    StringConstant ret = base64Enc(str);
    StringConstant result = new StringConstant("TQ==");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Enc3() {
    StringConstant str = new StringConstant("Ma");
    StringConstant ret = base64Enc(str);
    StringConstant result = new StringConstant("TWE=");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase64Enc4() {
    char[] strCharArray = new char[] { 32, 15, 249 };
    StringConstant str = new StringConstant(new String(strCharArray));
    StringConstant ret = base64Enc(str);
    StringConstant result = new StringConstant("IA/5");
    Assert.assertTrue(ret.equals(result));
  }

  private StringConstant base64Enc(StringConstant x) {
    Production reshape = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    Production enc64 = new EncoderDSLSemantics.Enc64(0, null, null, new Symbol[] {}, 0);
    Production padToMultiple = new EncoderDSLSemantics.PadToMultiple(0, null, null, new Symbol[] {}, 0);
    IntConstant four = new IntConstant(4);
    IntConstant six = new IntConstant(6);
    CharConstant eq = new CharConstant('=');
    return (StringConstant) padToMultiple.exec((StringConstant) enc64.exec(reshape.exec(x, six)), four, eq);
  }

  @Test
  public void testBase32Enc1() {
    StringConstant str = new StringConstant("Man");
    StringConstant ret = base32Enc(str);
    StringConstant result = new StringConstant("JVQW4===");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase32Enc2() {
    StringConstant str = new StringConstant("M");
    StringConstant ret = base32Enc(str);
    StringConstant result = new StringConstant("JU======");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testBase32Enc3() {
    StringConstant str = new StringConstant("Ma");
    StringConstant ret = base32Enc(str);
    StringConstant result = new StringConstant("JVQQ====");
    Assert.assertTrue(ret.equals(result));
  }

  private StringConstant base32Enc(StringConstant x) {
    Production reshape = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    Production enc32 = new EncoderDSLSemantics.Enc32(0, null, null, new Symbol[] {}, 0);
    Production padToMultiple = new EncoderDSLSemantics.PadToMultiple(0, null, null, new Symbol[] {}, 0);
    IntConstant five = new IntConstant(5);
    IntConstant eight = new IntConstant(8);
    CharConstant eq = new CharConstant('=');
    return (StringConstant) padToMultiple.exec((StringConstant) enc32.exec(reshape.exec(x, five)), eight, eq);
  }

  @Test
  public void testBase16Enc() {
    StringConstant str = new StringConstant("Man");
    StringConstant ret = base16Enc(str);
    StringConstant result = new StringConstant("4D616E");
    Assert.assertTrue(ret.equals(result));
  }

  private StringConstant base16Enc(StringConstant x) {
    Production reshape = new EncoderDSLSemantics.Reshape(0, null, null, new Symbol[] {}, 0);
    Production enc16 = new EncoderDSLSemantics.Enc16(0, null, null, new Symbol[] {}, 0);
    IntConstant four = new IntConstant(4);
    return (StringConstant) enc16.exec(reshape.exec(x, four));
  }

  @Test
  public void testUUEnc1() {
    StringConstant str = new StringConstant("Cat");
    Value ret = uuEnc(str);
    Value result = new StringConstant("#0V%T");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testUUEnc2() {
    StringConstant str = new StringConstant("C");
    Value ret = uuEnc(str);
    System.out.println(ret);
    Value result = new StringConstant("!0P``");
    Assert.assertTrue(ret.equals(result));
  }

  @Test
  public void testUUEnc3() {
    StringConstant str = new StringConstant("Mouse");
    Value ret = uuEnc(str);
    Value result = new StringConstant("%36]U<V4`");
    Assert.assertTrue(ret.equals(result));
  }

  private Value uuEnc(StringConstant x) {
    Production encUU = new EncoderDSLSemantics.EncUU();
    Production reshape = new EncoderDSLSemantics.Reshape();
    Production padToMultiple = new EncoderDSLSemantics.PadToMultiple();
    Production headerUU = new EncoderDSLSemantics.HeaderUU();
    IntConstant four = new IntConstant(4);
    IntConstant six = new IntConstant(6);
    CharConstant ch = new CharConstant('`');
    return headerUU.exec(padToMultiple.exec(encUU.exec(reshape.exec(x, six)), four, ch));
  }

  @Test
  public void testUTF8Enc() {
    StringConstant str = new StringConstant("₡");
    Value ret = utf8Enc(str);
    Value result = new StringConstant("E282A1");
    Assert.assertTrue(ret.equals(result));
  }

  private Value utf8Enc(StringConstant x) {
    Production enc16 = new EncoderDSLSemantics.Enc16();
    Production reshape = new EncoderDSLSemantics.Reshape();
    Production encUTF8 = new EncoderDSLSemantics.EncUTF8();
    Production codePoint = new EncoderDSLSemantics.CodePoint();
    IntConstant four = new IntConstant(4);
    return enc16.exec(reshape.exec(encUTF8.exec(codePoint.exec(x)), four));
  }

  @Test
  public void testUTF16Enc() {
    StringConstant str = new StringConstant("₡");
    Value ret = utf16Enc(str);
    Value result = new StringConstant("20A1");
    Assert.assertTrue(ret.equals(result));
  }

  private Value utf16Enc(StringConstant x) {
    Production enc16 = new EncoderDSLSemantics.Enc16();
    Production reshape = new EncoderDSLSemantics.Reshape();
    Production encUTF16 = new EncoderDSLSemantics.EncUTF16();
    Production codePoint = new EncoderDSLSemantics.CodePoint();
    IntConstant four = new IntConstant(4);
    return enc16.exec(reshape.exec(encUTF16.exec(codePoint.exec(x)), four));
  }

  @Test
  public void testUTF32Enc() {
    StringConstant str = new StringConstant("₡");
    Value ret = utf32Enc(str);
    Value result = new StringConstant("000020A1");
    Assert.assertTrue(ret.equals(result));
  }

  private Value utf32Enc(StringConstant x) {
    Production enc16 = new EncoderDSLSemantics.Enc16();
    Production reshape = new EncoderDSLSemantics.Reshape();
    Production encUTF32 = new EncoderDSLSemantics.EncUTF32();
    Production codePoint = new EncoderDSLSemantics.CodePoint();
    IntConstant four = new IntConstant(4);
    return enc16.exec(reshape.exec(encUTF32.exec(codePoint.exec(x)), four));
  }

}
