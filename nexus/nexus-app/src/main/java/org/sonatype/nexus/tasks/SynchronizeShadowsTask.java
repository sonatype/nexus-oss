/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.nexus.tasks.descriptors.SynchronizeShadowTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Publish indexes task.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = SynchronizeShadowTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class SynchronizeShadowsTask
    extends AbstractNexusRepositoriesTask<Object>
{
    /**
     * System event action: shadow sync
     */
    public static final String ACTION = "SYNC_SHADOW";

    @Override
    protected String getRepositoryFieldId()
    {
        return SynchronizeShadowTaskDescriptor.REPO_FIELD_ID;
    }

    public String getShadowRepositoryId()
    {
        return getRepositoryId();
    }

    public void setShadowRepositoryId( String shadowRepositoryId )
    {
        setRepositoryId( shadowRepositoryId );
    }
    
    @Override
    public String getRepositoryId()
    {
        return getParameters().get( getRepositoryFieldId() );
    }
    
    @Override
    public void setRepositoryId( String repositoryId )
    {
        if ( !StringUtils.isEmpty( repositoryId ) )
        {
            getParameters().put( getRepositoryFieldId(), repositoryId );
        }
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        ShadowRepository shadow =
            getRepositoryRegistry().getRepositoryWithFacet( getShadowRepositoryId(), ShadowRepository.class );

        shadow.synchronizeWithMaster();

        return null;
    }

    @Override
    protected String getAction()
    {
        return ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Synchronizing virtual repository ID='" + getShadowRepositoryId() + "') with it's master repository.";
    }

}
