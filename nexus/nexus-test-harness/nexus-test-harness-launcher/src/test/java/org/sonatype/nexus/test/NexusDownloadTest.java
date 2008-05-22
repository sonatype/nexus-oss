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
package org.sonatype.nexus.test;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

public class NexusDownloadTest
    extends TestCase
{
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
    }
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
    }
    public void testDownloadArtifact()
        throws Exception
    {
        /*URL url = new URL( "http://localhost:8081/nexus/content/groups/nexus-test/org/sonatype/nexus/release-jar/1.0.0-beta-4-SNAPSHOT/release-jar-1.0.0-beta-4-SNAPSHOT" );
        OutputStream out = new BufferedOutputStream( new FileOutputStream( "file.jar" ) );
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream();
        try
        {
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
                numWritten += numRead;
            }
        }
        finally
        {
            out.close();
            in.close();
        }*/
    }
}
