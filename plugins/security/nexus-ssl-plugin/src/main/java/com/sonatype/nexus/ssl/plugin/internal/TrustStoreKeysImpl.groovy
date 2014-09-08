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
package com.sonatype.nexus.ssl.plugin.internal

import com.sonatype.nexus.ssl.plugin.spi.CapabilityManager
import groovy.transform.WithReadLock
import groovy.transform.WithWriteLock
import org.sonatype.nexus.capability.CapabilityReference
import org.sonatype.nexus.rapture.TrustStoreKeys
import org.sonatype.sisu.goodies.common.ComponentSupport

import javax.inject.Inject
import javax.inject.Named

/**
 * Default {@link TrustStoreKeys}.
 *
 * @since 3.0
 */
@Named
class TrustStoreKeysImpl
extends ComponentSupport
implements TrustStoreKeys
{

  @Inject
  private Map<String, CapabilityManager> managers

  @Override
  @WithReadLock
  public boolean isEnabled(final String type, final String id) {
    CapabilityReference reference = getManager(type).get(id)
    return reference && reference.context().enabled
  }

  @Override
  @WithWriteLock
  public TrustStoreKeys setEnabled(final String type, final String id, final Boolean enabled) {
    getManager(type).enable(id, enabled != null && enabled)
    return this
  }

  private CapabilityManager getManager(final String type) {
    final CapabilityManager manager = managers.get(type)
    assert manager != null, "Capability manager of type '${type}' is not supported"
    return manager
  }

}
