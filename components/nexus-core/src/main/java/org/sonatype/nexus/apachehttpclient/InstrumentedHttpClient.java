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

package org.sonatype.nexus.apachehttpclient;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.RequestDirector;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

// NOTE: Duplicated and augmented from original 2.2.0 source to change signature of CTOR ClientConnectionManager parameter
// NOTE: Should get this changes into metrics-httpclient and avoid needing this

public class InstrumentedHttpClient
    extends DefaultHttpClient
{
  private final Log log = LogFactory.getLog(getClass());

  private final MetricsRegistry registry;

  public InstrumentedHttpClient(MetricsRegistry registry,
                                ClientConnectionManager manager,
                                HttpParams params)
  {
    super(manager, params);
    this.registry = registry;
  }

  public InstrumentedHttpClient(ClientConnectionManager manager, HttpParams params) {
    this(Metrics.defaultRegistry(), manager, params);
  }

  public InstrumentedHttpClient(HttpParams params) {
    this(new InstrumentedClientConnManager(), params);
  }

  public InstrumentedHttpClient() {
    this(null);
  }

  @Override
  protected RequestDirector createClientRequestDirector(HttpRequestExecutor requestExec,
                                                        ClientConnectionManager conman,
                                                        ConnectionReuseStrategy reustrat,
                                                        ConnectionKeepAliveStrategy kastrat,
                                                        HttpRoutePlanner rouplan,
                                                        HttpProcessor httpProcessor,
                                                        HttpRequestRetryHandler retryHandler,
                                                        RedirectStrategy redirectStrategy,
                                                        AuthenticationStrategy targetAuthStrategy,
                                                        AuthenticationStrategy proxyAuthStrategy,
                                                        UserTokenHandler userTokenHandler,
                                                        HttpParams params)
  {
    return new InstrumentedRequestDirector(
        registry,
        log,
        requestExec,
        conman,
        reustrat,
        kastrat,
        rouplan,
        httpProcessor,
        retryHandler,
        redirectStrategy,
        targetAuthStrategy,
        proxyAuthStrategy,
        userTokenHandler,
        params);
  }
}