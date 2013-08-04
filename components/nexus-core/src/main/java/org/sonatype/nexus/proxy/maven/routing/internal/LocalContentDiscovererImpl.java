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

package org.sonatype.nexus.proxy.maven.routing.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.routing.Config;
import org.sonatype.nexus.proxy.maven.routing.discovery.DiscoveryResult;
import org.sonatype.nexus.proxy.maven.routing.discovery.LocalContentDiscoverer;
import org.sonatype.nexus.proxy.maven.routing.internal.task.CancelableUtil;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link LocalContentDiscoverer} implementation.
 *
 * @author cstamas
 * @since 2.4
 */
@Named
@Singleton
public class LocalContentDiscovererImpl
    extends AbstractLoggingComponent
    implements LocalContentDiscoverer
{
  private static final String ID = "local";

  private final Config config;

  private final Walker walker;

  /**
   * Constructor.
   */
  @Inject
  public LocalContentDiscovererImpl(final Config config, final Walker walker) {
    this.config = checkNotNull(config);
    this.walker = checkNotNull(walker);
  }

  @Override
  public DiscoveryResult<MavenRepository> discoverLocalContent(final MavenRepository mavenRepository)
      throws IOException
  {
    final DiscoveryResult<MavenRepository> discoveryResult =
        new DiscoveryResult<MavenRepository>(mavenRepository);
    final WalkerContext context =
        new DefaultWalkerContext(mavenRepository, new ResourceStoreRequest("/"), new DefaultStoreWalkerFilter(),
            true);
    final PrefixCollectorProcessor prefixCollectorProcessor = new PrefixCollectorProcessor();
    context.getProcessors().add(prefixCollectorProcessor);

    try {
      walker.walk(context);
      final ParentOMatic parentOMatic = prefixCollectorProcessor.getParentOMatic();
      if (parentOMatic.getRoot().isLeaf()) {
        // tree is basically empty, so make the list too
        discoveryResult.recordSuccess(ID, "Repository crawled successfully (is empty)",
            new ArrayListPrefixSource(Collections.<String>emptyList()));
      }
      else {
        discoveryResult.recordSuccess(ID, "Repository crawled successfully", new ArrayListPrefixSource(
            getAllLeafPaths(parentOMatic, config.getLocalScrapeDepth())));
      }
    }
    catch (WalkerException e) {
      if (e.getWalkerContext().getStopCause() != null) {
        discoveryResult.recordError(ID, e.getWalkerContext().getStopCause());
      }
      else {
        discoveryResult.recordError(ID, e);
      }
    }
    return discoveryResult;
  }

  // ==

  protected List<String> getAllLeafPaths(final ParentOMatic parentOMatic, final int maxDepth) {
    // cut the tree
    if (maxDepth != Integer.MAX_VALUE) {
      parentOMatic.cutNodesDeeperThan(maxDepth);
    }
    // get leafs
    return parentOMatic.getAllLeafPaths();
  }

  protected static class PrefixCollectorProcessor
      extends AbstractWalkerProcessor
  {
    private final ParentOMatic parentOMatic;

    public PrefixCollectorProcessor() {
      this.parentOMatic = new ParentOMatic();
    }

    public ParentOMatic getParentOMatic() {
      return parentOMatic;
    }

    @Override
    public void processItem(final WalkerContext context, final StorageItem item)
        throws Exception
    {
      // cancelation
      CancelableUtil.checkInterruption();
      if (item instanceof StorageFileItem) {
        parentOMatic.addPath(item.getPath());
      }
    }
  }
}
