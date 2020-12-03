package relish.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import relish.abs.Abstractions.CharConstant;
import relish.abs.Abstractions.IntConstant;
import relish.abs.Abstractions.StringConstant;
import relish.abs.Abstractions.Value;
import relish.dsl.Production;
import relish.eval.DSLAstNode.CharLiteralNode;
import relish.eval.DSLAstNode.FunctionNode;
import relish.eval.DSLAstNode.IntLiteralNode;
import relish.eval.DSLAstNode.StringLiteralNode;
import relish.eval.DSLAstNode.VariableNode;
import relish.util.StringUtil;

public class DSLInterpreter {

  private Map<String, Production> nameToDslConstructs;

  public DSLInterpreter(Map<String, Production> nameToDslConstructs) {
    this.nameToDslConstructs = nameToDslConstructs;
  }

  public DSLAstNode parseProgram(String program) {
    List<String> tokens = tokenize(program);
    assert tokens.size() > 1 : program;
    String funcName = tokens.get(0);
    assert nameToDslConstructs.containsKey(funcName) : funcName;
    Production func = nameToDslConstructs.get(funcName);
    List<DSLAstNode> args = new ArrayList<>();
    // parse all arguments
    for (int i = 1; i < tokens.size(); ++i) {
      String token = tokens.get(i);
      if (isFunction(token)) {
        args.add(parseProgram(token));
      } else if (isIntLiteral(token)) {
        args.add(new IntLiteralNode(new IntConstant(Integer.parseInt(token))));
      } else if (isStrLiteral(token)) {
        args.add(new StringLiteralNode(new StringConstant(token.substring(1, token.length() - 1))));
      } else if (isCharLiteral(token)) {
        args.add(new CharLiteralNode(new CharConstant(token.charAt(1))));
      } else if (isVariable(token)) {
        args.add(new VariableNode(token));
      } else {
        throw new RuntimeException("Unknown token type: " + token);
      }
    }
    // build the AST node for current function
    return new FunctionNode(funcName, func, args);
  }

  public Value evaluate(DSLAstNode root, Map<String, Value> valuation) {
    if (root instanceof IntLiteralNode) {
      return ((IntLiteralNode) root).value;
    } else if (root instanceof StringLiteralNode) {
      return ((StringLiteralNode) root).value;
    } else if (root instanceof CharLiteralNode) {
      return ((CharLiteralNode) root).value;
    } else if (root instanceof VariableNode) {
      String varName = ((VariableNode) root).name;
      assert valuation.containsKey(varName) : varName;
      return valuation.get(varName);
    } else if (root instanceof FunctionNode) {
      FunctionNode funcNode = (FunctionNode) root;
      Value[] args = new Value[funcNode.arguments.size()];
      for (int i = 0; i < args.length; ++i) {
        args[i] = evaluate(funcNode.arguments.get(i), valuation);
      }
      return funcNode.func.exec(args);
    } else {
      throw new RuntimeException("Unknown subtype of DSLAstNode: " + root);
    }
  }

  // Deprecated due to performance issue
  @Deprecated
  public Value evaluate(String program, Map<String, Value> valuation) {
    List<String> tokens = tokenize(program);
    assert tokens.size() > 1 : program;
    String funcName = tokens.get(0);
    assert nameToDslConstructs.containsKey(funcName) : funcName;
    Production func = nameToDslConstructs.get(funcName);
    Value[] args = new Value[tokens.size() - 1];
    // evaluate all arguments
    for (int i = 1; i < tokens.size(); ++i) {
      String token = tokens.get(i);
      if (isFunction(token)) {
        args[i - 1] = evaluate(token, valuation);
      } else if (isIntLiteral(token)) {
        args[i - 1] = new IntConstant(Integer.parseInt(token));
      } else if (isStrLiteral(token)) {
        args[i - 1] = new StringConstant(token.substring(1, token.length() - 1));
      } else if (isCharLiteral(token)) {
        args[i - 1] = new CharConstant(token.charAt(1));
      } else if (isVariable(token)) {
        assert valuation.containsKey(token) : token;
        args[i - 1] = valuation.get(token);
      } else {
        throw new RuntimeException("Unknown token type: " + token);
      }
    }
    // evaluate the current function
    return func.exec(args);
  }

  public List<String> tokenize(String program) {
    assert isFunction(program) : program;
    List<String> tokens = new ArrayList<>();
    // parse the function name
    String funcName = strBeforeLeftParen(program);
    tokens.add(funcName);
    // parse the arguments
    String argString = StringUtil.getContentInParens(program);
    tokens.addAll(splitByCommaAndParens(argString));
    return tokens;
  }

  public boolean isFunction(String str) {
    return str.indexOf('(') != -1;
  }

  public boolean isIntLiteral(String str) {
    Pattern pattern = Pattern.compile("0|[1-9][0-9]*");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  public boolean isCharLiteral(String str) {
    Pattern pattern = Pattern.compile("'.'");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  public boolean isStrLiteral(String str) {
    Pattern pattern = Pattern.compile("\".*\"");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  public boolean isVariable(String str) {
    Pattern pattern = Pattern.compile("x[0-9]+");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  private String strBeforeLeftParen(String str) {
    return str.substring(0, str.indexOf('('));
  }

  private List<String> splitByCommaAndParens(String line) {
    List<String> tokens = new ArrayList<>();
    int numLeftParens = 0;
    int tokenStartIndex = 0;
    for (int i = 0; i < line.length(); ++i) {
      char ch = line.charAt(i);
      if (ch == ',' && numLeftParens == 0) {
        tokens.add(line.substring(tokenStartIndex, i));
        tokenStartIndex = i + 2;
        ++i;
      } else if (ch == '(') {
        ++numLeftParens;
      } else if (ch == ')') {
        --numLeftParens;
      }
    }
    tokens.add(line.substring(tokenStartIndex, line.length()));
    return tokens;
  }

}
