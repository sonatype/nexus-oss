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

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * A superclass class that holds an Xpp3Dom and maintains it.
 * 
 * @author cstamas
 */
public class DefaultExternalConfiguration<T extends AbstractXpp3DomExternalConfigurationHolder>
    implements ExternalConfiguration<T>
{
    private final ApplicationConfiguration applicationConfiguration;

    private final CoreConfiguration coreConfiguration;

    private final T configuration;

    private T changedConfiguration;

    public DefaultExternalConfiguration( ApplicationConfiguration applicationConfiguration,
                                         CoreConfiguration coreConfiguration, T configuration )
    {
        this.applicationConfiguration = applicationConfiguration;

        this.coreConfiguration = coreConfiguration;

        this.configuration = configuration;

        this.changedConfiguration = null;
    }

    public boolean isDirty()
    {
        return this.changedConfiguration != null;
    }

    public void validateChanges()
        throws ConfigurationException
    {
        if ( changedConfiguration != null )
        {
            changedConfiguration.validate( getApplicationConfiguration(), coreConfiguration );
        }
    }

    public void commitChanges()
        throws ConfigurationException
    {
        if ( changedConfiguration != null )
        {
            changedConfiguration.validate( getApplicationConfiguration(), coreConfiguration );

            configuration.apply( changedConfiguration );

            changedConfiguration = null;
        }
    }

    public void rollbackChanges()
    {
        changedConfiguration = null;
    }

    @SuppressWarnings( "unchecked" )
    public T getConfiguration( boolean forModification )
    {
        if ( forModification )
        {
            // copy configuration if needed
            if ( changedConfiguration == null )
            {
                changedConfiguration = (T) configuration.clone();
            }

            return changedConfiguration;
        }
        else
        {
            return configuration;
        }
    }

    // ==

    public ValidationResponse doValidateChanges( Xpp3Dom configuration )
    {
        return changedConfiguration.doValidateChanges( getApplicationConfiguration(), coreConfiguration, configuration );

    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }
}
