/**
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
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
package org.sonatype.guice.nexus.scanners;

import static org.sonatype.guice.plexus.scanners.AnnotatedPlexusComponentScanner.visitClassResources;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.plexus.scanners.PlexusComponentScanner;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;

/**
 * {@link PlexusComponentScanner} that uses runtime annotations to discover Nexus components.
 */
public final class AnnotatedNexusComponentScanner
    implements PlexusComponentScanner
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final List<RepositoryTypeDescriptor> repositoryTypes;

    private final List<String> exportedClassNames;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public AnnotatedNexusComponentScanner( final List<RepositoryTypeDescriptor> repositoryTypes,
                                           final List<String> exportedClassNames )
    {
        this.repositoryTypes = repositoryTypes;
        this.exportedClassNames = exportedClassNames;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Map<Component, DeferredClass<?>> scan( final ClassSpace space, final boolean localSearch )
        throws IOException
    {
        final NexusComponentClassVisitor visitor =
            new NexusComponentClassVisitor( space, repositoryTypes, exportedClassNames );

        return visitClassResources( space, visitor ).getComponents();
    }
}