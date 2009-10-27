package org.sonatype.nexus.integrationtests.nexus2379;

import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.test.utils.ErrorReportUtil;

public class Nexus2379MultipleErrorReportIT
    extends AbstractNexusIntegrationTest
{
    @Before
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