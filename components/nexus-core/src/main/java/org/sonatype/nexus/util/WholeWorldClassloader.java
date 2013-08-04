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

package org.sonatype.nexus.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ClassLoader which exposes all realms of a {@link ClassWorld}.
 *
 * @since 2.6
 */
public class WholeWorldClassloader
    extends ClassLoader
{
  private final ClassWorld classWorld;

  public WholeWorldClassloader(final ClassWorld classWorld) {
    this.classWorld = checkNotNull(classWorld);
  }

  protected ClassWorld getClassWorld() {
    return classWorld;
  }

  @Override
  public Class<?> loadClass(String name)
      throws ClassNotFoundException
  {
    return loadClass(name, false);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<?> loadClass(String name, boolean resolve)
      throws ClassNotFoundException
  {
    for (ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms()) {
      try {
        return realm.loadClass(name);
      }
      catch (ClassNotFoundException e) {
        // ignore it
      }
    }

    throw new ClassNotFoundException(name);
  }

  @Override
  @SuppressWarnings("unchecked")
  public URL getResource(String name) {
    for (ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms()) {
      URL result = realm.getResource(name);

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public InputStream getResourceAsStream(String name) {
    for (ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms()) {
      InputStream result = realm.getResourceAsStream(name);

      if (result != null) {
        return result;
      }
    }

    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Enumeration<URL> findResources(String name)
      throws IOException
  {
    ArrayList<URL> result = new ArrayList<URL>();

    for (ClassRealm realm : (Collection<ClassRealm>) getClassWorld().getRealms()) {
      Enumeration<URL> realmResources = realm.findResources(name);

      for (; realmResources.hasMoreElements(); ) {
        result.add(realmResources.nextElement());
      }
    }

    return Collections.enumeration(result);
  }

}
