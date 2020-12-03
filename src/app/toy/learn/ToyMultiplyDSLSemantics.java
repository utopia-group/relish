package app.toy.learn;

import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;

public class ToyMultiplyDSLSemantics {

  public static class Multiply extends Production {

    public Multiply(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private ConcreteValue multiply(IntConstant c1, IntConstant c2) {
      return new IntConstant(c1.value * c2.value);
    }

    @Override
    public String translateToProgramText() {
      return "Multiply";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args.length;
      assert (args[0] instanceof IntConstant) : args[0];
      assert (args[1] instanceof IntConstant) : args[1];
      return multiply((IntConstant) args[0], ((IntConstant) args[1]));
    }

  }

  public static class Id extends Production {

    public Id(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public String translateToProgramText() {
      return "Id";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args.length;
      assert (args[0] instanceof IntConstant) : args[0];
      return (IntConstant) args[0];
    }

  }

}
