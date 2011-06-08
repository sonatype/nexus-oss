/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package com.sonatype.nexus.p2.updatesite;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

@Component( role = SchedulerTask.class, hint = UpdateSiteMirrorTask.ROLE_HINT, instantiationStrategy = "per-lookup" )
public class UpdateSiteMirrorTask
    extends AbstractNexusRepositoriesTask<Object>
{
    public static final String ROLE_HINT = "UpdateSiteMirrorTask";

    @Requirement
    private RepositoryRegistry registry;

    @Override
    protected String getRepositoryFieldId()
    {
        return UpdateSiteMirrorTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        String repositoryId = getRepositoryId();
        String groupId = getRepositoryGroupId();

        if ( repositoryId != null )
        {
            UpdateSiteRepository repository = getRepository( repositoryId );

            if ( repository == null )
            {
                throw new IllegalStateException( ROLE_HINT + " only applicable to Eclipse Update Sites" );
            }

            repository.doMirror( getForce() );
        }
        else if ( groupId != null )
        {
            GroupRepository group = registry.getRepository( groupId ).adaptToFacet( GroupRepository.class );

            if ( group == null )
            {
                throw new IllegalStateException( "groupId is not a GroupRepository!" );
            }

            for ( Repository repository : group.getMemberRepositories() )
            {
                UpdateSiteRepository updateSite = getRepository( repository );

                if ( updateSite != null )
                {
                    updateSite.doMirror( getForce() );
                }
            }
        }
        else
        {
            for ( Repository repository : registry.getRepositories() )
            {
                UpdateSiteRepository updateSite = getRepository( repository );

                if ( updateSite != null )
                {
                    updateSite.doMirror( getForce() );
                }
            }
        }

        return null;
    }

    private UpdateSiteRepository getRepository( String repositoryId )
    {
        // bad id, no repo
        if ( repositoryId == null )
        {
            return null;
        }

        try
        {
            return getRepository( registry.getRepository( repositoryId ) );
        }
        // no repo
        catch ( NoSuchRepositoryException e )
        {
            return null;
        }
    }

    private UpdateSiteRepository getRepository( Repository repository )
    {
        // no object, no repo
        if ( repository == null )
        {
            return null;
        }

        // will return null if not an update site repo
        return repository.adaptToFacet( UpdateSiteRepository.class );
    }

    @Override
    protected String getAction()
    {
        return ROLE_HINT;
    }

    @Override
    protected String getMessage()
    {
        String repo = getRepositoryId();

        if ( repo == null )
        {
            repo = getRepositoryGroupId();

            if ( repo == null )
            {
                return "Mirroring content of All Eclipse Update Sites.";
            }

            return "Mirroring content of All Eclipse Update Sites in group ID='" + repo + "'.";
        }

        return "Mirroring content of Eclipse Update Site ID='" + repo + "'.";
    }

    public void setRepositoryId( String repositoryId )
    {
        try
        {
            registry.getRepository( repositoryId );
        }
        catch ( NoSuchRepositoryException e )
        {
            throw new IllegalStateException( e );
        }

        super.setRepositoryId( repositoryId );
    }

    public void setForce( boolean force )
    {
        addParameter( UpdateSiteMirrorTaskDescriptor.FORCE_MIRROR_FIELD_ID, Boolean.toString( force ) );
    }

    public boolean getForce()
    {
        return Boolean.parseBoolean( getParameter( UpdateSiteMirrorTaskDescriptor.FORCE_MIRROR_FIELD_ID ) );
    }
}
