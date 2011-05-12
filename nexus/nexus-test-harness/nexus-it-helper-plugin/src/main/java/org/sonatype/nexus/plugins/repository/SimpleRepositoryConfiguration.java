package org.sonatype.nexus.plugins.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.repository.AbstractRepositoryConfiguration;

public class SimpleRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    public static final String HELLO_SAID_COUNT = "helloSaidCount";

    public SimpleRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public int getSaidHelloCount()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), HELLO_SAID_COUNT, "0" ) );
    }

    public void setSaidHelloCount( int cnt )
    {
        setNodeValue( getRootNode(), HELLO_SAID_COUNT, String.valueOf( cnt ) );
    }
}
