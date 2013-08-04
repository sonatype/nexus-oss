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

package org.sonatype.nexus.client.rest.jersey;

import java.net.MalformedURLException;

import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.sisu.litmus.testsupport.TestSupport;

public class JerseyNexusClientTestSupport
    extends TestSupport
{

  /**
   * This method is a CHEAT! It would need to prepare and locally run a Nexus instance, but for now, RSO is used...
   * Naturally, this makes the tests unstable too...
   */
  protected NexusClient createClientForLiveInstance(
      final SubsystemFactory<?, JerseyNexusClient>... subsystemFactories)
      throws MalformedURLException
  {
    final NexusClientFactory factory = new JerseyNexusClientFactory(subsystemFactories);
    final NexusClient client = factory.createFor(BaseUrl.baseUrlFrom("https://repository.sonatype.org/"));
    return client;
  }

}
