package org.sonatype.nexus.plugins.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.plugins.model.io.xpp3.NexusPluginXpp3Writer;

public class ModelTest
    extends PlexusTestCase
{
    public void testSimple()
        throws IOException
    {
        PluginMetadata pd = new PluginMetadata();

        pd.setGroupId( "org.sonatype.nexus.plugins" );
        pd.setArtifactId( "sample-plugin" );
        pd.setVersion( "1.0.0" );

        NexusPluginXpp3Writer w = new NexusPluginXpp3Writer();

        File testOutput = new File( "target/plugin.xml" );

        FileWriter fw = new FileWriter( testOutput );

        w.write( fw, pd );

        fw.flush();

        fw.close();
    }

}
