package relish.dsl;

public class NonTerminalSymbol extends Symbol {

  // the maximum recursion depth that is allowed for this non-terminal symbol
  // this is used in the bounded FTA construction algorithm
  public int maxDepth;

  public NonTerminalSymbol(int id, String symbolName, int maxDepth) {
    super(id, symbolName);
    this.maxDepth = maxDepth;
  }

  @Override
  public String toString() {
    return symbolName;
  }

}
