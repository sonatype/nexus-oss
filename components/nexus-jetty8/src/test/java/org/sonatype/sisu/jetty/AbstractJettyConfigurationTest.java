/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.sisu.jetty;

import java.io.File;
import java.net.URL;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import static org.junit.Assert.fail;

public abstract class AbstractJettyConfigurationTest
    extends TestSupport
{
  protected String getJettyXmlPath(String jettyXmlName) {
    String result = null;

    ClassLoader cloader = Thread.currentThread().getContextClassLoader();
    URL res = cloader.getResource("jetty-xmls/" + jettyXmlName);
    if (res == null) {
      System.out.println("Can't find jetty-xml: " + jettyXmlName + " on classpath; trying filesystem.");
      File f = new File("src/test/resources/jetty-xmls/", jettyXmlName);

      if (!f.isFile()) {
        fail("Cannot find Jetty configuration file: " + jettyXmlName
            + " (tried classpath and base-path src/test/resources/jetty-xmls)");
      }

      result = f.getAbsolutePath();
    }
    else {
      result = res.getPath();
    }

    System.out.println("Jetty configuration path is: '" + result + "'");
    return result;
  }
}
