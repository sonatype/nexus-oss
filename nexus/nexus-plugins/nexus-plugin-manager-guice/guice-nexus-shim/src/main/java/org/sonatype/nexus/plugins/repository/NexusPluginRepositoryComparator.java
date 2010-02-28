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
package org.sonatype.nexus.plugins.repository;

import java.util.Comparator;

/**
 * {@link Comparator} that places two {@link NexusPluginRepository} in order of priority; smallest number first.
 */
final class NexusPluginRepositoryComparator
    implements Comparator<NexusPluginRepository>
{
    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public int compare( final NexusPluginRepository o1, final NexusPluginRepository o2 )
    {
        final int order = o1.getPriority() - o2.getPriority();
        if ( 0 == order )
        {
            return o1.getId().compareTo( o2.getId() );
        }
        return order;
    }
}
