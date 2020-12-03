package relish.abs;

import java.util.Arrays;

public abstract class Abstractions {

  /* Values */

  public static abstract class Value {

  }

  public static abstract class ConcreteValue extends Value {

    public abstract String translateToProgramText();

  }

  /* Value of Boolean Type */

  public static class BoolConstant extends ConcreteValue {

    public final boolean value;

    public BoolConstant(boolean value) {
      this.value = value;
    }

    @Override
    public String translateToProgramText() {
      return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      if (!(o instanceof BoolConstant)) return false;
      BoolConstant other = (BoolConstant) o;
      return value == other.value;
    }

    @Override
    public int hashCode() {
      return Boolean.hashCode(value);
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

  }

  /* Value of Integer type */

  public static class IntConstant extends ConcreteValue {

    public final int value;

    public IntConstant(int value) {
      this.value = value;
    }

    @Override
    public String translateToProgramText() {
      return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      if (!(o instanceof IntConstant)) return false;
      IntConstant other = (IntConstant) o;
      return value == other.value;
    }

    @Override
    public int hashCode() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

  }

  /* Value of Char type */

  public static class CharConstant extends ConcreteValue {

    public final char value;

    public CharConstant(char value) {
      this.value = value;
    }

    @Override
    public String translateToProgramText() {
      return "\'" + value + "\'";
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      if (!(o instanceof CharConstant)) return false;
      CharConstant other = (CharConstant) o;
      return value == other.value;
    }

    @Override
    public int hashCode() {
      return Character.hashCode(value);
    }

    @Override
    public String toString() {
      return "\'" + value + "\'";
    }

  }

  /* Value of String type */

  public static class StringConstant extends ConcreteValue {

    public final String value;

    public StringConstant(String value) {
      this.value = value;
    }

    @Override
    public String translateToProgramText() {
      return "\"" + String.valueOf(value) + "\"";
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      if (!(o instanceof StringConstant)) return false;
      StringConstant other = (StringConstant) o;
      return value.equals(other.value);
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    @Override
    public String toString() {
      return "\"" + value + "\"";
    }

  }

  /* Value of integer array type */

  public static class IntArrayConstant extends ConcreteValue {

    public final int[] value;

    public IntArrayConstant(int[] value) {
      this.value = value;
    }

    @Override
    public String translateToProgramText() {
      return Arrays.toString(value);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null) return false;
      if (o == this) return true;
      if (!(o instanceof IntArrayConstant)) return false;
      IntArrayConstant other = (IntArrayConstant) o;
      return Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
      return Arrays.toString(value);
    }

  }

  /* Err handling */
  public static class ErrConstant extends ConcreteValue {

    private static final ErrConstant instance = new ErrConstant();

    private ErrConstant() {
    }

    public static ErrConstant v() {
      return instance;
    }

    @Override
    public String translateToProgramText() {
      return "Err";
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public boolean equals(Object o) {
      return o == this;
    }

    @Override
    public String toString() {
      return "Err";
    }

  }

}
