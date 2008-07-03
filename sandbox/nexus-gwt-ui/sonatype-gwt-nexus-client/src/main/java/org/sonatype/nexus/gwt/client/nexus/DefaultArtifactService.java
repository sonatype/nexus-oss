package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.services.ArtifactService;

public class DefaultArtifactService
    extends AbstractNexusService
    implements ArtifactService
{

    public DefaultArtifactService( Nexus nexus, String path )
    {
        super( nexus, path );
    }

    public void getArtifact( String gid, String aid, String version, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

    public void getArtifactBasicInfos( String gid, String aid, String version, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

    public void getArtifactBuildInfos( String gid, String aid, String version, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

    public void getArtifactEnvironmentInfos( String gid, String aid, String version, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

    public void getArtifactProjectInfos( String gid, String aid, String version, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

}
