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

package org.sonatype.nexus.templates.repository.maven;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.NexusAppTestSupport;
import org.sonatype.nexus.proxy.maven.MavenGroupRepository;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.templates.TemplateSet;

import org.junit.Test;

public class MavenRepositoryTemplateTest
    extends NexusAppTestSupport
{
  private Nexus nexus;

  protected boolean loadConfigurationAtSetUp() {
    return false;
  }

  protected void setUp()
      throws Exception
  {
    super.setUp();

    nexus = lookup(Nexus.class);
  }

  protected Nexus getNexus() {
    return nexus;
  }

  @Test
  public void testAvailableRepositoryTemplateCount()
      throws Exception
  {
    TemplateSet templates = getNexus().getRepositoryTemplates();

    assertEquals("Template count is wrong!", 12, templates.size());
  }

  @Test
  public void testSimpleSelection()
      throws Exception
  {
    TemplateSet groups = getNexus().getRepositoryTemplates().getTemplates(MavenGroupRepository.class);

    assertEquals("Template count is wrong!", 2, groups.size());

    assertEquals("Template count is wrong!", 1, groups.getTemplates(new Maven1ContentClass()).size());
    assertEquals("Template count is wrong!", 1, groups.getTemplates(Maven1ContentClass.class).size());

    assertEquals("Template count is wrong!", 1, groups.getTemplates(new Maven2ContentClass()).size());
    assertEquals("Template count is wrong!", 1, groups.getTemplates(Maven2ContentClass.class).size());
  }
}
