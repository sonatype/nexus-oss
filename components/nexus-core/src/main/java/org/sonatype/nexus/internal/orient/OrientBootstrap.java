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
package org.sonatype.nexus.internal.orient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.orient.DatabaseManager;
import org.sonatype.sisu.goodies.lifecycle.Lifecycle;
import org.sonatype.sisu.goodies.lifecycle.LifecycleSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Orient bootstrap.
 * 
 * @since 3.0
 */
@Named
@Singleton
public class OrientBootstrap
    extends LifecycleSupport
{
  private final Provider<OrientServer> server;

  private final Provider<DatabaseManager> databaseManager;

  @Inject
  public OrientBootstrap(final Provider<OrientServer> server,
                         final Provider<DatabaseManager> databaseManager)
  {
    this.server = checkNotNull(server);
    this.databaseManager = checkNotNull(databaseManager);
  }

  @Override
  protected void doStart() throws Exception {
    server.get().start();

    if (databaseManager.get() instanceof Lifecycle) {
      ((Lifecycle)databaseManager.get()).start();
    }
  }

  @Override
  protected void doStop() throws Exception {
    if (databaseManager.get() instanceof Lifecycle) {
      ((Lifecycle)databaseManager.get()).stop();
    }

    server.get().stop();
  }
}