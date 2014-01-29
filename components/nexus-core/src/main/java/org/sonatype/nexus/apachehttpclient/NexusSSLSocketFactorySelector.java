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

import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.conn.ssl.SSLSocketFactorySelector;
import org.apache.http.protocol.HttpContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Selector that takes into account user provided selectors too.
 *
 * @since 2.8
 */
public class NexusSSLSocketFactorySelector
    implements SSLSocketFactorySelector
{
  private final SSLSocketFactory shared;

  private final List<SSLContextSelector> selectors;

  public NexusSSLSocketFactorySelector(final SSLSocketFactory shared, final List<SSLContextSelector> selectors) {
    this.shared = checkNotNull(shared);
    this.selectors = selectors;
  }

  public SSLSocketFactory select(final HttpContext context) {
    if (selectors != null) {
      for (SSLContextSelector selector : selectors) {
        SSLContext sslContext = selector.select(context);
        if (sslContext != null) {
          return sslContext.getSocketFactory();
        }
      }
    }
    return shared;
  }
}
