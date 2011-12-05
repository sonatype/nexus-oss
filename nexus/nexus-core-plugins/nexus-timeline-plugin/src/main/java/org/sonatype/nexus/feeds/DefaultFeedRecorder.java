/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.feeds;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.ExceptionUtils;
import org.sonatype.nexus.feeds.record.NexusItemInfo;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.timeline.Entries;
import org.sonatype.nexus.timeline.Entry;
import org.sonatype.nexus.timeline.NexusTimeline;
import com.google.common.base.Predicate;

/**
 * A feed recorder that uses DefaultNexus to record feeds.
 *
 * @author cstamas
 */
@Component( role = FeedRecorder.class )
public class DefaultFeedRecorder
    implements FeedRecorder
{

    public static final int DEFAULT_PAGE_SIZE = 40;

    public static final String REPOSITORY = "r";

    public static final String REPOSITORY_PATH = "path";

    public static final String REMOTE_URL = "rurl";

    public static final String CTX_PREFIX = "ctx.";

    public static final String ATR_PREFIX = "atr.";

    public static final String ACTION = "action";

    public static final String MESSAGE = "message";

    public static final String DATE = "date";

    public static final String STACK_TRACE = "strace";

    /**
     * Event type: repository
     */
    private static final String REPO_EVENT_TYPE = "REPO_EVENTS";

    private static final Set<String> REPO_EVENT_TYPE_SET = new HashSet<String>( 1 );

    {
        REPO_EVENT_TYPE_SET.add( REPO_EVENT_TYPE );
    }

    /**
     * Event type: system
     */
    private static final String SYSTEM_EVENT_TYPE = "SYSTEM";

    private static final Set<String> SYSTEM_EVENT_TYPE_SET = new HashSet<String>( 1 );

    {
        SYSTEM_EVENT_TYPE_SET.add( SYSTEM_EVENT_TYPE );
    }

    /**
     * Event type: authc/authz
     */
    private static final String AUTHC_AUTHZ_EVENT_TYPE = "AUTHC_AUTHZ";

    private static final Set<String> AUTHC_AUTHZ_EVENT_TYPE_SET = new HashSet<String>( 1 );

    {
        AUTHC_AUTHZ_EVENT_TYPE_SET.add( AUTHC_AUTHZ_EVENT_TYPE );
    }

    /**
     * Event type: error
     */
    private static final String ERROR_WARNING_EVENT_TYPE = "ERROR_WARNING";

    private static final Set<String> ERROR_WARNING_EVENT_TYPE_SET = new HashSet<String>( 1 );

    {
        ERROR_WARNING_EVENT_TYPE_SET.add( ERROR_WARNING_EVENT_TYPE );
    }

    /**
     * The time format used in events.
     */
    private static final String EVENT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSZ";

    private Logger logger = Slf4jPlexusLogger.getPlexusLogger( getClass() );

    /**
     * The timeline for persistent events and feeds.
     */
    @Requirement
    private NexusTimeline nexusTimeline;

    /**
     * The Feed filter (will checks for user access )
     */
    @Requirement
    private FeedArtifactEventFilter feedArtifactEventFilter;

    protected Logger getLogger()
    {
        return logger;
    }

    protected DateFormat getDateFormat()
    {
        return new SimpleDateFormat( EVENT_DATE_FORMAT );
    }

    protected Date getEventDate( final Map<String, String> map )
    {
        Date eventDate;

        try
        {
            eventDate = getDateFormat().parse( map.get( DATE ) );
        }
        catch ( ParseException e )
        {
            getLogger().warn( "Could not format event date!", e );

            eventDate = new Date();
        }

        return eventDate;
    }

    protected List<NexusArtifactEvent> getAisFromMaps( Entries data )
    {
        List<NexusArtifactEvent> result = new ArrayList<NexusArtifactEvent>();

        for ( Entry record : data )
        {
            Map<String, String> map = record.getData();

            NexusItemInfo ai = new NexusItemInfo();

            ai.setRepositoryId( map.get( REPOSITORY ) );

            ai.setPath( map.get( REPOSITORY_PATH ) );

            ai.setRemoteUrl( map.get( REMOTE_URL ) );

            HashMap<String, String> ctx = new HashMap<String, String>();
            HashMap<String, String> atr = new HashMap<String, String>();

            for ( String key : map.keySet() )
            {
                if ( key.startsWith( CTX_PREFIX ) )
                {
                    ctx.put( key.substring( 4 ), map.get( key ) );
                }
                else if ( key.startsWith( ATR_PREFIX ) )
                {
                    atr.put( key.substring( 4 ), map.get( key ) );
                }
            }

            NexusArtifactEvent nae =
                new NexusArtifactEvent( getEventDate( map ), map.get( ACTION ), map.get( MESSAGE ), ai );

            // NEXUS-4038: backward compatibility
            // Before this fix, nae had NO attributes separately stored, but only ctx map existed with ctx + atr content
            // overlayed
            // After fix we have two separate maps. To handle well "old" timeline records, when we detect there is no
            // atr map (atr map is empty which will never be after fix), we "emulate" and lift all the ctx map into atr
            // map instead.

            if ( atr.isEmpty() )
            {
                nae.addItemAttributes( ctx );
            }
            else
            {
                nae.addEventContext( ctx );
                nae.addItemAttributes( atr );
            }

            result.add( nae );
        }

        return this.feedArtifactEventFilter.filterArtifactEventList( result );
    }

    protected List<SystemEvent> getSesFromMaps( Entries data )
    {
        List<SystemEvent> result = new ArrayList<SystemEvent>();

        for ( Entry record : data )
        {
            Map<String, String> map = record.getData();

            HashMap<String, Object> ctx = new HashMap<String, Object>();

            for ( String key : map.keySet() )
            {
                if ( key.startsWith( CTX_PREFIX ) )
                {
                    ctx.put( key.substring( 4 ), map.get( key ) );
                }
            }

            SystemEvent se = new SystemEvent( getEventDate( map ), map.get( ACTION ), map.get( MESSAGE ) );

            se.addEventContext( ctx );

            result.add( se );
        }

        return result;
    }

    protected List<AuthcAuthzEvent> getAaesFromMaps( Entries data )
    {
        List<AuthcAuthzEvent> result = new ArrayList<AuthcAuthzEvent>();

        for ( Entry record : data )
        {
            Map<String, String> map = record.getData();

            HashMap<String, Object> ctx = new HashMap<String, Object>();

            for ( String key : map.keySet() )
            {
                if ( key.startsWith( CTX_PREFIX ) )
                {
                    ctx.put( key.substring( 4 ), map.get( key ) );
                }
            }

            AuthcAuthzEvent evt = new AuthcAuthzEvent( getEventDate( map ), map.get( ACTION ), map.get( MESSAGE ) );

            evt.addEventContext( ctx );

            result.add( evt );

        }

        return result;
    }

    protected List<ErrorWarningEvent> getEwesFromMaps( Entries data )
    {
        List<ErrorWarningEvent> result = new ArrayList<ErrorWarningEvent>();

        for ( Entry record : data )
        {
            Map<String, String> map = record.getData();

            HashMap<String, Object> ctx = new HashMap<String, Object>();

            for ( String key : map.keySet() )
            {
                if ( key.startsWith( CTX_PREFIX ) )
                {
                    ctx.put( key.substring( 4 ), map.get( key ) );
                }
            }

            ErrorWarningEvent evt =
                new ErrorWarningEvent( getEventDate( map ), map.get( ACTION ), map.get( MESSAGE ),
                                       map.get( STACK_TRACE ) );

            evt.addEventContext( ctx );

            result.add( evt );
        }

        return result;
    }

    // ==

    protected void releaseResult( Entries result )
    {
        if ( result != null )
        {
            result.release();
        }
    }

    public Entries getEvents( Set<String> types, Set<String> subtypes, Integer from, Integer count,
                              Predicate<Entry> filter )
    {
        int cnt = count != null ? count : DEFAULT_PAGE_SIZE;

        if ( from != null )
        {
            return nexusTimeline.retrieve( from, cnt, types, subtypes, filter );
        }
        else
        {
            return nexusTimeline.retrieve( 0, cnt, types, subtypes, filter );
        }
    }

    public List<NexusArtifactEvent> getNexusArtifectEvents( Set<String> subtypes, Integer from, Integer count,
                                                            Predicate<Entry> filter )
    {
        Entries result = null;

        try
        {
            result = getEvents( REPO_EVENT_TYPE_SET, subtypes, from, count, filter );

            return getAisFromMaps( result );
        }
        finally
        {
            releaseResult( result );
        }
    }

    public List<SystemEvent> getSystemEvents( Set<String> subtypes, Integer from, Integer count, Predicate<Entry> filter )
    {
        Entries result = null;

        try
        {
            result = getEvents( SYSTEM_EVENT_TYPE_SET, subtypes, from, count, filter );

            return getSesFromMaps( result );
        }
        finally
        {
            releaseResult( result );
        }
    }

    public List<AuthcAuthzEvent> getAuthcAuthzEvents( Set<String> subtypes, Integer from, Integer count,
                                                      Predicate<Entry> filter )
    {
        Entries result = null;

        try
        {
            result = getEvents( AUTHC_AUTHZ_EVENT_TYPE_SET, subtypes, from, count, filter );

            return getAaesFromMaps( result );
        }
        finally
        {
            releaseResult( result );
        }
    }

    public List<ErrorWarningEvent> getErrorWarningEvents( Set<String> subtypes, Integer from, Integer count,
                                                          Predicate<Entry> filter )
    {
        Entries result = null;

        try
        {
            result = getEvents( ERROR_WARNING_EVENT_TYPE_SET, subtypes, from, count, filter );

            return getEwesFromMaps( result );
        }
        finally
        {
            releaseResult( result );
        }
    }

    // ==

    public void addSystemEvent( String action, String message )
    {
        SystemEvent event = new SystemEvent( new Date(), action, message );

        addToTimeline( event );
    }

    private void putContext( final Map<String, String> map, final String prefix, final Map<String, ?> context )
    {
        for ( String key : context.keySet() )
        {
            Object value = context.get( key );

            if ( value == null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "The attribute with key '" + key + "' in event context is NULL!" );
                }

                value = "";
            }

            map.put( prefix + key, value.toString() );
        }
    }

    public void addAuthcAuthzEvent( AuthcAuthzEvent evt )
    {
        Map<String, String> map = new HashMap<String, String>();

        putContext( map, CTX_PREFIX, evt.getEventContext() );

        map.put( ACTION, evt.getAction() );

        map.put( MESSAGE, evt.getMessage() );

        map.put( DATE, getDateFormat().format( evt.getEventDate() ) );

        addToTimeline( map, AUTHC_AUTHZ_EVENT_TYPE, evt.getAction() );
    }

    public void addNexusArtifactEvent( NexusArtifactEvent nae )
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put( REPOSITORY, nae.getNexusItemInfo().getRepositoryId() );

        map.put( REPOSITORY_PATH, nae.getNexusItemInfo().getPath() );

        if ( nae.getNexusItemInfo().getRemoteUrl() != null )
        {
            map.put( REMOTE_URL, nae.getNexusItemInfo().getRemoteUrl() );
        }

        putContext( map, CTX_PREFIX, nae.getEventContext() );
        putContext( map, ATR_PREFIX, nae.getItemAttributes() );

        if ( nae.getMessage() != null )
        {
            map.put( MESSAGE, nae.getMessage() );
        }

        map.put( DATE, getDateFormat().format( nae.getEventDate() ) );

        map.put( ACTION, nae.getAction() );

        addToTimeline( map, REPO_EVENT_TYPE, nae.getAction() );
    }

    public SystemProcess systemProcessStarted( String action, String message )
    {
        SystemProcess prc = new SystemProcess( new Date(), action, message, new Date() );

        addToTimeline( prc );

        getLogger().info( prc.getMessage() );

        return prc;
    }

    public void systemProcessFinished( SystemProcess prc, String finishMessage )
    {
        prc.finished( finishMessage );

        addToTimeline( prc );

        getLogger().info( prc.getMessage() );
    }

    public void systemProcessCanceled( SystemProcess prc, String cancelMessage )
    {
        prc.canceled( cancelMessage );

        addToTimeline( prc );

        getLogger().info( prc.getMessage() );
    }

    public void systemProcessBroken( SystemProcess prc, Throwable e )
    {
        prc.broken( e );

        addToTimeline( prc );

        getLogger().info( prc.getMessage(), e );
    }

    protected void addToTimeline( SystemEvent se )
    {
        Map<String, String> map = new HashMap<String, String>();

        putContext( map, CTX_PREFIX, se.getEventContext() );

        map.put( DATE, getDateFormat().format( se.getEventDate() ) );

        map.put( ACTION, se.getAction() );

        map.put( MESSAGE, se.getMessage() );

        addToTimeline( map, SYSTEM_EVENT_TYPE, se.getAction() );
    }

    protected void addToTimeline( Map<String, String> map, String t1, String t2 )
    {
        nexusTimeline.add( System.currentTimeMillis(), t1, t2, map );
    }

    public void addErrorWarningEvent( String action, String message )
    {
        addErrorWarningEvent( new ErrorWarningEvent( new Date(), action, message, null ) );
    }

    public void addErrorWarningEvent( String action, String message, Throwable throwable )
    {
        String stackTrace = ExceptionUtils.getFullStackTrace( throwable );

        addErrorWarningEvent( new ErrorWarningEvent( new Date(), action, message, stackTrace ) );
    }

    protected void addErrorWarningEvent( ErrorWarningEvent errorEvt )
    {
        Map<String, String> map = new HashMap<String, String>();

        putContext( map, CTX_PREFIX, errorEvt.getEventContext() );

        map.put( ACTION, errorEvt.getAction() );

        map.put( DATE, getDateFormat().format( errorEvt.getEventDate() ) );

        if ( errorEvt.getMessage() != null )
        {
            map.put( MESSAGE, errorEvt.getMessage() );
        }

        if ( errorEvt.getStackTrace() != null )
        {
            map.put( STACK_TRACE, errorEvt.getStackTrace() );
        }

        addToTimeline( map, ERROR_WARNING_EVENT_TYPE, errorEvt.getAction() );
    }

}
