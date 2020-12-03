package relish.tests;

import org.junit.Assert;
import org.junit.Test;

import relish.verify.CBMCXmlOutput;
import relish.verify.CBMCXmlOutput.CBMCTypedValue;

public class CBMCXmlOutputTest {

  @Test
  public void testGetCProverStatus() {
    String filename = "test/relish/tests/cbmc-out-sample.xml";
    CBMCXmlOutput parser = new CBMCXmlOutput(filename);
    String result = "FAILURE";
    Assert.assertTrue(parser.getCProverStatus().equals(result));
  }

  @Test
  public void testGetVariableValue() {
    String filename = "test/relish/tests/cbmc-out-sample.xml";
    CBMCXmlOutput parser = new CBMCXmlOutput(filename);
    CBMCTypedValue ret = parser.getVariableValue("var1");
    String resultType = "char [4l]";
    String resultValue = "ARRAY_OF(0)";
    Assert.assertTrue(ret.type.equals(resultType));
    Assert.assertTrue(ret.value.equals(resultValue));
  }

}
