package org.sonatype.nexus.plugin.deploy;

import java.io.IOException;

public interface Zapper
{
    void deployDirectory( ZapperRequest zapperRequest )
        throws IOException;
}
