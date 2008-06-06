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
import org.sonatype.nexus.configuration.model.CAdvancedSchedule;
import org.sonatype.nexus.configuration.model.CDailySchedule;
import org.sonatype.nexus.configuration.model.CMonthlySchedule;
import org.sonatype.nexus.configuration.model.COnceSchedule;
import org.sonatype.nexus.configuration.model.CProps;
import org.sonatype.nexus.configuration.model.CSchedule;
import org.sonatype.nexus.configuration.model.CScheduledTask;
import org.sonatype.nexus.configuration.model.CTaskConfiguration;
import org.sonatype.nexus.configuration.model.CWeeklySchedule;
import org.sonatype.scheduling.schedules.CronSchedule;
import org.sonatype.scheduling.schedules.DailySchedule;
import org.sonatype.scheduling.schedules.MonthlySchedule;
import org.sonatype.scheduling.schedules.OnceSchedule;
import org.sonatype.scheduling.schedules.RunNowSchedule;
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
                CScheduledTask storedTask = (CScheduledTask) iter.next();            
                
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
            CScheduledTask storeableTask = translateFrom( task );
            
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
                CScheduledTask storedTask = (CScheduledTask) iter.next();
                
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
    
    private <T> ScheduledTask<T> translateFrom( CScheduledTask task )
    {
        ScheduledTask<T> useableTask = null;
        
        //TODO: Need to complete translation
        
        return useableTask;
    }
    
    private <T> CScheduledTask translateFrom ( ScheduledTask<T> task )
    {
        //Run now doesn't get stored
        if ( RunNowSchedule.class.isAssignableFrom( task.getSchedule().getClass() ))
        {
            return null;
        }
        
        CScheduledTask storeableTask = new CScheduledTask();
        
        storeableTask.setId( task.getId() );
        storeableTask.setName( task.getName() );
        storeableTask.setType( task.getType() );
        storeableTask.setStatus( task.getTaskState().name() );
        storeableTask.setLastRun( task.getLastRun() );
        storeableTask.setNextRun( task.getNextRun() );
        
        Schedule schedule = task.getSchedule();
        CSchedule storeableSchedule = null;
        
        if ( CronSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CAdvancedSchedule();
            ( ( CAdvancedSchedule) storeableSchedule ).setCronCommand( ( ( CronSchedule) schedule ).getCronExpression() );
        }
        else if ( DailySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CDailySchedule();
            ( ( CDailySchedule) storeableSchedule ).setStartDate( ( ( DailySchedule) schedule ).getStartDate() );
            ( ( CDailySchedule) storeableSchedule ).setEndDate( ( ( DailySchedule) schedule ).getEndDate() );
        }
        else if ( MonthlySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CMonthlySchedule();
            ( ( CMonthlySchedule) storeableSchedule ).setStartDate( ( ( MonthlySchedule) schedule ).getStartDate() );
            ( ( CMonthlySchedule) storeableSchedule ).setEndDate( ( ( MonthlySchedule) schedule ).getEndDate() );
            
            for ( Iterator iter = ( ( MonthlySchedule) schedule ).getDaysToRun().iterator(); iter.hasNext(); )
            {
                //TODO: String.valueOf is used because currently the days to run are integers in the monthly schedule
                //needs to be string
                ( ( CMonthlySchedule) storeableSchedule ).addDaysOfMonth( String.valueOf( iter.next() ) );
            }
        }
        else if ( OnceSchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new COnceSchedule();            
            ( ( COnceSchedule) storeableSchedule ).setStartDate( ( ( OnceSchedule) schedule ).getStartDate() );
        }
        else if ( WeeklySchedule.class.isAssignableFrom( schedule.getClass() ) )
        {
            storeableSchedule = new CWeeklySchedule();
            ( ( CWeeklySchedule) storeableSchedule ).setStartDate( ( ( WeeklySchedule) schedule ).getStartDate() );
            ( ( CWeeklySchedule) storeableSchedule ).setEndDate( ( ( WeeklySchedule) schedule ).getEndDate() );
            
            for ( Iterator iter = ( ( WeeklySchedule) schedule ).getDaysToRun().iterator(); iter.hasNext(); )
            {
                //TODO: String.valueOf is used because currently the days to run are integers in the weekly schedule
                //needs to be string
                ( ( CWeeklySchedule) storeableSchedule ).addDaysOfWeek( String.valueOf( iter.next() ) );
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
