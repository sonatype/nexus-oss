/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.web;

import java.io.File;
import java.util.Map;

import org.codehaus.plexus.util.StringUtils;

/**
 * This utils is used to initialize the nexus-work directory location. The order is plexus.properties > system property
 * > environment variables > default
 * 
 * @author Juven Xu
 */
public class NexusWorkDirUtils
{
    public static final String NEXUS_DEFAULT_ROOT = "/sonatype-work/nexus";

    public static final String KEY_NEXUS_WORK = "nexus-work";

    public static final String KEY_NEXUS_WORK_SYS_PROP = "plexus.nexus-work";

    public static final String KEY_NEXUS_WORK_ENV_VAR = "PLEXUS_NEXUS_WORK";

    /*
     * These variables are by default set based on ${nexus-work}, but also they are configurable.
     */
    public static final String KEY_RUNTIME = "runtime";

    public static final String KEY_SECURITY_XML_FILE = "security-xml-file";

    public static final String KEY_APPLICATION_CONF = "application-conf";

    public static final String RELATIVE_PATH_RUNTIME = "/runtime";

    public static final String RELATIVE_PATH_SECURITY_XML_FILE = "/conf/security.xml";

    public static final String RELATIVE_PATH_APPLICATION_CONF = "/conf";

    public static void setUpNexusWorkDir( Map<Object, String> context )
    {
        String root = setUpRootDir( context );

        setUpMinorDirs( context, root );
    }

    private static String setUpRootDir( Map<Object, String> context )
    {

        String value;

        // check if the value already exists (loaded by plexus container)
        if ( !StringUtils.isEmpty( context.get( KEY_NEXUS_WORK ) ) )
        {
            value = context.get( KEY_NEXUS_WORK );
        }

        // check system properties
        else if ( !StringUtils.isEmpty( System.getProperty( KEY_NEXUS_WORK_SYS_PROP ) ) )
        {
            value = System.getProperty( KEY_NEXUS_WORK_SYS_PROP );
        }

        // check environment variables
        else if ( !StringUtils.isEmpty( System.getenv().get( KEY_NEXUS_WORK_ENV_VAR ) ) )
        {
            value = System.getenv().get( KEY_NEXUS_WORK_ENV_VAR );
        }

        // no user customization found, use default
        else
        {
            value = new File( System.getProperty( "user.home" ), NEXUS_DEFAULT_ROOT ).getAbsolutePath();
        }

        // set plexus context value
        context.put( KEY_NEXUS_WORK, value );
        // set nexus work system property
        System.getProperties().put( KEY_NEXUS_WORK_SYS_PROP, value );

        return value;
    }

    /**
     * Set up other directory properties based on the root directory.
     */
    private static void setUpMinorDirs( Map<Object, String> context, String root )
    {
        if ( StringUtils.isEmpty( context.get( KEY_RUNTIME ) ) )
        {
            context.put( KEY_RUNTIME, new File( root, RELATIVE_PATH_RUNTIME ).getAbsolutePath() );
        }

        if ( StringUtils.isEmpty( context.get( KEY_SECURITY_XML_FILE ) ) )
        {
            context.put( KEY_SECURITY_XML_FILE, new File( root, RELATIVE_PATH_SECURITY_XML_FILE ).getAbsolutePath() );
        }

        if ( StringUtils.isEmpty( context.get( KEY_APPLICATION_CONF ) ) )
        {
            context.put( KEY_APPLICATION_CONF, new File( root, RELATIVE_PATH_APPLICATION_CONF ).getAbsolutePath() );
        }
    }

}
