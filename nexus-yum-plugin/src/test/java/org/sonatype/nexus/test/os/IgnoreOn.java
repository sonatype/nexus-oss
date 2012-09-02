package org.sonatype.nexus.test.os;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface IgnoreOn {

  /**
   * List of operation system names, for which the test method is ignored
   * 
   * @return
   */
  String[] value();

}
