package org.sonatype.nexus.integrationtests.nexus3860;

import java.io.File;

public class Nexus3860Jetty6WarCargoIT
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
