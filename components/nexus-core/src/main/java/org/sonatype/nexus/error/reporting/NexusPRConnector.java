/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.error.reporting;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.jira.connector.internal.HttpClientConnector;
import org.sonatype.nexus.apachehttpclient.Hc4Provider;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.DefaultRemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;

import org.apache.http.client.HttpClient;

/**
 * NexusConfiguration-aware connector for the attachment parts of sisu-problem-reporting.
 *
 * @since 2.1
 */
@Named
@Singleton
public class NexusPRConnector
    extends HttpClientConnector
{

  private final Hc4Provider httpClientProvider;

  private final RemoteStorageContext remoteStorageContext;

  @Inject
  public NexusPRConnector(final Hc4Provider httpClientProvider,
                          final ApplicationConfiguration applicationConfiguration)
  {
    this.httpClientProvider = httpClientProvider;
    this.remoteStorageContext = new DefaultRemoteStorageContext(
        applicationConfiguration.getGlobalRemoteStorageContext()
    );
  }

  @Override
  protected HttpClient client() {
    return httpClientProvider.createHttpClient(remoteStorageContext);
  }

  @Override
  public void setCredentials(final String username, final String password) {
    remoteStorageContext.setRemoteAuthenticationSettings(
        new UsernamePasswordRemoteAuthenticationSettings(username, password)
    );
  }

}

