/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.yum.plugin;

import org.junit.Test;
import org.sonatype.nexus.plugins.yum.plugin.client.subsystem.YumClient;

import com.sun.jersey.api.client.UniformInterfaceException;

public class SecurityIT extends AbstractIntegrationTestCase {
  @Test(expected = UniformInterfaceException.class)
  public void shouldNotHaveReadAccessToAliasesForAnonymous() throws Exception {
    final String alias = uniqueName();
    yum().createOrUpdateAlias("releases", alias, "1.2.3");
    createNexusClientForAnonymous(nexus()).getSubsystem(YumClient.class).getAliasVersion("releases", alias);
  }

  @Test(expected = UniformInterfaceException.class)
  public void shouldNotCreateAliasForAnonymous() throws Exception {
    createNexusClientForAnonymous(nexus()).getSubsystem(YumClient.class).createOrUpdateAlias("releases", uniqueName(), "1.2.3");
  }

  @Test(expected = UniformInterfaceException.class)
  public void shouldNotHaveUpdateAccessToAliasesForAnonymous() throws Exception {
    final String alias = uniqueName();
    yum().createOrUpdateAlias("releases", alias, "1.2.3");
    createNexusClientForAnonymous(nexus()).getSubsystem(YumClient.class).createOrUpdateAlias("releases", alias, "3.2.1");
  }

}
