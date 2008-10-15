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
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;


/**
 * This utils is used to initialize the nexus-work directory location. The order is plexus.xml > system property >
 * environment variables > default
 * 
 * @author Juven Xu
 */
public class NexusWorkDirUtils
{
    public static final String NEXUS_DEFAULT_ROOT = "/sonatype-work/nexus-work";
    public static final String KEY_NEXUS_WORK = "nexus-work";
    public static final String KEY_RUNTIME = "runtime";
    public static final String KEY_SECURITY_XML_FILE = "security-xml-file";
    public static final String RELATIVE_PATH_RUNTIME = "/runtime";
    public static final String RELATIVE_PATH_SECURITY_XML_FILE = "/conf/security.xml";
    
    public static void setUpNexusWorkDir( Map<Object, String> context )
    {
        String root = setUpRootDir(context);
        setUpMinorDirs(context, root);
    }
    
    private static String setUpRootDir(Map<Object, String> context){
        String value;
        //check if the value already exists (loaded by plexus container)
        value = context.get( KEY_NEXUS_WORK );
        if(!StringUtils.isEmpty( value )){
            return value;
        }
        //check system properties
        value = System.getProperty( KEY_NEXUS_WORK );
        if(!StringUtils.isEmpty( value )){
            context.put( KEY_NEXUS_WORK, new File(value).getAbsolutePath() );
            return value;
        }
        //check environment variables
        value = System.getenv().get( KEY_NEXUS_WORK );
        if(!StringUtils.isEmpty( value )){
            context.put( KEY_NEXUS_WORK, new File(value).getAbsolutePath() );
            return value;
        }
        //no user customization found, use default
        value = new File(System.getProperty( "user.home" ), NEXUS_DEFAULT_ROOT).getAbsolutePath();
        context.put( KEY_NEXUS_WORK, value);
        return value;
    }
    
    /**
     * Set up other directory properties based on the root directory.
     */
    private static void setUpMinorDirs(Map<Object, String> context, String root){
        context.put( KEY_RUNTIME, new File(root, RELATIVE_PATH_RUNTIME).getAbsolutePath());
        context.put( KEY_SECURITY_XML_FILE, new File(root, RELATIVE_PATH_SECURITY_XML_FILE).getAbsolutePath());
    }
    
}
