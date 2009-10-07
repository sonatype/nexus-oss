package org.sonatype.nexus.mock;


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
    //extends BlockJUnit4ClassRunner
{
/*
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
                //TODO
                    stc.takeScreenshot( "FAILURE" );
                    throw throwable;
                }
                finally
                {
                //TODO
                    stc.captureNetworkTraffic();
                }
            }
        };
    }
*/



}
