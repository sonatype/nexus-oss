package org.sonatype.nexus.integrationtests.report;

import java.io.PrintStream;

import org.apache.commons.io.output.NullOutputStream;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class ProgressListener
    extends TestListenerAdapter
{

    private PrintStream out;

    @Override
    public void onStart( ITestContext testContext )
    {
        super.onStart( testContext );

        out = System.out;
        System.setOut( new PrintStream( new NullOutputStream() ) );
    }

    @Override
    public void onFinish( ITestContext testContext )
    {
        super.onFinish( testContext );

        System.setOut( out );
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage( ITestResult tr )
    {
        super.onTestFailedButWithinSuccessPercentage( tr );

        showResult( tr, "partial success" );
    }

    @Override
    public void onTestFailure( ITestResult tr )
    {
        super.onTestFailure( tr );

        showResult( tr, "failed" );
    }

    @Override
    public void onTestSkipped( ITestResult tr )
    {
        super.onTestSkipped( tr );

        showResult( tr, "skipped" );
    }

    @Override
    public void onTestSuccess( ITestResult tr )
    {
        super.onTestSuccess( tr );

        showResult( tr, "success" );
    }

    private void showResult( ITestResult result, String status )
    {
        out.println( "Result: " + result.getTestClass().getName() + "." + result.getName() + "() ===> " + status );
    }

}
