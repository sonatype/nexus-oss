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

import org.hamcrest.Matcher;
import org.junit.Test;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfigurationBuilder;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

/**
 * {@link NexusBundleConfigurationBuilder} UTs.
 *
 * @since 1.9.3
 */
public class NexusBundleConfigurationBuilderTest {

    @Test
    public void builderConfiguresAllValues() {
        NexusBundleConfiguration config = new NexusBundleConfigurationBuilder()
                .artifactCoordinates("org.sonatype.nexus:nxus-oss-webapp:bundle:zip")
                .bundleId("id")
                .promoteOptionalPlugins(true)
                .configurePluginWebapps(true)
                .licensed(true)
                .excludes("**/foo", "**/bar")
                .pluginCoordinates("org.foo:plugin", "com.foo:plugin")
                .build();

        assertThat(config.getBundleId(), is("id"));
        assertThat(config.getBundleArtifactCoordinates(), is("org.sonatype.nexus:nxus-oss-webapp:bundle:zip"));
        assertThat(config.getExcludes(), hasItems("**/foo", "**/bar"));
        assertThat(config.getPluginCoordinates(), hasItems("org.foo:plugin", "com.foo:plugin"));
        assertThat(config.isLicensed(), is(true));
        assertThat(config.isPromoteOptionalPlugins(), is(true));
        assertThat(config.isConfigurePluginWebapps(), is(true));

    }

    // here as type safety hack only, empty() by itself does not seem to work
    private static final Matcher<Collection<String>> emptyStringCollection = empty();

    @Test
    public void validateDefaultValues() {
        NexusBundleConfiguration config = new NexusBundleConfigurationBuilder()
                .artifactCoordinates("coordinate")
                .bundleId("id")
                .build();

        assertThat(config.getBundleId(), is("id"));
        assertThat(config.getBundleArtifactCoordinates(), is("coordinate"));
        assertThat(config.getExcludes(), emptyStringCollection);
        assertThat(config.getPluginCoordinates(), emptyStringCollection);
        assertThat(config.isLicensed(), is(false));
        assertThat(config.isPromoteOptionalPlugins(), is(false));
        assertThat(config.isConfigurePluginWebapps(), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void validateMissingBundleId() {
        new NexusBundleConfigurationBuilder()
                .artifactCoordinates("coordinate")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void validateMissingBundleArtifactCoordinates() {
        new NexusBundleConfigurationBuilder()
                .bundleId("id")
                .build();
    }

}
