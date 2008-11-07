package org.sonatype.nexus.integrationtests;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.sonatype.nexus.test.utils.TestSuiteUtil;

@RunWith( Suite.class )
@SuiteClasses( { IntegrationTestSuiteClasses.class, IntegrationTestSuiteClassesSecurity.class } )
public class IntegrationTestSuite
{
    //

    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {
        TestSuiteUtil.startNexus();
    }

    @AfterClass
    public static void afterSuite()
        throws Exception
    {
        TestSuiteUtil.stopNexus();
    }

}
