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

import java.io.IOException;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.apache.commons.logging.Log;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.DefaultRequestDirector;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;

// NOTE: Duplicated and unchanged from original 2.2.0 source to support change in InstrumentedHttpClient
// NOTE: Should get this changes into metrics-httpclient and avoid needing this

class InstrumentedRequestDirector
    extends DefaultRequestDirector
{
  private final static String GET = "GET", POST = "POST", HEAD = "HEAD", PUT = "PUT",
      OPTIONS = "OPTIONS", DELETE = "DELETE", TRACE = "TRACE",
      CONNECT = "CONNECT", MOVE = "MOVE", PATCH = "PATCH";

  private final Timer getTimer;

  private final Timer postTimer;

  private final Timer headTimer;

  private final Timer putTimer;

  private final Timer deleteTimer;

  private final Timer optionsTimer;

  private final Timer traceTimer;

  private final Timer connectTimer;

  private final Timer moveTimer;

  private final Timer patchTimer;

  private final Timer otherTimer;

  InstrumentedRequestDirector(MetricsRegistry registry,
                              Log log,
                              HttpRequestExecutor requestExec,
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
    super(log,
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
    getTimer = registry.newTimer(HttpClient.class, "get-requests");
    postTimer = registry.newTimer(HttpClient.class, "post-requests");
    headTimer = registry.newTimer(HttpClient.class, "head-requests");
    putTimer = registry.newTimer(HttpClient.class, "put-requests");
    deleteTimer = registry.newTimer(HttpClient.class, "delete-requests");
    optionsTimer = registry.newTimer(HttpClient.class, "options-requests");
    traceTimer = registry.newTimer(HttpClient.class, "trace-requests");
    connectTimer = registry.newTimer(HttpClient.class, "connect-requests");
    moveTimer = registry.newTimer(HttpClient.class, "move-requests");
    patchTimer = registry.newTimer(HttpClient.class, "patch-requests");
    otherTimer = registry.newTimer(HttpClient.class, "other-requests");
  }

  @Override
  public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context)
      throws HttpException, IOException
  {
    final TimerContext timerContext = timer(request).time();
    try {
      return super.execute(target, request, context);
    }
    finally {
      timerContext.stop();
    }
  }

  private Timer timer(HttpRequest request) {
    final String method = request.getRequestLine().getMethod();
    if (GET.equalsIgnoreCase(method)) {
      return getTimer;
    }
    else if (POST.equalsIgnoreCase(method)) {
      return postTimer;
    }
    else if (PUT.equalsIgnoreCase(method)) {
      return putTimer;
    }
    else if (HEAD.equalsIgnoreCase(method)) {
      return headTimer;
    }
    else if (DELETE.equalsIgnoreCase(method)) {
      return deleteTimer;
    }
    else if (OPTIONS.equalsIgnoreCase(method)) {
      return optionsTimer;
    }
    else if (TRACE.equalsIgnoreCase(method)) {
      return traceTimer;
    }
    else if (CONNECT.equalsIgnoreCase(method)) {
      return connectTimer;
    }
    else if (PATCH.equalsIgnoreCase(method)) {
      return patchTimer;
    }
    else if (MOVE.equalsIgnoreCase(method)) {
      return moveTimer;
    }
    return otherTimer;
  }
}