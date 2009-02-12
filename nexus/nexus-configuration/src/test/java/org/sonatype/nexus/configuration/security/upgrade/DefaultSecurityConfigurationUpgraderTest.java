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
package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.StringWriter;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.jsecurity.model.Configuration;
import org.sonatype.jsecurity.model.io.xpp3.SecurityConfigurationXpp3Writer;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;

public class DefaultSecurityConfigurationUpgraderTest
    extends AbstractNexusTestCase
{

    protected SecurityConfigurationUpgrader configurationUpgrader;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.cleanDirectory( new File( getSecurityConfiguration() ).getParentFile() );

        this.configurationUpgrader = (SecurityConfigurationUpgrader) lookup( SecurityConfigurationUpgrader.class );
    }

    protected void resultIsFine( String path, Configuration configuration )
        throws Exception
    {
        SecurityConfigurationXpp3Writer w = new SecurityConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write( sw, configuration );

        String shouldBe = IOUtil.toString( getClass().getResourceAsStream( path + ".result" ) );
        
        assertEquals( shouldBe, sw.toString() );
    }

    public void testFrom100()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/nexus/configuration/security/upgrade/security-100.xml",
            getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/security/upgrade/security-100.xml", configuration );
    }

    public void testFrom100Part2()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/nexus/configuration/security/upgrade/security-100-2.xml",
            getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/security/upgrade/security-100-2.xml", configuration );
    }

    public void testFrom201to202()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/nexus/configuration/security/upgrade/security-100-2.xml",
            getSecurityConfiguration() );

        Configuration configuration = configurationUpgrader
            .loadOldConfiguration( new File( getSecurityConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/security/upgrade/security-100-2.xml", configuration );
    }
}
