/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.capabilities;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.capabilities.support.CapabilitySupport;
import org.sonatype.nexus.yum.YumRegistry;

/**
 * @since 3.0
 */
@Named( YumCapabilityDescriptor.TYPE_ID )
public class YumCapability
    extends CapabilitySupport
{

    private final YumRegistry yumRegistry;

    private YumCapabilityConfiguration configuration;

    @Inject
    public YumCapability( final YumRegistry yumRegistry )
    {
        this.yumRegistry = checkNotNull( yumRegistry );
    }

    @Override
    public void onCreate()
        throws Exception
    {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onLoad()
        throws Exception
    {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onUpdate()
        throws Exception
    {
        configuration = createConfiguration( context().properties() );
    }

    @Override
    public void onRemove()
        throws Exception
    {
        configuration = null;
    }

    @Override
    public void onActivate()
    {
        yumRegistry.setMaxNumberOfParallelThreads( configuration.maxNumberParallelThreads() );
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName();
    }

    YumCapabilityConfiguration createConfiguration( final Map<String, String> properties )
    {
        return new YumCapabilityConfiguration( properties );
    }

}
