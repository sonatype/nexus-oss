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
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

public class CGlobalHttpProxySettingsCoreConfiguration
    extends AbstractCoreConfiguration
{
    private boolean nullified;

    public CGlobalHttpProxySettingsCoreConfiguration( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }

    @Override
    public CRemoteHttpProxySettings getConfiguration( boolean forWrite )
    {
        return (CRemoteHttpProxySettings) super.getConfiguration( forWrite );
    }

    @Override
    protected CRemoteHttpProxySettings extractConfiguration( Configuration configuration )
    {
        return configuration.getGlobalHttpProxySettings();
    }

    public void initConfig()
    {
        CRemoteHttpProxySettings newProxy = new CRemoteHttpProxySettings();

        getApplicationConfiguration().getConfigurationModel().setGlobalHttpProxySettings( newProxy );

        setOriginalConfiguration( newProxy );
    }

    public void nullifyConfig()
    {
        setChangedConfiguration( null );

        setOriginalConfiguration( null );
        
        nullified = true;
    }

    @Override
    public ValidationResponse doValidateChanges( Object changedConfiguration )
    {
        return new ValidationResponse();
    }

    @Override
    public boolean isDirty()
    {
        return super.isDirty() || nullified;
    }

    @Override
    public void commitChanges()
        throws ConfigurationException
    {
        if ( nullified )
        {
            // nullified, nothing to validate and the super.commitChanges() will not work
            getApplicationConfiguration().getConfigurationModel().setGlobalHttpProxySettings( null );
        }
        else
        {
            super.commitChanges();
        }

        nullified = false;
    }

    @Override
    public void rollbackChanges()
    {
        super.rollbackChanges();

        nullified = false;
    }
    
    @Override
    protected void copyTransients( Object source, Object destination )
    {
        super.copyTransients( source, destination );
        
        // we need to manually set the authentication to null here, because of flawed overlay, where null objects do NOT overwrite non-null objects
        if ( ( ( CRemoteHttpProxySettings ) source ).getAuthentication() == null )
        {
            ( ( CRemoteHttpProxySettings ) destination ).setAuthentication( null );
        }
    }
}
