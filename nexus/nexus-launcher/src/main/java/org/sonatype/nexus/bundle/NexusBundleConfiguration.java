package org.sonatype.nexus.bundle;

import com.google.common.base.Preconditions;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Configuration for a nexus bundle
 */
public class NexusBundleConfiguration {

    public String getBundleArtifactCoordinates() {
        return bundleArtifactCoordinates;
    }

    public String getBundleId() {
        return bundleId;
    }

    public boolean isLicensed() {
        return licensed;
    }

    public List<String> getNexusBundleExcludes() {
        return new ArrayList<String>(nexusBundleExcludes);
    }

    public List<String> getPluginCoordinates() {
        return new ArrayList<String>(pluginCoordinates);
    }

    public boolean isConfigureOptionalPlugins() {
        return configuraOptionalPlugins;
    }

    public boolean isConfigurePluginWebapps() {
        return configurePluginWebapps;
    }

    /**
     * Unique ID to identify the bundle.
     */
    private String bundleId;

    /**
     * Artifact coordinates of the Nexus bundle to install
     */
    private String bundleArtifactCoordinates;

    /**
     * Plugin artifact coordinates to include in the Nexus bundle
     */
    private List<String> pluginCoordinates;

    /**
     * Promote the optional plugins included in the bundle
     */
    private boolean configuraOptionalPlugins;

    /**
     * If there is one or more *-webapp.zip files in runtime/apps/nexus/plugin-repository, then unpack that zip to nexus
     * base dir and delete the original file
     */
    private boolean configurePluginWebapps;

    /**
     * Patterns to exclude from the default bundle
     */
    private List<String> nexusBundleExcludes;

    /**
     * Should the bits required to license the bundle be configured?
     */
    private boolean licensed;

    private NexusBundleConfiguration(Builder builder) {
        // required
        this.bundleId = builder.bundleId;
        this.bundleArtifactCoordinates = builder.bundleArtifactCoordinates;
        this.licensed = builder.licensed;
        this.nexusBundleExcludes = builder.nexusBundleExcludes;
        this.pluginCoordinates = builder.pluginCoordinates;
        this.configuraOptionalPlugins = builder.configureOptionalPlugins;
        this.configurePluginWebapps = builder.configurePluginWebapps;
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
         * @param coords
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

        public NexusBundleConfiguration build() {
            return new NexusBundleConfiguration(this);
        }
    }
}
