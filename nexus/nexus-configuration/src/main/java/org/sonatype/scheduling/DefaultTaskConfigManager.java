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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.proxy.events.AbstractEvent;
import org.sonatype.nexus.proxy.events.EventListener;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.HourlySchedule;
import org.sonatype.scheduling.schedules.ManualRunSchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

/**
 * The default implementation of the Task Configuration manager. Will handle writing to and loading from the tasks.xml
 * file. Will also load a default set of tasks if there is no existing configuration
 */
@Component( role = TaskConfigManager.class )
public class DefaultTaskConfigManager
    extends AbstractLogEnabled
    implements TaskConfigManager, Initializable, Contextualizable, EventListener
{
    /**
     * The app config holding tasks.
     */
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    /**
     * Plexus.
     */
    private PlexusContainer plexusContainer;

    public void contextualize( Context ctx )
        throws ContextException
    {
        this.plexusContainer = (PlexusContainer) ctx.get( PlexusConstants.PLEXUS_KEY );
    }

    public void initialize()
        throws InitializationException
    {
        applicationConfiguration.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent e )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( e.getClass() ) )
        {
            // TODO
        }
    }

    public void initializeTasks( Scheduler scheduler )
    {
        List<CScheduledTask> tasks = new ArrayList<CScheduledTask>( applicationConfiguration
            .getConfiguration().getTasks() );

        if ( tasks != null )
        {
            List<CScheduledTask> tempList = new ArrayList<CScheduledTask>( tasks );

            getLogger().info( tempList.size() + " task(s) to load." );

            for ( CScheduledTask task : tempList )
            {
                getLogger().info( "Loading task - " + task.getName() );

                try
                {
                    SchedulerTask<?> nexusTask = (SchedulerTask<?>) plexusContainer.lookup( SchedulerTask.class, task
                        .getType() );

                    for ( Iterator iter = task.getProperties().iterator(); iter.hasNext(); )
                    {
                        CProps prop = (CProps) iter.next();
                        nexusTask.addParameter( prop.getKey(), prop.getValue() );
                    }

                    scheduler.initialize(
                        task.getId(),
                        task.getName(),
                        task.getType(),
                        nexusTask,
                        translateFrom( task.getSchedule(), task.getNextRun() ),
                        translateFrom( task.getProperties() ) ).setEnabled( task.isEnabled() );
                }
                catch ( ComponentLookupException e )
                {
                    // this is bad, Plexus did not find the component, possibly the task.getType() contains bad class
                    // name
                    getLogger()
                        .error(
                            "Unable to initialize task " + task.getName() + ", couldn't load service class "
                                + task.getId() );
                }
            }
        }

    }

    public <T> void addTask( ScheduledTask<T> task )
    {
        // RunNowSchedules are not saved
        if ( RunNowSchedule.class.isAssignableFrom( task.getSchedule().getClass() ) )
        {
            return;
        }

        synchronized ( applicationConfiguration )
        {
            List<CScheduledTask> tasks = applicationConfiguration.getConfiguration().getTasks();

            CScheduledTask foundTask = findTask( task.getId() );

            CScheduledTask storeableTask = translateFrom( task );

            if ( storeableTask != null )
            {
                if ( foundTask != null )
                {
                    tasks.remove( foundTask );
                }

                tasks.add( storeableTask );

                try
                {
                    applicationConfiguration.saveConfiguration();
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Could not save task changes!", e );
                }
            }
        }
    }

    public <T> void removeTask( ScheduledTask<T> task )
    {
        synchronized ( applicationConfiguration )
        {
            List<CScheduledTask> tasks = applicationConfiguration.getConfiguration().getTasks();

            CScheduledTask foundTask = findTask( task.getId() );

            if ( foundTask != null )
            {
                tasks.remove( foundTask );

                try
                {
                    applicationConfiguration.saveConfiguration();
                }
                catch ( IOException e )
                {
                    getLogger().warn( "Could not save task changes!", e );
                }
            }
        }
        // TODO: need to also add task to a history file
    }

    private CScheduledTask findTask( String id )
    {
        synchronized ( applicationConfiguration )
        {
            List<CScheduledTask> tasks = applicationConfiguration.getConfiguration().getTasks();

            for ( Iterator<CScheduledTask> iter = tasks.iterator(); iter.hasNext(); )
            {
                CScheduledTask storedTask = iter.next();

                if ( storedTask.getId().equals( id ) )
                {
                    return storedTask;
                }
            }

            return null;
        }
    }

    private Map<String, String> translateFrom( List list )
    {
        Map<String, String> map = new HashMap<String, String>();

        for ( Iterator iter = list.iterator(); iter.hasNext(); )
        {
            CProps prop = (CProps) iter.next();

            map.put( prop.getKey(), prop.getValue() );
        }

        return map;
    }

    private Schedule translateFrom( CScheduleConfig modelSchedule, Date lastRun )
    {
        Schedule schedule = null;

        if ( CScheduleConfig.TYPE_ADVANCED.equals( modelSchedule.getType() ) )
        {
            try
            {
                schedule = new CronSchedule( modelSchedule.getCronCommand() );
            }
            catch ( ParseException e )
            {
                // this will not happen, since it was persisted, hence already submitted
            }
        }
        else if ( CScheduleConfig.TYPE_MONTHLY.equals( modelSchedule.getType() ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();

            for ( Iterator iter = modelSchedule.getDaysOfMonth().iterator(); iter.hasNext(); )
            {
                String day = (String) iter.next();

                try
                {
                    daysToRun.add( Integer.valueOf( day ) );
                }
                catch ( NumberFormatException nfe )
                {
                    getLogger().error( "Invalid day being added to monthly schedule - " + day + " - skipping." );
                }
            }

            schedule = new MonthlySchedule( modelSchedule.getStartDate(), modelSchedule.getEndDate(), daysToRun );
        }
        else if ( CScheduleConfig.TYPE_WEEKLY.equals( modelSchedule.getType() ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();

            for ( Iterator iter = modelSchedule.getDaysOfWeek().iterator(); iter.hasNext(); )
            {
                String day = (String) iter.next();

                try
                {
                    daysToRun.add( Integer.valueOf( day ) );
                }
                catch ( NumberFormatException nfe )
                {
                    getLogger().error( "Invalid day being added to weekly schedule - " + day + " - skipping." );
                }
            }

            schedule = new WeeklySchedule( modelSchedule.getStartDate(), modelSchedule.getEndDate(), daysToRun );
        }
        else if ( CScheduleConfig.TYPE_DAILY.equals( modelSchedule.getType() ) )
        {
            schedule = new DailySchedule( modelSchedule.getStartDate(), modelSchedule.getEndDate() );
        }
        else if ( CScheduleConfig.TYPE_HOURLY.equals( modelSchedule.getType() ) )
        {
            schedule = new HourlySchedule( modelSchedule.getStartDate(), modelSchedule.getEndDate() );
        }
        else if ( CScheduleConfig.TYPE_ONCE.equals( modelSchedule.getType() ) )
        {
            schedule = new OnceSchedule( modelSchedule.getStartDate() );
        }
        else if ( CScheduleConfig.TYPE_RUN_NOW.equals( modelSchedule.getType() ) )
        {
            schedule = new RunNowSchedule();
        }
        else if ( CScheduleConfig.TYPE_MANUAL.equals( modelSchedule.getType() ) )
        {
            schedule = new ManualRunSchedule();
        }
        else
        {
            throw new IllegalArgumentException( "Unknown Schedule type: " + modelSchedule.getClass().getName() );
        }

        if ( lastRun != null )
        {
            schedule.getIterator().resetFrom( lastRun );
        }

        return schedule;
    }

    private <T> CScheduledTask translateFrom( ScheduledTask<T> task )
    {
        CScheduledTask storeableTask = new CScheduledTask();

        storeableTask.setEnabled( task.isEnabled() );
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
        CScheduleConfig storeableSchedule = new CScheduleConfig();

        if ( schedule != null )
        {
            if ( CronSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_ADVANCED );

                storeableSchedule.setCronCommand( ( (CronSchedule) schedule ).getCronString() );
            }
            else if ( MonthlySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_MONTHLY );

                storeableSchedule.setStartDate( ( (MonthlySchedule) schedule ).getStartDate() );

                storeableSchedule.setEndDate( ( (MonthlySchedule) schedule ).getEndDate() );

                for ( Iterator iter = ( (MonthlySchedule) schedule ).getDaysToRun().iterator(); iter.hasNext(); )
                {
                    // TODO: String.valueOf is used because currently the days to run are integers in the monthly
                    // schedule
                    // needs to be string
                    storeableSchedule.addDaysOfMonth( String.valueOf( iter.next() ) );
                }
            }
            else if ( WeeklySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_WEEKLY );

                storeableSchedule.setStartDate( ( (WeeklySchedule) schedule ).getStartDate() );

                storeableSchedule.setEndDate( ( (WeeklySchedule) schedule ).getEndDate() );

                for ( Iterator iter = ( (WeeklySchedule) schedule ).getDaysToRun().iterator(); iter.hasNext(); )
                {
                    // TODO: String.valueOf is used because currently the days to run are integers in the weekly
                    // schedule
                    // needs to be string
                    storeableSchedule.addDaysOfWeek( String.valueOf( iter.next() ) );
                }
            }
            else if ( DailySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_DAILY );

                storeableSchedule.setStartDate( ( (DailySchedule) schedule ).getStartDate() );

                storeableSchedule.setEndDate( ( (DailySchedule) schedule ).getEndDate() );
            }
            else if ( HourlySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_HOURLY );
                
                storeableSchedule.setStartDate( ( (HourlySchedule) schedule ).getStartDate() );
                
                storeableSchedule.setEndDate( ( (HourlySchedule) schedule ).getEndDate() );
            }
            else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_ONCE );

                storeableSchedule.setStartDate( ( (OnceSchedule) schedule ).getStartDate() );
            }
            else if ( RunNowSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_RUN_NOW );
            }
            else if ( ManualRunSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_MANUAL );
            }
            else
            {
                throw new IllegalArgumentException( "Unknown Schedule type: " + schedule.getClass().getName() );
            }
        }

        storeableTask.setSchedule( storeableSchedule );

        return storeableTask;
    }

}
