package org.sonatype.nexus.rest.mirrors;

import java.util.ArrayList;
import java.util.List;

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
    
    protected List<MirrorResource> nexusToRestModel( List<CMirror> mirrors )
    {
        List<MirrorResource> sortedList = new ArrayList<MirrorResource>();
        
        for ( CMirror mirror : mirrors )
        {
            sortedList.add( nexusToRestModel( mirror ) );
        }
        
        return sortedList;
    }
    
    protected MirrorResource nexusToRestModel( CMirror mirror )
    {
        MirrorResource resource = new MirrorResource();
        
        resource.setId( mirror.getId() );
        resource.setUrl( mirror.getUrl() );
        
        return resource;
    }
    
    protected List<CMirror> restToNexusModel( List<MirrorResource> resources )
    {
        List<CMirror> sortedList = new ArrayList<CMirror>();
        
        for ( MirrorResource resource : resources )
        {
            sortedList.add( restToNexusModel( resource ) );
        }

        return sortedList;
    }
    
    protected CMirror restToNexusModel( MirrorResource resource )
    {
        CMirror mirror = new CMirror();
        
        mirror.setId( resource.getId() );
        mirror.setUrl( resource.getUrl() );
        
        return mirror;
    }
}
