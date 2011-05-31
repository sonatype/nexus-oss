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
package org.sonatype.nexus.obr.task;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.obr.metadata.ObrMetadataSource;
import org.sonatype.nexus.obr.util.ObrUtils;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = "PublishObrDescriptorTask", instantiationStrategy = "per-lookup" )
public class PublishObrDescriptorTask
    extends AbstractNexusRepositoriesTask<Object>
{
    public static final String REPO_OR_GROUP_FIELD_ID = "repositoryId";

    @Requirement( hint = "obr-bindex" )
    private ObrMetadataSource obrMetadataSource;

    @Requirement
    private Walker walker;

    @Override
    protected String getRepositoryFieldId()
    {
        return REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected Object doRun()
        throws Exception
    {

        Repository repo;
        if ( getRepositoryId() != null )
        {
            repo = getRepositoryRegistry().getRepository( getRepositoryId() );
        }
        else if ( getRepositoryGroupId() != null )
        {
            repo = getRepositoryRegistry().getRepository( getRepositoryGroupId() );
        }
        else
        {
            throw new IllegalArgumentException( "Target repository must be set." );
        }

        buildObr( repo );

        return null;
    }

    private void buildObr( final GroupRepository repo )
        throws StorageException
    {
        final List<Repository> members = repo.getMemberRepositories();
        for ( final Repository repository : members )
        {
            buildObr( repository );
        }
    }

    private void buildObr( final Repository repo )
        throws StorageException
    {
        if ( repo.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            buildObr( repo.adaptToFacet( GroupRepository.class ) );
        }
        else if ( repo.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
        {
            buildObr( repo.adaptToFacet( ShadowRepository.class ).getMasterRepository() );
        }
        else
        {
            ObrUtils.buildObr( obrMetadataSource, ObrUtils.createObrUid( repo ), repo, walker );
        }
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_PUBLISHINDEX_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Publishing obr.xml for repository group " + getRepositoryGroupName();
        }
        return "Publishing obr.xml for repository " + getRepositoryName();
    }

}
