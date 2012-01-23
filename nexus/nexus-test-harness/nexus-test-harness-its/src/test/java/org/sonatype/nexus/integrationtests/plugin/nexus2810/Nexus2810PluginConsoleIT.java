/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.it.util.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.hamcrest.MatcherAssert;
import static org.hamcrest.Matchers.*;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus2810PluginConsoleIT
    extends AbstractPluginConsoleIT
{

    @Override
    protected void copyTestResources()
        throws IOException
    {
        super.copyTestResources();

        File source = this.getTestFile( "broken-plugin" );

        File desti = new File( this.getNexusBaseDir(), RELATIVE_PLUGIN_REPOSITORY_DIR );

        FileUtils.copyDirectoryStructure( source, desti );
    }

    @Test
    public void testListPluginInfos()
        throws Exception
    {
        List<PluginInfoDTO> pluginInfos = pluginConsoleMsgUtil.listPluginInfos();

        MatcherAssert.assertThat( getPluginsNames( pluginInfos ),
            hasItems( "Nexus : Core Plugins : Plugin Console", "Nexus Broken Plugin" ) );

        PluginInfoDTO pluginConsolePlugin =
            this.getPluginInfoByName( pluginInfos, "Nexus : Core Plugins : Plugin Console" );
        assertPropertyValid( "Name", pluginConsolePlugin.getName(), "Nexus : Core Plugins : Plugin Console" );
        assertPropertyValid( "Version", pluginConsolePlugin.getVersion() );
        assertPropertyValid( "Description", pluginConsolePlugin.getDescription(), "Adds a UI to view installed plugins." );
        assertPropertyValid( "Status", pluginConsolePlugin.getStatus(), "ACTIVATED" );
        if ( new File( "./.svn" ).exists() )
        {
            assertPropertyValid( "SCM Version", pluginConsolePlugin.getScmVersion() );
            assertPropertyValid( "SCM Timestamp", pluginConsolePlugin.getScmTimestamp() );
        }
        assertPropertyValid( "Site", pluginConsolePlugin.getSite() );
        Assert.assertTrue( StringUtils.isEmpty( pluginConsolePlugin.getFailureReason() ) );
        Assert.assertTrue( !pluginConsolePlugin.getRestInfos().isEmpty() );

        PluginInfoDTO pgpPlugin = this.getPluginInfoByName( pluginInfos, "Nexus Broken Plugin" );
        assertPropertyValid( "Name", pgpPlugin.getName() );
        assertPropertyValid( "Version", pgpPlugin.getVersion() );
        assertPropertyValid( "Status", pgpPlugin.getStatus(), "BROKEN" );
        Assert.assertNull( pgpPlugin.getDescription() );
        Assert.assertEquals( "N/A", pgpPlugin.getScmVersion() );
        Assert.assertEquals( "N/A", pgpPlugin.getScmTimestamp() );
        assertPropertyValid( "Site", pgpPlugin.getSite() );
        Assert.assertFalse( StringUtils.isEmpty( pgpPlugin.getFailureReason() ) );
        Assert.assertTrue( pgpPlugin.getRestInfos().isEmpty() );
    }
}
