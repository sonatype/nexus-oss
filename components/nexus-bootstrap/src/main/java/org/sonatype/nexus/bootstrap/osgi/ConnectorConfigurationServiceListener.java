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
package org.sonatype.nexus.bootstrap.osgi;

import org.sonatype.nexus.bootstrap.jetty.ConnectorConfiguration;
import org.sonatype.nexus.bootstrap.jetty.JettyServer;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

/**
 * Watches for a {@link ConnectorConfiguration} services and registers them with {@link JettyServer}.
 *
 * @since 3.0
 */
public final class ConnectorConfigurationServiceListener
    implements ServiceListener
{
  private final BundleContext bundleContext;

  private final JettyServer jettyServer;

  public ConnectorConfigurationServiceListener(BundleContext bundleContext, JettyServer jettyServer)
      throws InvalidSyntaxException
  {
    this.bundleContext = bundleContext;
    this.jettyServer = jettyServer;
  }

  @Override
  public void serviceChanged(final ServiceEvent event) {
    ConnectorConfiguration connectorConfiguration;
    switch (event.getType()) {
      case ServiceEvent.REGISTERED:
        connectorConfiguration = (ConnectorConfiguration) bundleContext.getService(event.getServiceReference());
        jettyServer.addCustomConnector(connectorConfiguration);
        break;
      case ServiceEvent.MODIFIED:
        connectorConfiguration = (ConnectorConfiguration) bundleContext.getService(event.getServiceReference());
        jettyServer.removeCustomConnector(connectorConfiguration);
        jettyServer.addCustomConnector(connectorConfiguration);
        break;
      case ServiceEvent.UNREGISTERING:
        connectorConfiguration = (ConnectorConfiguration) bundleContext.getService(event.getServiceReference());
        jettyServer.removeCustomConnector(connectorConfiguration);
        break;

      default:
        break;
    }
  }
}
