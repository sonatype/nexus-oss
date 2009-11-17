/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.web;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class NexusWorkDirUtilsTest
    extends TestCase
{
    public void testDefaultSetUp()
        throws IOException
    {
        System.getProperties().remove( NexusWorkDirUtils.KEY_NEXUS_WORK_SYS_PROP );
        if ( System.getenv().containsKey( NexusWorkDirUtils.KEY_NEXUS_WORK_ENV_VAR ) )
        {
            return;
        }
        Map<Object, Object> context = new HashMap<Object, Object>();
        String baseDir = this.getFakeBaseDir( "testDefaultSetUp" );
        context.put( "basedir", baseDir );
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        String defaultRoot = new File( System.getProperty( "user.home" ), "/sonatype-work/nexus" ).getAbsolutePath();
        assertDirectoryProperties( context, defaultRoot, baseDir );

    }

    public void testPlexusSetUp()
        throws IOException
    {
        String rootPath = new File( "/src/test/resources/nexus" ).getAbsolutePath();
        Map<Object, Object> context = new HashMap<Object, Object>();
        String baseDir = this.getFakeBaseDir( "testPlexusSetUp" );
        context.put( "basedir", baseDir );
        context.put( "nexus-work", rootPath );
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        assertDirectoryProperties( context, rootPath, baseDir );
    }

    public void testSystemPropertiesSetUp()
        throws IOException
    {
        String rootPath = new File( "/src/test/resources/nexus" ).getAbsolutePath();
        Map<Object, Object> context = new HashMap<Object, Object>();
        String baseDir = this.getFakeBaseDir( "testSystemPropertiesSetUp" );
        context.put( "basedir", baseDir );
        System.getProperties().put( "nexus-work", rootPath );
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        assertDirectoryProperties( context, rootPath, baseDir );
        System.getProperties().remove( "nexus-work" );
    }

    private String getFakeBaseDir( String testName )
    {
        return new File( "target", testName + "/WEB-INF" ).getAbsolutePath();
    }

    // after setting environment variable nexus-work in my local machine, this test passed
    /*
     * public void testEnvVarSetUp(){ String rootPath = new File("D:\\test").getAbsolutePath(); Map<Object, String>
     * context = new HashMap<Object, String>(); NexusWorkDirUtils.setUpNexusWorkDir( context );
     * assertDirectoryProperties(context, rootPath); }
     */

    private void assertDirectoryProperties( Map<Object, Object> context, String rootPath, String basedir )
        throws IOException
    {
        assertEquals( new File( rootPath ).getAbsolutePath(), context.get( NexusWorkDirUtils.KEY_NEXUS_WORK ) );
        assertEquals( new File( rootPath, NexusWorkDirUtils.RELATIVE_PATH_RUNTIME ).getAbsolutePath(), context
            .get( NexusWorkDirUtils.KEY_RUNTIME ) );
        assertEquals(
            new File( rootPath, NexusWorkDirUtils.RELATIVE_PATH_SECURITY_XML_FILE ).getAbsolutePath(),
            context.get( NexusWorkDirUtils.KEY_SECURITY_XML_FILE ) );
        assertEquals( new File( basedir, "../runtime/apps/nexus" ).getCanonicalPath(), context
            .get( NexusWorkDirUtils.KEY_NEXUS_APP ) );

    }
}
