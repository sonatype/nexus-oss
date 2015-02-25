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
package org.sonatype.nexus.repository.proxy.connection;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class RemoteConnectionStatus
{
  private final String description;

  private final String reason;

  private final DateTime validUntil;

  public RemoteConnectionStatus(final String description, final DateTime validUntil) {
    this(description, null, validUntil);
  }

  public RemoteConnectionStatus(final String description, final String reason, final DateTime validUntil) {
    this.description = checkNotNull(description);
    this.reason = reason;
    this.validUntil = checkNotNull(validUntil);
  }

  public String getDescription() {
    return description;
  }

  public String getReason() {
    return reason;
  }

  public DateTime validUntil() {
    return validUntil;
  }

}
