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
import org.sonatype.sisu.overlay.OverlayBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Configuration for a nexus bundle
 */
public class NexusBundleConfiguration {

    /**
     * Unique ID to identify the bundle.
     */
    public String getBundleId() {
        return bundleId;
    }

    /**
     * Artifact coordinates of the Nexus bundle to install
     */
    public String getBundleArtifactCoordinates() {
        return bundleArtifactCoordinates;
    }

    public boolean isLicensed() {
        return licensed;
    }

    /**
     * Patterns to exclude from the default bundle
     */
    public List<String> getNexusBundleExcludes() {
        return this.nexusBundleExcludes;
    }

    /**
     * Plugin artifact coordinates to include in the Nexus bundle
     */
    public List<String> getPluginCoordinates() {
        return this.pluginCoordinates;
    }

    public boolean isConfigureOptionalPlugins() {
        return configureOptionalPlugins;
    }

    public boolean isConfigurePluginWebapps() {
        return configurePluginWebapps;
    }

    public List<Overlay> getOverlays() {
        return this.overlays;
    }


    private String bundleId;


    private String bundleArtifactCoordinates;


    private List<String> pluginCoordinates;

    /**
     * Promote the optional plugins included in the bundle
     */
    private boolean configureOptionalPlugins;

    /**
     * If there is one or more *-webapp.zip files in runtime/apps/nexus/plugin-repository, then unpack that zip to nexus
     * base dir and delete the original file
     */
    private boolean configurePluginWebapps;


    private List<String> nexusBundleExcludes;

    private List<Overlay> overlays;

    /**
     * Should the bits required to license the bundle be configured?
     */
    private boolean licensed;

    private NexusBundleConfiguration(final Builder builder) {
        // required
        this.bundleId = builder.bundleId;
        this.bundleArtifactCoordinates = builder.bundleArtifactCoordinates;
        // optional
        this.licensed = builder.licensed;
        this.nexusBundleExcludes = builder.nexusBundleExcludes;
        this.pluginCoordinates = builder.pluginCoordinates;
        this.configureOptionalPlugins = builder.configureOptionalPlugins;
        this.configurePluginWebapps = builder.configurePluginWebapps;
        this.overlays = builder.overlays;
    }

    /**
     * Builder for building a NexusBundleConfiguration
     */
    public static class Builder {

        // required
        private String bundleArtifactCoordinates;
        private String bundleId;
        // optional
        private List<String> pluginCoordinates = new ArrayList<String>();
        private boolean licensed = false;
        private List<String> nexusBundleExcludes = new ArrayList<String>();
        private boolean configureOptionalPlugins = false;
        private boolean configurePluginWebapps = false;
        private List<Overlay> overlays = new ArrayList<Overlay>();

        /**
         * Copy constructor using the provided builder as the provider of default values.
         * @param builder
         */
        public Builder(final Builder builder) {
            this(builder.bundleArtifactCoordinates, builder.bundleId);
            this.licensed = builder.licensed;
            this.nexusBundleExcludes = new ArrayList<String>(builder.nexusBundleExcludes);
            this.pluginCoordinates = new ArrayList<String>(builder.pluginCoordinates);
            this.configureOptionalPlugins = builder.configureOptionalPlugins;
            this.configurePluginWebapps = builder.configurePluginWebapps;
            this.overlays = builder.overlays;
        }

        /**
         * Constructor useful for inject a builder with default artifact coordinates into tests.
         * @param artifactCoordinates
         */
        @Inject
        public Builder(@Named("${nexus.artifact}") final String artifactCoordinates) {
            Preconditions.checkNotNull(artifactCoordinates);
            this.bundleArtifactCoordinates = artifactCoordinates;
        }

        /**
         * Constructor with the minimum required values for a configuration.
         * @param artifactCoordinates
         * @param bundleId
         */
        public Builder(final String artifactCoordinates, final String bundleId) {
            Preconditions.checkNotNull(artifactCoordinates);
            Preconditions.checkNotNull(bundleId);
            this.bundleArtifactCoordinates = artifactCoordinates;
            this.bundleId = bundleId;
        }

        public Builder setPluginCoordinates(final String... pluginCoordinates) {
            Preconditions.checkNotNull(pluginCoordinates);
            this.pluginCoordinates = Arrays.asList(pluginCoordinates);
            return this;
        }

        /**
         * Add to the list of plugins to configure for the bundle.
         * @return The coordinates for the plugin to bundle.
         */
        public Builder addPluginCoordinates(final String... pluginCoordinate) {
            Preconditions.checkNotNull(pluginCoordinate);
            this.pluginCoordinates.addAll(Arrays.asList(pluginCoordinate));
            return this;
        }


        public Builder addBundleExcludes(final String... excludePatterns) {
            Preconditions.checkNotNull(excludePatterns);
            this.nexusBundleExcludes.addAll(Arrays.asList(excludePatterns));
            return this;
        }

        public Builder setBundleExcludes(final String... excludePatterns) {
            Preconditions.checkNotNull(excludePatterns);
            this.nexusBundleExcludes = Arrays.asList(excludePatterns);
            return this;
        }

        public Builder setConfigureOptionalPlugins(boolean configureOptionalPlugins) {
            this.configureOptionalPlugins = configureOptionalPlugins;
            return this;
        }

        public Builder setConfigurePluginWebapps(boolean configurePluginWebapps) {
            this.configurePluginWebapps = configurePluginWebapps;
            return this;
        }

        public Builder setLicensed(boolean licensed) {
            this.licensed = licensed;
            return this;
        }

        public Builder setBundleId(final String bundleId) {
            Preconditions.checkNotNull(bundleId);
            this.bundleId = bundleId;
            return this;
        }

        public Builder addOverlay(final Overlay... overlays) {
            Preconditions.checkNotNull(overlays);
            this.overlays.addAll(Arrays.asList(overlays));
            return this;
        }

        public NexusBundleConfiguration build() {
            // handle injected constructor missing bundle id
            if(this.bundleId == null){
                throw new IllegalStateException("bundleId must be set to a non-null value");
            }
            return new NexusBundleConfiguration(this);
        }
    }
}
