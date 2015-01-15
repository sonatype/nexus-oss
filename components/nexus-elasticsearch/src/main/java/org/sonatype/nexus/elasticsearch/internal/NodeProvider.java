/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */

package org.sonatype.nexus.elasticsearch.internal;

import java.io.File;
import java.net.URL;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

/**
 * ElasticSearch {@link Node} provider.
 *
 * @since 3.0
 */
@Named
@Singleton
public class NodeProvider
    extends ComponentSupport
    implements Provider<Node>
{
  private final File appDir;

  private Node node;

  // FIXME: Replace with ApplicationDirectories once we we have access to that class

  @Inject
  public NodeProvider(final @Named("${nexus-app}") File appDir) {
    this.appDir = checkNotNull(appDir);
  }

  @Override
  public Node get() {
    if (node == null) {
      try {
        Node node = create();

        log.debug("Waiting for green-status");
        node.client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

        this.node = node;
      }
      catch (Exception e) {
        log.warn("Failed to create node", e);
      }
    }
    return node;
  }

  private Node create() throws Exception {
    File file = new File(appDir, "etc/elasticsearch.yml");
    checkState(file.exists(), "Missing configuration: %s", file);
    URL url = file.toURI().toURL();
    log.info("Creating node with config: {}", url);

    ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
        .classLoader(Node.class.getClassLoader())
        .loadFromUrl(url);

    return nodeBuilder().settings(builder).node();
  }

  @PreDestroy
  public void shutdown() {
    if (node != null) {
      log.debug("Shutting down");
      node.close();
      node = null;
    }
  }
}
