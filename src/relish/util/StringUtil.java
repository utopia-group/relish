package relish.util;

import java.util.List;

public class StringUtil {

  public static byte[] toByteArray(String str) {
    byte[] ret = new byte[str.length()];
    for (int i = 0; i < str.length(); ++i) {
      ret[i] = (byte) str.charAt(i);
    }
    return ret;
  }

  public static String byteArrayToString(byte[] bytes) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < bytes.length; ++i) {
      // Java use 16-bit unsigned char and 8-bit signed byte
      // cast the byte value to short first, and set the 8 highest bits to 0
      short value = (short) (0x00FF & bytes[i]);
      // then cast the short to char
      char ch = (char) value;
      builder.append(ch);
    }
    return builder.toString();
  }

  public static String byteListToString(List<Byte> bytes) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < bytes.size(); ++i) {
      // Java use 16-bit unsigned char and 8-bit signed byte
      // cast the byte value to short first, and set the 8 highest bits to 0
      short value = (short) (0x00FF & bytes.get(i));
      // then cast the short to char
      char ch = (char) value;
      builder.append(ch);
    }
    return builder.toString();
  }

  public static String getContentInParens(String line) {
    return line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
  }

}
