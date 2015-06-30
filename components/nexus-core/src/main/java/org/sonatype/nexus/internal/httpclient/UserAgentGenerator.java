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
package org.sonatype.nexus.internal.httpclient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.SystemStatus;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Generates the {@code User-Agent} header value.
 *
 * @since 3.0
 */
@Named
@Singleton
public class UserAgentGenerator
  extends ComponentSupport
{
  private final Provider<SystemStatus> systemStatus;

  private String value;

  private String editorShort;

  @Inject
  public UserAgentGenerator(final Provider<SystemStatus> systemStatus) {
    this.systemStatus = checkNotNull(systemStatus);
  }

  public String generate() {
    SystemStatus status = systemStatus.get();

    // Cache platform details or re-cache if the edition has changed
    if (value == null || !status.getEditionShort().equals(editorShort)) {
      // track edition for cache invalidation
      editorShort = status.getEditionShort();

      value = String.format("Nexus/%s (%s; %s; %s; %s; %s)",
          status.getVersion(),
          editorShort,
          System.getProperty("os.name"),
          System.getProperty("os.version"),
          System.getProperty("os.arch"),
          System.getProperty("java.version"));
    }

    return value;
  }
}
