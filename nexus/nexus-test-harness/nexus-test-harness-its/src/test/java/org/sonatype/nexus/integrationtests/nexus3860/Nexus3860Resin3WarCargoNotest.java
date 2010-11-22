package org.sonatype.nexus.integrationtests.nexus3860;

import java.io.File;

import org.testng.annotations.Test;

//FIXME http://jira.codehaus.org/browse/CARGO-494
@Test( enabled = false )
public class Nexus3860Resin3WarCargoNotest
    extends AbstractCargoIT
{
    @Override
    public File getContainerLocation()
    {
        return new File( "target/nexus/resin-3.1.10" );
    }

    @Override
    public String getContainer()
    {
        return "resin3x";
    }
}
