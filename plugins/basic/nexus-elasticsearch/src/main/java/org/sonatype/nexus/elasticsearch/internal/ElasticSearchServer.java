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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.sonatype.nexus.common.stateguard.Guarded;
import org.sonatype.nexus.common.stateguard.StateGuardLifecycleSupport;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

import groovy.lang.Singleton;
import org.eclipse.sisu.EagerSingleton;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

@Named
@EagerSingleton
public class ElasticSearchServer
    extends StateGuardLifecycleSupport
    implements Provider<Client>
{

  private final ApplicationDirectories applicationDirectories;

  private Node node;

  @Inject
  public ElasticSearchServer(final ApplicationDirectories applicationDirectories) {
    this.applicationDirectories = checkNotNull(applicationDirectories);
  }

  public void doStart() {
    ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder()
        .classLoader(Node.class.getClassLoader())
        .put("cluster.name", "NEXUS") // we probably want to have a cluster / nexus instance
        .put("http.enabled", false)
        .put("path.data", applicationDirectories.getAppDirectory("elasticsearch"));

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

  @Override
  @PostConstruct
  public void start() throws Exception { super.start(); }

  @Override
  @PreDestroy
  public void stop() throws Exception { super.stop(); }

}
