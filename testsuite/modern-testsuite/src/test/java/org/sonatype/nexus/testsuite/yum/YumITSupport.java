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

package org.sonatype.nexus.testsuite.yum;

import java.util.Collection;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.capabilities.client.Capabilities;
import org.sonatype.nexus.client.core.subsystem.ServerConfiguration;
import org.sonatype.nexus.client.core.subsystem.config.RestApi;
import org.sonatype.nexus.client.core.subsystem.content.Content;
import org.sonatype.nexus.client.core.subsystem.repository.Repositories;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenGroupRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenHostedRepository;
import org.sonatype.nexus.client.core.subsystem.repository.maven.MavenProxyRepository;
import org.sonatype.nexus.testsuite.client.Events;
import org.sonatype.nexus.testsuite.client.RoutingTest;
import org.sonatype.nexus.testsuite.client.Scheduler;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;
import org.sonatype.nexus.yum.client.Repodata;
import org.sonatype.nexus.yum.client.Yum;
import org.sonatype.nexus.yum.client.capabilities.GenerateMetadataCapability;
import org.sonatype.nexus.yum.client.capabilities.MergeMetadataCapability;
import org.sonatype.nexus.yum.client.capabilities.ProxyMetadataCapability;

import org.junit.Before;
import org.junit.runners.Parameterized;

import static org.sonatype.nexus.testsuite.support.ParametersLoaders.firstAvailableTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.systemTestParameters;
import static org.sonatype.nexus.testsuite.support.ParametersLoaders.testParameters;
import static org.sonatype.sisu.goodies.common.Varargs.$;

/**
 * Support class for Yum ITs.
 *
 * @since 3.0
 */
@NexusStartAndStopStrategy(NexusStartAndStopStrategy.Strategy.EACH_TEST)
public class YumITSupport
    extends NexusRunningParametrizedITSupport
{

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return firstAvailableTestParameters(
        systemTestParameters(),
        testParameters(
            $("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip:bundle")
        )
    ).load();
  }

  public YumITSupport(final String nexusBundleCoordinates) {
    super(nexusBundleCoordinates);
  }

  @Override
  protected NexusBundleConfiguration configureNexus(NexusBundleConfiguration configuration) {
    return configuration
        .setLogLevel("org.sonatype.nexus.yum", "TRACE")
        .addPlugins(
            artifactResolver().resolvePluginFromDependencyManagement(
                "org.sonatype.nexus.plugins", "nexus-yum-repository-plugin"
            )
        );
  }

  @Before
  public void setBaseUrl() {
    final RestApi restApi = serverConfiguration().restApi();
    restApi.settings().setBaseUrl(nexus().getUrl().toExternalForm());
    restApi.settings().setForceBaseUrl(true);
    restApi.save();
  }

  protected MavenHostedRepository createYumEnabledRepository(final String repositoryId) {
    final MavenHostedRepository repository = repositories()
        .create(MavenHostedRepository.class, repositoryId)
        .excludeFromSearchResults()
        .save();

    enableMetadataGenerationFor(repositoryId);

    return repository;
  }

  protected MavenProxyRepository createYumEnabledProxyRepository(final String repositoryId, final String remoteUrl) {
    final MavenProxyRepository repository = repositories()
        .create(MavenProxyRepository.class, repositoryId)
        .doNotDownloadRemoteIndexes()
        .asProxyOf(remoteUrl)
        .withItemMaxAge(0)
        .save();

    enableMetadataProxyFor(repositoryId);

    return repository;
  }

  protected MavenGroupRepository createYumEnabledGroupRepository(final String repositoryId, final String... memberIds) {
    final MavenGroupRepository repository = repositories().create(MavenGroupRepository.class, repositoryId)
        .ofRepositories(memberIds)
        .save();

    enableMetadataMergeFor(repositoryId);

    return repository;
  }

  private void enableMetadataGenerationFor(final String repositoryId) {
    capabilities().create(GenerateMetadataCapability.class).withRepository(repositoryId).enable();
  }

  private void enableMetadataProxyFor(final String repositoryId) {
    capabilities().create(ProxyMetadataCapability.class).withRepository(repositoryId).enable();
  }

  private void enableMetadataMergeFor(final String repositoryId) {
    capabilities().create(MergeMetadataCapability.class).withRepository(repositoryId).enable();
  }

  protected Yum yum() {
    return client().getSubsystem(Yum.class);
  }

  protected Repodata repodata() {
    return client().getSubsystem(Repodata.class);
  }

  protected Repositories repositories() {
    return client().getSubsystem(Repositories.class);
  }

  protected Content content() {
    return client().getSubsystem(Content.class);
  }

  private Capabilities capabilities() {
    return client().getSubsystem(Capabilities.class);
  }

  public Scheduler scheduler() {
    return client().getSubsystem(Scheduler.class);
  }

  private ServerConfiguration serverConfiguration() {
    return client().getSubsystem(ServerConfiguration.class);
  }

  protected void waitForNexusToSettleDown()
      throws Exception
  {
    remoteLogger().info("Waiting for Nexus to settle down...");
    scheduler().waitForAllTasksToStop();
    client().getSubsystem(Events.class).waitForCalmPeriod();
    client().getSubsystem(RoutingTest.class).waitForAllRoutingUpdateJobToStop();
    remoteLogger().info("Nexus is quiet. Done waiting.");
  }

}
