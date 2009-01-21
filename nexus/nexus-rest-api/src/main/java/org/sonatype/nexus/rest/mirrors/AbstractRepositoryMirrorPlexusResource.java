package org.sonatype.nexus.rest.mirrors;

import org.restlet.data.Request;
import org.sonatype.nexus.configuration.model.CMirror;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.MirrorResource;

public abstract class AbstractRepositoryMirrorPlexusResource
    extends AbstractNexusPlexusResource
{
    /** Key to store Repo with which we work against. */
    public static final String REPOSITORY_ID_KEY = "repositoryId";
    
    /** Key to store Mirror with which we work against. */
    public static final String MIRROR_ID_KEY = "mirrorId";
    
    protected String getRepositoryId( Request request )
    {
        return request.getAttributes().get( REPOSITORY_ID_KEY ).toString();
    }
    
    protected String getMirrorId( Request request )
    {
        return request.getAttributes().get( MIRROR_ID_KEY ).toString();
    }
    
    protected MirrorResource nexusToRestModel( CMirror mirror )
    {
        MirrorResource resource = new MirrorResource();
        
        resource.setId( mirror.getId() );
        resource.setUrl( mirror.getUrl() );
        
        return resource;
    }
    
    protected CMirror restToNexusModel( MirrorResource resource, CMirror mirror )
    {
        if ( mirror == null )
        {
            mirror = new CMirror();
        }
        
        mirror.setUrl( resource.getUrl() );
        
        return mirror;
    }
}
