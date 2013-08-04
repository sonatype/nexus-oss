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

package org.sonatype.nexus.client.internal.rest;

import org.sonatype.nexus.client.core.Condition;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.core.condition.NexusStatusConditions;
import org.sonatype.nexus.client.core.spi.SubsystemFactory;
import org.sonatype.nexus.client.internal.util.Check;
import org.sonatype.nexus.client.rest.AuthenticationInfo;
import org.sonatype.nexus.client.rest.BaseUrl;
import org.sonatype.nexus.client.rest.ConnectionInfo;
import org.sonatype.nexus.client.rest.NexusClientFactory;

/**
 * @since 2.1
 */
public abstract class AbstractNexusClientFactory<NC extends NexusClient>
    implements NexusClientFactory
{

  private final Condition connectionCondition;

  private final SubsystemFactory<?, NC>[] subsystemFactories;

  protected AbstractNexusClientFactory(final Condition connectionCondition,
                                       SubsystemFactory<?, NC>[] subsystemFactories)
  {
    this.connectionCondition = Check.notNull(connectionCondition, "Connection condition");
    this.subsystemFactories = Check.notNull(subsystemFactories, "Subsystem factories");
  }

  protected AbstractNexusClientFactory(final SubsystemFactory<?, NC>[] subsystemFactories) {
    this(NexusStatusConditions.anyModern(), subsystemFactories);
  }

  @Override
  public final NexusClient createFor(final BaseUrl baseUrl) {
    return createFor(baseUrl, null);
  }

  @Override
  public final NexusClient createFor(final BaseUrl baseUrl, final AuthenticationInfo authenticationInfo) {
    final ConnectionInfo connectionInfo = new ConnectionInfo(baseUrl, authenticationInfo, null);
    return createFor(connectionInfo);
  }

  @Override
  public final NexusClient createFor(final ConnectionInfo connectionInfo) {
    final AbstractNexusClient nexusClient =
        doCreateFor(connectionCondition, subsystemFactories, connectionInfo);
    return nexusClient;
  }

  // ==

  protected abstract AbstractNexusClient doCreateFor(final Condition connectionCondition,
                                                     final SubsystemFactory<?, NC>[] subsystemFactories,
                                                     final ConnectionInfo connectionInfo);
}
