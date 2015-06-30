/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.internal.node;

import java.security.cert.Certificate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.common.node.LocalNodeAccess;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.common.TestAccessible;
import org.sonatype.sisu.goodies.ssl.keystore.KeyStoreManager;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link LocalNodeAccess}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class LocalNodeAccessImpl
    extends ComponentSupport
    implements LocalNodeAccess
{
  private final KeyStoreManager keyStoreManager;

  private volatile String id;

  @Inject
  public LocalNodeAccessImpl(final @Named(KeyStoreManagerImpl.NAME) KeyStoreManager keyStoreManager) {
    this.keyStoreManager = checkNotNull(keyStoreManager);
  }

  @TestAccessible
  protected String loadId() {
    try {
      Certificate cert = keyStoreManager.getCertificate();
      return NodeIdEncoding.nodeIdForCertificate(cert);
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  @Override
  public String getId() {
    // id is volatile, but we don't care if we duplicate load, so avoid explicit synchronized block
    if (id == null) {
      id = loadId();
      log.info("Node-ID: {}", id);
    }
    return id;
  }

  @Override
  public void reset() {
    id = null;
    log.debug("Reset");
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
        "id='" + id + '\'' +
        '}';
  }
}
