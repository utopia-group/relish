package relish.verify;

import java.util.List;

public class CBMCJsonOutput {

  public List<CBMCItem> items;

  public static class CBMCItem {
    public String program;
    public String messageText;
    public String messageType;
    public List<CBMCResult> result;
    public String cProverStatus;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\n");
      if (program != null) builder.append("program: ").append(program).append("\n");
      if (messageText != null) builder.append("messageText: ").append(messageText).append("\n");
      if (messageType != null) builder.append("messageType: ").append(messageType).append("\n");
      if (result != null) builder.append("result: ").append(result).append("\n");
      if (cProverStatus != null) builder.append("cProverStatus: ").append(cProverStatus).append("\n");
      builder.append("}\n");
      return builder.toString();
    }
  }

  public static class CBMCResult {
    public String description;
    public String property;
    public String status;
    public List<CBMCTrace> trace;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\n");
      builder.append("description: ").append(description).append("\n");
      builder.append("property: ").append(property).append("\n");
      builder.append("status: ").append(status).append("\n");
      builder.append("trace: ").append(trace).append("\n");
      builder.append("}\n");
      return builder.toString();
    }
  }

  public static class CBMCTrace {
    public boolean hidden;
    public int thread;
    public String stepType;
    public CBMCFunction function;
    public String assignmentType;
    public String lhs;
    public String mode;
    public CBMCSourceLocation sourceLocation;
    public CBMCValue value;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\n");
      builder.append("hidden: ").append(hidden).append("\n");
      builder.append("thread: ").append(thread).append("\n");
      builder.append("stepType: ").append(stepType).append("\n");
      if (function != null) builder.append("function: ").append(function).append("\n");
      if (assignmentType != null) builder.append("assignmentType: ").append(assignmentType).append("\n");
      if (lhs != null) builder.append("lhs: ").append(lhs).append("\n");
      if (mode != null) builder.append("mode: ").append(mode).append("\n");
      if (sourceLocation != null) builder.append("sourceLocation: ").append(sourceLocation).append("\n");
      if (value != null) builder.append("value: ").append(value).append("\n");
      builder.append("}\n");
      return builder.toString();
    }
  }

  public static class CBMCFunction {
    public String displayName;
    public String identifier;
    public CBMCSourceLocation sourceLocation;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\n");
      if (displayName != null) builder.append("displayName: ").append(displayName).append("\n");
      if (identifier != null) builder.append("identifier: ").append(identifier).append("\n");
      if (sourceLocation != null) builder.append("sourceLocation: ").append(sourceLocation).append("\n");
      builder.append("}");
      return builder.toString();
    }
  }

  public static class CBMCValue {
    public String binary;
    public String c_type;
    public String data;
    public String name;
    public Integer width;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\n");
      if (binary != null) builder.append("binary: ").append(binary).append("\n");
      if (c_type != null) builder.append("c_type: ").append(c_type).append("\n");
      if (data != null) builder.append("data: ").append(data).append("\n");
      if (name != null) builder.append("name").append(name).append("\n");
      if (width != null) builder.append("width: ").append(width).append("\n");
      builder.append("}");
      return builder.toString();
    }
  }

  public static class CBMCSourceLocation {
    public String file;
    public String function;
    public String line;

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("{\n");
      if (file != null) builder.append("file: ").append(file).append("\n");
      if (function != null) builder.append("function: ").append(function).append("\n");
      if (line != null) builder.append("line: ").append(line).append("\n");
      builder.append("}");
      return builder.toString();
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[\n");
    items.forEach((item) -> builder.append(item));
    builder.append("]\n");
    return builder.toString();
  }

}
