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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.LimitedInputStream;
import org.sonatype.nexus.NexusStreamResponse;
import org.sonatype.nexus.util.EnhancedProperties;

/**
 * Log4J log manager.
 * 
 * @author cstamas
 * @author juven
 */
@Component( role = LogManager.class )
public class Log4jLogManager
    implements LogManager
{
    @Requirement
    private org.slf4j.Logger logger;
    
    @Requirement
    private LogConfiguration<EnhancedProperties> logConfiguration;
    
    protected org.slf4j.Logger getLogger()
    {
        return logger;
    }

    public Log4jLogManager()
    {
        createLogDirectory();
    }

    public File getLogFile( String filename )
    {
        Set<File> logFiles = getLogFiles();

        for ( File logFile : logFiles )
        {
            if ( logFile.getName().equals( filename ) )
            {
                return logFile;
            }
        }

        return null;
    }

    public Set<File> getLogFiles()
    {
        HashSet<File> files = new HashSet<File>();

        for ( Logger logger : getLoggers() )
        {
            files.addAll( getLogFiles( logger ) );
        }

        return files;
    }

    @SuppressWarnings( { "deprecation", "unchecked" } )
    private List<Logger> getLoggers()
    {
        List<Logger> result = new ArrayList<Logger>();

        result.add( Logger.getRootLogger() );

        Enumeration<Category> categories = Category.getCurrentCategories();

        while ( categories.hasMoreElements() )
        {
            Category category = categories.nextElement();

            if ( category instanceof Logger )
            {
                result.add( (Logger) category );
            }
        }

        return result;
    }

    @SuppressWarnings( "unchecked" )
    private List<FileAppender> getFileAppenders( Category logger )
    {
        List<FileAppender> result = new ArrayList<FileAppender>();

        Enumeration<Appender> appenders = logger.getAllAppenders();

        while ( appenders.hasMoreElements() )
        {
            Appender appender = appenders.nextElement();

            if ( appender instanceof FileAppender )
            {
                result.add( (FileAppender) appender );
            }
        }

        return result;
    }

    protected Set<File> getLogFiles( Category logger )
    {
        HashSet<File> files = new HashSet<File>();

        for ( FileAppender appender : getFileAppenders( logger ) )
        {
            String file = appender.getFile();

            if ( file == null )
            {
                continue;
            }

            files.add( new File( file ) );
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

    public boolean isUserEdited()
    {
        return logConfiguration.isUserEdited();
    }

    public LogConfig getLogConfig()
        throws IOException
    {
        logConfiguration.load();

        return new SimpleLog4jConfig( logConfiguration.getConfig() );
    }

    public void setLogConfig( LogConfig logConfig )
        throws IOException
    {
        Map<String, String> config = logConfiguration.getConfig();

        config.putAll( logConfig );

        logConfiguration.apply();

        logConfiguration.save();
    }

    public Collection<NexusStreamResponse> getApplicationLogFiles()
        throws IOException
    {
        getLogger().debug( "List log files." );

        Set<File> files = getLogFiles();

        ArrayList<NexusStreamResponse> result = new ArrayList<NexusStreamResponse>( files.size() );

        for ( File file : files )
        {
            NexusStreamResponse response = new NexusStreamResponse();

            response.setName( file.getName() );

            // TODO:
            response.setMimeType( "text/plain" );

            response.setSize( file.length() );

            response.setInputStream( null );

            result.add( response );
        }

        return result;
    }

    /**
     * Retrieves a stream to the requested log file. This method ensures that the file is rooted in the log folder to
     * prevent browsing of the file system.
     * 
     * @param logFile path of the file to retrieve
     * @returns InputStream to the file or null if the file is not allowed or doesn't exist.
     */
    public NexusStreamResponse getApplicationLogAsStream( String logFile, long from, long count )
        throws IOException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Retrieving " + logFile + " log file." );
        }

        if ( logFile.contains( File.pathSeparator ) )
        {
            getLogger().warn( "Nexus refuses to retrive log files with path separators in its name." );

            return null;
        }

        File log = getLogFile( logFile );

        if ( log == null || !log.exists() )
        {
            getLogger().warn( "Log file does not exist: [" + logFile + "]" );

            return null;
        }

        NexusStreamResponse response = new NexusStreamResponse();

        response.setName( logFile );

        response.setMimeType( "text/plain" );

        response.setSize( log.length() );

        response.setFromByte( from );

        response.setBytesCount( count );

        response.setInputStream( new LimitedInputStream( new FileInputStream( log ), from, count ) );

        return response;
    }

}
