package relish.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import relish.util.ListMultiMap;
import relish.util.MultiMap;

// The context-free grammar G = (T, N, P, S)
// T: terminal symbols
// N: non-terminal symbols
// P: productions
// S: one unique start (non-terminal) symbol
public abstract class DSLGrammar {

  protected int maxSymbolCounter = -1;
  public final Collection<ConstantTerminalSymbol> constantTerminalSymbols = new ArrayList<>();
  public final Collection<VariableTerminalSymbol> variableTerminalSymbols = new ArrayList<>();
  public final Collection<NonTerminalSymbol> nonTerminalSymbols = new ArrayList<>();

  public NonTerminalSymbol startSymbol;

  protected int maxProductionCounter = -1;
  public final Collection<Production> productions = new ArrayList<>();

  public final Map<String, Symbol> nameToSymbol = new HashMap<>();
  public final Map<String, Production> operatorNameToProduction = new HashMap<>();

  public final MultiMap<Symbol, Production> symbolToOutProductions = new ListMultiMap<>();

  public DSLGrammar(String grammarFilePath) {
    parse(grammarFilePath);
    computeSymbolToOutProductions();
  }

  protected abstract void parse(String grammarFilePath);

  private void computeSymbolToOutProductions() {
    for (Production production : productions) {
      Symbol[] argumentSymbols = production.argumentSymbols;
      for (int i = 0; i < argumentSymbols.length; i ++) {
        Symbol argumentSymbol = argumentSymbols[i];
        symbolToOutProductions.put(argumentSymbol, production);
      }
    }
  }

  public Symbol getSymbolFromName(String name) {
    return nameToSymbol.get(name);
  }

  public Production getProductionFromOperatorName(String operatorName) {
    return operatorNameToProduction.get(operatorName);
  }

}
