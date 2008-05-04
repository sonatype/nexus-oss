/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
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
package org.sonatype.nexus.proxy.maven;

import junit.framework.TestCase;

public class Maven2ContentClassTest
    extends TestCase
{
    protected Maven2ContentClass contentClass;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        contentClass = new Maven2ContentClass();
    }

    public void testIsRemoteFile()
        throws Exception
    {
        /*
         * assertFalse( contentClass.isRemoteFile( "/org/codehaus/plexus" ) ); assertFalse( contentClass.isRemoteFile(
         * "/org/codehaus/plexus/" ) ); assertFalse( contentClass.isRemoteFile( "/.index/kuku" ) ); assertTrue(
         * contentClass.isRemoteFile( "/.index/somefile.zip" ) ); assertTrue( contentClass.isRemoteFile(
         * "/.index/somefile.properties" ) ); assertTrue( contentClass.isRemoteFile(
         * "/org/codehaus/plexus/plexus-jetty6/1.0-SNAPSHOT/plexus-jetty6-1.0-SNAPSHOT.pom" ) );
         */
    }
}
