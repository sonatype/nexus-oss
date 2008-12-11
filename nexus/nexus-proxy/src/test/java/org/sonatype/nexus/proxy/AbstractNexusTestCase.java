/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.proxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.IOUtil;

/**
 * Abstract test case for nexus tests. It is customizing the context and helps with nexus configurations.
 * 
 * @author cstamas
 */
public abstract class AbstractNexusTestCase
    extends PlexusTestCase
{
    public static final String WORK_CONFIGURATION_KEY = "nexus-work";
    public static final String APPS_CONFIGURATION_KEY = "apps";

    public static final File PLEXUS_HOME = new File( getBasedir(), "target/plexus-home" );
    public static final File WORK_HOME = new File( PLEXUS_HOME, "nexus-work" );
    public static final File CONF_HOME = new File( WORK_HOME, "conf" );

    protected void customizeContext( Context ctx )
    {
        ctx.put( WORK_CONFIGURATION_KEY, WORK_HOME.getAbsolutePath() );
        ctx.put( APPS_CONFIGURATION_KEY, PLEXUS_HOME.getAbsolutePath() );
    }

    protected String getNexusConfiguration()
    {
        return CONF_HOME + "/nexus.xml";
    }

    protected void copyDefaultConfigToPlace()
        throws IOException
    {
        IOUtil.copy( getClass().getResourceAsStream( "/META-INF/nexus/nexus.xml" ), new FileOutputStream(
            getNexusConfiguration() ) );
    }

}
