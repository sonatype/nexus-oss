package org.sonatype.nexus.integrationtests.nexus3560;

import java.io.File;

public class Nexus3560Jetty7WarCargoIT
    extends AbstractCargoIT
{
    @Override
    public File getContainerLocation()
    {
        return new File( "target/nexus/jetty-distribution-7.2.0.v20101020" );
    }

    @Override
    public String getContainer()
    {
        return "jetty7x";
    }
}
