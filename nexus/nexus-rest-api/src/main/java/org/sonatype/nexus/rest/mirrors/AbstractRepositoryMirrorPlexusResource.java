package org.sonatype.nexus.rest.mirrors;

import org.restlet.data.Request;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;

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
}
