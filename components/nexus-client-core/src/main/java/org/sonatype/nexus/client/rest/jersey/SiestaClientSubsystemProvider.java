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

package org.sonatype.nexus.client.rest.jersey;

import java.lang.reflect.Method;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.Path;

import org.sonatype.nexus.client.core.spi.SubsystemProvider;
import org.sonatype.sisu.siesta.client.ClientBuilder.Target.Factory;

/**
 * @since 2.7
 */
@Named
@Singleton
public class SiestaClientSubsystemProvider
    implements SubsystemProvider
{

  @Override
  public Object get(final Class type, final Map<Object, Object> context) {
    if (!type.isInterface()) {
      return null;
    }
    final Factory factory = (Factory) context.get(Factory.class);
    if (factory == null) {
      return null;
    }
    if (hasPathAnnotation(type)) {
      return factory.build(type);
    }
    for (final Method method : type.getMethods()) {
      if (method.getAnnotation(Path.class) != null) {
        return factory.build(type);
      }
    }
    return null;
  }

  private boolean hasPathAnnotation(final Class type) {
    if (type.getAnnotation(Path.class) != null) {
      return true;
    }
    for (final Class extended : type.getInterfaces()) {
      if (hasPathAnnotation(extended)) {
        return true;
      }
    }
    return false;
  }

}
