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
package org.sonatype.nexus.internal.commands;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.app.SystemStatus;

import org.apache.karaf.shell.commands.InfoProvider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Contributes {@code Nexus} section to {@code shell:info} command.
 *
 * @since 3.0
 */
@Named
@Singleton
public class NexusInfoProvider
  implements InfoProvider
{
  private final Provider<SystemStatus> systemStatusProvider;

  @Inject
  public NexusInfoProvider(final Provider<SystemStatus> systemStatusProvider) {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
  }

  @Override
  public String getName() {
    return "Nexus";
  }

  @Override
  public Properties getProperties() {
    Properties properties = new Properties();
    SystemStatus status = systemStatusProvider.get();
    properties.setProperty("Nexus Version", status.getVersion());
    properties.setProperty("Nexus Edition", status.getEditionShort());
    return properties;
  }
}
