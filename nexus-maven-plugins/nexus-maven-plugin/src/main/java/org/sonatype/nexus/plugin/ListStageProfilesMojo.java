/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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

    protected void doExecute()
        throws MojoExecutionException
    {
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
