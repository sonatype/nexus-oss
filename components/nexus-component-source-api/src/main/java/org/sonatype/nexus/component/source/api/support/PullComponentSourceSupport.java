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
package org.sonatype.nexus.component.source.api.support;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.model.Component;
import org.sonatype.nexus.component.source.api.ComponentEnvelope;
import org.sonatype.nexus.component.source.api.ComponentRequest;
import org.sonatype.nexus.component.source.api.ComponentSourceId;
import org.sonatype.nexus.component.source.api.PullComponentSource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A base class for implementing {@link PullComponentSource}s, providing enabling/disabling and autoblocking support.
 *
 * @since 3.0
 */
public abstract class PullComponentSourceSupport
    extends ComponentSupport
    implements PullComponentSource
{
  private final ComponentSourceId id;

  private boolean enabled = true;

  private final AutoBlockStrategy autoBlockStrategy;

  protected PullComponentSourceSupport(final ComponentSourceId id, final AutoBlockStrategy autoBlockStrategy) {
    this.id = checkNotNull(id);
    this.autoBlockStrategy = checkNotNull(autoBlockStrategy);
  }

  @Override
  public ComponentSourceId getId() {
    return id;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public final <T extends Component> Iterable<ComponentEnvelope<T>> fetchComponents(final ComponentRequest<T> request)
      throws IOException
  {
    if (!enabled) {
      log.info("Request made to disabled source {}.", id);
      return Lists.newArrayList();
    }

    if (!getAutoBlockState().isRequestingAllowed()) {
      log.info("Request made to auto-blocked source {}.", id);
      return Lists.newArrayList();
    }

    try {
      final Iterable<ComponentEnvelope<T>> result = doFetchComponents(request);
      autoBlockStrategy.handleConnectionSuccess();
      return result;
    }
    catch (Exception e) {
      autoBlockStrategy.handleConnectionFailure(e);
      return Lists.newArrayList();
    }
  }

  protected abstract <T extends Component> Iterable<ComponentEnvelope<T>> doFetchComponents(
      final ComponentRequest<T> request) throws Exception;

  @Override
  public AutoBlockState getAutoBlockState() {
    return autoBlockStrategy.getAutoBlockState();
  }

  @Nullable
  @Override
  public DateTime getBlockedUntil() {
    return autoBlockStrategy.getBlockedUntil();
  }
}
