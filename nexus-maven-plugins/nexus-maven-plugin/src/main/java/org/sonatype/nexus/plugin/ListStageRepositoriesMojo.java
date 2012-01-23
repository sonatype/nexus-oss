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

    protected void doExecute()
        throws MojoExecutionException
    {
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
