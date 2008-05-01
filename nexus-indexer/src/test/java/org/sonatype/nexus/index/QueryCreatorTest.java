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
