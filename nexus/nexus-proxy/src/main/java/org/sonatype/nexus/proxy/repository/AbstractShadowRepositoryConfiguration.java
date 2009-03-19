package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractShadowRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MASTER_REPOSITORY_ID = "masterRepositoryId";

    public AbstractShadowRepositoryConfiguration( Xpp3Dom configuration )
    {
        super( configuration );
    }

    public String getMasterRepositoryId()
    {
        return getNodeValue( getConfiguration( false ), MASTER_REPOSITORY_ID, null );
    }

    public void setMasterRepositoryId( String id )
    {
        setNodeValue( getConfiguration( true ), MASTER_REPOSITORY_ID, id );
    }
}
