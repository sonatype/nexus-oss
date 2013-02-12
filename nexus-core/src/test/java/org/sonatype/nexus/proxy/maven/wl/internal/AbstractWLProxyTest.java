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
package org.sonatype.nexus.proxy.maven.wl.internal;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.proxy.AbstractProxyTestEnvironment;
import org.sonatype.nexus.proxy.EnvironmentBuilder;
import org.sonatype.nexus.proxy.maven.wl.WLManager;

public abstract class AbstractWLProxyTest
    extends AbstractProxyTestEnvironment
{
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        waitForWLBackgroundUpdates();
    }

    protected void waitForWLBackgroundUpdates()
        throws Exception
    {
        // TODO: A hack, I don't want to expose this over component contract iface
        final WLManagerImpl wm = (WLManagerImpl) lookup( WLManager.class );
        while ( wm.isUpdateRunning() )
        {
            Thread.sleep( 500 );
        }
    }

    @Override
    protected final EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        lookup( ApplicationStatusSource.class ).setState( SystemState.STARTED );
        return createEnvironmentBuilder();
    }

    protected abstract EnvironmentBuilder createEnvironmentBuilder()
        throws Exception;
}
