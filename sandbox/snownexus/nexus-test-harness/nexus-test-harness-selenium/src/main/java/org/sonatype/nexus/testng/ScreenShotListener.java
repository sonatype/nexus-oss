package org.sonatype.nexus.testng;

import org.sonatype.nexus.mock.SeleniumTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class ScreenShotListener
    implements ITestListener
{

    public void onFinish( ITestContext context )
    {
    }

    public void onStart( ITestContext context )
    {
    }

    public void onTestFailedButWithinSuccessPercentage( ITestResult result )
    {
    }

    public void onTestFailure( ITestResult result )
    {
        Object[] tests = result.getTestClass().getInstances( false );
        for ( Object test : tests )
        {
            if ( test instanceof SeleniumTest )
            {
                ( (SeleniumTest) test ).captureNetworkTraffic();
                ( (SeleniumTest) test ).takeScreenshot( "Failure" );
            }
        }

    }

    public void onTestSkipped( ITestResult result )
    {
    }

    public void onTestStart( ITestResult result )
    {
    }

    public void onTestSuccess( ITestResult result )
    {
        Object[] tests = result.getTestClass().getInstances( false );
        for ( Object test : tests )
        {
            if ( test instanceof SeleniumTest )
            {
                ( (SeleniumTest) test ).captureNetworkTraffic();
            }
        }
    }

}
