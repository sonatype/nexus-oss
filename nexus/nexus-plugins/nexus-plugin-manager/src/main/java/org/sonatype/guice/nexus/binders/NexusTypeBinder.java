/**
 * Copyright (c) 2010 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
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
