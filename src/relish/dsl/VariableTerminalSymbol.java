package relish.dsl;

public class VariableTerminalSymbol extends TerminalSymbol {

  public VariableTerminalSymbol(int id, String symbolName) {
    super(id, symbolName);
  }

  public String translateToProgramText() {
    return symbolName;
  }

}
