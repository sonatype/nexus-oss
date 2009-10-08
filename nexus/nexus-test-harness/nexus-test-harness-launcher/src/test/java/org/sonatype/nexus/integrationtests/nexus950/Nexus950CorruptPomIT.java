/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus950;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus950CorruptPomIT
    extends AbstractNexusIntegrationTest
{

    @Test
    public void uploadCorruptPomTest() throws HttpException, IOException
    {
        
        File jarFile = this.getTestFile( "bad-pom.jar" );
        File badPomFile = this.getTestFile( "pom.xml" );
        
        HttpMethod resultMethod = DeployUtils.deployUsingPomWithRestReturnResult( this.getTestRepositoryId(), jarFile, badPomFile, "", "jar" );
        
        Assert.assertEquals( "Expected a 400 error returned.", resultMethod.getStatusCode(), 400 );        
    }
    
}
