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

import org.sonatype.sisu.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableList;

/**
 * Configuration for a nexus bundle.
 *
 * @since 1.9.3
 */
public class NexusBundleConfiguration {

    /**
     * Extracted bundle id.
     */
    private final String bundleId;

    /**
     * Bundle source artifact coordinates.
     */
    private final String bundleArtifactCoordinates;

    /**
     * List of additional plugins (beside ones already in the bundle)
     */
    private final List<String> pluginCoordinates;

    /**
     * Promote the optional plugins included in the bundle
     */
    private final boolean promoteOptionalPlugins;

    /**
     * If there is one or more *-webapp.zip files in runtime/apps/nexus/plugin-repository, then unpack that zip to nexus
     * base dir and delete the original file
     */
    private final boolean configurePluginWebapps;

    /**
     * Paths to be excluded from extracted bundle.
     */
    private final List<String> excludes;

    /**
     * Overlays to be applied over unpacked bundle.
     */
    private final List<Overlay> overlays;

    /**
     * Should the bits required to license the bundle be configured?
     */
    private final boolean licensed;

    public NexusBundleConfiguration(final String bundleId,
                                    final String bundleArtifactCoordinates,
                                    final List<String> pluginCoordinates,
                                    final boolean promoteOptionalPlugins,
                                    final List<String> excludes,
                                    final boolean licensed,
                                    final List<Overlay> overlays,
                                    final boolean configurePluginWebapps) {

        this.bundleId = checkNotNull(bundleId, "Bundle id must be set to a non null value");
        this.bundleArtifactCoordinates = checkNotNull(bundleArtifactCoordinates, "Bundle artifact coordinates must be set to a non null value");
        this.pluginCoordinates = unmodifiableList(new ArrayList<String>(pluginCoordinates == null ? Collections.<String>emptyList() : pluginCoordinates));
        this.promoteOptionalPlugins = promoteOptionalPlugins;
        this.excludes = unmodifiableList(new ArrayList<String>(excludes == null ? Collections.<String>emptyList() : excludes));
        this.licensed = licensed;
        this.overlays = unmodifiableList(new ArrayList<Overlay>(overlays == null ? Collections.<Overlay>emptyList() : overlays));
        this.configurePluginWebapps = configurePluginWebapps;
    }

    public String getBundleId() {
        return bundleId;
    }

    public String getBundleArtifactCoordinates() {
        return bundleArtifactCoordinates;
    }

    public boolean isLicensed() {
        return licensed;
    }

    public List<String> getExcludes() {
        return this.excludes;
    }

    public List<String> getPluginCoordinates() {
        return this.pluginCoordinates;
    }

    public boolean isPromoteOptionalPlugins() {
        return promoteOptionalPlugins;
    }

    public boolean isConfigurePluginWebapps() {
        return configurePluginWebapps;
    }

    public List<Overlay> getOverlays() {
        return this.overlays;
    }

}
