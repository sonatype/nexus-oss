/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.common.io.Closeables;
import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.protocol.HttpContext;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import sun.security.ssl.SSLSocketImpl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Nexus specific implementation of {@link LayeredConnectionSocketFactory}, used for HTTPS connections.
 *
 * @since 2.8
 */
public class NexusSSLConnectionSocketFactory
    implements LayeredConnectionSocketFactory
{
  private final SSLSocketFactory defaultSocketFactory;

  private final List<SSLContextSelector> selectors;

  private final X509HostnameVerifier hostnameVerifier;

  public NexusSSLConnectionSocketFactory(
      final SSLSocketFactory defaultSocketFactory,
      final X509HostnameVerifier hostnameVerifier,
      final List<SSLContextSelector> selectors)
  {
    this.defaultSocketFactory = checkNotNull(defaultSocketFactory);
    this.hostnameVerifier = checkNotNull(hostnameVerifier);
    this.selectors = selectors; // might be null
  }

  private SSLSocketFactory select(final HttpContext context) {
    if (selectors != null) {
      for (SSLContextSelector selector : selectors) {
        SSLContext sslContext = selector.select(context);
        if (sslContext != null) {
          return sslContext.getSocketFactory();
        }
      }
    }
    return defaultSocketFactory;
  }

  private void verifyHostname(final SSLSocket sslsock, final String hostname) throws IOException {
    try {
      hostnameVerifier.verify(hostname, sslsock);
    }
    catch (final IOException e) {
      Closeables.close(sslsock, true);
      throw e;
    }
  }

  @Override
  public Socket createSocket(final HttpContext context) throws IOException {
    return select(context).createSocket();
  }

  @Override
  @IgnoreJRERequirement
  public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
                              final InetSocketAddress remoteAddress,
                              final InetSocketAddress localAddress, final HttpContext context) throws IOException
  {
    checkNotNull(host);
    checkNotNull(remoteAddress);
    final Socket sock = socket != null ? socket : createSocket(context);
    if (localAddress != null) {
      sock.bind(localAddress);
    }
    // NEXUS-6838: SNI support
    if (sock instanceof SSLSocketImpl) {
      ((SSLSocketImpl)sock).setHost(host.getHostName());
    }
    try {
      sock.connect(remoteAddress, connectTimeout);
    }
    catch (final IOException e) {
      Closeables.close(sock, true);
      throw e;
    }
    // Setup SSL layering if necessary
    if (sock instanceof SSLSocket) {
      final SSLSocket sslsock = (SSLSocket) sock;
      sslsock.startHandshake();
      verifyHostname(sslsock, host.getHostName());
      return sock;
    }
    else {
      return createLayeredSocket(sock, host.getHostName(), remoteAddress.getPort(), context);
    }
  }

  @Override
  public Socket createLayeredSocket(final Socket socket, final String target, final int port, final HttpContext context)
      throws IOException
  {
    checkNotNull(socket);
    checkNotNull(target);
    final SSLSocket sslsock = (SSLSocket) select(context).createSocket(
        socket,
        target,
        port,
        true);
    sslsock.startHandshake();
    verifyHostname(sslsock, target);
    return sslsock;
  }
}
