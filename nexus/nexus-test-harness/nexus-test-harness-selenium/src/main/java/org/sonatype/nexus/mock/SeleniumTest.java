package org.sonatype.nexus.mock;

import static com.thoughtworks.selenium.grid.tools.ThreadSafeSeleniumSessionStorage.startSeleniumSession;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.pages.MainPage;
import org.sonatype.nexus.mock.util.PropUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.sonatype.nexus.testng.PlexusObjectFactory;
import org.sonatype.spice.jscoverage.JsonReportHandler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.grid.tools.ThreadSafeSeleniumSessionStorage;

@Test( sequential = true )
public abstract class SeleniumTest
    extends NexusMockTestCase

{
    protected Selenium selenium;

    protected MainPage main;

    private static JsonReportHandler handler;

    @BeforeClass
    public void createSelenium()
        throws Exception
    {
        if ( ( selenium = ThreadSafeSeleniumSessionStorage.session() ) == null )
        {
            final String seleniumServer = PropUtil.get( "seleniumServer", "localhost" );
            final int seleniumPort = PropUtil.get( "seleniumPort", 4444 );
            final String seleniumBrowser = PropUtil.get( "seleniumBrowser", "*firefox" );
            startSeleniumSession( seleniumServer, seleniumPort, seleniumBrowser,
                                  TestProperties.getString( "nexus.base.url" ) );

            selenium = ThreadSafeSeleniumSessionStorage.session();
            if ( !seleniumBrowser.contains( "iexplore" ) )
            {
                selenium.setSpeed( "500" );
            }
            selenium.getEval( "window.moveTo(1,1); window.resizeTo(1021,737);" );
        }

        main = new MainPage( selenium );
    }

    @BeforeMethod
    public void loadUrl()
        throws Exception
    {
        selenium.open( "/nexus" );

        // sometimes the browser window just froze between tasks
        try
        {
            selenium.getEval( "window.isRunning()" );
        }
        catch ( Exception e )
        {
            selenium.open( "/nexus" );

            selenium.getEval( "window.isRunning()" );
        }

    }

    @AfterMethod( alwaysRun = true )
    public void logout()
        throws Exception
    {
        if ( selenium != null )
        {
            main.clickLogout();
            getCoverage();

        }
    }

    /**
     * Takes a screenshot of the browser and saves it to the target/screenshots directory. The exact name of the file is
     * based on the currently executing test class and method name plus the line number of the source code that called
     * this method.
     * 
     * @throws java.io.IOException If the screenshot could not be taken.
     */
    protected void takeScreenshot()
        throws IOException
    {
        StackTraceElement ste = new Exception().getStackTrace()[1];
        takeScreenshot( "line-" + ste.getLineNumber() );
    }

    /**
     * Takes a screenshot of the browser and saves it to the target/screenshots directory. The name is a combination of
     * the currently executing test class and method name, plus the name parameterized supplied when calling this
     * method.
     * 
     * @param name A specific name to append to the screenshot file name.
     * @throws IOException If the screenshot could not be taken.
     */
    @Test( enabled = false )
    public void takeScreenshot( String name )
    {
        File parent = new File( "target/screenshots/" );
        // noinspection ResultOfMethodCallIgnored
        parent.mkdirs();

        String screen = selenium.captureScreenshotToString();
        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream( new File( parent, testId + "-" + name + ".png" ) );
            fos.write( Base64.decodeBase64( screen.getBytes() ) );
            fos.close();
        }
        catch ( IOException e )
        {
            log.error( e.getMessage(), e );
        }
    }

    @Test( enabled = false )
    public void captureNetworkTraffic()
    {
        try
        {
            File parent = new File( "target/network-traffic/" );
            // noinspection ResultOfMethodCallIgnored
            parent.mkdirs();

            FileOutputStream fos = new FileOutputStream( new File( parent, testId + ".txt" ) );
            fos.write( selenium.captureNetworkTraffic( "TODO" ).getBytes( "UTF-8" ) );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage(), e );
        }
    }

    @BeforeSuite
    public void coverageInit()
        throws ComponentLookupException
    {
        handler = lookup( JsonReportHandler.class );
    }

    protected void getCoverage()
        throws ComponentLookupException, IOException
    {
        handler.appendResults( selenium.getEval( "window.jscoverage_serializeCoverageToJSON()" ) );
        handler.persist();
    }

    protected void doLogin()
    {
        doLogin( User.ADMIN.getUsername(), User.ADMIN.getPassword() );
    }

    protected void doLogin( String username, String password )
    {
        selenium.runScript( "window.Sonatype.utils.doLogin( null, '" + username + "', '" + password + "');" );
    }

    @AfterClass
    public void cleanInstance()
        throws Exception
    {

        saveLogs();

        System.out.println( "Memory... free: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " - total: "
            + Runtime.getRuntime().totalMemory() / 1024 / 1024 );

        PlexusObjectFactory.getContainer().release( this );
        cleanFields();

        System.gc();
        System.out.println( "Memory gc... free: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " - total: "
            + Runtime.getRuntime().totalMemory() / 1024 / 1024 );

    }

    private void saveLogs()
    {
        File logsDir = new File( "./target/logs" );

        if ( logsDir.exists() )
        {
            File testLogs = new File( logsDir, testId );
            testLogs.mkdirs();

            File[] logs = logsDir.listFiles();
            for ( File log : logs )
            {
                try
                {
                    FileUtils.copyFile( log, new File( testLogs, log.getName() + testName ) );

                    FileUtils.writeStringToFile( log, "" );
                }
                catch ( IOException e )
                {
                    NexusMockTestCase.log.error( e.getMessage(), e );
                }
            }
        }
    }

    private void cleanFields()
        throws IllegalArgumentException, IllegalAccessException
    {
        List<Field> fields = getFields( getClass() );
        for ( Field field : fields )
        {
            if ( Modifier.isStatic( field.getModifiers() ) || Modifier.isFinal( field.getModifiers() ) )
            {
                continue;
            }

            field.setAccessible( true );
            if ( field.getDeclaringClass().isPrimitive() )
            {
                field.set( this, 0 );
            }
            else
            {
                field.set( this, null );
            }
        }
    }

    private List<Field> getFields( Class<?> clazz )
    {
        if ( clazz == null )
        {
            return Collections.emptyList();
        }

        Field[] f = clazz.getDeclaredFields();
        List<Field> fields = new ArrayList<Field>();
        fields.addAll( Arrays.asList( f ) );
        fields.addAll( getFields( clazz.getSuperclass() ) );
        return fields;
    }

    // profiling with yourkit, activate using -P youtkit-profile
    private static Object profiler;

    @BeforeSuite
    public void startProfiler()
    {
        Class<?> controllerClazz;
        try
        {
            controllerClazz = Class.forName( "com.yourkit.api.Controller" );
        }
        catch ( Exception e )
        {
            log.info( "Profiler not present" );
            return;
        }

        try
        {
            profiler = controllerClazz.newInstance();
            controllerClazz.getMethod( "captureMemorySnapshot" ).invoke( profiler );
        }
        catch ( Exception e )
        {
            Assert.fail( "Profiler was active, but failed due: " + e.getMessage(), e );
        }
    }

    @AfterMethod
    public void takeSnapshot()
    {
        if ( profiler != null )
        {
            try
            {
                profiler.getClass().getMethod( "forceGC" ).invoke( profiler );
                profiler.getClass().getMethod( "captureMemorySnapshot" ).invoke( profiler );
            }
            catch ( Exception e )
            {
                Assert.fail( "Profiler was active, but failed due: " + e.getMessage(), e );
            }
        }
    }

}
