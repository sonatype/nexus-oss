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

package org.sonatype.nexus.guice;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.binders.PlexusTypeBinder;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.PlexusAnnotatedMetadata;
import org.sonatype.guice.plexus.scanners.PlexusTypeListener;
import org.sonatype.guice.plexus.scanners.PlexusTypeVisitor;
import org.sonatype.inject.BeanScanning;
import org.sonatype.nexus.plugins.DefaultNexusPluginManager;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link PlexusBeanModule} that adds minimal Nexus {@link RepositoryType} semantics on top of Plexus/JSR330.
 *
 * @since 2.7
 */
public final class NexusAnnotatedBeanModule
    implements PlexusBeanModule
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  final ClassSpace space;

  final Map<?, ?> variables;

  final List<RepositoryTypeDescriptor> descriptors;

  final BeanScanning scanning;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  /**
   * Creates a bean source that scans the given class space for Plexus/JSR330 annotations using the given scanner.
   * 
   * @param space The local class space
   * @param variables The filter variables
   */
  public NexusAnnotatedBeanModule(final ClassSpace space,
                                  final Map<?, ?> variables,
                                  final List<RepositoryTypeDescriptor> descriptors)
  {
    this(space, variables, descriptors, BeanScanning.ON);
  }

  /**
   * Creates a bean source that scans the given class space for Plexus/JSR330 annotations using the given scanner.
   * 
   * @param space The local class space
   * @param variables The filter variables
   * @param scanning The scanning options
   */
  public NexusAnnotatedBeanModule(final ClassSpace space,
                                  final Map<?, ?> variables,
                                  final List<RepositoryTypeDescriptor> descriptors,
                                  final BeanScanning scanning)
  {
    this.space = space;
    this.variables = variables;
    this.descriptors = descriptors;
    this.scanning = scanning;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public PlexusBeanSource configure(final Binder binder) {
    if (null != space && scanning != BeanScanning.OFF) {
      new NexusSpaceModule().configure(binder);
    }
    return new NexusAnnotatedBeanSource(variables);
  }

  // ----------------------------------------------------------------------
  // Implementation types
  // ----------------------------------------------------------------------

  private final class NexusSpaceModule
      extends SpaceModule
  {
    NexusSpaceModule() {
      super(space, scanning);
    }

    @Override
    protected ClassSpaceVisitor visitor(final Binder binder) {
      return new PlexusTypeVisitor(new NexusTypeBinder(binder, descriptors, new PlexusTypeBinder(binder)));
    }
  }

  /**
   * Adapts the usual Plexus binding process to handle Nexus {@link RepositoryType} semantics.
   */
  private static final class NexusTypeBinder
      implements PlexusTypeListener
  {
    private static final Logger log = LoggerFactory.getLogger(NexusTypeBinder.class);

    private final Binder binder;

    private final List<RepositoryTypeDescriptor> descriptors;

    private final PlexusTypeListener delegate;

    /**
     * @param binder Guice binder
     * @param descriptors List to populate
     * @param delegate Original Plexus listener
     */
    NexusTypeBinder(final Binder binder,
                    final List<RepositoryTypeDescriptor> descriptors,
                    final PlexusTypeListener delegate)
    {
      this.binder = binder;
      this.descriptors = descriptors;
      this.delegate = delegate;
    }

    /**
     * Adds {@link RepositoryType} semantics on top of JSR330 semantics.
     *
     * @param qualifier always null, removed in refactored eclipse codebase; do not rely on it
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void hear(final @Deprecated Annotation qualifier, final Class<?> implementation, final Object source) {
      Class role = getRepositoryRole(implementation);
      if (role != null) {
        String hint = getRepositoryHint(implementation);

        // short-circuit the usual JSR330 binding to just use role+hint
        binder.bind(Key.get(role, Names.named(hint))).to(implementation);

        addRepositoryTypeDescriptor(role, hint);
      }
      else {
        delegate.hear(qualifier, implementation, source);
      }
    }

    /**
     * Adds {@link RepositoryType} semantics on top of Plexus semantics.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void hear(Component component, final DeferredClass<?> implementation, final Object source) {
      Class role = getRepositoryRole(implementation.load());
      if (role != null) {
        if (StringUtils.isBlank(component.hint())) {
          // if someone forgot to set the repository's hint then use the fully-qualified classname
          component = new ComponentImpl(component.role(), getRepositoryHint(implementation.load()),
              component.instantiationStrategy(), component.description());
        }
        addRepositoryTypeDescriptor(role, component.hint());
      }

      log.warn("Found legacy plexus component: {}", log.isDebugEnabled() ? implementation : implementation.getName());

      delegate.hear(component, implementation, source);
    }

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
     * 
     * @see {@link DefaultNexusPluginManager#createPluginInjector}
     */
    private void addRepositoryTypeDescriptor(final Class<? extends Repository> role, final String hint) {
      RepositoryType rt = role.getAnnotation(RepositoryType.class);
      descriptors.add(new RepositoryTypeDescriptor(role, hint, rt.pathPrefix(), rt.repositoryMaxInstanceCount()));
    }
  }

  /**
   * Enables Plexus annotation metadata for all types, not just @Components.
   */
  private static final class NexusAnnotatedBeanSource
      implements PlexusBeanSource
  {
    private final PlexusBeanMetadata metadata;

    NexusAnnotatedBeanSource(final Map<?, ?> variables) {
      metadata = new PlexusAnnotatedMetadata(variables);
    }

    public PlexusBeanMetadata getBeanMetadata(final Class<?> implementation) {
      return metadata;
    }
  }
}
