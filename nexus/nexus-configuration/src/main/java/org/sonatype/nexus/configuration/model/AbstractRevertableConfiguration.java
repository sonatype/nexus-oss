package org.sonatype.nexus.configuration.model;

import java.util.Collection;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.RevertableConfiguration;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractRevertableConfiguration
    implements RevertableConfiguration
{
    private final XStream xstream = new XStream();

    private Object originalConfiguration;

    private Object changedConfiguration;

    public Object getConfiguration( boolean forWrite )
    {
        if ( forWrite )
        {
            if ( getOriginalConfiguration() != null && getChangedConfiguration() == null )
            {
                // copy it
                setChangedConfiguration( copyObject( getOriginalConfiguration(), null ) );

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

    @SuppressWarnings( "unchecked" )
    protected Object copyObject( Object source, Object target )
    {
        if ( source == null && target == null )
        {
            return null;
        }
        else if ( source instanceof Collection<?> && target != null )
        {
            // one exception is config object is actually a list, we need to keep the same instance
            ( (Collection) target ).clear();

            ( (Collection) target ).addAll( (Collection) source );

            return target;
        }
        else if ( target == null )
        {
            // "clean" deep copy
            return getXStream().fromXML( getXStream().toXML( source ) );
        }
        else
        {
            // "overlay" actually
            return getXStream().fromXML( getXStream().toXML( source ), target );
        }
    }

    protected void copyTransients( Object source, Object destination )
    {
        // usually none, but see CRepository
    }

    protected boolean isThisDirty()
    {
        return getChangedConfiguration() != null;
    }

    public boolean isDirty()
    {
        return isDirty();
    }

    public void validateChanges()
        throws ConfigurationException
    {
        if ( isThisDirty() )
        {
            doValidateChanges( getChangedConfiguration() );
        }
    }

    public void commitChanges()
        throws ConfigurationException
    {
        if ( isThisDirty() )
        {
            try
            {
                doValidateChanges( getChangedConfiguration() );
            }
            catch ( ConfigurationException e )
            {
                rollbackChanges();

                throw e;
            }

            // nice, isn't it?
            setOriginalConfiguration( copyObject( getChangedConfiguration(), getOriginalConfiguration() ) );

            copyTransients( getChangedConfiguration(), getOriginalConfiguration() );

            setChangedConfiguration( null );
        }
    }

    public void rollbackChanges()
    {
        if ( isThisDirty() )
        {
            setChangedConfiguration( null );
        }
    }

    // ==

    protected abstract void doValidateChanges( Object changedConfiguration )
        throws ConfigurationException;
}
