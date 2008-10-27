package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.services.GlobalSettingsService;

public class DefaultGlobalSettingsService
    extends AbstractNexusService
    implements GlobalSettingsService
{

    public DefaultGlobalSettingsService( Nexus nexus, String path )
    {
        super( nexus, path );
    }

    public void listGlobalSettings( EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

    public void readGlobalSettings( String settingsPath, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
        
    }

    public void updateGlobalSettings( String settingsPath, Representation representation )
    {
        // TODO Auto-generated method stub
        
    }

}
