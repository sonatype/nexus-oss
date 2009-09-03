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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.sonatype.nexus.util.EnhancedProperties;

/**
 * Log4J log manager.
 *
 * @author cstamas
 * @author juven
 */
@Component( role = LogManager.class )
public class Log4jLogManager
    implements LogManager, Disposable
{
    @Requirement
    private LogConfiguration<EnhancedProperties> logConfiguration;

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
            	result.add( (FileAppender)appender);
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

	public void dispose() 
	{
		for ( Logger logger : getLoggers() )
		{
			for ( FileAppender appender : getFileAppenders( logger) )
			{
				appender.close();
			}
		}
	}
}
