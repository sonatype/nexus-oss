package org.sonatype.nexus.configuration.model;

import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractCoreConfiguration
    implements CoreConfiguration
{
    private final XStream xstream = new XStream();

    private Object originalConfiguration;

    private Object changedConfiguration;

    public AbstractCoreConfiguration( Object configuration )
    {
        setOriginalConfiguration( configuration );
    }

    public Object getConfiguration( boolean forWrite )
    {
        if ( forWrite )
        {
            if ( getOriginalConfiguration() != null && getChangedConfiguration() == null )
            {
                // copy it
                setChangedConfiguration( copyObject( getOriginalConfiguration() ) );

                copyTransients( getOriginalConfiguration(), getChangedConfiguration() );
            }

            return getChangedConfiguration();
        }
        else
        {
            return getOriginalConfiguration();
        }
    }

    protected XStream getXStream()
    {
        return xstream;
    }

    protected Object getOriginalConfiguration()
    {
        return originalConfiguration;
    }

    public void setOriginalConfiguration( Object originalConfiguration )
    {
        this.originalConfiguration = originalConfiguration;
    }

    protected Object getChangedConfiguration()
    {
        return changedConfiguration;
    }

    public void setChangedConfiguration( Object changedConfiguration )
    {
        this.changedConfiguration = changedConfiguration;
    }

    protected Object copyObject( Object source )
    {
        // nice, isn't it?
        return getXStream().fromXML( getXStream().toXML( source ) );
    }

    protected abstract void copyTransients( Object source, Object destination );

    public abstract ExternalConfiguration getExternalConfiguration();

    protected boolean isThisDirty()
    {
        return getChangedConfiguration() != null;
    }

    public boolean isDirty()
    {
        return isThisDirty() || ( getExternalConfiguration() != null && getExternalConfiguration().isDirty() );
    }

    public void commitChanges()
    {
        if ( isThisDirty() )
        {
            // nice, isn't it?
            setOriginalConfiguration( getXStream().fromXML( getXStream().toXML( getChangedConfiguration() ),
                                                            getOriginalConfiguration() ) );

            copyTransients( getChangedConfiguration(), getOriginalConfiguration() );

            setChangedConfiguration( null );
        }

        if ( getExternalConfiguration() != null && getExternalConfiguration().isDirty() )
        {
            getExternalConfiguration().commitChanges();
        }
    }

    public void rollbackChanges()
    {
        if ( isThisDirty() )
        {
            setChangedConfiguration( null );
        }

        if ( getExternalConfiguration() != null && getExternalConfiguration().isDirty() )
        {
            getExternalConfiguration().rollbackChanges();
        }
    }
}
