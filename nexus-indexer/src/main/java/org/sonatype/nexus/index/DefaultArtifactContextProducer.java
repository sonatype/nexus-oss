/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.IllegalArtifactCoordinateException;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.locator.ArtifactLocator;
import org.sonatype.nexus.index.locator.GavHelpedLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.MetadataLocator;
import org.sonatype.nexus.index.locator.PomLocator;

/**
 * A default implementation of the {@link ArtifactContextProducer}.
 * 
 * @author Tamas Cservenak
 * @author Eugene Kuleshov
 */
@Component(role = ArtifactContextProducer.class)
public class DefaultArtifactContextProducer
    implements ArtifactContextProducer
{
    private GavHelpedLocator pl = new PomLocator();

    private Locator ml = new MetadataLocator();

    /**
     * Get ArtifactContext for given pom or artifact (jar, war, etc). A file can be
     */
    public ArtifactContext getArtifactContext( IndexingContext context, File file )
        throws IllegalArtifactCoordinateException
    {
        // TODO shouldn't this use repository layout instead?

        String repositoryPath = context.getRepository().getAbsolutePath();
        String artifactPath = file.getAbsolutePath();
        
        // protection from IndexOutOfBounds
        if ( artifactPath.length() <= repositoryPath.length() ) 
        {
            return null;  // not an artifact
        }
        
        if ( !isIndexable( file ) )
        {
            return null;  // skipped
        }

        Gav gav = getGavFromPath( context, repositoryPath, artifactPath );

        if ( gav == null )
        {
            return null; // not an artifact
        }
        
        File pom;
        File artifact;

        if ( file.getName().endsWith( ".pom" ) )
        {
            ArtifactLocator al = new ArtifactLocator( context );
            artifact = al.locate( file, context.getGavCalculator(), gav );
            
            //If we found the matching artifact, switch over to indexing that, instead of the pom
            if ( artifact != null )
            {
                gav = getGavFromPath( context, repositoryPath, artifact.getAbsolutePath() );
            }
            
            pom = file;
        }
        else
        {
            artifact = file;
            pom = pl.locate( file, context.getGavCalculator(), gav );
        }

        String groupId = gav.getGroupId();

        String artifactId = gav.getArtifactId();

        String version = gav.getBaseVersion();

        String classifier = gav.getClassifier();

        ArtifactInfo ai = new ArtifactInfo( context.getRepositoryId(), groupId, artifactId, version, classifier );
        
        // store extension if classifier is not empty
        if ( !StringUtils.isEmpty( ai.classifier ) )
        {
            ai.packaging = gav.getExtension();
        }
        
        ai.fname = file.getName();
        ai.fextension = gav.getExtension();

        File metadata = ml.locate( pom );

        return new ArtifactContext( pom, artifact, metadata, ai, gav );
    }

    private boolean isIndexable( File file )
    {
        if ( file == null )
        {
            return false;
        }
        
        String filename = file.getName();
        
        if (   filename.startsWith( "maven-metadata" )
            // || filename.endsWith( "-javadoc.jar" )
            // || filename.endsWith( "-javadocs.jar" )
            // || filename.endsWith( "-sources.jar" )
            || filename.endsWith( ".properties" )
            || filename.endsWith( ".xml" )
            || filename.endsWith( ".asc" ) 
            || filename.endsWith( ".md5" )
            || filename.endsWith( ".sha1" ) )
        {
            return false;
        }
        
        return true;
    }
    
    private Gav getGavFromPath( IndexingContext context, String repositoryPath, String artifactPath )
        throws IllegalArtifactCoordinateException
    {
        String path = artifactPath.substring( repositoryPath.length() + 1 ).replace( '\\', '/' );

        return context.getGavCalculator().pathToGav( path );
    }

}
