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
import java.net.InetAddress;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * An {@link ClientConnectionOperator} that delegates operations to an {@link ClientConnectionOperator} selected via
 * {@link ClientConnectionOperatorSelector}. If no selector available (contributed by a plugin) it will act as default
 * in http client.
 * <p/>
 * The selected operator is the first non null operator returned by iterating the provided list of selectors.
 *
 * @since 2.4
 */
public class Hc4ClientConnectionOperator
    implements ClientConnectionOperator
{

  private final List<ClientConnectionOperatorSelector> selectors;

  private final DefaultClientConnectionOperator defaultOperator;

  public Hc4ClientConnectionOperator(final SchemeRegistry defaultSchemeRegistry,
                                     final List<ClientConnectionOperatorSelector> selectors)
  {
    this.selectors = selectors;
    defaultOperator = new DefaultClientConnectionOperator(defaultSchemeRegistry);
  }

  @Override
  public OperatedClientConnection createConnection() {
    return defaultOperator.createConnection();
  }

  @Override
  public void openConnection(final OperatedClientConnection conn,
                             final HttpHost target,
                             final InetAddress local,
                             final HttpContext context,
                             final HttpParams params)
      throws IOException
  {
    selectOperator(target, context, params).openConnection(conn, target, local, context, params);
  }

  @Override
  public void updateSecureConnection(final OperatedClientConnection conn,
                                     final HttpHost target,
                                     final HttpContext context,
                                     final HttpParams params)
      throws IOException
  {
    selectOperator(target, context, params).updateSecureConnection(conn, target, context, params);
  }

  private ClientConnectionOperator selectOperator(final HttpHost host,
                                                  final HttpContext context,
                                                  final HttpParams params)
  {
    if (selectors != null) {
      for (final ClientConnectionOperatorSelector selector : selectors) {
        final ClientConnectionOperator operator = selector.get(host, context, params);
        if (operator != null) {
          return operator;
        }
      }
    }
    return defaultOperator;
  }

}
