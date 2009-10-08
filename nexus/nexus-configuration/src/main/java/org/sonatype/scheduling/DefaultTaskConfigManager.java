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

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.AbstractConfigurable;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CScheduleConfig;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CScheduledTaskCoreConfiguration;
import org.sonatype.nexus.scheduling.NexusTask;
import org.sonatype.nexus.scheduling.TaskUtils;
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
 * The default implementation of the Task Configuration manager. Will handle writing to and loading from the tasks
 * within nexus.xml file.
 */
@Component ( role = TaskConfigManager.class )
public class DefaultTaskConfigManager
    extends AbstractConfigurable
    implements TaskConfigManager
{

    @Requirement
    private Logger logger;

    /**
     * The app config holding tasks.
     */
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    /**
     * Plexus.
     */
    @Requirement
    private PlexusContainer plexusContainer;

    protected PlexusContainer getPlexusContainer()
    {
        return plexusContainer;
    }

    // ==

    protected Logger getLogger()
    {
        return logger;
    }

    // ==

    public void initializeConfiguration()
        throws ConfigurationException
    {
        if ( getApplicationConfiguration().getConfigurationModel() != null )
        {
            configure( getApplicationConfiguration() );
        }
    }

    @Override
    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    @Override
    protected Configurator getConfigurator()
    {
        return null;
    }

    @Override
    protected List<CScheduledTask> getCurrentConfiguration( boolean forWrite )
    {
        return ( (CScheduledTaskCoreConfiguration) getCurrentCoreConfiguration() ).getConfiguration( forWrite );
    }

    @Override
    protected CoreConfiguration wrapConfiguration( Object configuration )
        throws ConfigurationException
    {
        if ( configuration instanceof ApplicationConfiguration )
        {
            return new CScheduledTaskCoreConfiguration( (ApplicationConfiguration) configuration );
        }
        else
        {
            throw new ConfigurationException( "The passed configuration object is of class \""
                                              + configuration.getClass().getName() + "\" and not the required \""
                                              + ApplicationConfiguration.class.getName() + "\"!"
            );
        }
    }

    // ==

    public void initializeTasks( Scheduler scheduler )
    {
        List<CScheduledTask> tasks = new ArrayList<CScheduledTask>( getCurrentConfiguration( false ) );

        if ( tasks != null )
        {
            List<CScheduledTask> tempList = new ArrayList<CScheduledTask>( tasks );

            getLogger().info( tempList.size() + " task(s) to load." );

            for ( CScheduledTask task : tempList )
            {
                getLogger().info( "Loading task - " + task.getName() );

                try
                {
                    SchedulerTask<?> nexusTask = createTaskInstance( task.getType() );

                    for ( Iterator iter = task.getProperties().iterator(); iter.hasNext(); )
                    {
                        CProps prop = (CProps) iter.next();
                        nexusTask.addParameter( prop.getKey(), prop.getValue() );
                    }

                    TaskUtils.setId( nexusTask, task.getId() );
                    TaskUtils.setName( nexusTask, task.getName() );

                    

                    DefaultScheduledTask<?> scheduledTask = ( DefaultScheduledTask<?> ) scheduler.initialize( task.getId(), task.getName(), task.getType(), nexusTask,
                                          translateFrom( task.getSchedule(), new Date( task.getNextRun() ) ) );
                    
                    scheduledTask.setEnabled( task.isEnabled() );
                    scheduledTask.setLastRun( new Date( task.getLastRun() ) );
                }
                catch ( IllegalArgumentException e )
                {
                    // this is bad, Plexus did not find the component, possibly the task.getType() contains bad class
                    // name
                    getLogger().warn(
                        "Unable to initialize task " + task.getName() + ", couldn't load service class "
                        + task.getId(), e
                    );
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
            List<CScheduledTask> tasks = getCurrentConfiguration( true );

            CScheduledTask foundTask = findTask( task.getId(), tasks );

            CScheduledTask storeableTask = translateFrom( task );

            if ( storeableTask != null )
            {
                if ( foundTask != null )
                {
                    tasks.remove( foundTask );
                    
                    storeableTask.setLastRun( foundTask.getLastRun() );
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
            List<CScheduledTask> tasks = getCurrentConfiguration( true );

            CScheduledTask foundTask = findTask( task.getId(), tasks );

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

    public SchedulerTask<?> createTaskInstance( String taskType )
        throws IllegalArgumentException
    {
        try
        {
            return (SchedulerTask<?>) getPlexusContainer().lookup( SchedulerTask.class, taskType );
        }
        catch ( ComponentLookupException ignore )
        {
            try
            {
                return (SchedulerTask<?>) getPlexusContainer().lookup( NexusTask.class, taskType );
            }
            catch ( ComponentLookupException e )
            {
                throw new IllegalArgumentException( "Could not create task of type" + taskType, e );
            }
        }
    }

    public <T> T createTaskInstance( Class<T> taskType )
        throws IllegalArgumentException
    {
        // the convention is to use the simple class name as the plexus hint
        return (T) createTaskInstance( taskType.getSimpleName() );
    }

    // ==

    private CScheduledTask findTask( String id, List<CScheduledTask> tasks )
    {
        synchronized ( applicationConfiguration )
        {
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

    private Schedule translateFrom( CScheduleConfig modelSchedule, Date nextRun )
    {
        Schedule schedule = null;

        Date startDate = null;
        Date endDate = null;
        
        if ( modelSchedule.getStartDate() > 0 )
        {
            startDate = new Date( modelSchedule.getStartDate() );
        }
        
        if ( modelSchedule.getEndDate() > 0 )
        {
            endDate = new Date( modelSchedule.getEndDate() );
        }

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

            schedule = new MonthlySchedule( startDate, endDate, daysToRun );
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

            schedule = new WeeklySchedule( startDate, endDate, daysToRun );
        }
        else if ( CScheduleConfig.TYPE_DAILY.equals( modelSchedule.getType() ) )
        {
            schedule = new DailySchedule( startDate, endDate );
        }
        else if ( CScheduleConfig.TYPE_HOURLY.equals( modelSchedule.getType() ) )
        {
            schedule = new HourlySchedule( startDate, endDate );
        }
        else if ( CScheduleConfig.TYPE_ONCE.equals( modelSchedule.getType() ) )
        {
            schedule = new OnceSchedule( startDate );
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

        if ( nextRun != null )
        {
            schedule.getIterator().resetFrom( nextRun );
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

        if ( task.getLastRun() != null )
        {
            storeableTask.setLastRun( task.getLastRun().getTime() );
        }

        if ( task.getNextRun() != null )
        {
            storeableTask.setNextRun( task.getNextRun().getTime() );
        }

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

                storeableSchedule.setStartDate( ( (MonthlySchedule) schedule ).getStartDate().getTime() );

                Date endDate = ( (MonthlySchedule) schedule ).getEndDate();

                if ( endDate != null )
                {
                    storeableSchedule.setEndDate( endDate.getTime() );
                }

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

                storeableSchedule.setStartDate( ( (WeeklySchedule) schedule ).getStartDate().getTime() );

                Date endDate = ( (WeeklySchedule) schedule ).getEndDate();

                if ( endDate != null )
                {
                    storeableSchedule.setEndDate( endDate.getTime() );
                }

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

                storeableSchedule.setStartDate( ( (DailySchedule) schedule ).getStartDate().getTime() );

                Date endDate = ( (DailySchedule) schedule ).getEndDate();

                if ( endDate != null )
                {
                    storeableSchedule.setEndDate( endDate.getTime() );
                }
            }
            else if ( HourlySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_HOURLY );

                storeableSchedule.setStartDate( ( (HourlySchedule) schedule ).getStartDate().getTime() );

                Date endDate = ( (HourlySchedule) schedule ).getEndDate();

                if ( endDate != null )
                {
                    storeableSchedule.setEndDate( endDate.getTime() );
                }
            }
            else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                storeableSchedule.setType( CScheduleConfig.TYPE_ONCE );

                storeableSchedule.setStartDate( ( (OnceSchedule) schedule ).getStartDate().getTime() );
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

    public String getName()
    {
        return "Scheduled Task Configuration";
    }

}
