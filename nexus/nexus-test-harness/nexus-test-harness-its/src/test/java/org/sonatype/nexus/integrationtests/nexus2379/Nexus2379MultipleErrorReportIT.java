package org.sonatype.nexus.integrationtests.nexus2379;

import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.ErrorReportUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus2379MultipleErrorReportIT
    extends AbstractNexusIntegrationTest
{
    @BeforeMethod
    public void cleanDirs()
        throws Exception
    {
        ErrorReportUtil.cleanErrorBundleDir( nexusWorkDir );
    }
    
    @Test
    public void validateMultipleErrors()
        throws Exception
    {        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateZipContents( nexusWorkDir );
        
        ErrorReportUtil.cleanErrorBundleDir( nexusWorkDir );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=500", Method.GET, null );
        
        ErrorReportUtil.validateNoZip( nexusWorkDir );
        
        RequestFacade.sendMessage( "service/local/exception?status=501", Method.GET, null );
        
        ErrorReportUtil.validateZipContents( nexusWorkDir );
    }
}