package org.sonatype.nexus.proxy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public abstract class AbstractShadowRepositoryConfiguration
    extends AbstractRepositoryConfiguration
{
    private static final String MASTER_REPOSITORY_ID = "masterRepositoryId";

    private static final String SYNCHRONIZE_AT_STARTUP = "synchronizeAtStartup";
    
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
    
    public boolean isSynchronizeAtStartup()
    {
        return Boolean
            .parseBoolean( getNodeValue( getConfiguration( false ), SYNCHRONIZE_AT_STARTUP, Boolean.FALSE.toString() ) );
    }

    public void setSynchronizeAtStartup( boolean val )
    {
        setNodeValue( getConfiguration( true ), SYNCHRONIZE_AT_STARTUP, Boolean.toString( val ) );
    }
}
