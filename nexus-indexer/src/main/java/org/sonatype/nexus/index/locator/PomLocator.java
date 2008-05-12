/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.locator;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;

public class PomLocator
    implements Locator
{
    public File locate( File source, Gav gav )
    {
        String artifactName = gav.getName();

        if ( gav.isHash() )
        {
            // correction for last .sha or .md5
            artifactName = artifactName.substring( 0, artifactName.lastIndexOf( "." ) );
        }

        // correction for classifier
        artifactName = artifactName.substring( 0, artifactName.lastIndexOf( "." ) );

        if ( gav.getClassifier() != null )
        {
            artifactName = artifactName.substring( 0, artifactName.length() - gav.getClassifier().length() - 1 );
        }

        return new File( source.getParent(), artifactName + ".pom" );
    }

}
