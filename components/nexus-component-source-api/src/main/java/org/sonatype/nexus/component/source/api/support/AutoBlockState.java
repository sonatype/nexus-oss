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

/**
 * Indicates the autoblocking-related connection state of a component source.
 *
 * @since 3.0
 */
public enum AutoBlockState
{
  /**
   * This component source isn't blocked, and will never auto-block.
   */
  NEVER_BLOCKS(true),

  /**
   * The source is not auto-blocked.
   */
  NOT_BLOCKED(true),

  /**
   * The source has been automatically blocked, and shouldn't be queried nor automatically checked.
   */
  AUTOBLOCKED(false),

  /**
   * The source has been blocked, but the minimum delay before automatically rechecking the source has elapsed. The
   * connection, however, has yet to be checked.
   */
  AUTOBLOCKED_STALE(true);

  private final boolean requestsAllowed;

  private AutoBlockState(final boolean requestsAllowed) {
    this.requestsAllowed = requestsAllowed;
  }

  /**
   * Is it okay to send requests to this source?
   */
  public boolean isRequestingAllowed() {
    return requestsAllowed;
  }
}
