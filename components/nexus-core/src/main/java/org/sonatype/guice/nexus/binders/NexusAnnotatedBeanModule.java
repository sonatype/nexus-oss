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

package org.sonatype.guice.nexus.binders;

import java.util.List;
import java.util.Map;

import org.sonatype.guice.bean.binders.SpaceModule;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.ClassSpaceVisitor;
import org.sonatype.guice.nexus.scanners.NexusTypeVisitor;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.PlexusAnnotatedMetadata;
import org.sonatype.inject.BeanScanning;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;

import com.google.inject.Binder;

/**
 * {@link PlexusBeanModule} that registers Plexus beans by scanning classes for runtime annotations.
 */
public final class NexusAnnotatedBeanModule
    implements PlexusBeanModule
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  final ClassSpace space;

  final Map<?, ?> variables;

  final List<String> classNames;

  final List<RepositoryTypeDescriptor> descriptors;

  final BeanScanning scanning;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  /**
   * Creates a bean source that scans the given class space for Nexus annotations using the given scanner.
   */
  public NexusAnnotatedBeanModule(final ClassSpace space, final Map<?, ?> variables, final List<String> classNames,
                                  final List<RepositoryTypeDescriptor> descriptors)
  {
    this(space, variables, classNames, descriptors, BeanScanning.ON);
  }

  /**
   * Creates a bean source that scans the given class space for Nexus annotations using the given scanner.
   */
  public NexusAnnotatedBeanModule(final ClassSpace space, final Map<?, ?> variables, final List<String> classNames,
                                  final List<RepositoryTypeDescriptor> descriptors, final BeanScanning scanning)
  {
    this.space = space;
    this.variables = variables;
    this.classNames = classNames;
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
      return new NexusTypeVisitor(new NexusTypeBinder(binder, classNames, descriptors));
    }
  }

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
