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
package com.sonatype.nexus.ssl.plugin.internal;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.sonatype.nexus.httpclient.HttpClientFactory;
import org.sonatype.nexus.httpclient.HttpClientFactory.Builder;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.storage.remote.httpclient.RemoteStorageContextCustomizer;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Certificates retriever from a host:port using Apache Http Client 4.
 *
 * @since ssl 1.0
 */
@Singleton
@Named
public class CertificateRetriever
    extends ComponentSupport
{

  private final HttpClientFactory httpClientFactory;

  @Inject
  public CertificateRetriever(final HttpClientFactory httpClientFactory) {
    this.httpClientFactory = checkNotNull(httpClientFactory);
  }

  private static final TrustManager ACCEPT_ALL_TRUST_MANAGER = new X509TrustManager()
  {
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    public void checkClientTrusted(X509Certificate[] certs, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] certs, String authType) {
    }
  };

  /**
   * Retrieves certificate chain of specified host:port using https protocol.
   *
   * @param host    to get certificate chain from (cannot be null)
   * @param port    of host to connect to
   * @param context used to configure proxy, authentication, other connection params (cannot be null)
   * @return certificate chain
   * @throws Exception Re-thrown from accessing the remote host
   */
  public Certificate[] retrieveCertificatesFromHttpsServer(final String host,
                                                           final int port,
                                                           final RemoteStorageContext context)
      throws Exception
  {
    checkNotNull(context);
    checkNotNull(host);

    log.info("Retrieving certificate from https://{}:{}", host, port);

    Builder httpClientBuilder = null;
    HttpClientConnectionManager connectionManager = null;
    try {
      final AtomicReference<Certificate[]> chain = new AtomicReference<Certificate[]>();
      final SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, new TrustManager[]{ACCEPT_ALL_TRUST_MANAGER}, null);

      final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sc, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
          .register("http", PlainConnectionSocketFactory.getSocketFactory())
          .register("https", sslSocketFactory).build();

      httpClientBuilder = httpClientFactory.prepare(new RemoteStorageContextCustomizer(context));
      connectionManager = new BasicHttpClientConnectionManager(registry);
      httpClientBuilder.getHttpClientBuilder().setConnectionManager(connectionManager);
      httpClientBuilder.getHttpClientBuilder().addInterceptorFirst(
          new HttpResponseInterceptor()
          {
            @Override
            public void process(final HttpResponse response, final HttpContext context)
                throws HttpException, IOException
            {
              final ManagedHttpClientConnection connection = HttpCoreContext.adapt(context).getConnection(ManagedHttpClientConnection.class);
              if (connection != null) {
                SSLSession session = connection.getSSLSession();
                if (session != null) {
                  chain.set(session.getPeerCertificates());
                }
              }
            }
          });
      httpClientBuilder.build().execute(new HttpGet("https://" + host + ":" + port));
      return chain.get();
    }
    finally {
      if (connectionManager != null) {
        connectionManager.shutdown();
      }
    }
  }

  /**
   * Retrieves certificate chain of specified host:port using direct socket connection.
   *
   * @param host to get certificate chain from (cannot be null)
   * @param port of host to connect to
   * @return certificate chain
   * @throws Exception Re-thrown from accessing the remote host
   */
  public Certificate[] retrieveCertificates(final String host,
                                            final int port)
      throws Exception
  {
    checkNotNull(host);

    log.info("Retrieving certificate from {}:{} using direct socket connection", host, port);

    SSLSocket socket = null;
    try {
      final SSLContext sc = SSLContext.getInstance("TLS");
      sc.init(null, new TrustManager[]{ACCEPT_ALL_TRUST_MANAGER}, null);

      final javax.net.ssl.SSLSocketFactory sslSocketFactory = sc.getSocketFactory();
      socket = (SSLSocket) sslSocketFactory.createSocket(host, port);
      socket.startHandshake();
      final SSLSession session = socket.getSession();
      return session.getPeerCertificates();
    }
    finally {
      if (socket != null) {
        socket.close();
      }
    }
  }

}
