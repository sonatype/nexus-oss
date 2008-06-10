/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.model.CAdvancedSchedule;
import org.sonatype.nexus.configuration.model.CDailySchedule;
import org.sonatype.nexus.configuration.model.CMonthlySchedule;
import org.sonatype.nexus.configuration.model.COnceSchedule;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CSchedule;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.CWeeklySchedule;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The default implementation of the Task Configuration manager. Will handle writing to and loading from the tasks.xml
 * file. Will also load a default set of tasks if there is no existing configuration
 * 
 * @plexus.component
 */
public class DefaultTaskConfigManager
    extends AbstractLogEnabled
    implements TaskConfigManager, Initializable, Contextualizable
{
    private PlexusContainer plexusContainer;

    private CTaskConfiguration config;

    /**
     * The configuration file.
     * 
     * @plexus.configuration default-value="${apps}/nexus/conf/tasks.xml"
     */
    private File configurationFile;

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    /* (non-Javadoc)
     * @see org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable#initialize()
     */
    public void initialize()
        throws InitializationException
    {
        loadConfig();
    }
    
    public void initializeTasks( Scheduler scheduler )
    {
        //TODO: this code needs to be synchronized because of access to config
        //object
        List<CScheduledTask> tempList = new ArrayList<CScheduledTask>( config.getTasks() );
        
        getLogger().info( tempList.size() + " task(s) to load." );
        
        for ( CScheduledTask task : tempList )
        {   
            getLogger().info( "Loading task - " + task.getType() );
            try
            {
                NexusTask<?> nexusTask = ( NexusTask<?> ) plexusContainer.lookup( task.getType() );
                for ( Iterator iter = task.getProperties().iterator(); iter.hasNext(); )
                {
                    CProps prop = ( CProps ) iter.next();
                    nexusTask.addParameter( prop.getKey(), prop.getValue() );
                }
                scheduler.schedule( 
                    task.getName(), 
                    nexusTask, 
                    translateFrom( task.getSchedule() ), 
                    translateFrom( task.getProperties() ),
                    false );
            }
            catch ( ComponentLookupException e )
            {
                // this is bad, Plexus did not find the component, possibly the task.getType() contains bad class name
                getLogger().error( "Unable to initialize task " + task.getName() + ", couldn't load service class " + task.getId() );
            }            
        }   
    }
    
    /* (non-Javadoc)
     * @see org.sonatype.scheduling.TaskConfigManager#addTask(org.sonatype.scheduling.ScheduledTask)
     */
    public <T> void addTask( ScheduledTask<T> task )
    {
        synchronized ( config )
        {
            CScheduledTask foundTask = findTask( task.getId() );
            CScheduledTask storeableTask = translateFrom( task );

            if ( storeableTask != null )
            {
                if ( foundTask != null )
                {
                    config.removeTask( foundTask );
                }            
                config.addTask( storeableTask );
                storeConfig();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.sonatype.scheduling.TaskConfigManager#removeTask(org.sonatype.scheduling.ScheduledTask)
     */
    public <T> void removeTask( ScheduledTask<T> task )
    {
        synchronized ( config )
        {
            CScheduledTask foundTask = findTask( task.getId() );
            if ( foundTask != null )
            {
                config.removeTask( foundTask );
                storeConfig();
            }
        }
        // TODO: need to also add task to a history file
    }
    
    private CScheduledTask findTask( String id )
    {
        synchronized ( config )
        {
            for ( Iterator iter = config.getTasks().iterator(); iter.hasNext(); )
            {
                CScheduledTask storedTask = (CScheduledTask) iter.next();

                if ( storedTask.getId().equals( id ) )
                {
                    return storedTask;
                }
            }
            
            return null;
        }
    }
    
    private Map<String,String> translateFrom( List list )
    {
        Map<String,String> map = new HashMap<String,String>();
        
        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            CProps prop = ( CProps )iter.next();
            map.put( prop.getKey(), prop.getValue() );
        }
        
        return map;
    }
    
    private Schedule translateFrom( CSchedule modelSchedule )
    {
        Schedule schedule = null;
        
        if ( CAdvancedSchedule.class.isAssignableFrom( modelSchedule.getClass() ) )
        {
            schedule = new CronSchedule( ( ( CAdvancedSchedule ) modelSchedule ).getCronCommand() );
        }
        else if ( CMonthlySchedule.class.isAssignableFrom( modelSchedule.getClass() ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();
            
            for ( Iterator iter = ( ( CMonthlySchedule ) modelSchedule ).getDaysOfMonth().iterator(); iter.hasNext(); )
            {
                String day = ( String ) iter.next();
                
                try
                {
                    daysToRun.add( Integer.valueOf( day ) );
                }
                catch ( NumberFormatException nfe )
                {
                    getLogger().error( "Invalid day being added to monthly schedule - " + day + " - skipping.");
                }
            }
            
            schedule = new MonthlySchedule( ( ( CMonthlySchedule ) modelSchedule ).getStartDate(),
                                            ( ( CMonthlySchedule ) modelSchedule ).getEndDate(),
                                            daysToRun );
        }
        else if ( CWeeklySchedule.class.isAssignableFrom( modelSchedule.getClass() ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();
            
            for ( Iterator iter = ( ( CWeeklySchedule ) modelSchedule ).getDaysOfWeek().iterator(); iter.hasNext(); )
            {
                String day = ( String ) iter.next();
                
                try
                {
                    daysToRun.add( Integer.valueOf( day ) );
                }
                catch ( NumberFormatException nfe )
                {
                    getLogger().error( "Invalid day being added to weekly schedule - " + day + " - skipping.");
                }
            }
            
            schedule = new WeeklySchedule( ( ( CWeeklySchedule ) modelSchedule ).getStartDate(),
                                            ( ( CWeeklySchedule ) modelSchedule ).getEndDate(),
                                            daysToRun );
        }
        else if ( CDailySchedule.class.isAssignableFrom( modelSchedule.getClass() ) )
        {
            schedule = new DailySchedule( ( ( CDailySchedule ) modelSchedule ).getStartDate(),
                                            ( ( CDailySchedule ) modelSchedule ).getEndDate() );
        }
        else if ( COnceSchedule.class.isAssignableFrom( modelSchedule.getClass() ) )
        {
            schedule = new OnceSchedule( ( ( COnceSchedule ) modelSchedule ).getStartDate() );
        }
        
        return schedule;
    }

    private <T> CScheduledTask translateFrom( ScheduledTask<T> task )
    {
        CScheduledTask storeableTask = new CScheduledTask();

        storeableTask.setId( task.getId() );
        storeableTask.setName( task.getName() );
        storeableTask.setType( task.getType() );
        storeableTask.setStatus( task.getTaskState().name() );
        storeableTask.setLastRun( task.getLastRun() );
        storeableTask.setNextRun( task.getNextRun() );
        
        for ( String key : task.getTaskParams().keySet() )
        {
            CProps props = new CProps();
            props.setKey( key );
            props.setValue( task.getTaskParams().get( key ) );
            
            storeableTask.addProperty( props );
        }

        Schedule schedule = task.getSchedule();
        CSchedule storeableSchedule = null;

        if ( CronSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CAdvancedSchedule();
            ( (CAdvancedSchedule) storeableSchedule ).setCronCommand( ( (CronSchedule) schedule ).getCronExpression() );
        }
        else if ( DailySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CDailySchedule();
            ( (CDailySchedule) storeableSchedule ).setStartDate( ( (DailySchedule) schedule ).getStartDate() );
            ( (CDailySchedule) storeableSchedule ).setEndDate( ( (DailySchedule) schedule ).getEndDate() );
        }
        else if ( MonthlySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CMonthlySchedule();
            ( (CMonthlySchedule) storeableSchedule ).setStartDate( ( (MonthlySchedule) schedule ).getStartDate() );
            ( (CMonthlySchedule) storeableSchedule ).setEndDate( ( (MonthlySchedule) schedule ).getEndDate() );

            for ( Iterator iter = ( (MonthlySchedule) schedule ).getDaysToRun().iterator(); iter.hasNext(); )
            {
                // TODO: String.valueOf is used because currently the days to run are integers in the monthly schedule
                // needs to be string
                ( (CMonthlySchedule) storeableSchedule ).addDaysOfMonth( String.valueOf( iter.next() ) );
            }
        }
        else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new COnceSchedule();
            ( (COnceSchedule) storeableSchedule ).setStartDate( ( (OnceSchedule) schedule ).getStartDate() );
        }
        else if ( WeeklySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CWeeklySchedule();
            ( (CWeeklySchedule) storeableSchedule ).setStartDate( ( (WeeklySchedule) schedule ).getStartDate() );
            ( (CWeeklySchedule) storeableSchedule ).setEndDate( ( (WeeklySchedule) schedule ).getEndDate() );

            for ( Iterator iter = ( (WeeklySchedule) schedule ).getDaysToRun().iterator(); iter.hasNext(); )
            {
                // TODO: String.valueOf is used because currently the days to run are integers in the weekly schedule
                // needs to be string
                ( (CWeeklySchedule) storeableSchedule ).addDaysOfWeek( String.valueOf( iter.next() ) );
            }
        }

        storeableTask.setSchedule( storeableSchedule );

        return storeableTask;
    }

    private void storeConfig()
    {
        XStream xstream = configureXStream( new XStream() );

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream( configurationFile );
            xstream.toXML( config, fos );
            fos.flush();
        }
        catch ( FileNotFoundException e )
        {
            getLogger().error( "Unable to write to " + configurationFile.getAbsolutePath(), e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to write to " + configurationFile.getAbsolutePath(), e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( IOException e )
                {
                }
            }
        }
    }

    private void loadConfig()
    {
        XStream xstream = configureXStream( new XStream( new DomDriver() ) );

        config = new CTaskConfiguration();

        if ( !configurationFile.exists() )
        {
            loadDefaultConfig();
        }

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( configurationFile );
            xstream.fromXML( fis, config );
        }
        catch ( FileNotFoundException e )
        {
            getLogger().error( "Unable to read from " + configurationFile.getAbsolutePath(), e );
        }
        finally
        {
            if ( fis != null )
            {
                try
                {
                    fis.close();
                }
                catch ( IOException e )
                {
                }
            }
        }
    }

    private void loadDefaultConfig()
    {
        try
        {
            configurationFile.getParentFile().mkdirs();
            IOUtil.copy( getClass().getResourceAsStream( "/META-INF/nexus/tasks.xml" ), new FileOutputStream(
                configurationFile ) );
        }
        catch ( FileNotFoundException e )
        {
            getLogger().error( "Unable to write default configuration to " + configurationFile.getAbsolutePath(), e );
        }
        catch ( IOException e )
        {
            getLogger().error( "Unable to write default configuration to " + configurationFile.getAbsolutePath(), e );
        }
    }

    private XStream configureXStream( XStream xstream )
    {
        xstream.omitField( CTaskConfiguration.class, "modelEncoding" );
        xstream.omitField( CScheduledTask.class, "modelEncoding" );
        xstream.omitField( CSchedule.class, "modelEncoding" );
        xstream.omitField( CAdvancedSchedule.class, "modelEncoding" );
        xstream.omitField( CDailySchedule.class, "modelEncoding" );
        xstream.omitField( CMonthlySchedule.class, "modelEncoding" );
        xstream.omitField( COnceSchedule.class, "modelEncoding" );
        xstream.omitField( CWeeklySchedule.class, "modelEncoding" );
        xstream.omitField( CProps.class, "modelEncoding" );

        return xstream;
    }
}
