package org.sonatype.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.model.CSchedule;
import org.sonatype.nexus.configuration.model.CScheduleAdvanced;
import org.sonatype.nexus.configuration.model.CScheduleDaily;
import org.sonatype.nexus.configuration.model.CScheduleMonthly;
import org.sonatype.nexus.configuration.model.CScheduleOnce;
import org.sonatype.nexus.configuration.model.CScheduleWeekly;
import org.sonatype.nexus.configuration.model.CTask;
import org.sonatype.nexus.configuration.model.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.CTaskScheduled;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The default implementation of the Task Configuration manager.  Will handle writing to and loading from
 * the tasks.xml file.  Will also load a default set of tasks if there is no existing configuration
 * 
 * @plexus.component
 */
public class DefaultTaskConfigManager 
    extends AbstractLogEnabled
    implements TaskConfigManager, Initializable
{    
    private CTaskConfiguration config;
   
    /**
     * The configuration file.
     * 
     * @plexus.configuration default-value="${apps}/nexus/conf/tasks.xml"
     */
    private File configurationFile;
    
    public void initialize()
        throws InitializationException
    {
        loadConfig();
    }
    
    public HashMap<String, List<ScheduledTask<?>>> getTasks()
    {
        synchronized( config )
        {
            HashMap<String, List<ScheduledTask<?>>> map = new HashMap<String, List<ScheduledTask<?>>>();
            
            for ( Iterator iter = config.getTasks().iterator(); iter.hasNext(); )
            {
                CTask storedTask = (CTask) iter.next();            
                
                if ( !map.containsKey( storedTask.getType() ))
                {
                    map.put( storedTask.getType() , new ArrayList<ScheduledTask<?>>() );
                }
                
                map.get( storedTask.getType() ).add( translateFrom( storedTask ) );
            }
            
            return map;
        }
    }
    
    public <T> void addTask( ScheduledTask<T> task )
    {
        synchronized( config )
        {
            CTask storeableTask = translateFrom( task );
            
            if ( storeableTask != null )
            {
                config.addTask( storeableTask );
                storeConfig();
            }
        }
    }
    
    public <T> void removeTask( ScheduledTask<T> task )
    {        
        synchronized( config )
        {
            for ( Iterator iter = config.getTasks().iterator(); iter.hasNext(); )
            {
                CTask storedTask = (CTask) iter.next();
                
                if ( storedTask.getId().equals( task.getId() ))
                {
                    iter.remove();
                    break;
                }
            }
            
            storeConfig();
        }
        //TODO: need to also add task to a history file
    }
    
    private <T> ScheduledTask<T> translateFrom( CTask task )
    {
        ScheduledTask<T> useableTask = null;
        
        //TODO: Need to complete translation
        
        if ( CTaskScheduled.class.isAssignableFrom( task.getClass() ) )
        {
        }
        else if ( CTask.class.isAssignableFrom( task.getClass() ) )
        {
        }
        
        return useableTask;
    }
    
    private <T> CTask translateFrom ( ScheduledTask<T> task )
    {
        CTask storeableTask = null;
        
        if ( ScheduledTask.class.isAssignableFrom( task.getClass() ) )
        {
            storeableTask = new CTaskScheduled();
            
            ( ( CTaskScheduled )storeableTask ).setLastRun( ( ( ScheduledTask<T> )task ).getLastRun() );
            ( ( CTaskScheduled )storeableTask ).setNextRun( ( ( ScheduledTask<T> )task ).getNextRun() );
            
            Schedule schedule = ( ( ScheduledTask<T> )task ).getSchedule();
            
            if ( CronSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                ( ( CronSchedule) schedule ).getCronExpression();
            }
            else if ( DailySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                ( ( DailySchedule) schedule ).getStartDate();
                ( ( DailySchedule) schedule ).getEndDate();
            }
            else if ( MonthlySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                ( ( MonthlySchedule) schedule ).getStartDate();
                ( ( MonthlySchedule) schedule ).getEndDate();
                ( ( MonthlySchedule) schedule ).getDaysToRun();
            }
            else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
                ( ( OnceSchedule) schedule ).getStartDate();
                ( ( OnceSchedule) schedule ).getEndDate();
            }
            else if ( WeeklySchedule.class.isAssignableFrom( schedule.getClass() ) )
            {
            }
        }
        
        if ( storeableTask != null )
        {
            storeableTask.setId( task.getId() );
            storeableTask.setType( task.getType() );
            task.getTaskState().name();
        }
        
        //TODO: need to complete translation
        
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
            IOUtil.copy( 
                getClass().getResourceAsStream( "/META-INF/nexus/tasks.xml" ), 
                new FileOutputStream( configurationFile ) );
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
        xstream.omitField( CTask.class, "modelEncoding" );
        xstream.omitField( CTaskScheduled.class, "modelEncoding" );
        xstream.omitField( CSchedule.class, "modelEncoding" );
        xstream.omitField( CScheduleAdvanced.class, "modelEncoding" );
        xstream.omitField( CScheduleDaily.class, "modelEncoding" );
        xstream.omitField( CScheduleMonthly.class, "modelEncoding" );
        xstream.omitField( CScheduleOnce.class, "modelEncoding" );
        xstream.omitField( CScheduleWeekly.class, "modelEncoding" );
        
        return xstream;
    }
}
