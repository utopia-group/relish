package app.codec.learn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.ConstantTerminalSymbol;
import relish.dsl.DSLGrammar;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.VariableTerminalSymbol;

public class DecoderDSLGrammar extends DSLGrammar {

  public DecoderDSLGrammar(String grammarFilePath) {
    super(grammarFilePath);
  }

  @Override
  protected void parse(String grammarFilePath) {
    // FIXME: Just manually specify the grammar instead of parsing
    parseConstantTerminalSymbols(null);
    parseVariableTerminalSymbols(null);
    parseNonTerminalSymbols(null);
    parseProductions(null);
    parseStartSymbols(null);
  }

  private void parseConstantTerminalSymbols(String line) {
    {
      String name = "num";
      Set<Value> values = new HashSet<>();
      Map<Value, Integer> costs = new HashMap<>();
      for (int c = 1; c <= 8; c ++) {
        IntConstant value = new IntConstant(c);
        values.add(value);
        costs.put(value, 10);
      }
      ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
      constantTerminalSymbols.add(constantTerminalSymbol);
      nameToSymbol.put(name, constantTerminalSymbol);
    }
    {
      String name = "ch";
      Set<Value> values = new HashSet<>();
      Map<Value, Integer> costs = new HashMap<>();
      {
        CharConstant value = new CharConstant('=');
        values.add(value);
        costs.put(value, 10);
      }
      {
        CharConstant value = new CharConstant('`');
        values.add(value);
        costs.put(value, 10);
      }
      ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
      constantTerminalSymbols.add(constantTerminalSymbol);
      nameToSymbol.put(name, constantTerminalSymbol);
    }
  }

  private void parseVariableTerminalSymbols(String line) {
    String name = "x1";
    VariableTerminalSymbol variable = new VariableTerminalSymbol(++maxSymbolCounter, name);
    variableTerminalSymbols.add(variable);
    nameToSymbol.put(name, variable);
  }

  private void parseNonTerminalSymbols(String line) {
    {
      String name = "S";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
    {
      String name = "B";
      int maxDepth = 1;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
    {
      String name = "D";
      int maxDepth = 1;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
    {
      String name = "C";
      int maxDepth = 2;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
    {
      String name = "I";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
  }

  private void parseProductions(String line) {
    {
      String operatorName = "Id3";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("S");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Id2(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id2";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("D") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Id2(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "LSBReshape";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B"), nameToSymbol.get("num") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new DecoderDSLSemantics.LSBReshape(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "AsUnicode";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("I") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("S");
      int cost = 1;
      Production production = new DecoderDSLSemantics.AsUnicode(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Dec64";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("D");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Dec64(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Dec32";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("D");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Dec32(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Dec16";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("D");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Dec16(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Dec64XML";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("D");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Dec64XML(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "DecUU";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("D");
      int cost = 1;
      Production production = new DecoderDSLSemantics.DecUU(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Dec32Hex";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("D");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Dec32Hex(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id1";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x1") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Id1(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "RemovePad";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C"), nameToSymbol.get("ch") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
      int cost = 1;
      Production production = new DecoderDSLSemantics.RemovePad(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Substr";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C"), nameToSymbol.get("num") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
      int cost = 1;
      Production production = new DecoderDSLSemantics.Substr(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "DecUTF8";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("I");
      int cost = 1;
      Production production = new DecoderDSLSemantics.DecUTF8(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "DecUTF16";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("I");
      int cost = 1;
      Production production = new DecoderDSLSemantics.DecUTF16(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "DecUTF32";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("I");
      int cost = 1;
      Production production = new DecoderDSLSemantics.DecUTF32(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
  }

  private void parseStartSymbols(String line) {
    startSymbol = (NonTerminalSymbol) nameToSymbol.get("S");
  }

}
