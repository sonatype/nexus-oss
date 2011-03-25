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

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.ExternalConfiguration;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public abstract class AbstractCoreConfiguration
    extends AbstractRevertableConfiguration
    implements CoreConfiguration
{
    private ApplicationConfiguration applicationConfiguration;

    private ExternalConfiguration<?> externalConfiguration;

    public AbstractCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        final Object extracted = extractConfiguration( applicationConfiguration.getConfigurationModel() );

        if ( extracted != null )
        {
            setOriginalConfiguration( extracted );
        }
        else
        {
            setOriginalConfiguration( getDefaultConfiguration() );
        }

        this.applicationConfiguration = applicationConfiguration;
    }

    protected ApplicationConfiguration getApplicationConfiguration()
    {
        return applicationConfiguration;
    }

    protected ExternalConfiguration<?> prepareExternalConfiguration( Object configuration )
    {
        // usually nothing, but CRepository and CPlugin does have them
        return null;
    }

    public ExternalConfiguration<?> getExternalConfiguration()
    {
        if ( externalConfiguration == null )
        {
            externalConfiguration = prepareExternalConfiguration( getOriginalConfiguration() );
        }

        return externalConfiguration;
    }

    public Object getDefaultConfiguration()
    {
        return null;
    }

    @Override
    public boolean isDirty()
    {
        return isThisDirty() || ( getExternalConfiguration() != null && getExternalConfiguration().isDirty() );
    }

    @Override
    public void validateChanges()
        throws ConfigurationException
    {
        super.validateChanges();

        if ( getExternalConfiguration() != null )
        {
            getExternalConfiguration().validateChanges();
        }
    }

    @Override
    public void commitChanges()
        throws ConfigurationException
    {
        super.commitChanges();

        if ( getExternalConfiguration() != null )
        {
            getExternalConfiguration().commitChanges();
        }
    }

    @Override
    public void rollbackChanges()
    {
        super.rollbackChanges();

        if ( getExternalConfiguration() != null )
        {
            getExternalConfiguration().rollbackChanges();
        }
    }

    // ==

    protected abstract Object extractConfiguration( Configuration configuration );
}
