package relish.dsl;

public class FunctionOccurrence {

  public final FunctionSymbol symbol;
  public final int index;

  public FunctionOccurrence(FunctionSymbol symbol, int index) {
    this.symbol = symbol;
    this.index = index;
  }

  @Override
  public int hashCode() {
    return symbol.hashCode() + index;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof FunctionOccurrence)) return false;
    return symbol.equals(((FunctionOccurrence) o).symbol) && index == ((FunctionOccurrence) o).index;
  }

  @Override
  public String toString() {
    return symbol + "_" + index;
  }

}
