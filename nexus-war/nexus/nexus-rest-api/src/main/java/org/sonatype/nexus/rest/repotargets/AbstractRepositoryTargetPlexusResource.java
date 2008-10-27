package org.sonatype.nexus.rest.repotargets;

import java.util.List;

import org.restlet.data.Request;
import org.sonatype.nexus.configuration.model.CRepositoryTarget;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;

public abstract class AbstractRepositoryTargetPlexusResource extends AbstractNexusPlexusResource
{

    @SuppressWarnings("unchecked")
    protected RepositoryTargetResource getNexusToRestResource( CRepositoryTarget target, Request request )
    {
        RepositoryTargetResource resource = new RepositoryTargetResource();

        resource.setId( target.getId() );

        resource.setName( target.getName() );

        resource.setResourceURI( request.getResourceRef().getPath() );

        resource.setContentClass( target.getContentClass() );

        List<String> patterns = target.getPatterns();

        for ( String pattern : patterns )
        {
            resource.addPattern( pattern );
        }

        return resource;
    }

    @SuppressWarnings("unchecked")
    protected CRepositoryTarget getRestToNexusResource( RepositoryTargetResource resource )
    {
        CRepositoryTarget target = new CRepositoryTarget();

        target.setId( resource.getId() );

        target.setName( resource.getName() );
        
        target.setContentClass( resource.getContentClass() );

        List<String> patterns = resource.getPatterns();

        for ( String pattern : patterns )
        {
            target.addPattern( pattern );
        }

        return target;
    }

    protected boolean validate( boolean isNew, RepositoryTargetResource resource )
    {
        if ( isNew )
        {
            if ( resource.getId() == null )
            {
                resource.setId( Long.toHexString( System.currentTimeMillis() ) );
            }
        }

        if ( resource.getId() == null )
        {
            return false;
        }

        return true;
    }
    
}
