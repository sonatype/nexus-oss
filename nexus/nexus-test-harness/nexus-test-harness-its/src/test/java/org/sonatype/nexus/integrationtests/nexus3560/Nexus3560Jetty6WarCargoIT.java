package org.sonatype.nexus.integrationtests.nexus3560;

import java.io.File;

public class Nexus3560Jetty6WarCargoIT
    extends AbstractCargoIT
{
    @Override
    public File getContainerLocation()
    {
        return new File( "target/nexus/jetty-6.1.26" );
    }

    @Override
    public String getContainer()
    {
        return "jetty6x";
    }
}
