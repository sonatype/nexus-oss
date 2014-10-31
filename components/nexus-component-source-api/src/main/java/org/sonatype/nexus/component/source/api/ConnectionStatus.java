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
package org.sonatype.nexus.component.source.api;

import java.io.IOException;

import javax.annotation.Nullable;

import org.sonatype.nexus.component.source.api.config.ComponentSourceConfig;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigId;
import org.sonatype.nexus.component.source.api.config.ComponentSourceConfigStore;
import org.sonatype.nexus.component.source.api.support.AutoBlockState;

import org.joda.time.DateTime;

/**
 * Facilities for inspecting the status of a connection.
 *
 * @since 3.0
 */
public interface ConnectionStatus
{
  /**
   * Returns the current state of auto-blocking.
   */
  AutoBlockState getAutoBlockState();

  /**
   * Return the earliest time that the connection might be expected to return to service.
   */
  @Nullable
  DateTime getBlockedUntil();

  /**
   * Connect to the remote resource to verify that the connection works.
   *
   * @throws IOException If the connection is not working.
   */
  void testConnection() throws IOException;
}
