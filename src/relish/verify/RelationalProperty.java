package relish.verify;

import java.util.List;

import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.StringConstant;
import relish.dsl.FunctionSymbol;

public class RelationalProperty {

  // relational properties of the form rootFunction(arg_1, ..., arg_n) = true
  public final PropertyFunction rootFunction;

  public RelationalProperty(PropertyFunction rootFunction) {
    this.rootFunction = rootFunction;
  }

  @Override
  public int hashCode() {
    return rootFunction.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof RelationalProperty)) return false;
    return rootFunction.equals(((RelationalProperty) o).rootFunction);
  }

  @Override
  public String toString() {
    return rootFunction.toString();
  }

  public String toCMBCText() {
    return rootFunction.toCBMCText();
  }

  /* Property Terms */

  public static abstract class PropertyTerm {

    public abstract String toCBMCText();

  }

  public static class PropertyConstant extends PropertyTerm {

    public final ConcreteValue value;

    public PropertyConstant(ConcreteValue value) {
      this.value = value;
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof PropertyConstant)) return false;
      return value.equals(((PropertyConstant) o).value);
    }

    @Override
    public String toString() {
      return value.toString();
    }

    @Override
    public String toCBMCText() {
      if (value instanceof StringConstant) {
        StringConstant strConst = (StringConstant) value;
        return "mkString(" + strConst.translateToProgramText() + ", " + strConst.value.length() + ")";
      } else {
        return value.toString();
      }
    }

  }

  public static class PropertyVariable extends PropertyTerm {

    public final String type;
    public final String name;

    public PropertyVariable(String type, String name) {
      this.type = type;
      this.name = name;
    }

    @Override
    public int hashCode() {
      return type.hashCode() + name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof PropertyVariable)) return false;
      return type.equals(((PropertyVariable) o).type) && name.equals(((PropertyVariable) o).name);
    }

    @Override
    public String toString() {
      return name + "[" + type + "]";
    }

    @Override
    public String toCBMCText() {
      return name;
    }

  }

  public static class PropertyFunction extends PropertyTerm {

    public final FunctionSymbol funcSymbol;

    public final List<PropertyTerm> arguments;

    public PropertyFunction(FunctionSymbol funcSymbol, List<PropertyTerm> arguments) {
      assert arguments.size() > 0;
      this.funcSymbol = funcSymbol;
      this.arguments = arguments;
    }

    public int arity() {
      return arguments.size();
    }

    @Override
    public int hashCode() {
      return funcSymbol.hashCode() + arguments.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof PropertyFunction)) return false;
      return funcSymbol.equals(((PropertyFunction) o).funcSymbol) && arguments.equals(((PropertyFunction) o).arguments);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(funcSymbol).append("(");
      for (PropertyTerm argument : arguments) {
        builder.append(argument).append(", ");
      }
      builder.delete(builder.length() - 2, builder.length());
      builder.append(")");
      return builder.toString();
    }

    @Override
    public String toCBMCText() {
      StringBuilder builder = new StringBuilder();
      String funcName = funcSymbol.functionName;
      if (funcName.equals("eq")) {
        assert arguments.size() == 2 : funcSymbol;
        String type = null;
        if (arguments.get(0) instanceof PropertyVariable) {
          type = ((PropertyVariable) arguments.get(0)).type;
        } else if (arguments.get(0) instanceof PropertyFunction) {
          type = ((PropertyFunction) arguments.get(0)).funcSymbol.returnType;
        }
        assert type != null;
        if (type != null && type.equals("String")) {
          builder.append("StringEq(").append(arguments.get(0).toCBMCText());
          builder.append(", ").append(arguments.get(1).toCBMCText()).append(")");
        } else {
          builder.append("(").append(arguments.get(0).toCBMCText());
          builder.append(" == ").append(arguments.get(1).toCBMCText()).append(")");
        }
      } else if (funcName.equals("and")) {
        assert arguments.size() == 2 : funcSymbol;
        builder.append("(").append(arguments.get(0).toCBMCText());
        builder.append(" && ").append(arguments.get(1).toCBMCText()).append(")");
      } else if (funcName.equals("or")) {
        assert arguments.size() == 2 : funcSymbol;
        builder.append("(").append(arguments.get(0).toCBMCText());
        builder.append(" || ").append(arguments.get(1).toCBMCText()).append(")");
      } else if (funcName.equals("imply")) {
        assert arguments.size() == 2 : funcSymbol;
        builder.append("(!").append(arguments.get(0).toCBMCText());
        builder.append(" || ").append(arguments.get(1).toCBMCText()).append(")");
      } else if (funcName.equals("not")) {
        assert arguments.size() == 1 : funcSymbol;
        builder.append("(!").append(arguments.get(0).toCBMCText()).append(")");
      } else if (funcName.equals("minus")) {
        assert arguments.size() == 1 : funcSymbol;
        builder.append("(-").append(arguments.get(0).toCBMCText()).append(")");
      } else {
        builder.append(funcSymbol).append("(");
        for (PropertyTerm argument : arguments) {
          builder.append(argument.toCBMCText()).append(", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append(")");
      }
      return builder.toString();
    }

  }

}
