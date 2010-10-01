/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.locator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.sonatype.nexus.artifact.ArtifactPackagingMapper;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.index.ArtifactContext.ModelReader;

/**
 * Artifact locator.
 * 
 * @author Damian Bradicich
 */
public class ArtifactLocator
    implements GavHelpedLocator
{
    private final ArtifactPackagingMapper mapper;

    public ArtifactLocator( ArtifactPackagingMapper mapper )
    {
        this.mapper = mapper;
    }

    public File locate( File source, GavCalculator gavCalculator, Gav gav )
    {
        // if we dont have this data, nothing we can do
        if ( source == null || !source.exists() || gav == null || gav.getArtifactId() == null
            || gav.getVersion() == null )
        {
            return null;
        }

        try
        {
            // need to read the pom model to get packaging
            Model model = new ModelReader().readModel( new FileInputStream( source ) );
            
            if ( model == null )
            {
                return null;
            }

            // now generate the artifactname
            String artifactName =
                gav.getArtifactId() + "-" + gav.getVersion() + "."
                    + mapper.getExtensionForPackaging( model.getPackaging() );

            File artifact = new File( source.getParent(), artifactName );

            if ( !artifact.exists() )
            {
                return null;
            }

            return artifact;
        }
        catch ( IOException e )
        {
            return null;
        }
    }
}
