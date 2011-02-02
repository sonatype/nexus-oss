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
package org.sonatype.nexus.maven.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.scheduling.SchedulerTask;

/**
 * @author Juven Xu
 */
@Component( role = SchedulerTask.class, hint = RebuildMavenMetadataTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class RebuildMavenMetadataTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{
    public static final String REBUILD_MAVEN_METADATA_ACTION = "REBUILD_MAVEN_METADATA";
    
    @Override
    protected String getRepositoryFieldId()
    {
        return RebuildMavenMetadataTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }
    
    @Override
    protected String getRepositoryPathFieldId()
    {
        return RebuildMavenMetadataTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        ResourceStoreRequest req = new ResourceStoreRequest( getResourceStorePath() );

        // no repo id, then do all repos
        if ( StringUtils.isEmpty( getRepositoryId() ) )
        {
            getNexus().rebuildMavenMetadataAllRepositories( req );
        }
        else
        {
            Repository repository = getRepositoryRegistry().getRepository( getRepositoryId() );

            // is this a Maven repository at all?
            if ( repository.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
            {
                MavenRepository mavenRepository = repository.adaptToFacet( MavenRepository.class );

                mavenRepository.recreateMavenMetadata( req );
            }
            else
            {
                getLogger().debug( "Repository \"" + repository.getName() + "\" (id=" + repository.getId()
                                       + ") is not a Maven repository. Will not rebuild maven metadata." );
            }
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return REBUILD_MAVEN_METADATA_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryId() != null )
        {
            return "Rebuilding maven metadata of repository " + getRepositoryName() + " from path "
                + getResourceStorePath() + " and below.";
        }
        else
        {
            return "Rebuilding maven metadata of all registered repositories";
        }
    }
}
