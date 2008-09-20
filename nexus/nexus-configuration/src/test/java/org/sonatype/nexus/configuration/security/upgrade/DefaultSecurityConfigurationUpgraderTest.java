/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.configuration.security.upgrade;

import java.io.File;
import java.io.FileOutputStream;
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
        
        this.configurationUpgrader = ( SecurityConfigurationUpgrader ) lookup( SecurityConfigurationUpgrader.ROLE );
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
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/security/upgrade/security-100.xml" ),
            new FileOutputStream( getSecurityConfiguration() ) );
    
        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getSecurityConfiguration() ) );
    
        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );
    
        resultIsFine( "/org/sonatype/nexus/configuration/security/upgrade/security-100.xml", configuration );
    }
}
