package relish.learn;

import relish.abs.Abstractions.BoolConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.NonTerminalSymbol;
import relish.dsl.Production;
import relish.dsl.Symbol;

public class EqDSLSemantics {

  public static class Eq extends Production {

    public Eq(int id, NonTerminalSymbol returnSymbol, String operatorName, Symbol[] argumentSymbols, int cost) {
      super(id, returnSymbol, operatorName, argumentSymbols, cost);
    }

    @Override
    public String translateToProgramText() {
      return "Eq";
    }

    @Override
    public Value exec(Value... args) {
      assert (args.length == 2) : args;

      if (args[0].equals(args[1])) {
        return new BoolConstant(true);
      } else {
        return new BoolConstant(false);
      }
    }

  }

}
