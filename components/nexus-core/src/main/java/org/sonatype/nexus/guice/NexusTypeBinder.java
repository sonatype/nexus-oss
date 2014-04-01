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
package org.sonatype.nexus.guice;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.sisu.space.QualifiedTypeBinder;
import org.eclipse.sisu.space.QualifiedTypeListener;
import org.eclipse.sisu.space.QualifiedTypeVisitor;
import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.SpaceVisitor;

/**
 * Adapts the usual Plexus binding process to handle Nexus {@link RepositoryType} semantics.
 * 
 * @since 2.8
 */
public final class NexusTypeBinder
    implements QualifiedTypeListener
{
  // ----------------------------------------------------------------------
  // Constants
  // ----------------------------------------------------------------------

  public static final SpaceModule.Strategy STRATEGY = new SpaceModule.Strategy()
  {
    public SpaceVisitor visitor(final Binder binder) {
      return new QualifiedTypeVisitor(new NexusTypeBinder(binder, new QualifiedTypeBinder(binder)));
    }
  };

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final Binder binder;

  private final QualifiedTypeListener delegate;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  /**
   * @param binder Guice binder
   * @param delegate Original Plexus listener
   */
  NexusTypeBinder(final Binder binder, final QualifiedTypeListener delegate) {
    this.binder = binder;
    this.delegate = delegate;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  /**
   * Adds {@link RepositoryType} semantics on top of JSR330 semantics.
   * 
   * @param qualifier always null, removed in refactored eclipse codebase; do not rely on it
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void hear(final Class<?> implementation, final Object source) {
    Class role = getRepositoryRole(implementation);
    if (role != null) {
      String hint = getRepositoryHint(implementation);

      // short-circuit the usual JSR330 binding to just use role+hint
      binder.bind(Key.get(role, Names.named(hint))).to(implementation);

      addRepositoryTypeDescriptor(role, hint);
    }
    else {
      delegate.hear(implementation, source);
    }
  }

  // ----------------------------------------------------------------------
  // Implementation methods
  // ----------------------------------------------------------------------

  /**
   * Scans the implementation's declared interfaces for one annotated with {@link RepositoryType}.
   */
  private static Class<?> getRepositoryRole(final Class<?> implementation) {
    for (Class<?> api : implementation.getInterfaces()) {
      if (api.isAnnotationPresent(RepositoryType.class)) {
        return api;
      }
    }
    return null;
  }

  /**
   * Checks for both kinds of @Named; uses fully-qualified classname if name is missing or blank.
   */
  private static String getRepositoryHint(final Class<?> implementation) {
    String name = null;
    if (implementation.isAnnotationPresent(javax.inject.Named.class)) {
      name = implementation.getAnnotation(javax.inject.Named.class).value();
    }
    else if (implementation.isAnnotationPresent(com.google.inject.name.Named.class)) {
      name = implementation.getAnnotation(com.google.inject.name.Named.class).value();
    }
    return StringUtils.isNotBlank(name) ? name : implementation.getName();
  }

  /**
   * Records a descriptor for the given repository role+hint.
   */
  private void addRepositoryTypeDescriptor(final Class<? extends Repository> role, final String hint) {
    RepositoryType rt = role.getAnnotation(RepositoryType.class);
    binder.bind(RepositoryTypeDescriptor.class).annotatedWith(Names.named(role + ":" + hint))
        .toInstance(new RepositoryTypeDescriptor(role, hint, rt.pathPrefix(), rt.repositoryMaxInstanceCount()));
  }
}
