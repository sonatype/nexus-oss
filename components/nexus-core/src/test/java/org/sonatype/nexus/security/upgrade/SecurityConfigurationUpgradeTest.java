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

package org.sonatype.nexus.security.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.nexus.proxy.events.EventInspector;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;

public class SecurityConfigurationUpgradeTest
    extends NexusAppTestSupport
{

  protected void copySecurityConfigToPlace()
      throws IOException
  {
    // some old nexus.xml to trigger "real" upgrade
    this.copyResource("/org/sonatype/nexus/security/upgrade/nexus.xml", getNexusConfiguration());
    // the security we want upgraded
    // since https://github.com/sonatype/nexus/commit/d1a5e6fdb79527f19ea5c744bafafd0ccd45f373 nexus.xml triggers
    // security upgrade too
    this.copyResource("/org/sonatype/nexus/security/upgrade/security.xml", getNexusSecurityConfiguration());
  }

  @Test
  public void testLoadComponent()
      throws Exception
  {
    Assert.assertNotNull(this.lookup(EventInspector.class, "SecurityUpgradeEventInspector"));
  }

  @Test
  public void testSecurityUpgradeAndEvent()
      throws Exception
  {
    testLoadComponent();

    this.copySecurityConfigToPlace();

    startNx();

    // verify
    this.verifyUpgrade("/org/sonatype/nexus/security/upgrade/security.result.xml");

  }

  private void verifyUpgrade(String resource)
      throws IOException
  {
    InputStream stream = null;
    StringWriter writer = new StringWriter();
    try {
      stream = getClass().getResourceAsStream(resource);
      IOUtil.copy(stream, writer);
    }
    finally {
      IOUtil.close(stream);
    }

    String expected = writer.toString();

    // security should be upgraded now. lets look at the security.xml
    String securityXML = FileUtils.fileRead(getNexusSecurityConfiguration());

    Assert.assertEquals(expected.replace("\r", ""), securityXML.replace("\r", ""));

  }

}
