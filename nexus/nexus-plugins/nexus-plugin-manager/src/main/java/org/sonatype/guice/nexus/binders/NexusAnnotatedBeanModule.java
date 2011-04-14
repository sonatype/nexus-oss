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
    public NexusAnnotatedBeanModule( final ClassSpace space, final Map<?, ?> variables, final List<String> classNames,
                                     final List<RepositoryTypeDescriptor> descriptors )
    {
        this( space, variables, classNames, descriptors, BeanScanning.ON );
    }

    /**
     * Creates a bean source that scans the given class space for Nexus annotations using the given scanner.
     */
    public NexusAnnotatedBeanModule( final ClassSpace space, final Map<?, ?> variables, final List<String> classNames,
                                     final List<RepositoryTypeDescriptor> descriptors, final BeanScanning scanning )
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

    public PlexusBeanSource configure( final Binder binder )
    {
        if ( null != space && scanning != BeanScanning.OFF )
        {
            new NexusSpaceModule().configure( binder );
        }
        return new NexusAnnotatedBeanSource( variables );
    }

    // ----------------------------------------------------------------------
    // Implementation types
    // ----------------------------------------------------------------------

    private final class NexusSpaceModule
        extends SpaceModule
    {
        NexusSpaceModule()
        {
            super( space, scanning );
        }

        @Override
        protected ClassSpaceVisitor visitor( final Binder binder )
        {
            return new NexusTypeVisitor( new NexusTypeBinder( binder, classNames, descriptors ) );
        }
    }

    private static final class NexusAnnotatedBeanSource
        implements PlexusBeanSource
    {
        private final PlexusBeanMetadata metadata;

        NexusAnnotatedBeanSource( final Map<?, ?> variables )
        {
            metadata = new PlexusAnnotatedMetadata( variables );
        }

        public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
        {
            return metadata;
        }
    }
}
