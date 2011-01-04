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
package org.sonatype.nexus.plugins.plugin.console.error.reporting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.nexus.error.report.ErrorReportBundleContentContributor;
import org.sonatype.nexus.error.report.ErrorReportBundleEntry;
import org.sonatype.nexus.plugins.plugin.console.PluginConsoleManager;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;

import com.thoughtworks.xstream.XStream;

@Component( role = ErrorReportBundleContentContributor.class, hint = "pluginList" )
public class PluginListErrorReportBundleContentContributor
    extends AbstractLogEnabled
    implements ErrorReportBundleContentContributor
{

    @Requirement
    private PluginConsoleManager pluginConsoleManager;

    public ErrorReportBundleEntry[] getEntries()
    {
        List<PluginInfo> l = pluginConsoleManager.listPluginInfo();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        XStream xs = new XStream();
        xs.alias( "PluginInfo", PluginInfo.class );
        xs.omitField( PluginInfo.class, "restInfos" );
        xs.toXML( l, bos );

        return new ErrorReportBundleEntry[] {//
        new ErrorReportBundleEntry( "PluginsInfo.xml", new ByteArrayInputStream( bos.toByteArray() ) ) //
        };
    }
}
