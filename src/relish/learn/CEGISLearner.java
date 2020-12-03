package relish.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import relish.dsl.DSLGrammarMap;
import relish.dsl.FunctionSymbol;
import relish.fta.ProgramTree;
import relish.util.PrintUtil;
import relish.verify.RelationalProperty;
import relish.verify.RelationalVerifier;

public class CEGISLearner {

  protected final DSLGrammarMap grammarMap;
  protected final RelationalVerifier verifier;

  public CEGISLearner(DSLGrammarMap grammarMap, RelationalVerifier verifier) {
    this.grammarMap = grammarMap;
    this.verifier = verifier;
  }

  // CEGIS loop
  public Map<FunctionSymbol, String> cegisLearn(RelationalProperty property) {
    ComposedCFTALearner learner = new ComposedCFTALearner(grammarMap);
    List<RelationalExample> examples = new ArrayList<>();
    Map<FunctionSymbol, String> funcSymbolToProgTexts = buildInitialPrograms();
    PrintUtil.printMap(funcSymbolToProgTexts);
    RelationalExample counterexample = verifier.verify(funcSymbolToProgTexts, property);
    while (counterexample != null) {
      examples.add(counterexample);
      Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees = learner.learnMultiple(examples);
      funcSymbolToProgTexts = programTreesToTexts(funcSymbolToProgTrees);
      PrintUtil.printMap(funcSymbolToProgTexts);
      counterexample = verifier.verify(funcSymbolToProgTexts, property);
    }
    return funcSymbolToProgTexts;
  }

  // simply return constants according to return types
  protected Map<FunctionSymbol, String> buildInitialPrograms() {
    Map<FunctionSymbol, String> ret = new HashMap<>();
    for (FunctionSymbol funcSymbol : grammarMap.functionSymbolSet()) {
      if (isInternalFuncSymbol(funcSymbol)) continue;
      String retType = funcSymbol.returnType;
      if (retType.equals("int")) {
        ret.put(funcSymbol, "#int _var; return _var;");
      } else if (retType.equals("bool")) {
        ret.put(funcSymbol, "#bool _var; return _var;");
      } else if (retType.equals("String")) {
        ret.put(funcSymbol, "#String _var; return _var;");
      } else if (retType.equals("void")) {
        ret.put(funcSymbol, "");
      } else {
        throw new RuntimeException("Unsupported return type");
      }
    }
    return ret;
  }

  // check if a function symbol is eq, and, or, imply, not
  protected boolean isInternalFuncSymbol(FunctionSymbol functionSymbol) {
    String funcName = functionSymbol.functionName;
    if (funcName.equals("eq") || funcName.equals("and") || funcName.equals("imply") || funcName.equals("or") || funcName.equals("not")
        || funcName.equals("minus")) {
      return true;
    }
    return false;
  }

  public Map<FunctionSymbol, String> programTreesToTexts(Map<FunctionSymbol, ProgramTree> funcSymbolToProgTrees) {
    Map<FunctionSymbol, String> funcSymbolToProgTexts = new HashMap<>();
    for (FunctionSymbol funcSymbol : funcSymbolToProgTrees.keySet()) {
      ProgramTree progTree = funcSymbolToProgTrees.get(funcSymbol);
      funcSymbolToProgTexts.put(funcSymbol, progTree.translateToProgramText());
    }
    return funcSymbolToProgTexts;
  }

}
