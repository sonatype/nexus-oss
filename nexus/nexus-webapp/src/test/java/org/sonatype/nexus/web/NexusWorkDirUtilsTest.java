/**
 * Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdPartyUrl}.
 *
 * This program is licensed to you under Version 3 only of the GNU
 * General Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.web;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

public class NexusWorkDirUtilsTest
    extends TestCase
{
    public void testDefaultSetUp()
    {
        System.getProperties().remove( NexusWorkDirUtils.KEY_NEXUS_WORK_SYS_PROP );
        if(System.getenv().containsKey( NexusWorkDirUtils.KEY_NEXUS_WORK_ENV_VAR )){
            return;
        }
        Map<Object, String> context = new HashMap<Object, String>();
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        String defaultRoot = new File( System.getProperty( "user.home" ), "/sonatype-work/nexus" ).getAbsolutePath();
        assertDirectoryProperties(context, defaultRoot);
        
    }
    
    public void testPlexusSetUp(){
        String rootPath = new File("/src/test/resources/nexus").getAbsolutePath();
        Map<Object, String> context = new HashMap<Object, String>();
        context.put( "nexus-work",  rootPath);
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        assertDirectoryProperties(context, rootPath);  
    }
    
    
    public void testSystemPropertiesSetUp(){
        String rootPath = new File("/src/test/resources/nexus").getAbsolutePath();
        Map<Object, String> context = new HashMap<Object, String>();
        System.getProperties().put( "nexus-work", rootPath );
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        assertDirectoryProperties(context, rootPath);  
        System.getProperties().remove( "nexus-work" );
    }
    
    
    
    //after setting environment variable nexus-work in my local machine, this test passed
/*    public void testEnvVarSetUp(){
        String rootPath = new File("D:\\test").getAbsolutePath();
        Map<Object, String> context = new HashMap<Object, String>();
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        assertDirectoryProperties(context, rootPath);  
    }*/
    
    private void assertDirectoryProperties(Map<Object, String> context, String rootPath){
        assertEquals(new File(rootPath).getAbsolutePath(), context.get( NexusWorkDirUtils.KEY_NEXUS_WORK ));
        assertEquals(new File(rootPath, NexusWorkDirUtils.RELATIVE_PATH_RUNTIME).getAbsolutePath(), context.get(NexusWorkDirUtils.KEY_RUNTIME ));
        assertEquals(new File(rootPath, NexusWorkDirUtils.RELATIVE_PATH_SECURITY_XML_FILE).getAbsolutePath(), context.get(NexusWorkDirUtils.KEY_SECURITY_XML_FILE ));
  
    }
}
