package org.sonatype.nexus.test;

import junit.framework.TestSuite;


public class NexusTestSuite extends TestSuite
{
    public NexusTestSuite()
    {
        addTest( new NexusDownloadTest() );
    }
}
