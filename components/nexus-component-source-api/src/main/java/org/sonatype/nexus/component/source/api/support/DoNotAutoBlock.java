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

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.joda.time.DateTime;

/**
 * Logs exceptions but never auto-blocks, to be used for sources that should never auto-block.
 *
 * @since 3.0
 */
public class DoNotAutoBlock
    extends ComponentSupport
    implements AutoBlockStrategy
{
  private final String sourceName;

  public DoNotAutoBlock(final String sourceName) {
    this.sourceName = sourceName;
  }

  @Override
  public AutoBlockState getAutoBlockState() {
    return AutoBlockState.NEVER_BLOCKS;
  }

  @Override
  public void handleConnectionFailure(final Exception e) {
    log.error("Error detected during communication with component source %s.", sourceName, e);

    // Nothing else to do, since we never autoblock.
  }

  @Override
  public void handleConnectionSuccess() {
    // does nothing
  }

  @Nullable
  @Override
  public DateTime getBlockedUntil() {
    return null;
  }
}
