/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus1375;

import java.io.File;

import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.LogConfigResource;
import org.sonatype.nexus.test.utils.LogConfigMessageUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author juven
 */
public class Nexus1375LogConfigIT
    extends AbstractNexusIntegrationTest
{

    protected LogConfigMessageUtil messageUtil;

    @BeforeClass
    public void setSecureTest()
    {
        messageUtil = new LogConfigMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );

        TestContainer.getInstance().getTestContext().setSecureTest( true );

        TestContainer.getInstance().getTestContext().useAdminForRequests();
    }

    @Test
    public void getLogConfig()
        throws Exception
    {
        LogConfigResource resource = messageUtil.getLogConfig();

        Assert.assertEquals( "DEBUG", resource.getRootLoggerLevel() );

        Assert.assertEquals( "logfile, record", resource.getRootLoggerAppenders() );

        Assert.assertEquals( "%4d{yyyy-MM-dd HH:mm:ss} %-5p [%-15.15t] - %c - %m%n", resource.getFileAppenderPattern() );

        File actualLoggerLocation = new File( resource.getFileAppenderLocation() ).getCanonicalFile();

        Assert.assertTrue( nexusLog.getAbsoluteFile().equals( actualLoggerLocation.getAbsoluteFile() ) );
    }

    @Test
    public void updateLogConfig()
        throws Exception
    {
        LogConfigResource resource = messageUtil.getLogConfig();

        Assert.assertEquals( "DEBUG", resource.getRootLoggerLevel() );
        Assert.assertEquals( "logfile, record", resource.getRootLoggerAppenders() );

        resource.setRootLoggerLevel( "ERROR" );

        messageUtil.updateLogConfig( resource );

        Assert.assertEquals( "ERROR", resource.getRootLoggerLevel() );

        resource.setRootLoggerLevel( "DEBUG" );

        messageUtil.updateLogConfig( resource );

        Assert.assertEquals( "DEBUG", resource.getRootLoggerLevel() );
    }
}
