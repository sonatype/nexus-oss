/**
 * Sonatype Nexus™ [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.integrationtests.proxy.nexus262;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;


/**
 * One step above the Sample Test, this one adds a 'remote repository': <a href='https://docs.sonatype.com/display/NX/Nexus+Test-Harness'>Nexus Test-Harness</a>
 */
public class Nexus262SimpleProxyTest extends AbstractNexusProxyIntegrationTest
{

    public Nexus262SimpleProxyTest()
    {
        super( "release-proxy-repo-1" );
    }
    
    @Test
    public void downloadFromProxy() throws IOException
    {
        File localFile = this.getLocalFile( "release-proxy-repo-1", "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml" );
                                                                                              
        log.debug( "localFile: "+ localFile.getAbsolutePath() );
        
        File artifact = this.downloadArtifact( "simple.artifact", "simpleXMLArtifact", "1.0.0", "xml", null, "target/downloads" );
        
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( artifact, localFile ) );
    }

}
