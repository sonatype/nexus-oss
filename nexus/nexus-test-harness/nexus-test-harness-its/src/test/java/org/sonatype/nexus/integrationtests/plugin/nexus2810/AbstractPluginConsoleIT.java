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
package org.sonatype.nexus.integrationtests.plugin.nexus2810;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.it.util.StringUtils;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.testng.Assert;

public abstract class AbstractPluginConsoleIT
    extends AbstractNexusIntegrationTest
{

    protected PluginConsoleMessageUtil pluginConsoleMsgUtil = new PluginConsoleMessageUtil();

    public AbstractPluginConsoleIT()
    {
        super();
    }

    public AbstractPluginConsoleIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    protected List<String> getPluginsNames( List<PluginInfoDTO> pluginInfos )
    {
        if ( pluginInfos == null )
        {
            return null;
        }
    
        List<String> names = new ArrayList<String>();
        for ( PluginInfoDTO pluginInfoDTO : pluginInfos )
        {
            names.add( pluginInfoDTO.getName() );
        }
        return names;
    }

    protected PluginInfoDTO getPluginInfoByName( List<PluginInfoDTO> pluginInfos, String name )
    {
        for ( PluginInfoDTO pluginInfo : pluginInfos )
        {
            if ( pluginInfo.getName().equals( name ) )
            {
                return pluginInfo;
            }
        }
    
        return null;
    }

    protected void assertPropertyValid( String name, String value, String... expectedValue )
    {
        if ( StringUtils.isEmpty( value ) )
        {
            Assert.fail( "Property '" + name + "' is empty!" );
        }
    
        if ( "N/A".equals( value ) )
        {
            Assert.fail( "Property '" + name + "' is N/A!" );
        }
    
        if ( expectedValue != null && expectedValue.length > 0 )
        {
            Assert.assertEquals( expectedValue[0], value );
        }
    }

}