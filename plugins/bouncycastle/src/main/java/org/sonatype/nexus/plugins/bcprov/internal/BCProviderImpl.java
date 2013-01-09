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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.sonatype.nexus.plugins.bcprov.BCManager;

/**
 * Guice {@link Provider} for {@link BouncyCastleProvider} JCE provider. It gives away always the same singleton/shared
 * instance of {@link BouncyCastleProvider} (same one that is registered with JCE).
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@Singleton
public class BCProviderImpl
    implements Provider<BouncyCastleProvider>
{
    private final BCManager bcManager;

    /**
     * Default constructor.
     * 
     * @param bcManager the {@link BCManager} instance.
     */
    @Inject
    public BCProviderImpl( final BCManager bcManager )
    {
        this.bcManager = bcManager;
    }

    @Override
    public BouncyCastleProvider get()
    {
        return bcManager.getProvider();
    }
}
