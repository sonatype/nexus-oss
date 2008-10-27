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
        System.getProperties().remove( "nexus-work" );
        if(System.getenv().containsKey( "nexus-work" )){
            return;
        }
        Map<Object, String> context = new HashMap<Object, String>();
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        String defaultRoot = new File( System.getProperty( "user.home" ), "/sonatype-work/nexus-work" ).getAbsolutePath();
        assertDirectoryProperties(context, defaultRoot);
        
    }
    
    public void testPlexusSetUp(){
        String rootPath = new File("/src/test/resources/nexus-work").getAbsolutePath();
        Map<Object, String> context = new HashMap<Object, String>();
        context.put( "nexus-work",  rootPath);
        NexusWorkDirUtils.setUpNexusWorkDir( context );
        assertDirectoryProperties(context, rootPath);  
    }
    
    
    public void testSystemPropertiesSetUp(){
        String rootPath = new File("/src/test/resources/nexus-work").getAbsolutePath();
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
