import junit.framework.TestCase;

public class RunHangTest extends TestCase {

public void testHang() {
  System.out.println("Test:");
  double d = Double.parseDouble("2.2250738585072012e-308");
  System.out.println("Value: " + d);
 }
}