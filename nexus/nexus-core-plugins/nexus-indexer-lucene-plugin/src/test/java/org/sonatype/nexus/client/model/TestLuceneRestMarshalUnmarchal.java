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
package org.sonatype.nexus.client.model;

import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.SearchResponse;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

public class TestLuceneRestMarshalUnmarchal
    extends TestMarshalUnmarchal
{
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        getJsonXStream().processAnnotations( SearchResponse.class );
        getXmlXStream().processAnnotations( SearchResponse.class );
        
        getJsonXStream().registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter( NexusArtifact.class,
            "artifact" ) );
        
        getXmlXStream().registerLocalConverter( SearchResponse.class, "data", new AliasingListConverter( NexusArtifact.class,
            "artifact" ) );
    }
    
    public void testSearchResponse()
    {
        SearchResponse response = new SearchResponse();
        response.setCount( 10 );
        response.setFrom( 50 );
        response.setTotalCount( 8 );
        response.setTooManyResults( true );

        NexusArtifact artifact1 = new NexusArtifact();
        artifact1.setArtifactId( "artifactId1" );
        artifact1.setClassifier( "classifier1" );
        artifact1.setContextId( "contextId1" );
        artifact1.setGroupId( "groupId1" );
        artifact1.setPackaging( "packaging1" );
        artifact1.setRepoId( "repoId1" );
        artifact1.setResourceURI( "resourceURI1" );
        artifact1.setVersion( "version1" );
        artifact1.setArtifactLink( "artifactLink" );
        artifact1.setExtension( "extension" );
        artifact1.setPomLink( "pomLink" );
        response.addData( artifact1 );

        NexusArtifact artifact2 = new NexusArtifact();
        artifact2.setArtifactId( "artifactId1" );
        artifact2.setClassifier( "classifier1" );
        artifact2.setContextId( "contextId1" );
        artifact2.setGroupId( "groupId1" );
        artifact2.setPackaging( "packaging1" );
        artifact2.setRepoId( "repoId1" );
        artifact2.setResourceURI( "resourceURI1" );
        artifact2.setVersion( "version1" );
        artifact2.setArtifactLink( "artifactLink2" );
        artifact2.setExtension( "extension2" );
        artifact2.setPomLink( "pomLink2" );
        response.addData( artifact2 );

        this.marshalUnmarchalThenCompare( response );
        this.validateXmlHasNoPackageNames( response );
    }
}
