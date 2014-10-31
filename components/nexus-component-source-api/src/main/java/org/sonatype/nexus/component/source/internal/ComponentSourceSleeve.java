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
package org.sonatype.nexus.component.source.internal;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.nexus.component.source.api.support.AutoBlockState;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * A {@link PullComponentSource} wrapper which can be disabled by the registry, to prevent out-of-date source instances
 * from being held by handlers indefinitely.
 *
 * @since 3.0
 */
public class ComponentSourceSleeve
    extends ComponentSupport
    implements PullComponentSource
{
  private boolean expired = false;

  private final PullComponentSource source;

  private final ComponentSourceId sourceId;

  public ComponentSourceSleeve(final PullComponentSource source) {
    this.source = checkNotNull(source);
    this.sourceId = checkNotNull(source.getId());
  }

  @Override
  public ComponentSourceId getId() {
    return sourceId;
  }

  public void markExpired() {
    this.expired = true;
  }

  @Override
  public <T extends Component> Iterable<ComponentEnvelope<T>> fetchComponents(final ComponentRequest<T> request)
      throws IOException
  {
    checkExpiry();
    return source.fetchComponents(request);
  }

  @Override
  public boolean isEnabled() {
    checkExpiry();
    return source.isEnabled();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    checkExpiry();
    source.setEnabled(enabled);
  }

  @Override
  public AutoBlockState getAutoBlockState() {
    checkExpiry();
    return source.getAutoBlockState();
  }

  @Nullable
  @Override
  public DateTime getBlockedUntil() {
    checkExpiry();
    return source.getBlockedUntil();
  }

  @Override
  public void testConnection() throws IOException {
    checkExpiry();
    source.testConnection();
  }

  private void checkExpiry() {
    checkState(!expired, "Expired instance of component source %s used.", sourceId);
  }
}
