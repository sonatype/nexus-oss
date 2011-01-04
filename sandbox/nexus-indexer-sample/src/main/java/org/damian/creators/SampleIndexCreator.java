/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.damian.creators;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;

/**
 * A Sample Index Creator that will show how to properly create your own IndexCreator component
 * @author Damian
 *
 */
// Define the plexus component, and the hint that plexus will use to load it
@Component( role = IndexCreator.class, hint = "sample" )
public class SampleIndexCreator
    extends AbstractIndexCreator
{    
    // The name of my sample field
    public static final String MY_FIELD = "myfield";
    
    /**
     * Populate ArtifactInfo with data specific to your application.  Note that the
     * artifactContext contains other useful objects, which may come in handy.
     */
    public void populateArtifactInfo( ArtifactContext artifactContext )
        throws IOException
    {
        // Add the data to the ArtifactInfo object, retrieved by whatever means you see fit.
        // you could get details from the artifact file in the context, or the pom
        // or pretty much anything else
        artifactContext.getArtifactInfo().getAttributes().put( MY_FIELD, "value" );
    }

    /** 
     * Popluate ArtifactInfo from exisiting lucene index document, will want to populate the
     * same fields that you populate in populateArtifactInfo
     */
    public boolean updateArtifactInfo( Document document, ArtifactInfo artifactInfo )
    {
        // Add the data to the ArtifactInfo from the index document.
        artifactInfo.getAttributes().put( MY_FIELD, document.get( MY_FIELD ) );
        
        //Note that returning false here will notify calling party of failure
        return true;
    }

    /**
     * Add data from the artifactInfo to the index
     */
    public void updateDocument( ArtifactInfo artifactInfo, Document document )
    {
        document.add( 
            new Field(
                //Field name, should be unique across all IndexCreator objects
                MY_FIELD, 
                // get your new data and add to the index
                artifactInfo.getAttributes().get( MY_FIELD ), 
                // Whether the data should be stored (YES or NO)
                Field.Store.YES, 
                // Whether the field should be indexed (NO, TOKENIZED, UNTOKENIZED)
                Field.Index.UN_TOKENIZED ) );
    }
}
