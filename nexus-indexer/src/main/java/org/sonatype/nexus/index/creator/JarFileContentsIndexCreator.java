/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;

/**
 * An index creator used to index Java class names from a Maven artifact.
 */
@Component( role = IndexCreator.class, hint = "jarContent" )
public class JarFileContentsIndexCreator
    extends AbstractIndexCreator
    implements LegacyDocumentUpdater
{

    public void populateArtifactInfo( ArtifactContext artifactContext )
        throws IOException
    {
        ArtifactInfo ai = artifactContext.getArtifactInfo();

        File artifactFile = artifactContext.getArtifact();

        if ( artifactFile != null && artifactFile.exists() && artifactFile.getName().endsWith( ".jar" ) )
        {
            updateArtifactInfo( ai, artifactFile );
        }
    }

    public void updateDocument( ArtifactInfo ai, Document doc )
    {
        if ( ai.classNames != null )
        {
            doc.add( new Field( ArtifactInfo.NAMES, ai.classNames, Field.Store.COMPRESS, Field.Index.TOKENIZED ) );
        }
    }

    public void updateLegacyDocument( ArtifactInfo ai, Document doc )
    {
        if ( ai.classNames != null )
        {
            String classNames = ai.classNames;

            // downgrade the classNames if needed
            if ( classNames.length() > 0 && classNames.charAt( 0 ) == '/' )
            {
                // conversion from the new format
                String[] lines = classNames.split( "\\n" );
                StringBuilder sb = new StringBuilder();
                for ( String line : lines )
                {
                    sb.append( line.substring( 1 ) ).append( '\n' );
                }

                classNames = sb.toString();
            }

            doc.add( new Field( ArtifactInfo.NAMES, classNames, Field.Store.COMPRESS, Field.Index.TOKENIZED ) );
        }
    }

    public boolean updateArtifactInfo( Document doc, ArtifactInfo artifactInfo )
    {
        String names = doc.get( ArtifactInfo.NAMES );

        if ( names != null )
        {
            if ( names.length() == 0 || names.charAt( 0 ) == '/' )
            {
                artifactInfo.classNames = names;
            }
            else
            {
                // conversion from the old format
                String[] lines = names.split( "\\n" );
                StringBuilder sb = new StringBuilder();
                for ( String line : lines )
                {
                    sb.append( '/' ).append( line ).append( '\n' );
                }
                artifactInfo.classNames = sb.toString();
            }

            return true;
        }

        return false;
    }

    private void updateArtifactInfo( ArtifactInfo ai, File f )
        throws IOException
    {
        ZipFile jar = null;

        try
        {
            jar = new ZipFile( f );

            StringBuilder sb = new StringBuilder();

            @SuppressWarnings( "unchecked" )
            Enumeration en = jar.entries();
            while ( en.hasMoreElements() )
            {
                ZipEntry e = (ZipEntry) en.nextElement();

                String name = e.getName();

                if ( name.endsWith( ".class" ) )
                {
                    // TODO verify if class is public or protected
                    // TODO skip all inner classes for now

                    int i = name.indexOf( "$" );

                    if ( i == -1 )
                    {
                        if ( name.charAt( 0 ) != '/' )
                        {
                            sb.append( '/' );
                        }

                        // class name without ".class"
                        sb.append( name.substring( 0, name.length() - 6 ) ).append( '\n' );
                    }
                }
                else if ( "META-INF/archetype.xml".equals( name ) //
                    || "META-INF/maven/archetype.xml".equals( name ) //
                    || "META-INF/maven/archetype-metadata.xml".equals( name ) )
                {
                    ai.packaging = "maven-archetype";
                }
            }

            if( sb.toString().trim().length() != 0 )
            {
                ai.classNames = sb.toString();
            }
            else
            {
                ai.classNames = null;
            }
        }
        finally
        {
            if ( jar != null )
            {
                try
                {
                    jar.close();
                }
                catch ( Exception e )
                {
                    getLogger().error( "Could not close jar file properly.", e );
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return "jarContent";
    }

}
