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
package org.sonatype.nexus.configuration.application.upgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.TimeZone;

import junit.framework.Assert;

import org.apache.log4j.lf5.util.StreamUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.AbstractNexusTestCase;
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

        this.configurationUpgrader = (ApplicationConfigurationUpgrader) lookup( ApplicationConfigurationUpgrader.class );
    }

    protected void resultIsFine( String path, Configuration configuration )
        throws Exception
    {
        NexusConfigurationXpp3Writer w = new NexusConfigurationXpp3Writer();

        StringWriter sw = new StringWriter();

        w.write( sw, configuration );

        // System.out.println(sw.toString());

        String shouldBe = IOUtil.toString( getClass().getResourceAsStream( path + ".result" ) );

        if ( !StringUtils.equals( shouldBe, sw.toString() ) )
        {
            // write the file out so we can have something to compare

            File expected = FileUtils.toFile( getClass().getResource( path + ".result" ) );
            File actual = new File( "target", expected.getName().replaceFirst( "result", "actual" ) );
            FileOutputStream out = new FileOutputStream( actual );
            try
            {
                IOUtil.copy( sw.toString(), out );
            }
            finally
            {
                IOUtil.close( out );
            }
            String diffMessage = "diff "+ expected.getAbsolutePath() + " "+ actual.getAbsolutePath();
            String message = "Files differ, you can manually diff them:\n" +diffMessage;

            // the method makes the error pretty, so we can keep it.
            assertEquals( message, shouldBe, sw.toString() );
        }
    }

    public void testFromDEC()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-001-1.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 7 + 2, configuration.getRepositories().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-001-1.xml", configuration );
    }

    public void testFromDECDmz()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-001-2.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        // 11 repos, 2 groups
        assertEquals( 11 + 2, configuration.getRepositories().size() );

        assertEquals( 3, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-001-2.xml", configuration );
    }

    public void testFromDECInt()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-001-3.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        assertEquals( 7 + 2, configuration.getRepositories().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-001-3.xml", configuration );
    }

    public void testFrom100()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-100.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        // 7 repos and 2 groups
        assertEquals( 7 + 2, configuration.getRepositories().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-100.xml", configuration );
    }

    public void testFrom101()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-101.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        // 15 repos, 5 groups, 2 shadows
        assertEquals( 15 + 5 + 2, configuration.getRepositories().size() );

        assertEquals( 4, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-101.xml", configuration );
    }

    public void testFrom103_1()
        throws Exception
    {
        TimeZone defaultTZ = TimeZone.getDefault();

        // use UTC for this test
        TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );

        copyFromClasspathToFile(
            "/org/sonatype/nexus/configuration/upgrade/103-1/nexus-103.xml",
            getNexusConfiguration() );

        // trick: copying by nexus.xml the tasks.xml too
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/103-1/tasks.xml", new File( new File(
            getNexusConfiguration() ).getParentFile(), "tasks.xml" ) );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        // set back to the default timezone
        TimeZone.setDefault( defaultTZ );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        // 6 repos 3 groups
        assertEquals( 6 + 3, configuration.getRepositories().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/103-1/nexus-103.xml", configuration );
    }

    public void testFrom103_2()
        throws Exception
    {
        // same as above, but we have no tasks.xml
        copyFromClasspathToFile(
            "/org/sonatype/nexus/configuration/upgrade/103-2/nexus-103.xml",
            getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        // 6 repos, 1 shadow, 2 groups
        assertEquals( 6 + 1 + 2, configuration.getRepositories().size() );

        assertEquals( 2, configuration.getRepositoryGrouping().getPathMappings().size() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/103-2/nexus-103.xml", configuration );
    }

    public void testFrom104()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-104.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-104.xml", configuration );
    }

    public void testFrom105()
        throws Exception
    {
        copyFromClasspathToFile( "/org/sonatype/nexus/configuration/upgrade/nexus-105.xml", getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus-105.xml", configuration );
    }

    public void testNEXUS1710()
        throws Exception
    {
        copyFromClasspathToFile(
            "/org/sonatype/nexus/configuration/upgrade/nexus1710/nexus.xml",
            getNexusConfiguration() );

        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );

        assertEquals( Configuration.MODEL_VERSION, configuration.getVersion() );

        resultIsFine( "/org/sonatype/nexus/configuration/upgrade/nexus1710/nexus.xml", configuration );
    }
    
//    public void testUpgradeStaticConfig()
//    throws Exception
//    {
//        copyFromClasspathToFile( "/META-INF/nexus/nexus.xml", getNexusConfiguration() );
//        
//        Configuration configuration = configurationUpgrader.loadOldConfiguration( new File( getNexusConfiguration() ) );
//
//        Assert.assertNotNull( configuration );
//        
//        NexusConfigurationXpp3Writer w = new NexusConfigurationXpp3Writer();
//
//        StringWriter sw = new StringWriter();
//
//        w.write( sw, configuration );
//        
//        File actual = new File( "target", "upgraded-nexus.xml" );
//        FileOutputStream out = new FileOutputStream( actual );
//        try
//        {
//            IOUtil.copy( sw.toString(), out );
//        }
//        finally
//        {
//            IOUtil.close( out );
//        }
//        
//    }
}
