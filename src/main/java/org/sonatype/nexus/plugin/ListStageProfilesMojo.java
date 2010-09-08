/*
 * Nexus Plugin for Maven
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.plugin;

import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageProfile;

/**
 * Lists all Nexus profiles repositories for a user.
 * 
 * @goal staging-profiles-list
 * @requiresProject false
 */
public class ListStageProfilesMojo
    extends AbstractStagingMojo
{

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

        StageClient client = getClient();

        List<StageProfile> profiles;
        try
        {
            profiles = client.getStageProfiles();
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find staging profiles: " + e.getMessage(), e );
        }

        if ( profiles != null )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( "The following staging profiles were found:\n" );

            if ( !profiles.isEmpty() )
            {
                Formatter formatter = new Formatter( builder );
                String format = "| %1$-15s| %2$-30s| %3$-7s|\n";
                formatter.format( Locale.getDefault(), format, "Id", "Name", "Mode" );
                for ( StageProfile profile : profiles )
                {
                    formatter.format( Locale.getDefault(), format, profile.getProfileId(), profile.getName(),
                        profile.getMode() );
                    // builder.append( listProfile( profile ) );
                }
            }
            else
            {
                builder.append( "\n\nNone." );
            }

            builder.append( "\n\n" );

            getLog().info( builder.toString() );
        }
        else
        {
            getLog().info( "\n\nNo staging profiles found.\n\n" );
        }

    }

}
