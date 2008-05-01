/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
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
        Query q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "something is dotted" );

        assertEquals( "a:something* a:is* a:dotted*", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.ARTIFACT_ID, "something.is.dotted" );

        assertEquals( "a:\"something is dotted\"", q.toString() );

        q = queryCreator.constructQuery( ArtifactInfo.GROUP_ID, "something.is.dotted" );

        assertEquals( "g:something.is.dotted*", q.toString() );
    }

}
