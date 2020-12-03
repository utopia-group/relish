package relish.learn;

import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;

public class MinusDSLSemantics {

  public static class Minus extends Production {

    public Minus(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private IntConstant minus(IntConstant c1) {
      return new IntConstant(-c1.value);
    }

    @Override
    public String translateToProgramText() {
      return "Minus";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args;

      // handle error values
      if (args[0] instanceof ErrConstant) {
        return ErrConstant.v();
      }

      assert (args[0] instanceof IntConstant) : args[0];
      return minus((IntConstant) args[0]);
    }

  }

}
