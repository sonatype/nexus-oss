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

package org.sonatype.nexus;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;

import org.junit.Test;

public class RebuildAttributesTest
    extends NexusAppTestSupport
{
  private DefaultNexus defaultNexus;

  private RepositoryRegistry repositoryRegistry;

  protected void setUp()
      throws Exception
  {
    super.setUp();

    defaultNexus = (DefaultNexus) lookup(Nexus.class);

    repositoryRegistry = lookup(RepositoryRegistry.class);
  }

  protected void tearDown()
      throws Exception
  {
    super.tearDown();
  }

  protected boolean loadConfigurationAtSetUp() {
    return false;
  }

  public DefaultNexus getDefaultNexus() {
    return defaultNexus;
  }

  public void setDefaultNexus(DefaultNexus defaultNexus) {
    this.defaultNexus = defaultNexus;
  }

  @Test
  public void testRepositoryRebuildAttributes()
      throws IOException
  {
    try {
      RepositoryTemplate hostedRepoTemplate =
          (RepositoryTemplate) getDefaultNexus().getRepositoryTemplates()
              .getTemplates(Maven2HostedRepositoryTemplate.class).getTemplates(RepositoryPolicy.RELEASE)
              .pick();

      hostedRepoTemplate.getConfigurableRepository().setId("test");
      hostedRepoTemplate.getConfigurableRepository().setName("Test");
      hostedRepoTemplate.getConfigurableRepository().setLocalStatus(LocalStatus.IN_SERVICE);

      hostedRepoTemplate.create();

      repositoryRegistry.getRepository("test")
          .recreateAttributes(new ResourceStoreRequest(RepositoryItemUid.PATH_ROOT), null);
    }
    catch (ConfigurationException e) {
      fail("ConfigurationException creating repository");
    }
    catch (NoSuchRepositoryException e) {
      fail("NoSuchRepositoryException reindexing repository");
    }
  }
}
