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
package org.sonatype.nexus.internal.jetty;

import java.util.IdentityHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.bootstrap.jetty.ConnectorConfiguration;
import org.sonatype.nexus.jetty.ConnectorRegistrar;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.collect.Maps;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Connector registrar component.
 *
 * @since 3.0
 */
@Singleton
@Named
public class ConnectorRegistrarImpl
    extends ComponentSupport
    implements ConnectorRegistrar
{
  private final BundleContext bundleContext;

  private final IdentityHashMap<ConnectorConfiguration, ServiceRegistration<ConnectorConfiguration>> managedConfigurations;

  @Inject
  public ConnectorRegistrarImpl(final BundleContext bundleContext) {
    this.bundleContext = checkNotNull(bundleContext);
    this.managedConfigurations = Maps.newIdentityHashMap();
  }

  @Override
  public void addConnector(final ConnectorConfiguration connectorConfiguration) {
    checkNotNull(connectorConfiguration);
    checkState(!managedConfigurations.containsKey(connectorConfiguration));
    final ServiceRegistration<ConnectorConfiguration> serviceRegistration =
        bundleContext.registerService(ConnectorConfiguration.class, connectorConfiguration, null);
    managedConfigurations.put(connectorConfiguration, serviceRegistration);
  }

  @Override
  public void removeConnector(final ConnectorConfiguration connectorConfiguration) {
    checkNotNull(connectorConfiguration);
    final ServiceRegistration<ConnectorConfiguration> serviceRegistration =
        managedConfigurations.remove(connectorConfiguration);
    if (serviceRegistration != null) {
      serviceRegistration.unregister();
    }
  }
}
