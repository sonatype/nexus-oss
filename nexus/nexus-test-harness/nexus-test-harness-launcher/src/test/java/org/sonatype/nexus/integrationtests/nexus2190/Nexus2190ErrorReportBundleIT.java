package org.sonatype.nexus.integrationtests.nexus2190;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.ErrorReportUtil;

public class Nexus2190ErrorReportBundleIT
    extends AbstractNexusIntegrationTest
{
    @Before
    public void cleanDirs()
        throws Exception
    {
        ErrorReportUtil.cleanErrorBundleDir( nexusWorkDir );
    }
    
    @Test
    public void validateBundle()
        throws Exception
    {
        RequestFacade.sendMessage( "service/local/exception", Method.POST, null );
        
        ErrorReportUtil.validateZipContents( nexusWorkDir );
    }
}

