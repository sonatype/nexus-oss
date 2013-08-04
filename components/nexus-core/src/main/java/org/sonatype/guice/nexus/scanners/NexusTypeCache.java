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

package org.sonatype.guice.nexus.scanners;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceScanner;
import org.sonatype.guice.bean.scanners.EmptyAnnotationVisitor;
import org.sonatype.guice.bean.scanners.EmptyClassVisitor;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;
import org.sonatype.guice.bean.scanners.asm.Type;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.plugin.ExtensionPoint;
import org.sonatype.plugin.Managed;

/**
 * Caching {@link ClassVisitor} that maintains a map of interface names to {@link NexusType}s.
 */
final class NexusTypeCache
    extends EmptyClassVisitor
{
  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  private static final String EXTENSION_POINT_DESC = Type.getDescriptor(ExtensionPoint.class);

  private static final String REPOSITORY_TYPE_DESC = Type.getDescriptor(RepositoryType.class);

  private static final String MANAGED_DESC = Type.getDescriptor(Managed.class);

  private static final String SINGLETON_DESC = Type.getDescriptor(Singleton.class);

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final Map<String, NexusType> cachedResults = new HashMap<String, NexusType>();

  private final RepositoryTypeAnnotationVisitor repositoryTypeVisitor = new RepositoryTypeAnnotationVisitor();

  NexusType nexusType;

  private boolean isSingleton;

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  /**
   * Attempts to find the {@link NexusType} of a given interface.
   *
   * @param space The class space
   * @param desc  The interface name
   * @return Nexus component type
   */
  public NexusType nexusType(final ClassSpace space, final String name) {
    if (name.startsWith("java")) {
      return MarkedNexusTypes.UNKNOWN;
    }
    if (!cachedResults.containsKey(name)) {
      nexusType = MarkedNexusTypes.UNKNOWN;
      isSingleton = false;

      ClassSpaceScanner.accept(this, space.getResource(name + ".class"));

      cachedResults.put(name, nexusType);
    }
    return cachedResults.get(name);
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    if (EXTENSION_POINT_DESC.equals(desc)) {
      nexusType = MarkedNexusTypes.EXTENSION_POINT;
    }
    else if (MANAGED_DESC.equals(desc)) {
      nexusType = MarkedNexusTypes.MANAGED;
    }
    else if (SINGLETON_DESC.equals(desc)) {
      isSingleton = true;
    }
    else if (REPOSITORY_TYPE_DESC.equals(desc)) {
      repositoryTypeVisitor.reset();
      return repositoryTypeVisitor;
    }
    return null;
  }

  @Override
  public void visitEnd() {
    if (isSingleton) {
      nexusType = nexusType.asSingleton();
    }
  }

  // ----------------------------------------------------------------------
  // RepositoryType annotation scanner
  // ----------------------------------------------------------------------

  final class RepositoryTypeAnnotationVisitor
      extends EmptyAnnotationVisitor
  {
    private String pathPrefix;

    private int repositoryMaxInstanceCount;

    public void reset() {
      pathPrefix = null;
      repositoryMaxInstanceCount = RepositoryType.UNLIMITED_INSTANCES;
    }

    @Override
    public void visit(final String name, final Object value) {
      if ("pathPrefix".equals(name)) {
        pathPrefix = (String) value;
      }
      else if ("repositoryMaxInstanceCount".equals(name)) {
        repositoryMaxInstanceCount = ((Integer) value).intValue();
      }
    }

    @Override
    public void visitEnd() {
      if (pathPrefix != null) {
        nexusType = new DetailedNexusType(new RepositoryTypeImpl(pathPrefix, repositoryMaxInstanceCount));
      }
    }
  }
}
