package org.sonatype.nexus.test.os;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class OsTestRule implements MethodRule {

  @Override
  public Statement apply(final Statement statement, final FrameworkMethod method, final Object target) {
    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        IgnoreOn ignoreOn = method.getAnnotation(IgnoreOn.class);
        if (ignoreOn == null || !matches(ignoreOn.value())) {
          statement.evaluate();
        }
      }

      private boolean matches(String[] osNames) {
        if (osNames != null) {
          String systemOsName = System.getProperty("os.name").toLowerCase();
          for (String osName : osNames) {
            if (systemOsName.contains(osName.toLowerCase())) {
              return true;
            }
          }
        }

        return false;
      }
    };
  }

}
