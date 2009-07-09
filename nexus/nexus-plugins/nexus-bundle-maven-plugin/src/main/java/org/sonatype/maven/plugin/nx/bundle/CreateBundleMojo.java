package org.sonatype.maven.plugin.nx.bundle;

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
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugin.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugin.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugin.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugin.assembly.model.Assembly;
import org.apache.maven.plugin.assembly.model.DependencySet;
import org.apache.maven.plugin.assembly.model.FileItem;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;

/**
 * Create a Nexus plugin bundle.
 * 
 * @goal create-bundle
 * @phase package
 */
public class CreateBundleMojo
    extends AbstractMojo
{
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @parameter
     */
    private BundleConfiguration bundle;

    /**
     * @component
     */
    private AssemblyArchiver archiver;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException
    {
        Assembly assembly = new Assembly();
        assembly.addFormat( "zip" );
        assembly.setId( "bundle" );
        assembly.setIncludeBaseDirectory( false );

        DependencySet ds = new DependencySet();

        ds.setScope( Artifact.SCOPE_RUNTIME );
        ds.setOutputDirectory( project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion()
            + "/dependencies" );

        ds.setUseProjectArtifact( false );

        ds.addExclude( "org.sonatype.nexus*" );
        ds.addExclude( "com.sonatype.nexus*" );
        ds.addExclude( "*:nexus-plugin:*" );

        assembly.addDependencySet( ds );

        FileItem fi = new FileItem();
        fi.setSource( project.getArtifact().getFile().getPath() );
        fi.setOutputDirectory( project.getGroupId() + "/" + project.getArtifactId() + "/" + project.getVersion() );

        assembly.addFile( fi );

        if ( bundle == null )
        {
            bundle = new BundleConfiguration( project, session );
        }
        else
        {
            bundle.initDefaults( project, session );
        }

        bundle.configureAssembly( assembly );

        try
        {
            File assemblyFile =
                archiver.createArchive( assembly, bundle.getAssemblyFileName( assembly ), "zip", bundle );
            projectHelper.attachArtifact( project, "zip", assembly.getId(), assemblyFile );
        }
        catch ( ArchiveCreationException e )
        {
            throw new MojoExecutionException( "Failed to create Nexus plugin bundle: " + e.getMessage(), e );
        }
        catch ( AssemblyFormattingException e )
        {
            throw new MojoExecutionException( "Failed to create Nexus plugin bundle: " + e.getMessage(), e );
        }
        catch ( InvalidAssemblerConfigurationException e )
        {
            throw new MojoExecutionException( "Failed to create Nexus plugin bundle: " + e.getMessage(), e );
        }
    }
}
