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

import java.util.Collection;
import org.hamcrest.Matcher;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Test;
import static org.sonatype.nexus.bundle.NexusBundleConfiguration.Builder;

/**
 *
 * @author plynch
 */
public class NexusBundleConfigurationTest {

    private Builder testBuilder = new Builder("org.sonatype.nexus:nxus-oss-webapp:bundle:zip", "mybundle").setConfigureOptionalPlugins(true).setConfigurePluginWebapps(true).setLicensed(true).addBundleExcludes("**/foo", "**/bar").addPluginCoordinates("org.foo:plugin", "com.foo:plugin");

    private void assertDefaultConfig(NexusBundleConfiguration config){
        assertThat(config.getBundleId(), is("mybundle"));
        assertThat(config.getBundleArtifactCoordinates(), is("org.sonatype.nexus:nxus-oss-webapp:bundle:zip"));
        assertThat(config.getNexusBundleExcludes(), hasItems("**/foo", "**/bar"));
        assertThat(config.getPluginCoordinates(), hasItems("org.foo:plugin", "com.foo:plugin"));
        assertThat(config.isLicensed(), is(true));
        assertThat(config.isConfigureOptionalPlugins(), is(true));
        assertThat(config.isConfigurePluginWebapps(), is(true));
    }

    @Test
    public void builderConfiguresAllValues() {
        NexusBundleConfiguration config = testBuilder.build();
        assertDefaultConfig(config);
    }

    @Test
    public void builderCopyConstructorCopiesAllValues() {
        Builder copy = new Builder(testBuilder);
        NexusBundleConfiguration config = copy.build();
        assertDefaultConfig(config);
    }

    // here as type safety hack only, empty() by itself does not seem to work
    private static final Matcher<Collection<String>> emptyStringCollection = empty();

    @Test
    public void validateDefaultValues(){
        NexusBundleConfiguration config = new Builder("coordinate", "id").build();
        assertThat(config.getBundleId(), is("id"));
        assertThat(config.getBundleArtifactCoordinates(), is("coordinate"));
        assertThat(config.getNexusBundleExcludes(), emptyStringCollection);
        assertThat(config.getPluginCoordinates(), emptyStringCollection);
        assertThat(config.isLicensed(), is(false));
        assertThat(config.isConfigureOptionalPlugins(), is(false));
        assertThat(config.isConfigurePluginWebapps(), is(false));
    }

    @Test(expected=IllegalStateException.class)
    public void validateMissingBundleId(){
        NexusBundleConfiguration config = new Builder("coordinate").build();
    }


}
