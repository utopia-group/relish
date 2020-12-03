package app.comparator.learn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import app.Benchmark;
import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.ConstantTerminalSymbol;
import relish.dsl.DSLGrammar;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;
import relish.dsl.VariableTerminalSymbol;
import relish.learn.ComposedCFTALearner;

public class ComparatorDSLGrammar extends DSLGrammar {

  public ComparatorDSLGrammar(String grammarFilePath) {
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
      String name = "t";
      Set<Value> values = new HashSet<>();
      Map<Value, Integer> costs = new HashMap<>();
      {
        IntConstant value = new IntConstant(0);
        values.add(value);
        costs.put(value, 1);
      }
      {
        IntConstant value = new IntConstant(1);
        values.add(value);
        costs.put(value, 1);
      }
      ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
      constantTerminalSymbols.add(constantTerminalSymbol);
      nameToSymbol.put(name, constantTerminalSymbol);
    }

    {
      String name = "k";
      Set<Value> values = new HashSet<>();
      Map<Value, Integer> costs = new HashMap<>();
      for (int k = 1; k <= 3; k ++) {
        IntConstant value = new IntConstant(k);
        values.add(value);
        costs.put(value, 2);
      }
      ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
      constantTerminalSymbols.add(constantTerminalSymbol);
      nameToSymbol.put(name, constantTerminalSymbol);
    }

    {
      String name = "j";
      Set<Value> values = new HashSet<>();
      Map<Value, Integer> costs = new HashMap<>();
      for (int j = 0; j <= 4; j ++) {
        IntConstant value = new IntConstant(j);
        values.add(value);
        costs.put(value, 2);
      }
      ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
      constantTerminalSymbols.add(constantTerminalSymbol);
      nameToSymbol.put(name, constantTerminalSymbol);
    }

    {
      String name = "c";
      Set<Value> values = new HashSet<>();
      Map<Value, Integer> costs = new HashMap<>();
      for (char c = 'a'; c <= 'z'; c ++) {
        CharConstant value = new CharConstant(c);
        values.add(value);
        costs.put(value, 1000);
        if ("c15".equals(Benchmark.benchmarkName) && c == 'a') {
          costs.put(value, 10);
        }
      }
      for (char c = 'A'; c <= 'Z'; c ++) {
        CharConstant value = new CharConstant(c);
        values.add(value);
        costs.put(value, 1000);
      }
      for (char c = '1'; c <= '9'; c ++) {
        CharConstant value = new CharConstant(c);
        values.add(value);
        costs.put(value, 1000);
      }
      ConstantTerminalSymbol constantTerminalSymbol = new ConstantTerminalSymbol(++maxSymbolCounter, name, values, costs);
      constantTerminalSymbols.add(constantTerminalSymbol);
      nameToSymbol.put(name, constantTerminalSymbol);
    }

    // backtracking K 
    {
      if ("c15".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c16".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c9".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c13".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c11".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c19".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c18".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c17".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      } else if ("c5".equals(Benchmark.benchmarkName)) {
        ComposedCFTALearner.K = Integer.MAX_VALUE;
      }
    }

  }

  private void parseVariableTerminalSymbols(String line) {

    {
      String name = "x1";
      VariableTerminalSymbol variable = new VariableTerminalSymbol(++maxSymbolCounter, name);
      variableTerminalSymbols.add(variable);
      nameToSymbol.put(name, variable);
    }

    {
      String name = "x2";
      VariableTerminalSymbol variable = new VariableTerminalSymbol(++maxSymbolCounter, name);
      variableTerminalSymbols.add(variable);
      nameToSymbol.put(name, variable);
    }

  }

  private void parseNonTerminalSymbols(String line) {

    {
      String name = "res";
      int maxDepth = 1;
      if ("c15".equals(Benchmark.benchmarkName)) {
        maxDepth = 2;
      } else if ("c17".equals(Benchmark.benchmarkName)) {
        maxDepth = 2;
      } else if ("c19".equals(Benchmark.benchmarkName)) {
        maxDepth = 2;
      } else if ("c20".equals(Benchmark.benchmarkName)) {
        maxDepth = 3;
      }
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }

    {
      String name = "comp";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }

    {
      String name = "intVal";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }

    {
      String name = "strVal";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }

    {
      String name = "pos";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }

    {
      String name = "x";
      int maxDepth = 0;
      NonTerminalSymbol nonTerminalSymbol = new NonTerminalSymbol(++maxSymbolCounter, name, maxDepth);
      nonTerminalSymbols.add(nonTerminalSymbol);
      nameToSymbol.put(name, nonTerminalSymbol);
    }

  }

  private void parseProductions(String line) {
    {
      String operatorName = "Id";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("comp") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("res");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.Id(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Conditional";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("comp"), nameToSymbol.get("res") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("res");
      int cost = 100;
      Production production = new ComparatorDSLSemantics.Conditional(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "IntLt";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("intVal"), nameToSymbol.get("intVal") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("comp");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.IntLt(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "StrLt";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("strVal"), nameToSymbol.get("strVal") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("comp");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.StrLt(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "CountChar";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x"), nameToSymbol.get("c") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("intVal");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.CountChar(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Len";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("intVal");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.Len(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "ToInt";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("strVal"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("intVal");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.ToInt(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id3";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("strVal");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.Id3(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "SubStr";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x"), nameToSymbol.get("pos"), nameToSymbol.get("pos"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("strVal");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.SubStr(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "StartPos";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x"), nameToSymbol.get("t"), nameToSymbol.get("k"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("pos");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.StartPos(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "EndPos";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x"), nameToSymbol.get("t"), nameToSymbol.get("k"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("pos");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.EndPos(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "End";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("pos");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.End(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "ConstPos";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("j"), };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("pos");
      int cost = 30;
      Production production = new ComparatorDSLSemantics.ConstPos(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id1";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x1") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("x");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.Id1(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
    {
      String operatorName = "Id2";
      Symbol[] argumentSymbols = new Symbol[] { nameToSymbol.get("x2") };
      NonTerminalSymbol returnSymbol = (NonTerminalSymbol) nameToSymbol.get("x");
      int cost = 1;
      Production production = new ComparatorDSLSemantics.Id2(++maxProductionCounter, returnSymbol, operatorName, argumentSymbols, cost);
      productions.add(production);
      operatorNameToProduction.put(operatorName, production);
    }
  }

  private void parseStartSymbols(String line) {
    startSymbol = (NonTerminalSymbol) nameToSymbol.get("res");
  }

}
