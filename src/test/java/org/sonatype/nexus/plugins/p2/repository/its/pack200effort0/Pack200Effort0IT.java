package org.sonatype.nexus.plugins.p2.repository.its.pack200effort0;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.sonatype.nexus.plugins.p2.repository.its.AbstractNexusProxyP2IT;
import org.testng.annotations.Test;

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
        downloadFile(
            new URL( getNexusTestRepoUrl() + "empty.jar.pack.gz" ),
            new File( "target/pack200effort0.jar.pack.gz" ).getCanonicalPath()
        );
    }

}
