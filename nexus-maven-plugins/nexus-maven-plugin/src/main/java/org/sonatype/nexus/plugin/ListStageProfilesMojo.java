/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
                String format = "| %1$-30s| %2$-16s| %3$-15s|\n";
                formatter.format( Locale.getDefault(), format, "Name", "Type", "Id" );
                for ( StageProfile profile : profiles )
                {
                    formatter.format( Locale.getDefault(), format, profile.getName(),
                        "GROUP".equals( profile.getMode() ) ? "Build Promotion" : "Staging", profile.getProfileId() );
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
