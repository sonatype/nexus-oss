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
package org.sonatype.nexus.jetty;

import java.util.List;

import org.eclipse.jetty.http.HttpScheme;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Jetty server connector manager.
 *
 * @since 3.9
 */
public interface JettyConnectorManager
{
  /**
   * Returns the list of supported HTTP Schemes. This depends on current Nexus and it's embedded Jetty configuration.
   */
  List<HttpScheme> supportedSchemes();

  /**
   * Returns the "default" configuration for given {@link HttpScheme}. Caller may customize the returned {@link
   * HttpConfiguration} as needed.
   */
  HttpConfiguration defaultHttpConfiguration(final HttpScheme httpScheme);

  /**
   * Creates and adds a {@link ServerConnector} to Jetty server based on passed in information. The connector is NOT
   * started, it should be started/stopped by caller.
   */
  ServerConnector addConnector(final HttpScheme httpScheme,
                               final int port,
                               final HttpConfiguration httpConfiguration);

  /**
   * Removes the {@link ServerConnector} from Jetty server. The connector should be stopped before doing this by
   * caller.
   */
  void removeConnector(final ServerConnector connector);
}
