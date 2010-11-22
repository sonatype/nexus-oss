package org.sonatype.nexus.integrationtests.nexus3860;

import java.io.File;

public class Nexus3860Tomcat5WarCargoIT
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
