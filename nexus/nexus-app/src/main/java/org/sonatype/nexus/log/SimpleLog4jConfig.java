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
package org.sonatype.nexus.log;

import java.util.HashMap;
import java.util.Map;

/**
 * Extract the most import part of the log4j configuration file
 * 
 * @author juven
 */
public class SimpleLog4jConfig
    extends HashMap<String, String>
    implements LogConfig
{
    private static final long serialVersionUID = -4276278316976003784L;

    private static final String KEY_ROOT_LOGGER = "log4j.rootLogger";

    private static final String KEY_FILE_APPENDER_LOCATION = "log4j.appender.logfile.File";

    private static final String KEY_FILE_APPENDER_PATTERN = "log4j.appender.logfile.layout.ConversionPattern";

    public SimpleLog4jConfig( String rootLogger, String fileAppenderLocation, String fileAppenderPattern )
    {
        setRootLogger( rootLogger );

        setFileAppenderLocation( fileAppenderLocation );

        setFileAppenderPattern( fileAppenderPattern );
    }

    public SimpleLog4jConfig( Map<String, String> prop )
    {
        putAll( prop );
    }

    public String getFileAppenderLocation()
    {
        return get( KEY_FILE_APPENDER_LOCATION );
    }

    public void setFileAppenderLocation( String fileAppenderLocation )
    {
        put( KEY_FILE_APPENDER_LOCATION, fileAppenderLocation );
    }

    public String getFileAppenderPattern()
    {
        return get( KEY_FILE_APPENDER_PATTERN );
    }

    public void setFileAppenderPattern( String fileAppenderPattern )
    {
        put( KEY_FILE_APPENDER_PATTERN, fileAppenderPattern );
    }

    public String getRootLogger()
    {
        return get( KEY_ROOT_LOGGER );
    }

    public void setRootLogger( String rootLogger )
    {
        put( KEY_ROOT_LOGGER, rootLogger );
    }

}
