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
package sample;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfigurationBuilder;
import org.sonatype.nexus.bundle.launcher.NexusBundleLauncher;
import org.sonatype.nexus.test.ConfigurableInjectedTest;

import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 *
 */
public class BasicLauncherTest extends ConfigurableInjectedTest {

    @Inject
    private Logger logger;

    @Inject
    private NexusBundleLauncher nexusBundleLauncher;

    @Inject
    private NexusBundleConfigurationBuilder nexusBuilder;

    @Inject
    @Named("${nexus.artifact}")
    private String bundleArtifactCoordinates;

    @Before
    public void before() {
        assertThat(logger, notNullValue());
        assertThat(nexusBundleLauncher, notNullValue());
        assertThat(nexusBuilder, notNullValue());
        assertThat(bundleArtifactCoordinates, notNullValue());
    }

    @Test
    public void injectedBundleArtifact() {
        NexusBundleConfiguration config = nexusBuilder
                .bundleId(testName.getMethodName())
                .build();

        assertThat(config.getBundleArtifactCoordinates(), is(equalTo(bundleArtifactCoordinates)));
        assertThat(config.getBundleId(), is(equalTo(testName.getMethodName())));

        startAndStop(config);
    }

    @Test
    public void manual() {
        NexusBundleConfiguration config = new NexusBundleConfigurationBuilder()
                .artifactCoordinates(bundleArtifactCoordinates)
                .bundleId(testName.getMethodName())
                .build();

        assertThat(config.getBundleArtifactCoordinates(), is(equalTo(bundleArtifactCoordinates)));
        assertThat(config.getBundleId(), is(equalTo(testName.getMethodName())));

        startAndStop(config);
    }

    @Test
    public void methods() {
        NexusBundleConfiguration config = nexusBuilder
                .bundleId(testName.getMethodName())
                .pluginCoordinates("org.sonatype.nexus.plugins:nexus-groovy-console-plugin:zip:bundle:1.9.3-SNAPSHOT")
                .build();

        assertThat(config.getBundleArtifactCoordinates(), is(equalTo(bundleArtifactCoordinates)));
        assertThat(config.getBundleId(), is(equalTo(testName.getMethodName())));

        startAndStop(config);
    }

    private void startAndStop(NexusBundleConfiguration config) {
//        NexusBundle bundle = null;
//        try {
//            bundle = nexusBundleLauncher.start(config);
//        } finally {
//            if (bundle != null) {
//                nexusBundleLauncher.stop(bundle);
//            }
//        }
    }

}
