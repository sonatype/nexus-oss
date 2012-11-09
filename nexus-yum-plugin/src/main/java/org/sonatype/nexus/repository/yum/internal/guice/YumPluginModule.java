/*
 * Copyright (c) 2008-2012 Sonatype, Inc.
 *
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/pro/attributions
 * Sonatype and Sonatype Nexus are trademarks of Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation.
 * M2Eclipse is a trademark of the Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.yum.internal.guice;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.inject.Named;

import org.sonatype.nexus.repository.yum.Yum;
import org.sonatype.nexus.repository.yum.internal.YumFactory;
import org.sonatype.nexus.repository.yum.internal.YumImpl;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

@Named
public class YumPluginModule
    extends AbstractModule
{

    private static final int POOL_SIZE = 10;

    @Override
    protected void configure()
    {
        bind( ScheduledThreadPoolExecutor.class ).toInstance( new ScheduledThreadPoolExecutor( POOL_SIZE ) );
        install( new FactoryModuleBuilder().implement( Yum.class, YumImpl.class ).build( YumFactory.class ) );
    }

}
