/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.bundle.launcher.support;

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
 * @since 1.10.0
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
     * @since 1.10.0
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
     * @since 1.10.0
     */
    @Inject
    protected void configureStartTimeout(
        final @Named( "${" + START_TIMEOUT + ":-" + START_TIMEOUT_DEFAULT + "}" ) Integer startTimeout )
    {
        super.setStartTimeout( startTimeout );
    }

    /**
     * Sets a Nexus specific bundle resolver.
     * <p/>
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Inject
    protected void setBundleResolver( final @NexusSpecific BundleResolver bundleResolver )
    {
        super.setBundleResolver( bundleResolver );
    }

    /**
     * Sets a Nexus specific target directory resolver.
     * <p/>
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Inject
    protected void setTargetDirectoryResolver( final @NexusSpecific TargetDirectoryResolver targetDirectoryResolver )
    {
        super.setTargetDirectoryResolver( targetDirectoryResolver );
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Override
    public List<File> getPlugins()
    {
        return plugins;
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.10.0
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
     * @since 1.10.0
     */
    @Override
    public NexusBundleConfiguration setPlugins( final File... plugins )
    {
        return setPlugins( Arrays.asList( plugins ) );
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.10.0
     */
    @Override
    public NexusBundleConfiguration addPlugins( final File... plugins )
    {
        this.plugins.addAll( Arrays.asList( plugins ) );
        return this;
    }

}
