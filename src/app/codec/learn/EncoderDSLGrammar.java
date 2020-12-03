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

public class EncoderDSLGrammar extends DSLGrammar {

  public EncoderDSLGrammar(String grammarFilePath) {
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
      String name = "C";
      int maxDepth = 2;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
    {
      String name = "E";
      int maxDepth = 1;
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
      String name = "I";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }
  }

  private void parseProductions(String line) {
    {
      String operatorName = "Id2";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("E") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Id2(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "PadToMultiple";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C"), nameToSymbol.get("num"), nameToSymbol.get("ch") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
      int cost = 1;
      Production production = new EncoderDSLSemantics.PadToMultiple(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "HeaderUU";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("C") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
      int cost = 1;
      Production production = new EncoderDSLSemantics.HeaderUU(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Enc64";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("E");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Enc64(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Enc32";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("E");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Enc32(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Enc16";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("E");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Enc16(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Enc64XML";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("E");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Enc64XML(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "EncUU";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("E");
      int cost = 1;
      Production production = new EncoderDSLSemantics.EncUU(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Enc32Hex";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("E");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Enc32Hex(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id1";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x1") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Id1(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Reshape";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("B"), nameToSymbol.get("num") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new EncoderDSLSemantics.Reshape(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "EncUTF8";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("I") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new EncoderDSLSemantics.EncUTF8(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "EncUTF16";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("I") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new EncoderDSLSemantics.EncUTF16(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "EncUTF32";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("I") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("B");
      int cost = 1;
      Production production = new EncoderDSLSemantics.EncUTF32(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "CodePoint";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x1") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("I");
      int cost = 1;
      Production production = new EncoderDSLSemantics.CodePoint(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
  }

  private void parseStartSymbols(String line) {
    startSymbol = (NonTerminalSymbol) nameToSymbol.get("C");
  }

}
