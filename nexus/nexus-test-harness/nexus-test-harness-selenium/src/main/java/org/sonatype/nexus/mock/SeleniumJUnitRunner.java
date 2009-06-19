package org.sonatype.nexus.mock;

import static org.sonatype.nexus.mock.TestContext.RESOURCES_DIR;
import static org.sonatype.nexus.mock.TestContext.RESOURCES_SOURCE_DIR;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.TestProperties;

/**
 * <p>
 * A special JUnit runner that provides additional behaviors specific to Selenium test cases. This runner adds the
 * following behavior:
 * </p>
 * <ul>
 * <li>The test class gets it's JUnit description injected prior to test evaluation (see
 * {@link SeleniumTest#setDescription(org.junit.runner.Description)}).</li>
 * <li>If a test fails, a screenshot is automatically taken with the name "FAILURE" appended to the end of it.</li>
 * </ul>
 * <p>
 * NOTE: This runner only works for tests that extend {@link SeleniumTest}.
 * </p>
 */
public class SeleniumJUnitRunner
    extends BlockJUnit4ClassRunner
{

    protected static Logger log = Logger.getLogger( SeleniumJUnitRunner.class );

    public SeleniumJUnitRunner( Class<?> klass )
        throws InitializationError
    {
        super( klass );
    }

    @Override
    protected Statement methodInvoker( FrameworkMethod method, Object test )
    {
        if ( !( test instanceof SeleniumTest ) )
        {
            throw new RuntimeException( "Only works with SeleniumTest" );
        }

        final SeleniumTest stc = ( (SeleniumTest) test );
        stc.setDescription( describeChild( method ) );

        return new InvokeMethod( method, test )
        {
            @Override
            public void evaluate()
                throws Throwable
            {
                try
                {
                    super.evaluate();
                }
                catch ( Throwable throwable )
                {
                    stc.takeScreenshot( "FAILURE" );
                    throw throwable;
                }
                finally
                {
                    stc.captureNetworkTraffic();
                }
            }
        };
    }

    @Override
    public void run( RunNotifier notifier )
    {
        // org.sonatype.nexus.selenium.nexus1962.Nexus1962TaskTest
        String testName = getDescription().getDisplayName();

        String testId = testName.substring( 0, testName.lastIndexOf( '.' ) );
        testId = testId.substring( testId.lastIndexOf( '.' ) + 1 );
        TestContext.setTestId( testId );

        try
        {
            copyTestResources();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }

        super.run( notifier );

        TestContext.setTestId( null );
    }

    protected void copyTestResources()
        throws IOException
    {
        File source = new File( RESOURCES_SOURCE_DIR, TestContext.getTestId() );
        if ( !source.exists() )
        {
            return;
        }

        File destination = new File( RESOURCES_DIR, TestContext.getTestId() );

        FileTestingUtils.interpolationDirectoryCopy( source, destination, TestProperties.getAll() );
    }

}
