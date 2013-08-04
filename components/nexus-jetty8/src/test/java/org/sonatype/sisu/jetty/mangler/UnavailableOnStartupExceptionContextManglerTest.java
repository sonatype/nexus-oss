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

package org.sonatype.sisu.jetty.mangler;

import java.io.File;

import org.sonatype.sisu.jetty.AbstractJettyConfigurationTest;
import org.sonatype.sisu.jetty.Jetty8;

import junit.framework.Assert;
import org.junit.Test;

public class UnavailableOnStartupExceptionContextManglerTest
    extends AbstractJettyConfigurationTest
{
  @Test
  public void testNoPreconfiguredWars()
      throws Exception
  {
    Jetty8 subject = new Jetty8(new File(getJettyXmlPath("jetty-with-rewrite-handler.xml")));
    int affected = subject.mangleServer(new UnavailableOnStartupExceptionContextMangler());
    Assert.assertEquals("Config contains no WebApps!", 0, affected);
  }

  @Test
  public void testOneWar()
      throws Exception
  {
    Jetty8 subject = new Jetty8(new File(getJettyXmlPath("jetty-one-war-context.xml")));
    int affected = subject.mangleServer(new UnavailableOnStartupExceptionContextMangler());
    Assert.assertEquals("One WAR needs to be set!", 1, affected);
  }

  @Test
  public void testTwoWars()
      throws Exception
  {
    Jetty8 subject = new Jetty8(new File(getJettyXmlPath("jetty-two-war-context.xml")));
    int affected = subject.mangleServer(new UnavailableOnStartupExceptionContextMangler());
    Assert.assertEquals("Two WARs needs to be set!", 2, affected);
  }
}
