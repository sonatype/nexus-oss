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
package org.sonatype.nexus.ldap.internal.ssl;

import java.util.Map;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.sonatype.nexus.ldap.internal.realms.DefaultLdapContextFactory;

import com.google.common.collect.Maps;
import org.apache.shiro.realm.ldap.LdapContextFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link LdapContextFactory} that is using a {@link SSLSocketFactory} configured to use Nexus SSL TrustStore.
 *
 * @since 2.4
 */
public class SSLLdapContextFactory
    implements LdapContextFactory
{

  private final SSLContext sslContext;

  private final DefaultLdapContextFactory delegate;

  public SSLLdapContextFactory(final SSLContext sslContext,
                               final DefaultLdapContextFactory delegate)
  {
    this.sslContext = checkNotNull(sslContext);
    this.delegate = checkNotNull(delegate);

    final Map<String, String> envProperties = Maps.newHashMap();
    envProperties.put("java.naming.ldap.factory.socket", ThreadLocalSocketFactory.class.getName());
    delegate.addAdditionalEnvironment(envProperties);
  }

  @Override
  public LdapContext getSystemLdapContext()
      throws NamingException
  {
    final ClassLoader backup = Thread.currentThread().getContextClassLoader();
    try {
      ThreadLocalSocketFactory.set(sslContext.getSocketFactory());
      Thread.currentThread().setContextClassLoader(ThreadLocalSocketFactory.class.getClassLoader());
      return delegate.getSystemLdapContext();
    }
    finally {
      Thread.currentThread().setContextClassLoader(backup);
      ThreadLocalSocketFactory.remove();
    }
  }

  @Override
  public LdapContext getLdapContext(final String username, final String password)
      throws NamingException
  {
    final ClassLoader backup = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(ThreadLocalSocketFactory.class.getClassLoader());
      ThreadLocalSocketFactory.set(sslContext.getSocketFactory());
      return delegate.getLdapContext(username, password);
    }
    finally {
      Thread.currentThread().setContextClassLoader(backup);
      ThreadLocalSocketFactory.remove();
    }
  }

  @Override
  public LdapContext getLdapContext(final Object principal, final Object credentials)
      throws NamingException
  {
    final ClassLoader backup = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(ThreadLocalSocketFactory.class.getClassLoader());
      ThreadLocalSocketFactory.set(sslContext.getSocketFactory());
      return delegate.getLdapContext(principal, credentials);
    }
    finally {
      Thread.currentThread().setContextClassLoader(backup);
      ThreadLocalSocketFactory.remove();
    }
  }

}
