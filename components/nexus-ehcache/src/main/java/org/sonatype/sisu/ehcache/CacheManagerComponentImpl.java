/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.sisu.ehcache;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.management.MBeanServer;

import org.sonatype.appcontext.AppContext;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of CacheManagerComponent. Note: as SISU-93 is not yet here, and this component does need
 * explicit shutdown (in case when multiple instances are re-created of it, like in UT environment), you have to use
 * {@link #shutdown()} method.
 *
 * @author cstamas
 */
@Named
@Singleton
public class CacheManagerComponentImpl
    implements CacheManagerComponent
{
  private final Logger logger;

  private final AppContext appContext;

  private CacheManager cacheManager;

  /**
   * Creates CacheManagerProviderImpl using the provided AppContext. See
   * {@link #CacheManagerProviderImpl(Logger, AppContext, File)} for details.
   */
  @Inject
  public CacheManagerComponentImpl(@Nullable final AppContext appContext)
      throws IOException
  {
    this(appContext, null);
  }

  /**
   * Creates CacheManagerProviderImpl instance using default logger. See
   * {@link #CacheManagerProviderImpl(Logger, AppContext, File)} for details.
   */
  public CacheManagerComponentImpl(final AppContext appContext, final File file)
      throws IOException
  {
    this(LoggerFactory.getLogger(CacheManagerComponentImpl.class), appContext, file);
  }

  /**
   * Creates a new instance of CacheManagerProviderImpl by using provided logger, appcontext and file to pointing at
   * EHCache XML configuration file.
   *
   * @param logger     the logger to use, might not be {@code null}.
   * @param appContext the appContext to use, might be {@code null}.
   * @param file       the EHCache XML configuration file, {@code null} if you rely on defaults.
   * @throws IOException          in case of some fatal problem.
   * @throws NullPointerException if logger is null.
   */
  public CacheManagerComponentImpl(final Logger logger, final AppContext appContext, final File file)
      throws IOException, NullPointerException
  {
    if (logger == null) {
      throw new NullPointerException("Supplied logger cannot be null!");
    }
    this.logger = logger;
    this.appContext = appContext;
    this.cacheManager = buildCacheManager(file);
    try {
      final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
      ManagementService.registerMBeans(cacheManager, mBeanServer, false, false, true, true);
    }
    catch (final Exception e) {
      logger.warn("Failed to register EHCache manager due to {}:{}", e.getClass(), e.getMessage());
    }
  }

  public synchronized void shutdown() {
    if (cacheManager != null) {
      getLogger().info("Shutting down EHCache CacheManager.");
      cacheManager.removalAll();
      cacheManager.shutdown();
      cacheManager = null;
    }
  }

  public CacheManager getCacheManager() {
    return cacheManager;
  }

  public CacheManager buildCacheManager(final File file)
      throws IOException
  {
    return buildCacheManager(getAppContext(), file);
  }

  // ==

  @Override
  public void finalize()
      throws Throwable
  {
    try {
      shutdown();
    }
    finally {
      super.finalize();
    }
  }

  // ==

  protected Logger getLogger() {
    return logger;
  }

  protected AppContext getAppContext() {
    return appContext;
  }

  protected CacheManager buildCacheManager(final AppContext appContext, final File file)
      throws IOException
  {
    String ehCacheXml;
    if (file != null) {
      getLogger().info("Configuring EHCache CacheManager from file \"{}\".", file.getAbsolutePath());
      ehCacheXml = locateCacheManagerConfigurationFromFile(file);
    }
    else {
      getLogger().info("Configuring EHCache CacheManager from classpath.");
      ehCacheXml = locateCacheManagerConfigurationFromClasspath();
    }

    if (ehCacheXml != null) {
      Configuration configuration;
      if (appContext != null) {
        configuration =
            ConfigurationFactory.parseConfiguration(new ByteArrayInputStream(appContext.interpolate(
                ehCacheXml).getBytes("UTF-8")));
      }
      else {
        configuration =
            ConfigurationFactory.parseConfiguration(new ByteArrayInputStream(ehCacheXml.getBytes("UTF-8")));
      }

      configureDiskStore(appContext, configuration);
      configuration.setUpdateCheck(false);
      return new CacheManager(configuration);
    }
    else {
      logger.info("No EHCache configuration found, using defaults (is this really what you want?).");
      return new CacheManager();
    }
  }

  protected String locateCacheManagerConfigurationFromFile(final File file)
      throws IOException
  {
    // FIXME: Buffer!
    final FileInputStream fis = new FileInputStream(file);
    try {
      return IO.toString(fis);
    }
    finally {
      fis.close();
    }
  }

  protected String locateCacheManagerConfigurationFromClasspath()
      throws IOException
  {
    InputStream configStream = getClass().getResourceAsStream("/ehcache.xml");

    if (configStream != null) {
      getLogger().debug("Using configuration found at classpath:/ehcache.xml");
      return IO.toString(configStream);
    }
    else {
      configStream = getClass().getResourceAsStream("/ehcache-default.xml");

      if (configStream != null) {
        getLogger().debug("Using configuration found at classpath:/ehcache-default.xml");
        return IO.toString(configStream);
      }
      else {
        logger.debug("No EHCache configuration found an classpath!");
        return null;
      }
    }
  }

  protected void configureDiskStore(final AppContext appContext, final Configuration ehConfig) {
    if (ehConfig.getDiskStoreConfiguration() != null && ehConfig.getDiskStoreConfiguration().getPath() != null) {
      final String path = ehConfig.getDiskStoreConfiguration().getPath();
      final String interpolatedPath = appContext != null ? appContext.interpolate(path) : path;
      try {
        ehConfig.getDiskStoreConfiguration().setPath(new File(interpolatedPath).getCanonicalPath());
      }
      catch (IOException e) {
        getLogger().warn("Could not canonize the path \"{}\"!", interpolatedPath, e);

        // set the best we can
        ehConfig.getDiskStoreConfiguration().setPath(new File(interpolatedPath).getAbsolutePath());
      }
    }
  }
}
