/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.index.creator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.M2GavCalculator;
import org.sonatype.nexus.index.ArtifactAvailablility;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.ArtifactIndexingContext;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.locator.JavadocLocator;
import org.sonatype.nexus.index.locator.Locator;
import org.sonatype.nexus.index.locator.Sha1Locator;
import org.sonatype.nexus.index.locator.SignatureLocator;
import org.sonatype.nexus.index.locator.SourcesLocator;

/**
 * Minimal index creator to create repository index for using in the repository search (e.g. from IDE plugins).
 * 
 * @plexus.component role-hint="artifactId-minimal"
 */
public class MinimalArtifactInfoIndexCreator
    extends AbstractIndexCreator
{
    private ModelReader modelReader = new ModelReader();

    private Locator jl = new JavadocLocator();

    private Locator sl = new SourcesLocator();

    private Locator sigl = new SignatureLocator();

    private Locator sha1l = new Sha1Locator();

    
    public void populateArtifactInfo( ArtifactIndexingContext context ) 
    {
        ArtifactContext artifactContext = context.getArtifactContext();
        
        File artifact = artifactContext.getArtifact();
        
        File pom = artifactContext.getPom();
        
        ArtifactInfo ai = artifactContext.getArtifactInfo();

        if( pom != null )
        {
            Model model = modelReader.readModel( pom, ai.groupId, ai.artifactId, ai.version );
            
            if ( model != null ) 
            {
                ai.name = model.getName();
  
                ai.description = model.getDescription();
  
                ai.packaging = model.getPackaging() == null ? "jar" : model.getPackaging();
                  
                // look for archetypes
                if ( !ai.packaging.equals("maven-archetype") && //
                    artifact != null && //
                    ( "maven-plugin".equals(ai.packaging) // 
                        || ai.artifactId.indexOf("archetype") > -1 //
                        || ai.groupId.indexOf("archetype") > -1 ) ) 
                {
                    ZipFile jf = null;
                    try {
                      jf = new ZipFile( artifact );
                      
                      if ( jf.getEntry("META-INF/archetype.xml") != null //
                          || jf.getEntry("META-INF/maven/archetype.xml") != null 
                          || jf.getEntry("META-INF/maven/archetype-metadata.xml") != null ) 
                      {
                          ai.packaging = "maven-archetype";
                      }
                    } 
                    catch (Exception e) 
                    {
                    } 
                    finally 
                    {
                        close( jf );
                    }
                }
            }
            
            if( "maven-plugin".equals( ai.packaging ) && artifact != null )
            {
                ZipFile jf = null;
                
                InputStream is = null;
                
                try 
                {
                    jf = new ZipFile( artifact );
                    
                    ZipEntry entry = jf.getEntry("META-INF/maven/plugin.xml");
                    
                    if ( entry != null ) 
                    {
                        is = jf.getInputStream( entry );
                
                        PluginDescriptorBuilder builder = new PluginDescriptorBuilder();
                        
                        PluginDescriptor descriptor = builder.build(new InputStreamReader(is));
                
                        ai.prefix = descriptor.getGoalPrefix();
                
                        ai.goals = new ArrayList<String>();
                
                        for (Object o : descriptor.getMojos()) 
                        {
                            ai.goals.add( ( ( MojoDescriptor ) o ).getGoal() );
                        }
                    }
                } 
                catch (Exception e) 
                {
                } 
                finally 
                {
                    close( jf );
                    IOUtil.close( is );
                }
            }
          
            Gav gav = M2GavCalculator.calculate( //
                ai.groupId.replace( '.', '/' ) + '/' //
                + ai.artifactId + '/' // 
                + ai.version + '/' //
                + ( artifact == null ? ai.artifactId + '-' + ai.version + ".jar" : artifact.getName() ) );
  
            File sha1 = sha1l.locate( pom, gav );
            
            if ( sha1.exists() )
            {
                try
                {
                    ai.sha1 = StringUtils.chomp( FileUtils.fileRead( sha1 ) ).trim().split( " " )[0];
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
            
            File sources = sl.locate( pom, gav );
            
            ai.sourcesExists = sources.exists() ? ArtifactAvailablility.PRESENT 
                : ArtifactAvailablility.NOT_PRESENT;
    
            File javadoc = jl.locate( pom, gav );
            
            ai.javadocExists = javadoc.exists() ? ArtifactAvailablility.PRESENT
                : ArtifactAvailablility.NOT_PRESENT;
    
            File signature = sigl.locate( pom, gav );
            
            ai.signatureExists = signature.exists() ? ArtifactAvailablility.PRESENT
                : ArtifactAvailablility.NOT_PRESENT;
        }
        
        if( artifact != null )
        {
            ai.lastModified = artifact.lastModified();
    
            ai.size = artifact.length();
        }
    }
    
    public void updateDocument( ArtifactIndexingContext context, Document doc )
    {
        ArtifactInfo ai = context.getArtifactContext().getArtifactInfo();
        
        String info = new StringBuilder() //
            .append( ai.packaging ).append( AbstractIndexCreator.FS ) //
            .append( Long.toString( ai.lastModified ) ).append( AbstractIndexCreator.FS ) //
            .append( Long.toString( ai.size ) ).append( AbstractIndexCreator.FS ) //
            .append( ai.sourcesExists.toString() ).append( AbstractIndexCreator.FS ) //
            .append( ai.javadocExists.toString() ).append( AbstractIndexCreator.FS ) //
            .append( ai.signatureExists.toString() ).toString();
        
        doc.add( new Field( ArtifactInfo.INFO, info, Field.Store.YES, Field.Index.NO ) );

        doc.add( new Field( ArtifactInfo.GROUP_ID, ai.groupId, Field.Store.NO, Field.Index.UN_TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.ARTIFACT_ID, ai.artifactId, Field.Store.NO, Field.Index.TOKENIZED ) );

        doc.add( new Field( ArtifactInfo.VERSION, ai.version, Field.Store.NO, Field.Index.TOKENIZED ) );

        if ( ai.name != null )
        {
            doc.add( new Field( ArtifactInfo.NAME, ai.name, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.description != null )
        {
            doc.add( new Field( ArtifactInfo.DESCRIPTION, ai.description, Field.Store.YES, Field.Index.NO ) );
        }

        if ( ai.packaging != null )
        {
            doc.add( new Field( ArtifactInfo.PACKAGING, ai.packaging, Field.Store.NO, Field.Index.UN_TOKENIZED ) );
        }

        if( ai.prefix != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_PREFIX, ai.prefix, Field.Store.YES, Field.Index.NO ) );
        }
        
        if( ai.goals != null )
        {
            doc.add( new Field( ArtifactInfo.PLUGIN_GOALS, lst2str( ai.goals ), Field.Store.YES, Field.Index.NO ) );
        }
        
        if ( ai.sha1 != null )
        {
            doc.add( new Field( ArtifactInfo.SHA1, ai.sha1, Field.Store.NO, Field.Index.UN_TOKENIZED ) );
        }
    }

    public boolean updateArtifactInfo( IndexingContext ctx, Document doc, ArtifactInfo ai )
    {
        ai.repository = ctx.getRepositoryId();

        boolean res = false;

        String uinfo = doc.get( ArtifactInfo.UINFO );

        if ( uinfo != null )
        {
            String[] r = FS_PATTERN.split( uinfo );

            ai.repository = ctx.getRepositoryId();

            ai.groupId = r[0];

            ai.artifactId = r[1];

            ai.version = r[2];

            if ( r.length > 3 )
            {
                ai.classifier = renvl( r[3] );
            }

            res = true;
        }

        String info = doc.get( ArtifactInfo.INFO );

        if ( info != null )
        {
            String[] r = FS_PATTERN.split( info );

            ai.packaging = r[0];

            ai.lastModified = Long.parseLong( r[1] );

            ai.size = Long.parseLong( r[2] );

            ai.sourcesExists = ArtifactAvailablility.fromString( r[3] );

            ai.javadocExists = ArtifactAvailablility.fromString( r[4] );

            ai.signatureExists = ArtifactAvailablility.fromString( r[5] );

            if ( "maven-plugin".equals( ai.packaging ) )
            {
                ai.prefix = doc.get( ArtifactInfo.PLUGIN_PREFIX );

                String goals = doc.get( ArtifactInfo.PLUGIN_GOALS );

                if ( goals != null )
                {
                    ai.goals = str2lst( goals );
                }
            }

            res = true;
        }

        String md5 = doc.get( ArtifactInfo.MD5 );

        if ( md5 != null )
        {
            ai.md5 = md5;

            res = true;
        }

        String name = doc.get( ArtifactInfo.NAME );

        if ( name != null )
        {
            ai.name = name;

            res = true;
        }

        String description = doc.get( ArtifactInfo.DESCRIPTION );

        if ( description != null )
        {
            ai.description = description;

            res = true;
        }

        return res;

        // artifactInfo.fname = ???

        // artifactInfo.sha1 = ???
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
    
    /**
     * Caching lightweight model reader
     */
    public static class ModelReader
    {
        private final HashMap<File, Model> models = new HashMap<File, Model>();

        public Model getModel( File pom, String groupId, String artifactId, String version )
        {
            Model model = models.get( pom );
            if ( model == null )
            {
                model = readModel( pom, groupId, artifactId, version );
                models.put( pom, model );
            }
            return model;
        }

        public Model readModel( File pom, String groupId, String artifactId, String version )
        {
            Xpp3Dom dom = readPom( pom );

            if ( dom == null )
            {
                return null;
            }

            String packaging = null;

            if ( dom.getChild( "packaging" ) != null )
            {
                packaging = dom.getChild( "packaging" ).getValue();
            }

//            Xpp3Dom parent = dom.getChild( "parent" );
//
//            if ( parent != null )
//            {
//                String parentGroupId = parent.getChild( "groupId" ).getValue();
//
//                String parentArtifactId = parent.getChild( "artifactId" ).getValue();
//
//                String parentVersion = parent.getChild( "version" ).getValue();
//
//                String parentPomPath = getPath( parentGroupId, parentArtifactId, parentVersion, artifactId + "-"
//                    + version + ".pom" );
//
//                String repository = getRepository( groupId, artifactId, version, pom );
//
//                // if ( repository != null )
//                // {
//                // Model parentModel = getModel( new File( repository, parentPomPath ), parentGroupId, parentArtifactId,
//                // parentVersion );
//                //                  
//                // if ( parentModel !=null )
//                // {
//                // //
//                // }
//                // }
//            }

            Model model = new Model();

            model.setPackaging( packaging );

            if ( dom.getChild( "name" ) != null )
            {
                model.setName( dom.getChild( "name" ).getValue() );
            }

            if ( dom.getChild( "description" ) != null )
            {
                model.setDescription( dom.getChild( "description" ).getValue() );
            }

            return model;
        }

//        private String getRepository( String groupId, String artifactId, String version, File pom )
//        {
//            String pomPath = getPath( groupId, artifactId, version, pom.getName() );
//
//            String fullPomPath = pom.getAbsolutePath();
//
//            int n = fullPomPath.replace( '\\', '/' ).indexOf( pomPath.replace( '\\', '/' ) );
//
//            if ( n == -1 )
//            {
//                return null;
//            }
//
//            return fullPomPath.substring( 0, n );
//        }
//
//        private String getPath( String groupId, String artifactId, String version, String fname )
//        {
//            return new StringBuilder()
//                .append( groupId.replace( '.', File.separatorChar ) ).append( File.separatorChar ).append( artifactId )
//                .append( File.separatorChar ).append( version ).append( File.separatorChar ).append( fname ).toString();
//        }

        private Xpp3Dom readPom( File pom )
        {
            Reader r = null;
            try
            {
                r = new FileReader( pom );

                return Xpp3DomBuilder.build( r );
            }
            catch ( Exception e )
            {
            }
            finally
            {
                if ( r != null )
                {
                    try
                    {
                        r.close();
                    }
                    catch ( IOException ex )
                    {
                    }
                }
            }
            return null;
        }

    }
    
}
