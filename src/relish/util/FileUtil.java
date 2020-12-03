package relish.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

  public static List<String> readFromFile(String filePath) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line = null;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
      return lines;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("File reading error");
    }
  }

  public static void writeToFile(String filePath, List<String> lines) {
    assert lines != null;
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("File writing error");
    }
  }

  public static void execCommand(String[] cmds, String outputPath) {
    try {
      ProcessBuilder builder = new ProcessBuilder(cmds);
      builder.redirectOutput(new File(outputPath));
      Process process = builder.start();
      process.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Command execution failure");
    }
  }

}
