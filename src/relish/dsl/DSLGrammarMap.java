package relish.dsl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DSLGrammarMap {

  // a map from function symbols to their corresponding grammars
  private final Map<FunctionSymbol, DSLGrammar> funcToGrammars = new HashMap<>();
  // a map from function names to function symbols
  private final Map<String, FunctionSymbol> funcNameToSymbols = new HashMap<>();

  public FunctionSymbol mkFunctionSymbol(String name, String returnType) {
    assert !funcNameToSymbols.containsKey(name) : name;
    FunctionSymbol symbol = new FunctionSymbol(name, returnType);
    funcNameToSymbols.put(name, symbol);
    return symbol;
  }

  public FunctionSymbol getFunctionSymbolByName(String name) {
    assert funcNameToSymbols.containsKey(name) : name;
    return funcNameToSymbols.get(name);
  }

  public DSLGrammar get(FunctionSymbol symbol) {
    return funcToGrammars.get(symbol);
  }

  public Set<FunctionSymbol> functionSymbolSet() {
    return funcToGrammars.keySet();
  }

  public void put(FunctionSymbol symbol, DSLGrammar grammar) {
    assert !funcToGrammars.containsKey(symbol) : symbol;
    funcToGrammars.put(symbol, grammar);
  }

  @Override
  public int hashCode() {
    assert false : "Unreachable";
    return funcToGrammars.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    assert false : "Unreachable";
    return this == o;
  }

  @Override
  public String toString() {
    return funcToGrammars.toString();
  }

}
