package relish.learn;

import relish.abs.Abstractions.BoolConstant;
import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;

public class NotDSLSemantics {

  public static class Not extends Production {

    public Not(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private BoolConstant not(BoolConstant c1) {
      BoolConstant boolTrue = new BoolConstant(true);
      BoolConstant boolFalse = new BoolConstant(false);
      if (c1.equals(boolFalse)) {
        return boolTrue;
      } else {
        return boolFalse;
      }
    }

    @Override
    public String translateToProgramText() {
      return "Not";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 1) : args;
      // handle error values
      {
        if (args[0] instanceof ErrConstant) {
          return ErrConstant.v();
        }
      }

      assert (args[0] instanceof BoolConstant) : args[0];
      return not((BoolConstant) args[0]);
    }

  }

}
