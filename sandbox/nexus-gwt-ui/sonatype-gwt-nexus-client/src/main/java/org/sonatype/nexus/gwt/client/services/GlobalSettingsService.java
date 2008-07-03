package org.sonatype.nexus.gwt.client.services;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Representation;

/**
 * The GlobalSettings service.
 * 
 * @author cstamas
 */
public interface GlobalSettingsService
{
    void listGlobalSettings( EntityResponseHandler handler );

    void readGlobalSettings( String settingsPath, EntityResponseHandler handler );

    void updateGlobalSettings( String settingsPath, Representation representation );
}
