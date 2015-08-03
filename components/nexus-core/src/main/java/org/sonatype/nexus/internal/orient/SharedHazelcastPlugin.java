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
package org.sonatype.nexus.internal.orient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.orientechnologies.orient.server.hazelcast.OHazelcastPlugin;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link OHazelcastPlugin} using a shared {@link HazelcastInstance}.
 *
 * @since 3.0
 */
public class SharedHazelcastPlugin
    extends OHazelcastPlugin
{
  public static final String INSTANCE_NAME = "nexus";

  @Override
  protected HazelcastInstance configureHazelcast() {
    return (HazelcastInstance) Proxy.newProxyInstance(HazelcastInstance.class.getClassLoader(),
        new Class<?>[] { HazelcastInstance.class }, new InvocationHandler()
        {
          private Object delegate = checkNotNull(Hazelcast.getHazelcastInstanceByName(INSTANCE_NAME));

          public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("shutdown".equals(method.getName())) {
              delegate = null; // detach proxy, but keep delegate alive
              return null;
            }
            if (delegate == null) {
              throw new HazelcastInstanceNotActiveException();
            }
            return method.invoke(delegate, args);
          }
        });
  }
}
