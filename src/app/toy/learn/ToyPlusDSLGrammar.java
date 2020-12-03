package app.toy.learn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.ConstantTerminalSymbol;
import relish.dsl.DSLGrammar;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.VariableTerminalSymbol;
import relish.util.FileUtil;

public class ToyPlusDSLGrammar extends DSLGrammar {

  public ToyPlusDSLGrammar(String grammarFilePath) {
    super(grammarFilePath);
  }

  @Override
  protected void parse(String grammarFilePath) {
    List<String> lines = FileUtil.readFromFile(grammarFilePath);
    for (String line : lines) {
      line = line.replaceAll("\\W+", "");
      if (line.isEmpty() || line.startsWith("//")) continue;
    }

    // FIXME: Just manually specify the grammar instead of parsing
    parseConstantTerminalSymbols(null);
    parseVariableTerminalSymbols(null);
    parseNonTerminalSymbols(null);
    parseProductions(null);
    parseStartSymbols(null);
  }

  private void parseConstantTerminalSymbols(String line) {
    Set<Value> values = new HashSet<>();
    Map<Value, Integer> costs = new HashMap<>();
    String name = "c";
    for (int c = 1; c <= 2; c ++) {
      IntConstant value = new IntConstant(c);
      values.add(value);
      costs.put(value, 1);
    }
    ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
    constantTerminalSymbols.add(constantTerminalSymbol);
    nameToSymbol.put(name, constantTerminalSymbol);
  }

  private void parseVariableTerminalSymbols(String line) {
    String name = "x1";
    VariableTerminalSymbol variable = new VariableTerminalSymbol(++maxSymbolCounter, name);
    variableTerminalSymbols.add(variable);
    nameToSymbol.put(name, variable);
  }

  private void parseNonTerminalSymbols(String line) {
    String name = "e";
    int maxDepth = 1;
    NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
    nonTerminalSymbols.add(nonTerminalSymbol);
    nameToSymbol.put(name, nonTerminalSymbol);
  }

  private void parseProductions(String line) {
    {
      String operatorName = "Plus";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("e"), nameToSymbol.get("c") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("e");
      int cost = 1;
      Production production = new ToyPlusDSLSemantics.Plus(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x1") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("e");
      int cost = 1;
      Production production = new ToyPlusDSLSemantics.Id(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
  }

  private void parseStartSymbols(String line) {
    startSymbol = (NonTerminalSymbol) nameToSymbol.get("e");
  }

}
