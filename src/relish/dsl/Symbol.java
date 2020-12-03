package relish.dsl;

public abstract class Symbol {

  public final int id;

  public final String symbolName;

  public Symbol(int id, String symbolName) {
    this.id = id;
    this.symbolName = symbolName;
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public String toString() {
    return symbolName;
  }

}
