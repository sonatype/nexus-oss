/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.hazelcast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.app.ApplicationDirectories;
import org.sonatype.nexus.common.node.LocalNodeAccess;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.util.ExceptionUtil;
import org.apache.karaf.cellar.core.utils.CombinedClassLoader;
import org.eclipse.sisu.EagerSingleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Expose the shared {@link HazelcastInstance}.
 *
 * @since 3.0
 */
@Named(HazelcastInstanceProvider.INSTANCE_NAME)
@EagerSingleton
public class HazelcastInstanceProvider
    extends ComponentSupport
    implements Provider<HazelcastInstance>
{
  public static final String INSTANCE_NAME = "nexus";

  static {
    ExceptionUtil.class.toString(); // workaround https://github.com/orientechnologies/orientdb/pull/4609
  }

  private final BundleContext bundleContext;

  private final ApplicationDirectories directories;

  private final LocalNodeAccess localNodeAccess;

  private final CombinedClassLoader combinedClassLoader;

  private HazelcastInstance instance;

  private ServiceRegistration<?> registration;

  @Inject
  public HazelcastInstanceProvider(final BundleContext bundleContext,
                                   final ApplicationDirectories directories,
                                   final LocalNodeAccess localNodeAccess)
  {
    this.bundleContext = checkNotNull(bundleContext);
    this.directories = checkNotNull(directories);
    this.localNodeAccess = checkNotNull(localNodeAccess);

    combinedClassLoader = new CombinedClassLoader();

    // start tracking bundles whose packages should be distributed
    new HazelcastBundleTracker(bundleContext, combinedClassLoader);
  }

  @Override
  public HazelcastInstance get() {
    instance = Hazelcast.newHazelcastInstance(getHazelcastConfig());
    checkState(instance != null, "Missing instance named: %s", INSTANCE_NAME);
    log.info("Instance: {}", instance);

    // expose hazelcast instance as service so cellar's blueprint components can consume it
    registration = bundleContext.registerService(HazelcastInstance.class, instance, null);

    return instance;
  }

  @PreDestroy
  public void shutdown() {
    if (instance != null) {
      log.info("Shutting down: {}", instance);
      try {
        registration.unregister();
      }
      catch (final IllegalStateException e) {
        // bundle has already been stopped and the service unregistered
      }
      finally {
        registration = null;
        instance.shutdown();
        instance = null;
      }
    }
  }

  private Config getHazelcastConfig() {
    Config config;
    File hazelcastXml = new File(new File(directories.getInstallDirectory(), "etc"), "hazelcast.xml");
    try (InputStream in = new BufferedInputStream(new FileInputStream(hazelcastXml))) {
      config = new XmlConfigBuilder(in).build();
    }
    catch (IOException e) {
      log.warn("No configuration found; using defaults");
      config = new XmlConfigBuilder().build();
    }

    config.setInstanceName(INSTANCE_NAME);
    config.setClassLoader(combinedClassLoader);

    // Customize member attributes
    // FIXME: Change to contributor model, to make this easier to extend with components
    MemberAttributeConfig attributes = new MemberAttributeConfig();
    attributes.setStringAttribute("node.identity", localNodeAccess.getId());
    config.setMemberAttributeConfig(attributes);

    log.debug("Config: {}", config);
    return config;
  }
}
