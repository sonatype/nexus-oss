package org.sonatype.scheduling;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.sonatype.nexus.configuration.AbstractNexusTestCase;
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
import org.sonatype.scheduling.schedules.Schedule;
import org.sonatype.scheduling.schedules.WeeklySchedule;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DefaultTaskConfigManagerTest
    extends AbstractNexusTestCase
{
    private DefaultScheduler defaultScheduler;

    private DefaultTaskConfigManager defaultManager;

    private File configurationFile;

    /** 
     * NOTE: this is only populated after call to loadConfig()
     */
    private CTaskConfiguration configuration;

    public void setUp()
        throws Exception
    {
        super.setUp();

        defaultScheduler = (DefaultScheduler) lookup( Scheduler.class.getName() );
        defaultManager = (DefaultTaskConfigManager) lookup( TaskConfigManager.class.getName() );
        configurationFile = new File( PLEXUS_HOME + "/nexus/conf/tasks.xml" );
    }

    public void testStoreOnceSchedule()
    {
        ScheduledTask<Integer> task = createScheduledTask( createSchedule( "once" ) );
        defaultManager.addTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 1 );
        assertTrue( "test".equals( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getName() ) );
        assertTrue( COnceSchedule.class.isAssignableFrom( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getSchedule().getClass() ) );
        defaultManager.removeTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 0 );
    }

    public void testStoreDailySchedule()
    {
        ScheduledTask<Integer> task = createScheduledTask( createSchedule( "daily" ) );
        defaultManager.addTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 1 );
        assertTrue( "test".equals( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getName() ) );
        assertTrue( CDailySchedule.class.isAssignableFrom( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getSchedule().getClass() ) );
        defaultManager.removeTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 0 );
    }

    public void testStoreWeeklySchedule()
    {
        ScheduledTask<Integer> task = createScheduledTask( createSchedule( "weekly" ) );
        defaultManager.addTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 1 );
        assertTrue( "test".equals( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getName() ) );
        assertTrue( CWeeklySchedule.class.isAssignableFrom( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getSchedule().getClass() ) );
        defaultManager.removeTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 0 );
    }

    public void testStoreMonthlySchedule()
    {
        ScheduledTask<Integer> task = createScheduledTask( createSchedule( "monthly" ) );
        defaultManager.addTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 1 );
        assertTrue( "test".equals( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getName() ) );
        assertTrue( CMonthlySchedule.class.isAssignableFrom( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getSchedule().getClass() ) );
        defaultManager.removeTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 0 );
    }

    public void testStoreAdvancedSchedule()
    {
        ScheduledTask<Integer> task = createScheduledTask( createSchedule( "advanced" ) );
        defaultManager.addTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 1 );
        assertTrue( "test".equals( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getName() ) );
        assertTrue( CAdvancedSchedule.class.isAssignableFrom( ( (CScheduledTask) configuration.getTasks().get( 0 ) ).getSchedule().getClass() ) );
        defaultManager.removeTask( task );
        loadConfig();
        assertTrue( configuration.getTasks().size() == 0 );
    }

    private Schedule createSchedule( String type )
    {
        if ( "once".equals( type ) )
        {
            return new OnceSchedule( new Date() );
        }
        else if ( "daily".equals( type ) )
        {
            return new DailySchedule( new Date(), new Date() );
        }
        else if ( "weekly".equals( type ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();
            daysToRun.add( new Integer( 0 ) );
            return new WeeklySchedule( new Date(), new Date(), daysToRun );
        }
        else if ( "monthly".equals( type ) )
        {
            Set<Integer> daysToRun = new HashSet<Integer>();
            daysToRun.add( new Integer( 1 ) );
            return new MonthlySchedule( new Date(), new Date(), daysToRun );
        }
        else if ( "advanced".equals( type ) )
        {
            return new CronSchedule( "someexpression" );
        }

        return null;
    }

    private ScheduledTask<Integer> createScheduledTask( Schedule schedule )
    {
        TestCallable callable = new TestCallable();
        return new DefaultScheduledTask<Integer>( "test", callable.getClass().getName(), defaultScheduler, callable,
                                                  schedule );
    }

    private void loadConfig()
    {
        XStream xstream = configureXStream( new XStream( new DomDriver() ) );

        configuration = new CTaskConfiguration();

        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream( configurationFile );
            xstream.fromXML( fis, configuration );
        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
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

    public class TestCallable
        implements Callable<Integer>
    {
        private int runCount = 0;

        public Integer call()
            throws Exception
        {
            return runCount++;
        }

        public int getRunCount()
        {
            return runCount;
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
