package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractProxyRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String PROXY_MODE = "proxyMode";

    private static final String REMOTE_STATUS_CHECK_MODE = "remoteStatusCheckMode";

    private static final String ITEM_MAX_AGE = "itemMaxAge";

    private static final String ITEM_AGING_ACTIVE = "itemAgingActive";

    public AbstractProxyRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public ProxyMode getProxyMode()
    {
        return ProxyMode.valueOf( getNodeValue( getRootNode(), PROXY_MODE, ProxyMode.ALLOW.toString() ) );
    }

    public void setProxyMode( ProxyMode mode )
    {
        setNodeValue( getRootNode(), PROXY_MODE, mode.toString() );
    }

    public RepositoryStatusCheckMode getRepositoryStatusCheckMode()
    {
        return RepositoryStatusCheckMode.valueOf( getNodeValue( getRootNode(), REMOTE_STATUS_CHECK_MODE,
            RepositoryStatusCheckMode.AUTO_BLOCKED_ONLY.toString() ) );
    }

    public void setRepositoryStatusCheckMode( RepositoryStatusCheckMode mode )
    {
        setNodeValue( getRootNode(), REMOTE_STATUS_CHECK_MODE, mode.toString() );
    }

    public int getItemMaxAge()
    {
        return Integer.parseInt( getNodeValue( getRootNode(), ITEM_MAX_AGE, "1440" ) );
    }

    public void setItemMaxAge( int age )
    {
        setNodeValue( getRootNode(), ITEM_MAX_AGE, String.valueOf( age ) );
    }

    public boolean isItemAgingActive()
    {
        return Boolean.parseBoolean( getNodeValue( getRootNode(), ITEM_AGING_ACTIVE, Boolean.TRUE.toString() ) );
    }

    public void setItemAgingActive( boolean value )
    {
        setNodeValue( getRootNode(), ITEM_AGING_ACTIVE, Boolean.toString( value ) );
    }
}
