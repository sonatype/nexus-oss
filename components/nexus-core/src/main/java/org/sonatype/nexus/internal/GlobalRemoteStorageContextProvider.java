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
package org.sonatype.nexus.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Global {@link RemoteStorageContext} provider.
 *
 * @since 3.0
 */
@Named("global")
@Singleton
public class GlobalRemoteStorageContextProvider
  implements Provider<RemoteStorageContext>
{
  private final ApplicationConfiguration configuration;

  @Inject
  public GlobalRemoteStorageContextProvider(final ApplicationConfiguration configuration) {
    this.configuration = checkNotNull(configuration);
  }

  @Override
  public RemoteStorageContext get() {
    return configuration.getGlobalRemoteStorageContext();
  }
}
