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
import java.util.Properties;

/**
 * Extract the most import part of the log4j configuration file
 * 
 * @author juven
 */
public class SimpleLog4jConfig
{
    private static final String KEY_ROOT_LOGGER = "log4j.rootLogger";

    private static final String KEY_FILE_APPENDER_LOCAION = "log4j.appender.logfile.File";

    private static final String KEY_FILE_APPENDER_PATTERN = "log4j.appender.logfile.layout.ConversionPattern";

    private String rootLogger;

    private String fileAppenderLocation;

    private String fileAppenderPattern;

    public SimpleLog4jConfig( String rootLogger, String fileAppenderLocation, String fileAppenderPattern )
    {
        this.rootLogger = rootLogger;

        this.fileAppenderLocation = fileAppenderLocation;

        this.fileAppenderPattern = fileAppenderPattern;
    }

    public SimpleLog4jConfig( Map<String, String> prop )
    {
        this.rootLogger = prop.get( KEY_ROOT_LOGGER );

        this.fileAppenderLocation = prop.get( KEY_FILE_APPENDER_LOCAION );

        this.fileAppenderPattern = prop.get( KEY_FILE_APPENDER_PATTERN );
    }

    public Map<String, String> toMap()
    {
        Map<String, String> result = new HashMap<String, String>();

        result.put( KEY_ROOT_LOGGER, rootLogger );

        if ( fileAppenderLocation != null )
        {
            result.put( KEY_FILE_APPENDER_LOCAION, fileAppenderLocation );
        }

        if ( fileAppenderPattern != null )
        {
            result.put( KEY_FILE_APPENDER_PATTERN, fileAppenderPattern );
        }
        return result;
    }

    public String getFileAppenderLocation()
    {
        return fileAppenderLocation;
    }

    public void setFileAppenderLocation( String fileAppenderLocation )
    {
        this.fileAppenderLocation = fileAppenderLocation;
    }

    public String getFileAppenderPattern()
    {
        return fileAppenderPattern;
    }

    public void setFileAppenderPattern( String fileAppenderPattern )
    {
        this.fileAppenderPattern = fileAppenderPattern;
    }

    public String getRootLogger()
    {
        return rootLogger;
    }

    public void setRootLogger( String rootLogger )
    {
        this.rootLogger = rootLogger;
    }

}
