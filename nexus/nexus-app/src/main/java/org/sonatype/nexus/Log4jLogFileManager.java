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
package org.sonatype.nexus;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Log4J file manager.
 * 
 * @author cstamas
 */
@Component( role = LogFileManager.class )
public class Log4jLogFileManager
    implements LogFileManager
{
    public Log4jLogFileManager()
    {
        createLogDirectory();
    }

    public File getLogFile( String filename )
    {
        Logger logger = Logger.getRootLogger();

        Enumeration<Appender> appenders = (Enumeration<Appender>) logger.getAllAppenders();

        while ( appenders.hasMoreElements() )
        {
            Appender appender = appenders.nextElement();

            if ( FileAppender.class.isAssignableFrom( appender.getClass() ) )
            {
                File logfile = new File( ( (FileAppender) appender ).getFile() );

                if ( logfile.getName().equals( filename ) )
                {
                    return logfile;
                }
            }
        }

        return null;
    }

    public Set<File> getLogFiles()
    {
        Logger logger = Logger.getRootLogger();

        Enumeration<Appender> appenders = (Enumeration<Appender>) logger.getAllAppenders();

        HashSet<File> files = new HashSet<File>();

        while ( appenders.hasMoreElements() )
        {
            Appender appender = appenders.nextElement();

            if ( FileAppender.class.isAssignableFrom( appender.getClass() ) )
            {
                files.add( new File( ( (FileAppender) appender ).getFile() ) );
            }
        }

        return files;
    }

    public void createLogDirectory()
    {
        for ( File file : getLogFiles() )
        {
            File parent = file.getParentFile();

            if ( parent != null && !parent.exists() )
            {
                parent.mkdirs();
            }
        }
    }

}
