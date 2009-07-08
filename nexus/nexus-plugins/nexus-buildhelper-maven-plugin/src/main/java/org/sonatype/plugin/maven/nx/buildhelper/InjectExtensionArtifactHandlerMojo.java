package org.sonatype.plugin.maven.nx.buildhelper;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Injects an {@link ArtifactHandler} instance, loaded from build extensions, into the current project's
 * {@link Artifact} instance. The new handler is loaded using the project's packaging.
 * 
 * @goal inject-artifact-handler
 * 
 * @phase initialize
 */
public class InjectExtensionArtifactHandlerMojo
    implements Mojo
{
    /**
     * The current project instance.
     * 
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The {@link ArtifactHandlerManager} into which any extension {@link ArtifactHandler} instances should have been
     * injected when the extensions were loaded.
     * 
     * @component
     */
    private ArtifactHandlerManager artifactHandlerManager;

    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    private Log log;

    @SuppressWarnings( "unchecked" )
    public void execute()
        throws MojoExecutionException
    {
        Map<String, ?> handlerDescriptors = session.getContainer().getComponentDescriptorMap( ArtifactHandler.ROLE );
        if ( handlerDescriptors != null )
        {
            getLog().info( "Registering all unregistered ArtifactHandlers..." );

            if ( artifactHandlerManager instanceof DefaultArtifactHandlerManager )
            {
                Set<String> existingHints =
                    ( (DefaultArtifactHandlerManager) artifactHandlerManager ).getHandlerTypes();
                if ( existingHints != null )
                {
                    for ( String hint : existingHints )
                    {
                        handlerDescriptors.remove( hint );
                    }
                }
            }

            if ( handlerDescriptors.isEmpty() )
            {
                getLog().info( "All ArtifactHandlers are registered. Continuing..." );
            }
            else
            {
                Map<String, ArtifactHandler> unregisteredHandlers =
                    new HashMap<String, ArtifactHandler>( handlerDescriptors.size() );

                for ( String hint : handlerDescriptors.keySet() )
                {
                    try
                    {
                        unregisteredHandlers.put( hint, (ArtifactHandler) session.lookup( ArtifactHandler.ROLE, hint ) );
                        getLog().info( "Adding: " + hint );
                    }
                    catch ( ComponentLookupException e )
                    {
                        getLog().warn(
                                       "Failed to lookup ArtifactHandler with hint: " + hint + ". Reason: "
                                           + e.getMessage(), e );
                    }
                }

                artifactHandlerManager.addHandlers( unregisteredHandlers );
            }
        }

        getLog().info( "...done.\nSetting ArtifactHandler on project-artifact: " + project.getId() + "..." );

        String packaging = project.getPackaging();
        Artifact artifact = project.getArtifact();

        ArtifactHandler handler = artifactHandlerManager.getArtifactHandler( packaging );
        artifact.setArtifactHandler( handler );

        getLog().info( "...done." );
    }

    public Log getLog()
    {
        return log;
    }

    public void setLog( final Log log )
    {
        this.log = log;
    }

}
