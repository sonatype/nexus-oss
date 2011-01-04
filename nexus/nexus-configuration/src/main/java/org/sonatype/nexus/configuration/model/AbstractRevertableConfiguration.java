/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.model;

import java.util.Collection;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.InvalidConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
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
            checkValidationResponse( doValidateChanges( getChangedConfiguration() ) );
        }
    }

    public void commitChanges()
        throws ConfigurationException
    {
        if ( isThisDirty() )
        {
            try
            {
                checkValidationResponse( doValidateChanges( getChangedConfiguration() ) );
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

    protected void checkValidationResponse( ValidationResponse response )
        throws ConfigurationException
    {
        if ( response != null && !response.isValid() )
        {
            throw new InvalidConfigurationException( response );
        }
    }

    public abstract ValidationResponse doValidateChanges( Object changedConfiguration );
}
