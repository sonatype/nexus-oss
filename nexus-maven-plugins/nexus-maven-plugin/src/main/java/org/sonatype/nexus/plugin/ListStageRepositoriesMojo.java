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

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.sonatype.nexus.restlight.common.RESTLightClientException;
import org.sonatype.nexus.restlight.stage.StageClient;
import org.sonatype.nexus.restlight.stage.StageRepository;

/**
 * Lists all open Nexus staging repositories for a user. These are staging repositories that are not yet available for
 * access via Maven's artifact resolution process; they are waiting to be marked as "finished".
 * 
 * @goal staging-list
 * @requiresProject false
 * @aggregator
 */
// TODO: Remove aggregator annotation once we have a better solution, but we should only run this once per build.
public class ListStageRepositoriesMojo
    extends AbstractStagingMojo
{

    public void execute()
        throws MojoExecutionException
    {
        fillMissing();

        initLog4j();

        StageClient client = getClient();

        List<StageRepository> repos;
        try
        {
            repos = client.getOpenStageRepositories();
        }
        catch ( RESTLightClientException e )
        {
            throw new MojoExecutionException( "Failed to find open staging repository: " + e.getMessage(), e );
        }

        if ( repos != null )
        {
            StringBuilder builder = new StringBuilder();
            builder.append( "The following OPEN staging repositories were found: " );

            if ( !repos.isEmpty() )
            {
                for ( StageRepository repo : repos )
                {
                    builder.append( "\n\n-  " );
                    builder.append( listRepo( repo ) );
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
            getLog().info( "\n\nNo open staging repositories found.\n\n" );
        }

        listRepos( null, null, null, "The following CLOSED staging repositories were found" );
    }

}
