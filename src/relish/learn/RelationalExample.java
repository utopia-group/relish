package relish.learn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;

import relish.abs.Abstractions.ConcreteValue;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.verify.RelationalProperty;
import relish.verify.RelationalProperty.PropertyConstant;
import relish.verify.RelationalProperty.PropertyFunction;
import relish.verify.RelationalProperty.PropertyTerm;
import relish.verify.RelationalProperty.PropertyVariable;

public class RelationalExample {

  // relational examples of the form rootFunction(arg_1, ..., arg_n) = true
  public final ExampleFunction rootFunction;

  public RelationalExample(ExampleFunction rootFunction) {
    this.rootFunction = rootFunction;
  }

  public String toEUSolverText() {
    return rootFunction.toEUSolverText();
  }

  @Override
  public int hashCode() {
    return rootFunction.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) return true;
    if (!(o instanceof RelationalExample)) return false;
    return rootFunction.equals(((RelationalExample) o).rootFunction);
  }

  @Override
  public String toString() {
    return rootFunction + " = AllTrue";
  }

  /* Example Terms */

  public static abstract class ExampleTerm {

    public abstract String toEUSolverText();

  }

  public static class ExampleConstant extends ExampleTerm {

    // use a ConcreteValue array to represent a constant
    // to enable example merge and simplification
    public final ConcreteValue[] values;

    public ExampleConstant(ConcreteValue value) {
      values = new ConcreteValue[] { value };
    }

    public ExampleConstant(ConcreteValue[] values) {
      this.values = values;
    }

    @Override
    public String toEUSolverText() {
      assert values.length == 1 : values;
      return values[0].translateToProgramText();
    }

    @Override
    public int hashCode() {
      return Arrays.asList(values).hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof ExampleConstant)) return false;
      return Arrays.asList(values).equals(Arrays.asList(((ExampleConstant) o).values));
    }

    @Override
    public String toString() {
      return Arrays.toString(values);
    }

  }

  public static class ExampleFunction extends ExampleTerm {

    public final FunctionOccurrence occurrence;

    public final List<ExampleTerm> arguments;

    public ExampleFunction(FunctionOccurrence occurrence, List<ExampleTerm> arguments) {
      assert arguments.size() > 0;
      this.occurrence = occurrence;
      this.arguments = arguments;
    }

    public int arity() {
      return arguments.size();
    }

    @Override
    public String toEUSolverText() {
      StringBuilder builder = new StringBuilder();
      builder.append("(");
      String funcName = occurrence.symbol.functionName;
      if (funcName.equals("eq")) {
        builder.append("=");
      } else if (funcName.equals("minus")) {
        builder.append("MyMinus");
      } else {
        builder.append(occurrence.symbol.functionName);
      }
      for (ExampleTerm argument : arguments) {
        builder.append(" ").append(argument.toEUSolverText());
      }
      builder.append(")");
      return builder.toString();
    }

    @Override
    public int hashCode() {
      return occurrence.hashCode() + arguments.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) return true;
      if (!(o instanceof ExampleFunction)) return false;
      return occurrence.equals(((ExampleFunction) o).occurrence) && arguments.equals(((ExampleFunction) o).arguments);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(occurrence).append("(");
      for (ExampleTerm argument : arguments) {
        builder.append(argument).append(", ");
      }
      builder.delete(builder.length() - 2, builder.length());
      builder.append(")");
      return builder.toString();
    }

  }

  /* Example Merge */

  // merge multiple examples of the same shape (property) to a single one
  // by converting multiple valuations to a valuation on arrays/lists
  // assign unique indices for all occurrences of the same function symbol
  // NOTE: the relational properties inferred in this method would take different constant
  // occurrences as different variables, even if they are equal in value.
  // This could make the inferred relational properties different than the original ones.
  // But it would not affect the correctness.
  public static List<RelationalExample> mergeExamples(List<RelationalExample> examples) {
    // relational property -> (variable -> variable values)
    Map<RelationalProperty, Map<PropertyVariable, List<ConcreteValue>>> propToValuations = new HashMap<>();

    // populate the property to valuations map
    for (RelationalExample example : examples) {
      Pair<RelationalProperty, Map<PropertyVariable, ConcreteValue>> pair = extractPropAndValuation(example);
      RelationalProperty property = pair.getValue0();
      Map<PropertyVariable, ConcreteValue> valuation = pair.getValue1();
      if (propToValuations.containsKey(property)) {
        Map<PropertyVariable, List<ConcreteValue>> listValuation = propToValuations.get(property);
        assert listValuation.size() == valuation.size();
        for (PropertyVariable var : valuation.keySet()) {
          assert listValuation.containsKey(var) : var;
          listValuation.get(var).add(valuation.get(var));
        }
        propToValuations.put(property, listValuation);
      } else {
        Map<PropertyVariable, List<ConcreteValue>> listValuation = new HashMap<>();
        for (PropertyVariable var : valuation.keySet()) {
          List<ConcreteValue> values = new ArrayList<>();
          values.add(valuation.get(var));
          listValuation.put(var, values);
        }
        propToValuations.put(property, listValuation);
      }
    }

    // current occurrence index map
    Map<FunctionSymbol, Integer> funcToIndices = new HashMap<>();

    // rebuild all examples using list valuations
    List<RelationalExample> mergedExamples = new ArrayList<>();
    for (RelationalProperty property : propToValuations.keySet()) {
      RelationalExample example = instantiate(property, propToValuations.get(property), funcToIndices);
      mergedExamples.add(example);
    }

    return mergedExamples;
  }

  // prefix of property variables
  private static final String VAR = "x";
  // global variable to keep track of current variable index
  // should reset to 0 before calling extractPropAndValuationImpl
  private static int varIndex = 0;

  // extract relational properties and valuations from relational examples
  private static Pair<RelationalProperty, Map<PropertyVariable, ConcreteValue>> extractPropAndValuation(RelationalExample example) {
    Map<PropertyVariable, ConcreteValue> valuations = new HashMap<>();
    ExampleFunction exampleFunc = example.rootFunction;
    varIndex = 0; // variable index starts with 1 (should be set to 0)
    PropertyFunction propFunc = extractPropAndValuationImpl(exampleFunc, valuations);
    RelationalProperty property = new RelationalProperty(propFunc);
    return new Pair<>(property, valuations);
  }

  // helper function for extracting relational properties and valuations
  private static PropertyFunction extractPropAndValuationImpl(ExampleFunction exampleFunc, Map<PropertyVariable, ConcreteValue> valuations) {
    List<PropertyTerm> propArgs = new ArrayList<>();
    for (ExampleTerm exampleArg : exampleFunc.arguments) {
      if (exampleArg instanceof ExampleConstant) {
        ExampleConstant argConstant = (ExampleConstant) exampleArg;
        PropertyVariable propArg = new PropertyVariable("Poly", VAR + (++varIndex));
        assert argConstant.values.length == 1 : Arrays.toString(argConstant.values);
        valuations.put(propArg, argConstant.values[0]);
        propArgs.add(propArg);
      } else if (exampleArg instanceof ExampleFunction) {
        ExampleFunction argFunc = (ExampleFunction) exampleArg;
        PropertyFunction propArg = extractPropAndValuationImpl(argFunc, valuations);
        propArgs.add(propArg);
      } else {
        throw new RuntimeException("Unknown subtype of ExampleConstant");
      }
    }
    return new PropertyFunction(exampleFunc.occurrence.symbol, propArgs);
  }

  // instantiate the relational property with valuations and return a relational example
  private static RelationalExample instantiate(RelationalProperty property, Map<PropertyVariable, List<ConcreteValue>> valuations,
      Map<FunctionSymbol, Integer> funcToIndices) {
    PropertyFunction propFunc = property.rootFunction;
    ExampleFunction exampleFunc = instantiateImpl(propFunc, valuations, funcToIndices);
    return new RelationalExample(exampleFunc);
  }

  // helper method for instantiate
  private static ExampleFunction instantiateImpl(PropertyFunction propFunc, Map<PropertyVariable, List<ConcreteValue>> valuations,
      Map<FunctionSymbol, Integer> funcToIndices) {

    // assign the next available index and update the index map
    int index = 1;
    FunctionSymbol funcSymbol = propFunc.funcSymbol;
    if (funcToIndices.containsKey(funcSymbol)) {
      index = funcToIndices.get(funcSymbol);
    }
    funcToIndices.put(funcSymbol, index + 1);
    FunctionOccurrence funcOccur = new FunctionOccurrence(funcSymbol, index);

    // instantiate arguments
    List<ExampleTerm> exampleArgs = new ArrayList<>();
    for (PropertyTerm propArg : propFunc.arguments) {
      if (propArg instanceof PropertyVariable) {
        PropertyVariable propVar = (PropertyVariable) propArg;
        assert valuations.containsKey(propVar) : propVar;
        List<ConcreteValue> valueList = valuations.get(propVar);
        ConcreteValue[] values = new ConcreteValue[valueList.size()];
        for (int i = 0; i < valueList.size(); ++i) {
          values[i] = valueList.get(i);
        }
        exampleArgs.add(new ExampleConstant(values));
      } else if (propArg instanceof PropertyConstant) {
        // probably unreachable
        PropertyConstant propConst = (PropertyConstant) propArg;
        ConcreteValue value = propConst.value;
        exampleArgs.add(new ExampleConstant(value));
      } else if (propArg instanceof PropertyFunction) {
        ExampleFunction exampleFunc = instantiateImpl((PropertyFunction) propArg, valuations, funcToIndices);
        exampleArgs.add(exampleFunc);
      } else {
        throw new RuntimeException("Unknown subtype of PropertyTerm");
      }
    }

    return new ExampleFunction(funcOccur, exampleArgs);
  }

}
