/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.creator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;

/**
 * A Maven Plugin index creator used to provide information about Maven Plugins.
 * 
 * @author cstamas
 */
@Component( role = IndexCreator.class, hint = MavenPluginArtifactInfoIndexCreator.ID )
public class MavenPluginArtifactInfoIndexCreator
    extends AbstractIndexCreator
{
    public static final String ID = "maven-plugin";

    private static final String MAVEN_PLUGIN_PACKAGING = "maven-plugin";

    public void populateArtifactInfo( ArtifactContext ac )
    {
        File artifact = ac.getArtifact();

        ArtifactInfo ai = ac.getArtifactInfo();

        // we need the file to perform these checks, and those may be only JARs
        if ( artifact != null && MAVEN_PLUGIN_PACKAGING.equals( ai.packaging ) && "jar".equals( ai.fextension ) )
        {
            // TODO: recheck, is the following true? "Maven plugins and Maven Archetypes can be only JARs?"

            // 1st, check for maven plugin
            checkMavenPlugin( ai, artifact );
        }
    }

    private void checkMavenPlugin( ArtifactInfo ai, File artifact )
    {
        ZipFile jf = null;

        InputStream is = null;

        try
        {
            jf = new ZipFile( artifact );

            ZipEntry entry = jf.getEntry( "META-INF/maven/plugin.xml" );

            if ( entry == null )
            {
                return;
            }

            is = new BufferedInputStream( jf.getInputStream( entry ) );

            PlexusConfiguration plexusConfig =
                new XmlPlexusConfiguration( Xpp3DomBuilder.build( new InputStreamReader( is ) ) );

            ai.prefix = plexusConfig.getChild( "goalPrefix" ).getValue();

            ai.goals = new ArrayList<String>();

            PlexusConfiguration[] mojoConfigs = plexusConfig.getChild( "mojos" ).getChildren( "mojo" );

            for ( PlexusConfiguration mojoConfig : mojoConfigs )
            {
                ai.goals.add( mojoConfig.getChild( "goal" ).getValue() );
            }
        }
        catch ( Exception e )
        {
            getLogger().info( "Failed to parsing Maven plugin " + artifact.getAbsolutePath(), e );
        }
        finally
        {
            close( jf );

            IOUtil.close( is );
        }
    }

    public void updateDocument( ArtifactInfo ai, Document doc )
    {
        if ( ai.prefix != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_PREFIX, ai.prefix, Field.Store.YES, Field.Index.UN_TOKENIZED ) );
        }

        if ( ai.goals != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_GOALS, ArtifactInfo.lst2str( ai.goals ), Field.Store.YES,
                Field.Index.NO ) );
        }
    }

    public boolean updateArtifactInfo( Document doc, ArtifactInfo ai )
    {
        boolean res = false;

        if ( "maven-plugin".equals( ai.packaging ) )
        {
            ai.prefix = doc.get( ArtifactInfo.PLUGIN_PREFIX );

            String goals = doc.get( ArtifactInfo.PLUGIN_GOALS );

            if ( goals != null )
            {
                ai.goals = ArtifactInfo.str2lst( goals );
            }

            res = true;
        }

        return res;
    }

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
