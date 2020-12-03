package relish.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javatuples.Pair;

import relish.abs.Abstractions.BoolConstant;
import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.ConcreteValue;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionOccurrence;
import relish.dsl.FunctionSymbol;
import relish.learn.RelationalExample;
import relish.learn.RelationalExample.ExampleConstant;
import relish.learn.RelationalExample.ExampleFunction;
import relish.learn.RelationalExample.ExampleTerm;
import relish.util.FileUtil;
import relish.util.PrintUtil;
import relish.verify.CBMCXmlOutput.CBMCTypedValue;
import relish.verify.RelationalProperty.PropertyConstant;
import relish.verify.RelationalProperty.PropertyFunction;
import relish.verify.RelationalProperty.PropertyTerm;
import relish.verify.RelationalProperty.PropertyVariable;

public class RelationalVerifier {

  // number of loop unwinding for CBMC
  public static final int BOUND = 5;
  // intermediate input file path for CBMC
  public final String IN_FILE;
  // intermediate output file path for CBMC
  public final String OUT_FILE;
  // CBMC command
  public final String CBMC;

  // grammars and function symbols/signatures
  protected final DSLGrammarMap grammarMap;
  // location of the specification/semantics file (implementation of DSL functions)
  protected final String specFile;

  @Deprecated
  public RelationalVerifier(DSLGrammarMap grammarMap, String specFile) {
    // Should not be used 
    throw new RuntimeException();
  }

  public RelationalVerifier(DSLGrammarMap grammarMap, String specFile, String in_file, String out_file) {
    this.grammarMap = grammarMap;
    this.specFile = specFile;
    this.IN_FILE = in_file;
    this.OUT_FILE = out_file;
    this.CBMC = "cbmc --z3 --partial-loops --unwind " + BOUND + " --trace --xml-ui " + in_file;
  }

  public RelationalExample verify(Map<FunctionSymbol, String> funcSymbolToProgTexts, RelationalProperty property) {
    // long startTime = System.currentTimeMillis();
    buildCBMCInput(funcSymbolToProgTexts, property);
    FileUtil.execCommand(CBMC.split("\\s+"), OUT_FILE);
    CBMCXmlOutput xmlOutput = new CBMCXmlOutput(OUT_FILE);
    RelationalExample counterExample = buildCounterExample(xmlOutput, property);
    // long endTime = System.currentTimeMillis();
    // PrintUtil.println("=========== Verification Time: " + (endTime - startTime) + " ms");
    // PrintUtil.println("=== " + counterExample);
    return counterExample;
  }

  // generate the input file of CBMC, stored at IN_FILE
  protected void buildCBMCInput(Map<FunctionSymbol, String> funcSymbolToProgTexts, RelationalProperty property) {
    // load the implementation of all DSL functions
    List<String> lines = FileUtil.readFromFile(specFile);
    // emit the implementation of all synthesized functions
    for (FunctionSymbol funcSymbol : grammarMap.functionSymbolSet()) {
      if (isInternalFunctionSymbol(funcSymbol)) continue;
      StringBuilder signature = new StringBuilder();
      signature.append(funcSymbol.returnType).append(" ");
      signature.append(funcSymbol.functionName).append("(");
      for (Pair<String, String> parameter : funcSymbol.parameters) {
        signature.append(parameter.getValue0()).append(" ");
        signature.append(parameter.getValue1()).append(", ");
      }
      signature.delete(signature.length() - 2, signature.length());
      signature.append(")");
      lines.add(signature.toString() + " {");
      // ------ hack to handle the uninterpreted functions ------
      String progText = funcSymbolToProgTexts.get(funcSymbol);
      if (progText.startsWith("#")) {
        lines.add(progText.substring(1));
      } else {
        lines.add("  return " + progText + ";");
      }
      // ------ end of hack ------
      lines.add("}\n");
    }
    lines.add("int main() {");
    // define all variables
    Set<PropertyVariable> vars = getAllVariables(property);
    for (PropertyVariable var : vars) {
      // model the String type using char array
      if (var.type.equals("String")) {
        lines.add("  unsigned char " + var.name + "Data[LEN];");
        lines.add("  String " + var.name + ";");
        lines.add("  " + var.name + ".length = LEN;");
        lines.add("  for (int i = 0; i < LEN; ++i) {");
        lines.add("    __CPROVER_assume(" + var.name + "Data[i] >= '0' && " + var.name + "Data[i] <= 'z');");
        lines.add("    " + var.name + ".data[i] = " + var.name + "Data[i];");
        lines.add("  }");
      } else {
        lines.add("  " + var.type + " " + var.name + ";");
      }
    }
    // assert the property
    lines.add("  int result = " + property.toCMBCText() + ";");
    lines.add("  __CPROVER_assert(result == 1, \"P1\");");
    lines.add("  return 0;");
    lines.add("}\n");
    FileUtil.writeToFile(IN_FILE, lines);
  }

  // build the counter example given the output of CBMC
  protected RelationalExample buildCounterExample(CBMCXmlOutput output, RelationalProperty property) {
    if (isVerified(output)) return null;
    // get concrete values for all variables
    Set<PropertyVariable> vars = getAllVariables(property);
    Map<PropertyVariable, CBMCTypedValue> valuations = new HashMap<>();
    for (PropertyVariable var : vars) {
      if (var.type.equals("String")) {
        CBMCTypedValue dataValue = output.getVariableValue(var.name + "Data");
        PropertyVariable dataVar = new PropertyVariable("unsigned char []", var.name + "Data");
        valuations.put(dataVar, dataValue);
      }
      // handle all variables
      CBMCTypedValue varValue = output.getVariableValue(var.name);
      assert varValue != null : var;
      valuations.put(var, varValue);
    }
    // instantiate the property with concrete values
    return instantiate(property, valuations);
  }

  // check if CBMC can successfully verify the property
  protected boolean isVerified(CBMCXmlOutput output) {
    String status = output.getCProverStatus();
    if (status.equals("SUCCESS")) {
      return true;
    } else if (status.equals("FAILURE")) {
      return false;
    } else {
      throw new RuntimeException("Unknown CProverStatus");
    }
  }

  // instantiate the relational property with valuations and return a relational example
  public RelationalExample instantiate(RelationalProperty property, Map<PropertyVariable, CBMCTypedValue> valuations) {
    PropertyFunction propFunc = property.rootFunction;
    ExampleFunction exampleFunc = instantiateImpl(valuations, propFunc);
    return new RelationalExample(exampleFunc);
  }

  // helper method for instantiate
  private ExampleFunction instantiateImpl(Map<PropertyVariable, CBMCTypedValue> valuations, PropertyFunction propFunc) {
    List<ExampleTerm> exampleArgs = new ArrayList<>();
    for (PropertyTerm propArg : propFunc.arguments) {
      if (propArg instanceof PropertyVariable) {
        PropertyVariable propVar = (PropertyVariable) propArg;
        assert valuations.containsKey(propVar) : propVar;
        ConcreteValue value = assignConcreteValue(valuations, propVar);
        exampleArgs.add(new ExampleConstant(value));
      } else if (propArg instanceof PropertyConstant) {
        PropertyConstant propConst = (PropertyConstant) propArg;
        ConcreteValue value = propConst.value;
        exampleArgs.add(new ExampleConstant(value));
      } else if (propArg instanceof PropertyFunction) {
        ExampleFunction exampleFunc = instantiateImpl(valuations, (PropertyFunction) propArg);
        exampleArgs.add(exampleFunc);
      } else {
        throw new RuntimeException("Unknown subtype of PropertyTerm");
      }
    }
    // assign index 0 to expect future reassignment
    FunctionOccurrence funcOccur = new FunctionOccurrence(propFunc.funcSymbol, 0);
    return new ExampleFunction(funcOccur, exampleArgs);
  }

  // helper method for assigning concrete values based on types
  private ConcreteValue assignConcreteValue(Map<PropertyVariable, CBMCTypedValue> valuations, PropertyVariable propVar) {
    CBMCTypedValue typedValue = valuations.get(propVar);
    assert typedValue != null : propVar;
    if (propVar.type.equals("int")) {
      int intValue = Integer.parseInt(typedValue.value);
      return new IntConstant(intValue);
    } else if (propVar.type.equals("bool")) {
      boolean boolValue = Boolean.getBoolean(typedValue.value);
      return new BoolConstant(boolValue);
    } else if (propVar.type.equals("String")) {
      CBMCTypedValue dataTypedValue = valuations.get(new PropertyVariable("unsigned char []", propVar.name + "Data"));
      return new StringConstant(buildCBMCStringValue(dataTypedValue));
    } else if (propVar.type.equals("char")) {
      String str = typedValue.value;
      assert str.length() == 1;
      return new CharConstant(str.charAt(0));
    } else {
      throw new RuntimeException("Unknown type of property variable: " + propVar.type);
    }
  }

  // helper method for building String values from CBMC output
  private String buildCBMCStringValue(CBMCTypedValue typedValue) {
    String type = typedValue.type;
    String value = typedValue.value;
    if (type.startsWith("unsigned char [")) {
      PrintUtil.println("=== CBMC String value: " + type + " " + value);
      String lenStr = type.replaceAll("[^\\d]+", "");
      // handle length
      int len = Integer.parseInt(lenStr);
      // handle initial array
      Pattern pattern = Pattern.compile("ARRAY_OF\\((.+)\\)");
      Matcher matcher = pattern.matcher(value);
      char ch = 0;
      if (matcher.find()) {
        ch = (char) Integer.parseInt(matcher.group(1));
      } else {
        throw new RuntimeException("Didn't find content of the array");
      }
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < len; ++i) {
        builder.append(ch);
      }
      // handle assignment
      Pattern assignmentPattern = Pattern.compile("WITH \\[(\\d+)l:=(\\d+)\\]");
      Matcher assignmentMatcher = assignmentPattern.matcher(value);
      while (assignmentMatcher.find()) {
        int index = Integer.parseInt(assignmentMatcher.group(1));
        char val = (char) Integer.parseInt(assignmentMatcher.group(2));
        builder.setCharAt(index, val);
      }
      PrintUtil.println("=== len: " + builder.length() + " data: " + dumpStringUsingAscii(builder.toString()));
      return builder.toString();
    } else {
      throw new RuntimeException("Unknown CBMC String value type");
    }
  }

  // extract all variables from the relational property
  protected Set<PropertyVariable> getAllVariables(RelationalProperty property) {
    Set<PropertyVariable> vars = new HashSet<>();
    PropertyFunction rootFunc = property.rootFunction;
    getAllVariablesImpl(vars, rootFunc);
    return vars;
  }

  // auxiliary method for implementation of getAllVariables
  private void getAllVariablesImpl(Set<PropertyVariable> ret, PropertyFunction func) {
    for (PropertyTerm argument : func.arguments) {
      if (argument instanceof PropertyVariable) {
        ret.add((PropertyVariable) argument);
      } else if (argument instanceof PropertyFunction) {
        PropertyFunction argFunc = (PropertyFunction) argument;
        getAllVariablesImpl(ret, argFunc);
      } else if (argument instanceof PropertyConstant) {
        ;
      } else {
        throw new RuntimeException("Unknown subtype of PropertyTerm");
      }
    }
  }

  // check if a function symbol is eq, and, or, imply, not
  protected boolean isInternalFunctionSymbol(FunctionSymbol functionSymbol) {
    String funcName = functionSymbol.functionName;
    if (funcName.equals("eq") || funcName.equals("and") || funcName.equals("imply") || funcName.equals("or") || funcName.equals("not")
        || funcName.equals("minus")) {
      return true;
    }
    return false;
  }

  private String dumpStringUsingAscii(String str) {
    StringBuilder builder = new StringBuilder();
    builder.append("ARRAY(");
    for (int i = 0; i < str.length(); ++i) {
      builder.append((int) str.charAt(i)).append(", ");
    }
    builder.delete(builder.length() - 2, builder.length());
    builder.append(")");
    return builder.toString();
  }

}
