/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.scheduling;

import java.util.HashSet;
import java.util.Set;

import org.sonatype.nexus.scheduling.RepositoryTaskActivityDescriptor.AttributesModificationOperator;
import org.sonatype.nexus.scheduling.RepositoryTaskActivityDescriptor.ModificationOperator;

public class DefaultRepositoryTaskFilter
    implements RepositoryTaskFilter
{
    private boolean allowsRepositoryScanning;

    private boolean allowsScheduledTasks;

    private boolean allowsUserInitiatedTasks;

    private Set<ModificationOperator> contentOperators;

    private Set<AttributesModificationOperator> attributeOperators;

    public boolean allowsAttributeOperations( Set<AttributesModificationOperator> ops )
    {
        return getOrCreateAttributesOperations().containsAll( ops );
    }

    public DefaultRepositoryTaskFilter addAttributeOperator( AttributesModificationOperator attributeOperator )
    {
        getOrCreateAttributesOperations().add( attributeOperator );

        return this;
    }

    public DefaultRepositoryTaskFilter setAttributeOperators( Set<AttributesModificationOperator> attributeOperators )
    {
        this.attributeOperators = attributeOperators;

        return this;
    }

    public boolean allowsContentOperations( Set<ModificationOperator> ops )
    {
        return getOrCreateContentOperations().containsAll( ops );
    }

    public DefaultRepositoryTaskFilter addContentOperators( ModificationOperator contentOperator )
    {
        getOrCreateContentOperations().add( contentOperator );

        return this;
    }

    public DefaultRepositoryTaskFilter setContentOperators( Set<ModificationOperator> contentOperators )
    {
        this.contentOperators = contentOperators;

        return this;
    }

    public boolean allowsRepositoryScanning( String fromPath )
    {
        return allowsRepositoryScanning;
    }

    public DefaultRepositoryTaskFilter setAllowsRepositoryScanning( boolean allowsRepositoryScanning )
    {
        this.allowsRepositoryScanning = allowsRepositoryScanning;

        return this;
    }

    public boolean allowsScheduledTasks()
    {
        return allowsScheduledTasks;
    }

    public DefaultRepositoryTaskFilter setAllowsScheduledTasks( boolean allowsScheduledTasks )
    {
        this.allowsScheduledTasks = allowsScheduledTasks;

        return this;
    }

    public boolean allowsUserInitiatedTasks()
    {
        return allowsUserInitiatedTasks;
    }

    public DefaultRepositoryTaskFilter setAllowsUserInitiatedTasks( boolean allowsUserInitiatedTasks )
    {
        this.allowsUserInitiatedTasks = allowsUserInitiatedTasks;

        return this;
    }

    // ==

    protected Set<ModificationOperator> getOrCreateContentOperations()
    {
        if ( contentOperators == null )
        {
            contentOperators = new HashSet<ModificationOperator>();
        }

        return contentOperators;
    }

    protected Set<AttributesModificationOperator> getOrCreateAttributesOperations()
    {
        if ( attributeOperators == null )
        {
            attributeOperators = new HashSet<AttributesModificationOperator>();
        }

        return attributeOperators;
    }

}
