/**
 * Copyright (c) 2007-2008 Sonatype, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License Version 1.0, which accompanies this distribution and is
 * available at http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonatype.nexus.index.locator;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;

/**
 * Artifact locator.
 * 
 * @author Damian Bradicich
 */
public class ArtifactLocator
    implements GavHelpedLocator
{
    private IndexingContext context = null;
    
    public ArtifactLocator( IndexingContext context )
    {
        this.context = context;
    }
    
    public File locate( File source, GavCalculator gavCalculator, Gav gav )
    {
        if ( context != null )
        {
            ArtifactInfo ai = new ArtifactInfo( 
                null, 
                gav.getGroupId(), 
                gav.getArtifactId(), 
                gav.getVersion(), 
                gav.getClassifier() );
            
            Term term = new Term( ArtifactInfo.UINFO, ai.getUinfo() );
            
            try
            {
                TopDocs topdocs = context.getIndexSearcher().search( new TermQuery( term ), null, 1 );
                
                if ( topdocs != null
                    && topdocs.scoreDocs != null
                    && topdocs.scoreDocs.length > 0 )
                {
                    Document doc = context.getIndexReader().document( topdocs.scoreDocs[0].doc );
                    
                    if ( doc != null )
                    {
                        for ( IndexCreator indexCreator : context.getIndexCreators() )
                        {
                            indexCreator.updateArtifactInfo( doc, ai );
                        }
                        
                        if ( !StringUtils.isEmpty( ai.artifactId )
                            && !StringUtils.isEmpty( ai.version )
                            && !StringUtils.isEmpty( ai.fextension ) )
                        {
                            return new File( 
                                source.getParentFile(), 
                                ai.artifactId 
                                    + "-" 
                                    + ai.version 
                                    + ( StringUtils.isEmpty( ai.classifier ) ? "" : ( "-" + ai.classifier ) )
                                    + "." 
                                    + ai.fextension );
                        }
                    }
                }
            }
            catch ( IOException e )
            {
                // problem reading index, so no artifact returned
            }
        }
        return null;
    }
}
