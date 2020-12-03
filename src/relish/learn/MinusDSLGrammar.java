package relish.learn;

import relish.dsl.DSLGrammar;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.VariableTerminalSymbol;

public class MinusDSLGrammar extends DSLGrammar {

  public MinusDSLGrammar() {
    this(null);
  }

  @Deprecated
  public MinusDSLGrammar(String grammarFilePath) {
    super(grammarFilePath);
  }

  @Override
  protected void parse(String grammarFilePath) {
    addVariableTerminalSymbols();
    addNonTerminalSymbols();
    addProductions();
    addStartSymbols();
  }

  private void addVariableTerminalSymbols() {
    {
      String name = "x1";
      VariableTerminalSymbol variable = new VariableTerminalSymbol(++maxSymbolCounter, name);
      variableTerminalSymbols.add(variable);
      nameToSymbol.put(name, variable);
    }
  }

  private void addNonTerminalSymbols() {
    String name = "e";
    int maxDepth = 1;
    NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
    nonTerminalSymbols.add(nonTerminalSymbol);
    nameToSymbol.put(name, nonTerminalSymbol);
  }

  private void addProductions() {
    String operatorName = "Minus";
    Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x1") };
    NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("e");
    int cost = 1;
    Production production = new MinusDSLSemantics.Minus(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
    productions.add(production);
    operatorNameToProduction.put(operatorName, production);
  }

  private void addStartSymbols() {
    startSymbol = (NonTerminalSymbol) nameToSymbol.get("e");
  }

}
