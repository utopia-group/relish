package relish.dsl;

public abstract class TerminalSymbol extends Symbol {

  public TerminalSymbol(int id, String symbolName) {
    super(id, symbolName);
  }

  @Override
  public String toString() {
    return symbolName;
  }

}
