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
package org.sonatype.nexus.jsecurity;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;

public class DefaultNexusSecurityUpgradeTest
    extends PlexusTestCase
{

    private static final String ORG_CONFIG_FILE = "target/test-classes/org/sonatype/nexus/jsecurity/security.xml";

    private final String workDir = "target/DefaultNexusSecurityUpgradeTest/work/";
    
    private final String configLocation = workDir+"conf/security.xml";

    
    
    public void testDoUpgrade() throws Exception
    {
        NexusSecurity nexusSecurity = this.lookup( NexusSecurity.class );
    }
    
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy the file to a different location because we are going to change it
        FileUtils.copyFile( new File( ORG_CONFIG_FILE ), new File( configLocation ) );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "nexus-work", workDir );
        
//        context.put( "security-xml-file", COPY_CONFIG_FILE );
    }

}
