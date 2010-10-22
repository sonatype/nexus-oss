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
package org.sonatype.guice.nexus.scanners;

import org.sonatype.guice.plexus.scanners.PlexusTypeListener;
import org.sonatype.nexus.plugins.RepositoryType;

/**
 * {@link PlexusTypeListener} that also listens for Nexus metadata.
 */
public interface NexusTypeListener
    extends PlexusTypeListener
{
    /**
     * Invoked when the {@link NexusTypeListener} finds a public/exported class.
     * 
     * @param clazz The fully-qualified class name
     */
    void hear( String clazz );

    /**
     * Invoked when the {@link NexusTypeListener} finds a {@link RepositoryType}.
     * 
     * @param repositoryType The repository type
     */
    void hear( RepositoryType repositoryType );
}
