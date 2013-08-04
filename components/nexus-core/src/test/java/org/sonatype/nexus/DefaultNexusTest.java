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

package org.sonatype.nexus;

import java.util.Map;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.events.EventInspectorHost;
import org.sonatype.nexus.plugins.NexusPluginManager;
import org.sonatype.nexus.proxy.maven.packaging.ArtifactPackagingMapper;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;
import org.sonatype.nexus.templates.repository.RepositoryTemplate;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.security.SecuritySystem;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNexusTest
    extends NexusAppTestSupport
{

  private DefaultNexus injectedDefaultNexus;

  private RepositoryTypeRegistry repositoryTypeRegistry;

  private RepositoryRegistry repositoryRegistry;

  @Mock
  private ApplicationEventMulticaster applicationEventMulticaster;

  @Mock
  private ApplicationStatusSource applicationStatusSource;

  @Mock
  private ArtifactPackagingMapper artifactPackagingMapper;

  @Mock
  private NexusConfiguration nexusConfiguration;

  @Mock
  private NexusPluginManager nexusPluginManager;

  @Mock
  private EventInspectorHost eventInspectorHost;

  @Mock
  private SystemStatus systemStatus;

  @Mock
  private Logger mockLogger;

  @Mock
  private NexusScheduler nexusScheduler;

  @Mock
  private SecuritySystem securitySystem;

  @Mock
  private RepositoryRegistry mockedRepositoryRegistry;

  @Mock
  private EventBus eventBus;

  @InjectMocks
  @Spy
  private DefaultNexus spyDefaultNexus;

  public DefaultNexus getInjectedDefaultNexus() {
    return injectedDefaultNexus;
  }

  @Override
  protected void setUp()
      throws Exception
  {
    super.setUp();

    injectedDefaultNexus = (DefaultNexus) lookup(Nexus.class);

    repositoryTypeRegistry = lookup(RepositoryTypeRegistry.class);

    repositoryRegistry = lookup(RepositoryRegistry.class);

    // sensible mock defaults
    doReturn("App Name").when(systemStatus).getAppName();
    doReturn("123-SNAPSHOT").when(systemStatus).getVersion();
    doReturn(mockLogger).when(spyDefaultNexus).getLogger();
    doReturn(systemStatus).when(spyDefaultNexus).getSystemStatus();
    doReturn(systemStatus).when(applicationStatusSource).getSystemStatus();
  }

  @Test
  public void initializeLogsApplicationInitialize()
      throws Exception
  {
    spyDefaultNexus.initialize();
    verify(spyDefaultNexus, times(1)).logInitialize();
  }

  @Test
  public void logInitializeFormat()
      throws Exception
  {
    spyDefaultNexus.logInitialize();
    verify(mockLogger).info(startsWith("\n-------------------------------------------------\n\n"));
    verify(mockLogger).info(contains("Initializing App Name 123-SNAPSHOT"));
    verify(mockLogger).info(endsWith("\n\n-------------------------------------------------"));
  }

  @Test
  public void getNexusNameForLogsWithLongEdition()
      throws Exception
  {
    final String name = spyDefaultNexus.getNexusNameForLogs();
    assertThat(name, equalTo("App Name 123-SNAPSHOT"));
  }

  @Test
  public void stopServiceLogsStop()
      throws Exception
  {
    spyDefaultNexus.stopService();
    verify(mockLogger).info(eq("Stopped {}"), eq("App Name 123-SNAPSHOT"));
  }

  @Test
  public void startServiceLogsStartAndNexusWork()
      throws Exception
  {
    doReturn(true).when(mockLogger).isInfoEnabled();
    //doReturn( mock( File.class ) ).when( nexusConfiguration ).getWorkingDirectory();
    spyDefaultNexus.startService();
    verify(mockLogger).info(eq("Started {}"), eq("App Name 123-SNAPSHOT"));
    verify(mockLogger).info(eq("Nexus Work Directory : {}"), isNull());

  }

  @Override
  protected boolean loadConfigurationAtSetUp() {
    return false;
  }

  @Test
  public void testRepositoryTemplates()
      throws Exception
  {
    TemplateSet repoTemplates = getInjectedDefaultNexus().getRepositoryTemplates();

    assertThat(repoTemplates, notNullValue());
    assertThat(repoTemplates.size(), is(12));

    RepositoryTemplate template =
        (AbstractRepositoryTemplate) getInjectedDefaultNexus().getRepositoryTemplateById(
            "default_hosted_release");

    assertThat(template, notNullValue());
    assertThat(template, instanceOf(AbstractRepositoryTemplate.class));

    // just adjust some params on template
    {
      // FIXME: how to handle this gracefully and in general way?
      AbstractRepositoryTemplate repoTemplate = (AbstractRepositoryTemplate) template;

      repoTemplate.getConfigurableRepository().setId("created-from-template");
      repoTemplate.getConfigurableRepository().setName("Repository created from template");
    }

    Repository repository = template.create();

    // this call will throw NoSuchRepositoryException if repo is not registered with registry
    assertThat(repositoryRegistry.getRepository("created-from-template"), notNullValue());
    assertThat(repository.getRepositoryKind(), notNullValue());
    assertThat(repository.getRepositoryKind().isFacetAvailable(HostedRepository.class), is(true));
    assertThat(repository.getRepositoryKind().isFacetAvailable(ProxyRepository.class), is(false));

    // assertNotNull( getInjectedDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_HOSTED_RELEASE ) );
    // assertNotNull( getInjectedDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_HOSTED_SNAPSHOT ) );
    // assertNotNull( getInjectedDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_PROXY_RELEASE ) );
    // assertNotNull( getInjectedDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_PROXY_SNAPSHOT ) );
    // FIXME tamas here you go
    // assertNotNull( getInjectedDefaultNexus().createFromTemplate( RepositoryTemplate.DEFAULT_VIRTUAL ) );
  }

  @Test
  public void testListRepositoryContentClasses()
      throws Exception
  {
    Map<String, ContentClass> plexusContentClasses = getContainer().lookupMap(ContentClass.class);
    Map<String, ContentClass> contentClasses = repositoryTypeRegistry.getContentClasses();

    assertThat(plexusContentClasses.size(), equalTo(contentClasses.size()));
    assertThat(plexusContentClasses.values().containsAll(contentClasses.values()), is(true));
  }

  /**
   * Ignored test, as Nexus is not bouncable (reusable) as SecuritySystem is not reusable (coz Shiro SecurityManager
   * is not restartable).
   */
  @Test
  @Ignore
  public void testBounceNexus()
      throws Exception
  {
    getInjectedDefaultNexus().stop();

    getInjectedDefaultNexus().start();
  }
}
