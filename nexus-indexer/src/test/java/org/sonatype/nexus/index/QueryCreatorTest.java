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

import org.apache.lucene.search.Query;
import org.codehaus.plexus.PlexusTestCase;

public class QueryCreatorTest
    extends PlexusTestCase
{

    protected QueryCreator queryCreator;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        queryCreator = (QueryCreator) lookup( QueryCreator.ROLE );
    }

    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    public void testConstructQuery()
    {
        // ARTIFACT_ID : dots are not left in place

        Query q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "something is dotted" );

        assertEquals( "a:something* a:is* a:dotted*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "something.is.dotted" );

        assertEquals( "+a:something +a:is +a:dotted*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "\"something.is.dotted\"" );

        assertEquals( "a:\"something is dotted\"", q.toString() );

        // GROUP_ID : dots are left in place

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "something is dotted" );

        assertEquals( "g:something* g:is* g:dotted*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "something.is.dotted" );

        assertEquals( "g:something.is.dotted*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "\"something.is.dotted\"" );

        assertEquals( "g:\"something.is.dotted\"*", q.toString() );

        // some special chars

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "_" );

        assertEquals( "a:_*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "geronimo-javamail_1.4" );

        assertEquals( "+a:geronimo +a:javamail +a:1 +a:4*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "commons-col" );

        assertEquals( "+a:commons +a:col*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "\"commons-col\"" );

        assertEquals( "a:\"commons col\"", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "_" );

        assertEquals( "g:_*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "geronimo-javamail_1.4" );

        assertEquals( "+g:geronimo +g:javamail +g:1.4*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "commons-col" );

        assertEquals( "+g:commons +g:col*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "\"commons-col\"" );

        assertEquals( "g:\"commons col\"", q.toString() );
        
        // VERSION : should not be splitted

        q = queryCreator.constructQuery( ArtifactInfo.VERSION, "1.2" );

        assertEquals( "v:1.2*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.VERSION, "\"1.2\"" );

        assertEquals( "v:\"1.2\"*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.VERSION, "1.2-SNAP" );

        assertEquals( "+v:1.2 +v:snap*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.VERSION, "\"1.2-SNAPSHOT\"" );

        assertEquals( "v:\"1.2 snapshot\"", q.toString() );
    }

}
