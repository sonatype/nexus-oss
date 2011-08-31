package org.sonatype.nexus.bundle;

import com.google.common.base.Preconditions;
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

        public Builder setBundleId(final String bundleId) {
            Preconditions.checkNotNull(bundleId);
            this.bundleId = bundleId;
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
