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
package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;
import org.sonatype.nexus.configuration.application.upgrade.ApplicationConfigurationUpgrader;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;

public class DefaultApplicationConfigurationUpgraderTest
    extends AbstractNexusTestCase
{

    protected ApplicationConfigurationUpgrader configurationUpgrader;

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.cleanDirectory( new File( getNexusConfiguration() ).getParentFile() );

        this.configurationUpgrader = (ApplicationConfigurationUpgrader) lookup( ApplicationConfigurationUpgrader.ROLE );
    }

    protected void resultIsFine( String path, Configuration configuration )
        throws Exception
    {
        NexusConfigurationXpp3Writer w = new NexusConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write( sw, configuration );

        // System.out.println(sw.toString());

        String shouldBe = IOUtil.toString( getClass().getResourceAsStream( path + ".result" ) );

        assertEquals( shouldBe, sw.toString() );
    }

    public void testFromDEC()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-001-1.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 7, configuration.getRepositories().size() );

        assertEquals( 0, configuration.getRepositoryShadows().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-001-1.xml", configuration );
    }

    public void testFromDECDmz()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-001-2.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 11, configuration.getRepositories().size() );

        assertEquals( 0, configuration.getRepositoryShadows().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 3, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-001-2.xml", configuration );
    }

    public void testFromDECInt()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-001-3.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 7, configuration.getRepositories().size() );

        assertEquals( 0, configuration.getRepositoryShadows().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-001-3.xml", configuration );
    }

    public void testFrom100()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-100.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 7, configuration.getRepositories().size() );

        assertEquals( 0, configuration.getRepositoryShadows().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-100.xml", configuration );
    }

    public void testFrom101()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-101.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 15, configuration.getRepositories().size() );

        assertEquals( 2, configuration.getRepositoryShadows().size() );

        assertEquals( 5, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 4, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-101.xml", configuration );
    }

    public void testFrom103_1()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/103-1/nexus-103.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        // trick: copying by nexus.xml the tasks.xml too
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/103-1/tasks.xml" ),
            new FileOutputStream( new File( new File( getNexusConfiguration() ).getParentFile(), "tasks.xml" ) ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 6, configuration.getRepositories().size() );

        assertEquals( 1, configuration.getRepositoryShadows().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/103-1/nexus-103.xml", configuration );
    }

    public void testFrom103_2()
        throws Exception
    {
        // same as above, but we have no tasks.xml
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/103-2/nexus-103.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 6, configuration.getRepositories().size() );

        assertEquals( 1, configuration.getRepositoryShadows().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getRepositoryGroups().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/103-2/nexus-103.xml", configuration );
    }
    
    public void testFrom104()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-104.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-104.xml", configuration );
    }
    
    public void testFrom105()
        throws Exception
    {
        IOUtil.copy(
            getClass().getResourceAsStream( "/org/sonatype/nexus/configuration/upgrade/nexus-105.xml" ),
            new FileOutputStream( getNexusConfiguration() ) );
    
        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );
    
        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );
    
        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-105.xml", configuration );
    }
}
