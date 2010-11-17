package org.sonatype.nexus.integrationtests.nexus3560;

import java.io.File;

public class Nexus3560Tomcat6WarCargoIT
    extends AbstractCargoIT
{
    @Override
    public File getContainerLocation()
    {
        return new File( "target/nexus/apache-tomcat-6.0.29" );
    }

    @Override
    public String getContainer()
    {
        return "tomcat6x";
    }
}
