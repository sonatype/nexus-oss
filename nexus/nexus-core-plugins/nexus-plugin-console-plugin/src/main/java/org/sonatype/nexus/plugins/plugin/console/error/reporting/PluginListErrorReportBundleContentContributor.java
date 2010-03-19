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
