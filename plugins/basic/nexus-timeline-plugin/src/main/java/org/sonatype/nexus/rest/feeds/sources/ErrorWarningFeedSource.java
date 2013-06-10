/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.feeds.sources;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.sonatype.nexus.log.LogManager;

import com.google.common.collect.Lists;
import com.google.common.io.Closer;
import com.google.common.io.Files;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;

/**
 * Show Nexus ERROR and WARN log lines from last log file.
 * 
 * @author cstamas
 */
@Component( role = FeedSource.class, hint = "errorWarning" )
public class ErrorWarningFeedSource
    extends AbstractFeedSource
{
    public static final String CHANNEL_KEY = "errorWarning";

    private static final int LINES_TO_SCAN = 1000;

    @Requirement
    private LogManager logManager;

    public String getFeedKey()
    {
        return CHANNEL_KEY;
    }

    @Override
    public String getTitle()
    {
        return "Errors and Warnings";
    }

    @Override
    public String getDescription()
    {
        return "Errors and Warnings from Nexus log";
    }

    public String getFeedName()
    {
        return getDescription();
    }

    protected int getLinesToScan( final Map<String, String> params )
        throws ResourceException
    {
        int linesToScan = LINES_TO_SCAN;
        try
        {
            if ( params.containsKey( "lts" ) )
            {
                linesToScan = Integer.valueOf( params.get( "lts" ) );
            }
        }
        catch ( NumberFormatException e )
        {
            throw new ResourceException(
                Status.CLIENT_ERROR_BAD_REQUEST,
                "The 'lts' parameters must be number!",
                e );
        }
        return linesToScan;
    }

    public SyndFeed getFeed( final Integer from, final Integer count, final Map<String, String> params )
        throws IOException
    {
        final SyndFeedImpl feed = new SyndFeedImpl();
        feed.setTitle( getTitle() );
        feed.setDescription( getDescription() );
        feed.setAuthor( "Nexus " + getApplicationStatusSource().getSystemStatus().getVersion() );
        feed.setPublishedDate( new Date() );
        final List<SyndEntry> entries = Lists.newArrayList();
        final File logFile = logManager.getLogFile( "nexus.log" );

        // if there is no such file, user probably customized logging configuration
        // or we run as WAR etc.... simply return
        // empty feed, as here we can't do much
        // FIXME: some logic to ask for log files and figure out?
        if ( logFile != null )
        {
            Closer closer = Closer.create();
            try
            {
                final LineNumberReader reader =
                    new LineNumberReader( Files.newReader( logFile, Charset.forName( "UTF-8" ) ) );
                reader.setLineNumber( Integer.MAX_VALUE );
                final int totalLines = reader.getLineNumber();
                final int linesToScan = getLinesToScan( params );
                if ( totalLines > linesToScan )
                {
                    reader.setLineNumber( totalLines - linesToScan );
                }
                else
                {
                    reader.setLineNumber( 0 );
                }
                String logLine = reader.readLine();
                while ( logLine != null )
                {
                    if ( logLine.contains( " WARN " ) || logLine.contains( " ERROR " ) )
                    {
                        final SyndEntry entry = new SyndEntryImpl();

                        if ( logLine.contains( " ERROR " ) )
                        {
                            entry.setTitle( "Error" );
                        }
                        else if ( logLine.contains( " WARN " ) )
                        {
                            entry.setTitle( "Warning" );
                        }

                        final StringBuilder contentValue = new StringBuilder();
                        contentValue.append( logLine );

                        // FIXME: Grab following stacktrace if any in log
                        // if ( StringUtils.isNotEmpty( item.getStackTrace() ) )
                        // {
                        // // we need <br/> and &nbsp; to display stack trace on RSS
                        // String stackTrace = item.getStackTrace().replace(
                        // (String) System.getProperties().get( "line.separator" ),
                        // "<br/>" );
                        // stackTrace = stackTrace.replace( "\t", "&nbsp;&nbsp;&nbsp;&nbsp;" );
                        // contentValue.append( "<br/>" ).append( stackTrace );
                        // }

                        SyndContent content = new SyndContentImpl();
                        content.setType( MediaType.TEXT_PLAIN.toString() );
                        content.setValue( contentValue.toString() );
                        entry.setPublishedDate( new Date() ); // FIXME: item.getEventDate();
                        entry.setAuthor( feed.getAuthor() );
                        entry.setLink( "/" );
                        entry.setDescription( content );
                        entries.add( entry );
                    }
                    logLine = reader.readLine();
                }

            }
            catch ( Throwable e )
            {
                throw closer.rethrow( e );
            }
            finally
            {
                closer.close();
            }

        }
        feed.setEntries( entries );
        return feed;
    }
}