/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.log;

import java.util.Map;

import org.sonatype.nexus.util.EnhancedProperties;

/**
 * Extract the most import part of the log4j configuration file
 * 
 * @author juven
 */
public class SimpleLog4jConfig
    extends EnhancedProperties
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
