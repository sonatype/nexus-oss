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

import java.lang.annotation.Annotation;
import java.util.List;

import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.nexus.scanners.NexusTypeListener;
import org.sonatype.guice.plexus.binders.PlexusTypeBinder;
import org.sonatype.guice.plexus.scanners.PlexusTypeListener;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.repository.Repository;

import com.google.inject.Binder;
import org.codehaus.plexus.component.annotations.Component;

public final class NexusTypeBinder
    implements NexusTypeListener
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final PlexusTypeListener plexusTypeBinder;

  private final List<String> classNames;

  private final List<RepositoryTypeDescriptor> descriptors;

  private RepositoryType repositoryType;

  // ----------------------------------------------------------------------
  // Constructors
  // ----------------------------------------------------------------------

  public NexusTypeBinder(final Binder binder, final List<String> classNames,
                         final List<RepositoryTypeDescriptor> descriptors)
  {
    plexusTypeBinder = new PlexusTypeBinder(binder);

    this.classNames = classNames;
    this.descriptors = descriptors;
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public void hear(final String clazz) {
    classNames.add(clazz);
  }

  public void hear(final RepositoryType type) {
    repositoryType = type;
  }

  @SuppressWarnings("unchecked")
  public void hear(final Component component, final DeferredClass<?> clazz, final Object source) {
    plexusTypeBinder.hear(component, clazz, source);
    if (null != repositoryType) {
      descriptors.add(new RepositoryTypeDescriptor((Class<? extends Repository>) component.role(), component.hint(),
          repositoryType.pathPrefix(),
          repositoryType.repositoryMaxInstanceCount()));

      repositoryType = null;
    }
  }

  public void hear(final Annotation qualifier, final Class<?> qualifiedType, final Object source) {
    plexusTypeBinder.hear(qualifier, qualifiedType, source);
  }
}
