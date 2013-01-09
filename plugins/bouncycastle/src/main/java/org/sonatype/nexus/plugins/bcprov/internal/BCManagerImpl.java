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
package org.sonatype.nexus.plugins.bcprov.internal;

import java.security.Security;

import javax.inject.Named;
import javax.inject.Singleton;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.plugins.bcprov.BCManager;

/**
 * Default {@link BCManager}.
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@Singleton
public class BCManagerImpl
    extends AbstractLoggingComponent
    implements BCManager
{
    private final BouncyCastleProvider bouncyCastleProvider;

    private boolean registeredBouncyCastleProvider;

    private boolean uninstallBouncyCastleProvider;

    /**
     * Default constructor.
     */
    public BCManagerImpl()
    {
        this.bouncyCastleProvider = new BouncyCastleProvider();
        this.registeredBouncyCastleProvider = false;
        this.uninstallBouncyCastleProvider = false;
    }

    @Override
    public synchronized boolean registerProvider()
    {
        if ( !registeredBouncyCastleProvider )
        {
            getLogger().info( "Registering BC Provider with JCE..." );
            uninstallBouncyCastleProvider = Security.addProvider( bouncyCastleProvider ) != -1;
            registeredBouncyCastleProvider = true;

            if ( !uninstallBouncyCastleProvider )
            {
                getLogger().info(
                    "BC JCE provider is already registered wih JCE by another party. This might lead to problems if registered version is not the one expected by Nexus!" );
            }
        }
        return uninstallBouncyCastleProvider;
    }

    @Override
    public synchronized boolean unregisterProvider()
    {
        if ( registeredBouncyCastleProvider )
        {
            if ( uninstallBouncyCastleProvider )
            {
                getLogger().info( "Removing BC Provider from JCE..." );
                Security.removeProvider( BouncyCastleProvider.PROVIDER_NAME );
                uninstallBouncyCastleProvider = false;
            }
            else
            {
                getLogger().info( "Not removing BC Provider from JCE as it was registered by some other party..." );
            }
            registeredBouncyCastleProvider = false;
            return true;
        }
        return false;
    }

    @Override
    public BouncyCastleProvider getProvider()
    {
        return bouncyCastleProvider;
    }
}
