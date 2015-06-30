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

import javax.net.SocketFactory;

import static com.google.common.base.Preconditions.checkState;

/**
 * A {@link SocketFactory} that is able to create socket factories / LDAP context. By default LDAP is only able to use
 * one socket factory for all contexts because it only allows setting the name of the class via context property
 * "java.naming.ldap.factory.socket".
 *
 * @see SSLLdapContextFactory
 * @since 2.4
 */
public abstract class ThreadLocalSocketFactory
    extends SocketFactory
{

  static ThreadLocal<SocketFactory> local = new ThreadLocal<SocketFactory>();

  public static SocketFactory getDefault() {
    SocketFactory result = local.get();
    checkState(result != null, "Socket factory not set");
    return result;
  }

  public static void set(SocketFactory factory) {
    local.set(factory);
  }

  public static void remove() {
    local.remove();
  }

}