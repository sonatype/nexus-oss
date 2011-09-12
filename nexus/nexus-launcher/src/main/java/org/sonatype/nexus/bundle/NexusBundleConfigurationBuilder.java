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
package org.sonatype.nexus.bundle;

import com.google.common.base.Preconditions;
import org.sonatype.sisu.overlay.Overlay;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link NexusBundleConfiguration} builder.
 *
 * @since 1.9.3
 */
public class NexusBundleConfigurationBuilder {

    // required
    private String bundleArtifactCoordinates;

    private String bundleId;

    // optional
    private List<String> pluginCoordinates = new ArrayList<String>();

    private boolean licensed = false;

    private List<String> excludes = new ArrayList<String>();

    private boolean promoteOptionalPlugins = false;

    private boolean configurePluginWebapps = false;

    private List<Overlay> overlays = new ArrayList<Overlay>();

    @Inject
    public NexusBundleConfigurationBuilder artifactCoordinates(@Named("${nexus.artifact}") final String bundleArtifactCoordinates) {
        this.bundleArtifactCoordinates = bundleArtifactCoordinates;
        return this;
    }

    public NexusBundleConfigurationBuilder setPluginCoordinates(final String... pluginCoordinates) {
        Preconditions.checkNotNull(pluginCoordinates);
        this.pluginCoordinates = Arrays.asList(pluginCoordinates);
        return this;
    }

    public NexusBundleConfigurationBuilder pluginCoordinates(final String... pluginCoordinate) {
        Preconditions.checkNotNull(pluginCoordinate);
        this.pluginCoordinates.addAll(Arrays.asList(pluginCoordinate));
        return this;
    }


    public NexusBundleConfigurationBuilder excludes(final String... excludePatterns) {
        Preconditions.checkNotNull(excludePatterns);
        this.excludes.addAll(Arrays.asList(excludePatterns));
        return this;
    }

    public NexusBundleConfigurationBuilder setExcludes(final String... excludePatterns) {
        Preconditions.checkNotNull(excludePatterns);
        this.excludes = Arrays.asList(excludePatterns);
        return this;
    }

    public NexusBundleConfigurationBuilder promoteOptionalPlugins(boolean configureOptionalPlugins) {
        this.promoteOptionalPlugins = configureOptionalPlugins;
        return this;
    }

    public NexusBundleConfigurationBuilder configurePluginWebapps(boolean configurePluginWebapps) {
        this.configurePluginWebapps = configurePluginWebapps;
        return this;
    }

    public NexusBundleConfigurationBuilder licensed(boolean licensed) {
        this.licensed = licensed;
        return this;
    }

    public NexusBundleConfigurationBuilder bundleId(final String bundleId) {
        Preconditions.checkNotNull(bundleId);
        this.bundleId = bundleId;
        return this;
    }

    public NexusBundleConfigurationBuilder overlay(final Overlay... overlays) {
        Preconditions.checkNotNull(overlays);
        this.overlays.addAll(Arrays.asList(overlays));
        return this;
    }

    public NexusBundleConfiguration build() {
        try {
            return new NexusBundleConfiguration(
                    bundleId,
                    bundleArtifactCoordinates,
                    pluginCoordinates,
                    promoteOptionalPlugins,
                    excludes,
                    licensed,
                    overlays,
                    configurePluginWebapps
            );
        } catch (NullPointerException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

}
