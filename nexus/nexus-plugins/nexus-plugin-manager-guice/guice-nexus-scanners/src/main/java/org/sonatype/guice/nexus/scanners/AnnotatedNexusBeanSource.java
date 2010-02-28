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

import java.util.Map;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.plexus.config.PlexusBeanMetadata;
import org.sonatype.guice.plexus.config.PlexusBeanSource;
import org.sonatype.guice.plexus.scanners.AnnotatedPlexusBeanSource;
import org.sonatype.guice.plexus.scanners.PlexusComponentScanner;

/**
 * {@link PlexusBeanSource} that collects {@link PlexusBeanMetadata} by scanning classes for runtime annotations.
 */
public final class AnnotatedNexusBeanSource
    extends AnnotatedPlexusBeanSource
{
    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    /**
     * Creates a bean source that scans the given class space for Nexus/Plexus annotations using the given scanner.
     * 
     * @param space The local class space
     * @param variables The filter variables
     * @param scanner The component scanner
     */
    public AnnotatedNexusBeanSource( final ClassSpace space, final Map<?, ?> variables,
                                     final PlexusComponentScanner scanner )
    {
        super( space, variables, scanner );
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public PlexusBeanMetadata getBeanMetadata( final Class<?> implementation )
    {
        return this; // assume all classes are potential components
    }
}
