/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;

/**
 * A Maven Archetype index creator used to detect and correct the artifact packaging to "maven-archetype" if the
 * inspected JAR is an Archetype. Since packaging is already handled by Minimal creator, this Creator only alters the
 * supplied ArtifactInfo packaging field during processing, but does not interferes with Lucene document fill-up or the
 * ArtifactInfo fill-up (the update* methods are empty).
 * 
 * @author cstamas
 */
@Component( role = IndexCreator.class, hint = MavenArchetypeArtifactInfoIndexCreator.ID )
public class MavenArchetypeArtifactInfoIndexCreator
    extends AbstractIndexCreator
{
    public static final String ID = "maven-archetype";

    private static final String MAVEN_ARCHETYPE_PACKAGING = "maven-archetype";

    private static final String[] ARCHETYPE_XML_LOCATIONS =
        { "META-INF/maven/archetype.xml", "META-INF/archetype.xml", "META-INF/maven/archetype-metadata.xml" };

    public void populateArtifactInfo( ArtifactContext ac )
    {
        File artifact = ac.getArtifact();

        ArtifactInfo ai = ac.getArtifactInfo();

        // we need the file to perform these checks, and those may be only JARs
        if ( artifact != null && !MAVEN_ARCHETYPE_PACKAGING.equals( ai.packaging )
            && artifact.getName().endsWith( ".jar" ) )
        {
            // TODO: recheck, is the following true? "Maven plugins and Maven Archetypes can be only JARs?"

            // check for maven archetype, since Archetypes seems to not have consistent packaging,
            // and depending on the contents of the JAR, this call will override the packaging to "maven-archetype"!
            checkMavenArchetype( ai, artifact );
        }
    }

    /**
     * Archetypes that are added will have their packaging types set correctly (to maven-archetype)
     * 
     * @param ai
     * @param artifact
     */
    private void checkMavenArchetype( ArtifactInfo ai, File artifact )
    {
        ZipFile jf = null;

        try
        {
            jf = new ZipFile( artifact );

            for ( String location : ARCHETYPE_XML_LOCATIONS )
            {
                if ( checkEntry( ai, jf, location ) )
                {
                    return;
                }
            }
        }
        catch ( Exception e )
        {
            getLogger().info( "Failed to parse Maven artifact " + artifact.getAbsolutePath(), e );
        }
        finally
        {
            close( jf );
        }
    }

    private boolean checkEntry( ArtifactInfo ai, ZipFile jf, String entryName )
    {
        ZipEntry entry = jf.getEntry( entryName );

        if ( entry != null )
        {
            ai.packaging = MAVEN_ARCHETYPE_PACKAGING;

            return true;
        }
        return false;
    }

    public void updateDocument( ArtifactInfo ai, Document doc )
    {
        // nothing to update, minimal will maintain it.
    }

    public boolean updateArtifactInfo( Document doc, ArtifactInfo ai )
    {
        // nothing to update, minimal will maintain it.

        return false;
    }

    // ==

    private void close( ZipFile zf )
    {
        if ( zf != null )
        {
            try
            {
                zf.close();
            }
            catch ( IOException ex )
            {
            }
        }
    }

    @Override
    public String toString()
    {
        return ID;
    }
}
