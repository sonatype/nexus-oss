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
package org.sonatype.nexus.hazelcast.internal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.app.ApplicationDirectories;
import org.sonatype.nexus.hazelcast.HazelcastMemberAttributesCustomizer;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
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
    // HACK: workaround https://github.com/orientechnologies/orientdb/pull/4609
    ExceptionUtil.class.toString();
  }

  private final BundleContext bundleContext;

  private final ApplicationDirectories directories;

  private final List<HazelcastMemberAttributesCustomizer> memberAttributesCustomizers;

  private final CombinedClassLoader combinedClassLoader;

  private HazelcastInstance instance;

  private ServiceRegistration<?> registration;

  @Inject
  public HazelcastInstanceProvider(final BundleContext bundleContext,
                                   final ApplicationDirectories directories,
                                   final List<HazelcastMemberAttributesCustomizer> memberAttributesCustomizers)
  {
    this.bundleContext = checkNotNull(bundleContext);
    this.directories = checkNotNull(directories);
    this.memberAttributesCustomizers = checkNotNull(memberAttributesCustomizers);
    this.combinedClassLoader = new CombinedClassLoader();

    // start tracking bundles whose packages should be distributed
    new HazelcastBundleTracker(bundleContext, combinedClassLoader);
  }

  @Override
  public HazelcastInstance get() {
    instance = Hazelcast.newHazelcastInstance(getHazelcastConfig());
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

  /**
   * Load the application Hazelcast configuration if present, or fallback to defaults if the file is missing.
   */
  private Config getHazelcastConfig() {
    Config config;

    // attempt to load default configuration
    File file = new File(new File(directories.getInstallDirectory(), "etc"), "hazelcast.xml");
    if (file.exists()) {
      try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
        config = new XmlConfigBuilder(in).build();
      }
      catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    else {
      // else fallback if for some reason it was missing
      log.warn("Missing configuration: {}; using defaults", file.getAbsolutePath());
      config = new XmlConfigBuilder().build();
    }

    // Customize configuration
    customize(config);

    // Always set the instance-name and CL for sanity
    config.setInstanceName(INSTANCE_NAME);
    config.setClassLoader(combinedClassLoader);

    log.debug("Config: {}", config);
    return config;
  }

  /**
   * Customize the configuration.
   */
  private void customize(final Config config) {
    // customize member attributes
    MemberAttributeConfig attributes = new MemberAttributeConfig();
    for (HazelcastMemberAttributesCustomizer customizer : memberAttributesCustomizers) {
      customizer.apply(attributes);
    }

    if (log.isDebugEnabled()) {
      log.debug("Customized member attributes:");
      for (Map.Entry<String, Object> entry : attributes.getAttributes().entrySet()) {
        log.debug("  {}={}", entry.getKey(), entry.getValue());
      }
    }

    config.setMemberAttributeConfig(attributes);
  }
}
