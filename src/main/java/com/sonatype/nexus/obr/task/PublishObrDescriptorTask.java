/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.obr.task;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

import com.sonatype.nexus.obr.metadata.ObrMetadataSource;
import com.sonatype.nexus.obr.util.ObrUtils;

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

    private void buildObr( GroupRepository repo )
        throws StorageException
    {
        List<Repository> members = repo.getMemberRepositories();
        for ( Repository repository : members )
        {
            buildObr( repository );
        }
    }

    private void buildObr( Repository repo )
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
