package org.sonatype.nexus.test;

import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.codehaus.plexus.logging.AbstractLogEnabled;



public class NexusTestSuite extends AbstractLogEnabled
{
    private TestSuite suite = new TestSuite();
    public NexusTestSuite()
    {
        suite.addTest( new NexusDownloadTest() );
    }
    
    public void testIntegration()
    {
        TestResult result = new TestResult();
        suite.run( result );
        
        if (result.errorCount() > 0)
        {
            getLogger().error( "Some tests have failed" );
        }
        
        try
        {
            Thread.sleep( 20000 );
        }
        catch ( InterruptedException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
