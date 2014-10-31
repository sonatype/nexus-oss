/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.testsuite.trials;

import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;

/**
 * Controller class for Nexus, decoupled from IT infrastructure to make it reusable in Trials.
 */
public class NxController
{
  private final NexusClient nexusClient;

  public NxController(final NexusClient client) {
    this.nexusClient = client;
  }

  // == Client and repository management

  public NexusClient client() { return nexusClient; }

  public Content content() {
    return nexusClient.getSubsystem(Content.class);
  }

  public Repositories repositories() {
    return nexusClient.getSubsystem(Repositories.class);
  }

}
