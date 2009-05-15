/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * An artifact context used to provide information about artifact during
 * scanning. It is passed to the {@link IndexCreator}, which can populate
 * {@link ArtifactInfo} for the given artifact.
 * 
 * @see IndexCreator#populateArtifactInfo(ArtifactContext)
 * @see NexusIndexer#scan(IndexingContext)
 * 
 * @author Jason van Zyl
 * @author Tamas Cservenak
 */
public class ArtifactContext
{
    private final File pom;

    private final File artifact;

    private final File metadata;

    private final ArtifactInfo artifactInfo;

    private final Gav gav;

    private final List<Exception> errors = new ArrayList<Exception>();

    public ArtifactContext( File pom, File artifact, File metadata, ArtifactInfo artifactInfo, Gav gav )
    {
        if( artifactInfo == null )
        {
           throw new IllegalArgumentException( "Parameter artifactInfo must not be null");
        }
        
        this.pom = pom;
        this.artifact = artifact;
        this.metadata = metadata;
        this.artifactInfo = artifactInfo;
        this.gav = gav == null ? artifactInfo.calculateGav() : gav;
    }

    public File getPom()
    {
        return pom;
    }
    
    public Model getPomModel()
    {
        // First check for local pom file
        if ( getPom() != null && getPom().exists() )
        {
            try
            {
                return new ModelReader().readModel( new FileInputStream( getPom() ) );
            }
            catch ( FileNotFoundException e )
            {
            }
        }
        // Otherwise, check for pom contained in maven generated artifact
        else if ( getArtifact() != null )
        {
            ZipFile jar = null;

            try
            {
                jar = new ZipFile( getArtifact() );

                Enumeration<? extends ZipEntry> en = jar.entries();
                while ( en.hasMoreElements() )
                {
                    ZipEntry e = en.nextElement();
                    String name = e.getName();

                    // pom will be stored under /META-INF/maven/groupId/artifactId/pom.xml
                    if ( name.equals( "META-INF/maven/" + gav.getGroupId() + "/" + gav.getArtifactId() + "/pom.xml" ) )
                    {
                        return new ModelReader().readModel( jar.getInputStream( e ) );
                    }
                }
            }
            catch ( IOException e )
            {
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
                    }
                }
            }
        }
        
        return null;
    }
    
    public File getArtifact()
    {
        return artifact;
    }

    public File getMetadata()
    {
        return metadata;
    }

    public ArtifactInfo getArtifactInfo()
    {
        return artifactInfo;
    }

    public Gav getGav()
    {
        return gav;
    }

    public List<Exception> getErrors() 
    {
        return errors;
    }
    
    public void addError(Exception e) 
    {
        errors.add( e );
    }

    /**
     * Creates Lucene Document using {@link IndexCreator}s from the given {@link IndexingContext}.
     */
    public Document createDocument( IndexingContext context )
    {
        Document doc = new Document();
    
        // unique key
        doc.add( new Field( ArtifactInfo.UINFO, getArtifactInfo().getUinfo(),
            Store.YES, Index.UN_TOKENIZED ) );
    
        doc.add( new Field( ArtifactInfo.LAST_MODIFIED, //
            Long.toString( System.currentTimeMillis() ), Store.YES, Index.NO ) );
        
        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            try 
            {
                indexCreator.populateArtifactInfo( this );
            } 
            catch ( IOException ex ) 
            {
                addError( ex );
            }
        }
    
        // need a second pass in case index creators updated document attributes
        for ( IndexCreator indexCreator : context.getIndexCreators() )
        {
            indexCreator.updateDocument( getArtifactInfo(), doc );
        }
    
        return doc;
    }
    
    public static class ModelReader
    {
        public Model readModel( InputStream pom )
        {
            if ( pom == null )
            {
                return null;
            }

            Model model = new Model();
            
            Xpp3Dom dom = readPomInputStream( pom );
            
            if ( dom == null )
            {
                return null;
            }
            
            if ( dom.getChild( "packaging" ) != null )
            {
                model.setPackaging( dom.getChild( "packaging" ).getValue() );
            }
            // Special case, packaging should be null instead of default .jar if not set in pom
            else
            {
                model.setPackaging( null );
            }
            
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
    
        private Xpp3Dom readPomInputStream( InputStream is )
        {
            Reader r = new InputStreamReader( is );
            try
            {
                return Xpp3DomBuilder.build( r );
            }
            catch ( XmlPullParserException e )
            {
            }
            catch ( IOException e )
            {
            }
            finally
            {
                try
                {
                    r.close();
                }
                catch ( IOException e )
                {
                }
            }
            
            return null;
        }
    }
}
