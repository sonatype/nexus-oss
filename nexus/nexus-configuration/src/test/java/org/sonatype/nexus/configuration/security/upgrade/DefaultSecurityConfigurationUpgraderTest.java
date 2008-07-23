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
import java.io.StringWriter;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.security.model.Configuration;
import org.sonatype.nexus.configuration.security.model.io.xpp3.NexusSecurityConfigurationXpp3Writer;

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
        NexusSecurityConfigurationXpp3Writer w = new NexusSecurityConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write( sw, configuration );

        // System.out.println(sw.toString());

        String shouldBe = IOUtil.toString( getClass().getResourceAsStream( path + ".result" ) );

        assertEquals( shouldBe, sw.toString() );
    }
    
    public void testUpgrade()
        throws Exception
    {
        //We can test upgrading once an upgrade is necessary
        assertTrue( true );
    }
}
