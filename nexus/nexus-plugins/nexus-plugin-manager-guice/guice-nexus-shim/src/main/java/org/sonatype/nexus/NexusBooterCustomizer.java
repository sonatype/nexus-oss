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
package org.sonatype.nexus;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.sonatype.appbooter.PlexusAppBooterCustomizer;

/**
 * Nexus specific {@link PlexusAppBooterCustomizer}.
 */
public final class NexusBooterCustomizer
    implements PlexusAppBooterCustomizer
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public void customizeContainerConfiguration( final ContainerConfiguration configuration )
    {
        // nothing to customize
    }

    public void customizeContext( final Context context )
    {
        // nothing to customize
    }

    public void customizeContainer( final PlexusContainer container )
    {
        // nothing to customize
    }
}
