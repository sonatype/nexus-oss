/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.storage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.security.ClientInfo;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Component event.
 *
 * @since 3.0
 */
public abstract class ComponentEvent
{
  private final Component component;

  private final Repository repository;

  private final ClientInfo clientInfo;

  public ComponentEvent(final Component component, final Repository repository, @Nullable final ClientInfo clientInfo) {
    this.component = checkNotNull(component);
    this.repository = checkNotNull(repository);
    this.clientInfo = clientInfo;
  }

  @Nonnull
  public Component getComponent() {
    return component;
  }

  @Nonnull
  public Repository getRepository() {
    return repository;
  }

  @Nullable
  public ClientInfo getClientInfo() { return clientInfo; }
}
