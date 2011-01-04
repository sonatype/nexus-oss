/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.guice.nexus.binders;

import java.lang.annotation.Annotation;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.nexus.scanners.NexusTypeListener;
import org.sonatype.guice.plexus.binders.PlexusTypeBinder;
import org.sonatype.guice.plexus.scanners.PlexusTypeListener;
import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;

import com.google.inject.Binder;

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

    public NexusTypeBinder( final Binder binder, final List<String> classNames,
                            final List<RepositoryTypeDescriptor> descriptors )
    {
        plexusTypeBinder = new PlexusTypeBinder( binder );

        this.classNames = classNames;
        this.descriptors = descriptors;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void hear( final String clazz )
    {
        classNames.add( clazz );
    }

    public void hear( final RepositoryType type )
    {
        repositoryType = type;
    }

    public void hear( final Component component, final DeferredClass<?> clazz, final Object source )
    {
        plexusTypeBinder.hear( component, clazz, source );
        if ( null != repositoryType )
        {
            descriptors.add( new RepositoryTypeDescriptor( component.role().getName(), component.hint(),
                                                           repositoryType.pathPrefix(),
                                                           repositoryType.repositoryMaxInstanceCount() ) );

            repositoryType = null;
        }
    }

    public void hear( final Annotation qualifier, final Class<?> qualifiedType, final Object source )
    {
        plexusTypeBinder.hear( qualifier, qualifiedType, source );
    }
}
