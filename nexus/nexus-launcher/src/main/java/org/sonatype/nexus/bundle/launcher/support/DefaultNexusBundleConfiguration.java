/**
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
package org.sonatype.nexus.bundle.launcher.support;

import org.sonatype.inject.Nullable;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.sisu.bl.support.DefaultBundleConfiguration;
import org.sonatype.sisu.bl.support.resolver.BundleResolver;
import org.sonatype.sisu.bl.support.resolver.TargetDirectoryResolver;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Default Nexus bundle configuration.
 *
 * @since 2.0
 */
@Named
public class DefaultNexusBundleConfiguration
    extends DefaultBundleConfiguration<NexusBundleConfiguration>
    implements NexusBundleConfiguration
{

    /**
     * Start timeout configuration property key.
     */
    public static final String START_TIMEOUT = "NexusBundleConfiguration.startTimeout";

    /**
     * List of Nexus plugins to be installed. Should never be null.
     */
    private List<File> plugins;

    /**
     * Constructor.
     *
     * @since 2.0
     */
    @Inject
    public DefaultNexusBundleConfiguration()
    {
        setPlugins();
    }

    /**
     * Sets number of seconds to wait for Nexus to boot. If injected will use the timeout bounded to
     * {@link #START_TIMEOUT} with a default of {@link #START_TIMEOUT_DEFAULT} seconds.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Inject
    protected void configureNexusStartTimeout(
        final @Nullable @Named( "${" + START_TIMEOUT + "}" ) Integer startTimeout )
    {
        if ( startTimeout != null )
        {
            super.setStartTimeout( startTimeout );
        }
    }

    /**
     * Sets a Nexus specific bundle resolver.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Inject
    protected void setBundleResolver( final @Nullable @NexusSpecific BundleResolver bundleResolver )
    {
        super.setBundleResolver( bundleResolver );
    }

    /**
     * Sets a Nexus specific target directory resolver.
     * <p/>
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Inject
    protected void setTargetDirectoryResolver(
        final @Nullable @NexusSpecific TargetDirectoryResolver targetDirectoryResolver )
    {
        super.setTargetDirectoryResolver( targetDirectoryResolver );
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public List<File> getPlugins()
    {
        return plugins;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public NexusBundleConfiguration setPlugins( final List<File> plugins )
    {
        this.plugins = new ArrayList<File>();
        if ( plugins != null )
        {
            this.plugins.addAll( plugins );
        }
        return this;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public NexusBundleConfiguration setPlugins( final File... plugins )
    {
        return setPlugins( Arrays.asList( plugins ) );
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public NexusBundleConfiguration addPlugins( final File... plugins )
    {
        this.plugins.addAll( Arrays.asList( plugins ) );
        return this;
    }

}
