/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugins.capabilities.it;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import org.junit.Before;
import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.NexusRunningITSupport;
import org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy;
import org.sonatype.nexus.bundle.launcher.support.NexusBundleResolver;
import org.sonatype.nexus.client.core.NexusClient;
import org.sonatype.nexus.client.rest.NexusClientFactory;
import org.sonatype.nexus.client.rest.UsernamePasswordAuthenticationInfo;
import org.sonatype.nexus.plugins.capabilities.client.Capabilities;
import org.sonatype.sisu.bl.support.resolver.BundleResolver;

import javax.inject.Inject;
import java.io.File;

import static org.sonatype.nexus.bundle.launcher.NexusStartAndStopStrategy.Strategy.EACH_TEST;
import static org.sonatype.nexus.client.rest.BaseUrl.baseUrlFrom;

@NexusStartAndStopStrategy(EACH_TEST)
public abstract class CapabilitiesITSupport
        extends NexusRunningITSupport {

    protected static final String TEST_REPOSITORY = "releases";

    @Inject
    private NexusClientFactory nexusClientFactory;

    private NexusClient nexusClient;

    @Override
    protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
        return configuration.addPlugins(
                resolvePluginFromDependencyManagement(
                        "org.sonatype.nexus.plugins", "nexus-capabilities-plugin"
                ),
                resolvePluginFromDependencyManagement(
                        "org.sonatype.nexus.plugins.capabilities",
                        "nexus-capabilities-testsuite-helper"
                )
        );
    }

    @Override
    public void configure(final Binder binder) {
        super.configure(binder);
        binder
                .bind(BundleResolver.class)
                .annotatedWith(Names.named(NexusBundleResolver.FALLBACK_NEXUS_BUNDLE_RESOLVER))
                .toInstance(
                        new BundleResolver() {
                            @Override
                            public File resolve() {
                                return resolveFromDependencyManagement(
                                        "org.sonatype.nexus", "nexus-oss-webapp", null, null, null, null
                                );
                            }
                        }
                );
    }

    @Before
    public void createNexusClient() {
        nexusClient = nexusClientFactory.createFor(
                baseUrlFrom(nexus().getUrl()),
                new UsernamePasswordAuthenticationInfo("admin", "admin123")
        );
    }

    protected Capabilities capabilities() {
        return nexusClient.getSubsystem(Capabilities.class);
    }

}
