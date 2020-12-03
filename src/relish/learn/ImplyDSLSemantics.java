package relish.learn;

import relish.abs.Abstractions.BoolConstant;
import relish.abs.Abstractions.ErrConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;

public class ImplyDSLSemantics {

  public static class Imply extends Production {

    public Imply(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    private BoolConstant imply(BoolConstant c1, BoolConstant c2) {
      BoolConstant boolTrue = new BoolConstant(true);
      BoolConstant boolFalse = new BoolConstant(false);
      if (c1.equals(boolFalse) || c2.equals(boolTrue)) {
        return boolTrue;
      } else {
        return boolFalse;
      }
    }

    @Override
    public String translateToProgramText() {
      return "Imply";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args;

      // handle error values
      {
        if (args[0] instanceof ErrConstant || args[1] instanceof ErrConstant) {
          return ErrConstant.v();
        }
      }

      assert (args[0] instanceof BoolConstant) : args[0];
      assert (args[1] instanceof BoolConstant) : args[1];
      return imply((BoolConstant) args[0], (BoolConstant) args[1]);
    }

  }

}
