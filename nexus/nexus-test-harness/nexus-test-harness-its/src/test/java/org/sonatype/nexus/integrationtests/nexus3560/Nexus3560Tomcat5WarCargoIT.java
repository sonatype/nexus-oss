package org.sonatype.nexus.integrationtests.nexus3560;

import java.io.File;

public class Nexus3560Tomcat5WarCargoIT
    extends AbstractCargoIT
{
    @Override
    public File getContainerLocation()
    {
        return new File( "target/nexus/apache-tomcat-5.5.31" );
    }

    @Override
    public String getContainer()
    {
        return "tomcat5x";
    }
}
