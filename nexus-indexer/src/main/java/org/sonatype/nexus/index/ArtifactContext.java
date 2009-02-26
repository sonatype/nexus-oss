/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
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
}
