/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.tasks.descriptors.properties.RepositoryOrGroupPropertyDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * @author velo
 */
@Component( role = SchedulerTask.class, hint = "ResetGroupIndexTask", instantiationStrategy = "per-lookup" )
public class ResetGroupIndexTask
    extends AbstractNexusTask<Object>
{

    @Requirement
    private IndexerManager indexerManager;

    @Override
    public boolean isExposed()
    {
        return false;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            indexerManager.resetGroupIndex( getRepositoryGroupId(), false );
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return "REMERGE";
    }

    @Override
    protected String getMessage()
    {
        return "Updating group merge";
    }

    public String getRepositoryGroupId()
    {
        return getParameters().get( RepositoryOrGroupPropertyDescriptor.ID );
    }

    public void setRepositoryGroupId( String repositoryGroupId )
    {
        if ( !StringUtils.isEmpty( repositoryGroupId ) )
        {
            getParameters().put( RepositoryOrGroupPropertyDescriptor.ID, repositoryGroupId );
        }
    }

}
