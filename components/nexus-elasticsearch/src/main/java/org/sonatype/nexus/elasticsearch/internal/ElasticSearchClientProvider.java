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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;

import org.eclipse.sisu.EagerSingleton;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

@Named
@Singleton
public class ElasticSearchClientProvider
    extends StateGuardLifecycleSupport
    implements Provider<Client>
{

  private final File appDir;

  private Node node;

  @Inject
  public ElasticSearchClientProvider(final @Named("${nexus-app}") File appDir) {
    this.appDir = checkNotNull(appDir);
  }

  public void doStart() throws Exception {
    File config = new File(appDir, "etc/elasticsearch.yml");
    ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
        .classLoader(Node.class.getClassLoader())
        .loadFromUrl(config.toURI().toURL());
    node = nodeBuilder().settings(builder).node();
    node.client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
  }

  public void doStop() {
    node.close();
  }

  @Guarded(by = State.STARTED)
  public Client get() {
    return node.client();
  }

  @Named
  @EagerSingleton
  static class Booter
  {

    @Inject
    ElasticSearchClientProvider provider;

    @PostConstruct
    void start() throws Exception { provider.start(); }

    @PreDestroy
    void stop() throws Exception { provider.stop(); }
  }

}
