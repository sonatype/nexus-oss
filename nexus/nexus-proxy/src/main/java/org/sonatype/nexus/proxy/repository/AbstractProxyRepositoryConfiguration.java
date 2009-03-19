package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class AbstractProxyRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String PROXY_MODE = "proxyMode";

    private static final String REMOTE_STATUS_CHECK_MODE = "remoteStatusCheckMode";

    private static final String ITEM_MAX_AGE = "itemMaxAge";

    public AbstractProxyRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public ProxyMode getProxyMode()
    {
        return ProxyMode.valueOf( getNodeValue( getConfiguration(), PROXY_MODE, ProxyMode.ALLOW.toString() ) );
    }

    public void setProxyMode( ProxyMode mode )
    {
        setNodeValue( getConfiguration(), PROXY_MODE, mode.toString() );
    }

    public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
    {
        return RepositoryStatusCheckMode.valueOf( getNodeValue(
            getConfiguration(),
            REMOTE_STATUS_CHECK_MODE,
            RepositoryStatusCheckMode.AUTO_BLOCKED_ONLY.toString() ) );
    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        setNodeValue( getConfiguration(), REMOTE_STATUS_CHECK_MODE, mode.toString() );
    }

    public int getItemMaxAge()
    {
        return Integer.parseInt( getNodeValue( getConfiguration(), ITEM_MAX_AGE, "1440" ) );
    }

    public void setItemMaxAge( int age )
    {
        setNodeValue( getConfiguration(), ITEM_MAX_AGE, String.valueOf( age ) );
    }
}
