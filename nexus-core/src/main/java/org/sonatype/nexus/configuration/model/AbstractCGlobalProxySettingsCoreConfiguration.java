/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.configuration.model;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.configuration.validation.ValidationResponse;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * @since 2.5
 */
public abstract class AbstractCGlobalProxySettingsCoreConfiguration
    extends AbstractCoreConfiguration
{
    private boolean nullified;

    public AbstractCGlobalProxySettingsCoreConfiguration( ApplicationConfiguration applicationConfiguration )
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
        return getGlobalProxySettings( configuration );
    }

    public void initConfig()
    {
        CRemoteHttpProxySettings newProxy = new CRemoteHttpProxySettings();

        setGlobalProxySettings( newProxy );

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
            setGlobalProxySettings( null );
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

        // we need to manually set the authentication to null here, because of flawed overlay, where null objects do NOT
        // overwrite non-null objects
        if ( ( (CRemoteHttpProxySettings) source ).getAuthentication() == null )
        {
            ( (CRemoteHttpProxySettings) destination ).setAuthentication( null );
        }
    }

    protected abstract CRemoteHttpProxySettings getGlobalProxySettings( final Configuration configuration );

    protected abstract void setGlobalProxySettings( final CRemoteHttpProxySettings newProxy );

}
