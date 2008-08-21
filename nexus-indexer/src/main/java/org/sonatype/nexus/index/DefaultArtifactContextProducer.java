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
package org.sonatype.nexus.index;

import java.io.File;

import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.locator.GavHelpedLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.MetadataLocator;
import org.sonatype.nexus.index.locator.PomLocator;

/**
 * The default implementation of the ArtifactContextProducer.
 * 
 * @author cstamas
 * @author Eugene Kuleshov
 * @plexus.component
 */
public class DefaultArtifactContextProducer
    implements ArtifactContextProducer
{
    private GavHelpedLocator pl = new PomLocator();

    private Locator ml = new MetadataLocator();

    /**
     * Get ArtifactContext for given pom or artifact (jar, war, etc). A file can be
     */
    public ArtifactContext getArtifactContext( IndexingContext context, File file )
    {
        // TODO shouldn't this use repository layout instead?
        String path = null;

        // protection from IndexOutOfBounds
        if ( file.getAbsolutePath().length() > context.getRepository().getAbsolutePath().length() )
        {
            path = file.getAbsolutePath().substring( context.getRepository().getAbsolutePath().length() + 1 ).replace(
                '\\',
                '/' );
        }
        else
        {
            // this is junk path?
            return null;
        }

        Gav gav = context.getGavCalculator().pathToGav( path );

        if ( gav == null )
        {
            // XXX what then? Without GAV we are screwed (look below).
            // It should simply stop/skip, since it is not an artifact.
            return null;
        }

        String groupId = gav.getGroupId();

        String artifactId = gav.getArtifactId();

        String version = gav.getBaseVersion();

        String classifier = gav.getClassifier();

        File pom;

        File artifact;

        if ( file.getName().endsWith( ".pom" ) )
        {
            pom = file;

            // there is no "reliable" way to go from pom to artifact!
            // this is completely unimportant here, since if artifact exists,
            // scan will find it and will do an "update" on existing "pom only" Lucene document.
            // And API consumers should send artifacts to being added to index, not only poms.
            // (but if sending both, again, will be ok coz of that above)
            artifact = null;
        }
        else
        {
            artifact = file;

            pom = pl.locate( file, context.getGavCalculator(), gav );

            // if ( !pom.exists() )
            // {
            // return null;
            // }
        }

        ArtifactInfo ai = new ArtifactInfo( context.getRepositoryId(), groupId, artifactId, version, classifier );

        // ArtifactInfo ai = new ArtifactInfo(
        // fname,
        // groupId,
        // artifactId,
        // version,
        // classifier,
        // packaging,
        // name,
        // description,
        // artifact.lastModified(),
        // artifact.length(),
        // md5Text,
        // sha1Text,
        // sourcesExists,
        // javadocExists,
        // signatureExists,
        // context.getRepositoryId() );

        File metadata = ml.locate( pom );

        return new ArtifactContext( pom, artifact, metadata, ai, gav );
    }

}
