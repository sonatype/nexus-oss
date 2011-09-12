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

import org.sonatype.nexus.bundle.launcher.NexusBundleLauncher;
import javax.inject.Inject;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.sonatype.nexus.bundle.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.ManagedNexusBundle;
import org.sonatype.nexus.test.ConfigurableInjectedTest;

/**
 *
 */
public class BasicLauncherTest extends ConfigurableInjectedTest{

    @Inject
    private Logger logger;

    @Inject
    private NexusBundleLauncher nexusBundleLauncher;

    @Inject
    private NexusBundleConfiguration.Builder nexusBuilder;

    @Before
    public void before() {
        assertThat(logger, notNullValue());
        logger.debug(testName.getMethodName());
        assertThat(nexusBundleLauncher, notNullValue());
    }

    @After
    public void after() {
    }

    @Test
    public void testBundleService(){
        String nexusOSSArtifactCoords = "org.sonatype.nexus:nexus-oss-webapp:tar.gz:bundle:1.9.3-SNAPSHOT";
        NexusBundleConfiguration config = new NexusBundleConfiguration.Builder(nexusOSSArtifactCoords, "mybundle").build();
        config = nexusBuilder.setBundleId("nexus1").build();
        assertThat(config.getBundleArtifactCoordinates(), is("org.sonatype.nexus:nexus-oss-webapp:tar.gz:bundle:1.9.3-SNAPSHOT"));
        nexusBuilder.addPluginCoordinates("org.sonatype.nexus.plugins:nexus-groovy-console-plugin:zip:bundle:1.9.3-SNAPSHOT");
        config = nexusBuilder.setBundleId("nexus2").build();
//        ManagedNexusBundle bundle = null;
//        try
//        {
//            bundle = nexusBundleLauncher.start(config);
//        }
//        finally {
//            if (bundle!=null) {
//                nexusBundleLauncher.stop(bundle);
//            }
//        }
    }

}
