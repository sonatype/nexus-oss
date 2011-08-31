package org.sonatype.nexus.plugins.p2.repository.its.pack200effort0;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.junit.Test;
import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;

public class Pack200Effort0IT
    extends AbstractNexusProxyP2IT
{

    public Pack200Effort0IT()
    {
        super( "pack200effort0" );
    }

    @Test
    public void pack200effort0()
        throws IOException
    {
        File target = new File( "target/pack200effort0.jar.pack.gz" ).getCanonicalFile();

        downloadFile( new URL( getNexusTestRepoUrl() + "empty.jar.pack.gz" ), target.getCanonicalPath() );
    }
}
