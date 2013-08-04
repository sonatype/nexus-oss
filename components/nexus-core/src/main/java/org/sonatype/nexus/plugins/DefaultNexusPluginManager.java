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

package org.sonatype.nexus.plugins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.aether.util.version.GenericVersionScheme;
import org.sonatype.aether.version.InvalidVersionSpecificationException;
import org.sonatype.aether.version.Version;
import org.sonatype.aether.version.VersionScheme;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.nexus.binders.NexusAnnotatedBeanModule;
import org.sonatype.guice.plexus.binders.PlexusXmlBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.inject.Parameters;
import org.sonatype.nexus.guice.AbstractInterceptorModule;
import org.sonatype.nexus.guice.NexusModules.PluginModule;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.events.PluginActivatedEvent;
import org.sonatype.nexus.plugins.events.PluginRejectedEvent;
import org.sonatype.nexus.plugins.repository.NoSuchPluginRepositoryArtifactException;
import org.sonatype.nexus.plugins.repository.PluginRepositoryArtifact;
import org.sonatype.nexus.plugins.repository.PluginRepositoryManager;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.util.AlphanumComparator;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plugin.metadata.GAVCoordinate;
import org.sonatype.plugins.model.ClasspathDependency;
import org.sonatype.plugins.model.PluginDependency;
import org.sonatype.plugins.model.PluginMetadata;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.yammer.metrics.annotation.Timed;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default {@link NexusPluginManager} implementation backed by a {@link PluginRepositoryManager}.
 */
@Named
@Singleton
public class DefaultNexusPluginManager
    implements NexusPluginManager
{
  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private final PluginRepositoryManager repositoryManager;

  private final EventBus eventBus;

  private final RepositoryTypeRegistry repositoryTypeRegistry;

  private final MimeSupport mimeSupport;

  private final DefaultPlexusContainer container;

  private final Map<String, String> variables;

  private final List<AbstractInterceptorModule> interceptorModules;

  private final Map<GAVCoordinate, PluginDescriptor> activePlugins = new HashMap<GAVCoordinate, PluginDescriptor>();

  private final Map<GAVCoordinate, PluginResponse> pluginResponses = new HashMap<GAVCoordinate, PluginResponse>();

  private final VersionScheme versionParser = new GenericVersionScheme();

  @Inject
  public DefaultNexusPluginManager(final RepositoryTypeRegistry repositoryTypeRegistry,
                                   final EventBus eventBus,
                                   final PluginRepositoryManager repositoryManager,
                                   final DefaultPlexusContainer container,
                                   final MimeSupport mimeSupport,
                                   final @Parameters Map<String, String> variables,
                                   final List<AbstractInterceptorModule> interceptorModules)
  {
    this.repositoryTypeRegistry = checkNotNull(repositoryTypeRegistry);
    this.eventBus = checkNotNull(eventBus);
    this.repositoryManager = checkNotNull(repositoryManager);
    this.container = checkNotNull(container);
    this.mimeSupport = checkNotNull(mimeSupport);
    this.variables = checkNotNull(variables);
    this.interceptorModules = checkNotNull(interceptorModules);
  }

  // ----------------------------------------------------------------------
  // Public methods
  // ----------------------------------------------------------------------

  public Map<GAVCoordinate, PluginDescriptor> getActivatedPlugins() {
    return new HashMap<GAVCoordinate, PluginDescriptor>(activePlugins);
  }

  public Map<GAVCoordinate, PluginMetadata> getInstalledPlugins() {
    return repositoryManager.findAvailablePlugins();
  }

  public Map<GAVCoordinate, PluginResponse> getPluginResponses() {
    return new HashMap<GAVCoordinate, PluginResponse>(pluginResponses);
  }

  @Timed
  public Collection<PluginManagerResponse> activateInstalledPlugins() {
    final List<PluginManagerResponse> result = new ArrayList<PluginManagerResponse>();

    // if multiple V's for GAs are found, choose the one with biggest version (and pray that plugins has sane
    // versioning)
    Map<GAVCoordinate, PluginMetadata> filteredPlugins =
        filterInstalledPlugins(repositoryManager.findAvailablePlugins());

    for (final GAVCoordinate gav : filteredPlugins.keySet()) {
      // activate what we found in reposes
      result.add(activatePlugin(gav, true, filteredPlugins.keySet()));
    }
    return result;
  }

  public boolean isActivatedPlugin(final GAVCoordinate gav) {
    return isActivatedPlugin(gav, true);
  }

  public PluginManagerResponse activatePlugin(final GAVCoordinate gav) {
    // if multiple V's for GAs are found, choose the one with biggest version (and pray that plugins has sane
    // versioning)
    Map<GAVCoordinate, PluginMetadata> filteredPlugins =
        filterInstalledPlugins(repositoryManager.findAvailablePlugins());

    return activatePlugin(gav, true, filteredPlugins.keySet());
  }

  public PluginManagerResponse deactivatePlugin(final GAVCoordinate gav) {
    throw new UnsupportedOperationException(); // TODO
  }

  public boolean installPluginBundle(final URL bundle)
      throws IOException
  {
    throw new UnsupportedOperationException(); // TODO
  }

  public boolean uninstallPluginBundle(final GAVCoordinate gav)
      throws IOException
  {
    throw new UnsupportedOperationException(); // TODO
  }

  // ----------------------------------------------------------------------
  // Implementation methods
  // ----------------------------------------------------------------------

  /**
   * Filters a map of GAVCoordinates by "max" version. Hence, in the result Map, it is guaranteed that only one GA
   * combination will exists, and if input contained multiple V's for same GA, the one GAV contained in result with
   * have max V.
   */
  protected Map<GAVCoordinate, PluginMetadata> filterInstalledPlugins(
      final Map<GAVCoordinate, PluginMetadata> installedPlugins)
  {
    final HashMap<GAVCoordinate, PluginMetadata> result =
        new HashMap<GAVCoordinate, PluginMetadata>(installedPlugins.size());

    nextInstalledEntry:
    for (Map.Entry<GAVCoordinate, PluginMetadata> installedEntry : installedPlugins.entrySet()) {
      for (Iterator<Map.Entry<GAVCoordinate, PluginMetadata>> resultItr = result.entrySet().iterator();
           resultItr.hasNext(); ) {
        final Map.Entry<GAVCoordinate, PluginMetadata> resultEntry = resultItr.next();
        if (resultEntry.getKey().matchesByGA(installedEntry.getKey())) {
          if (compareVersionStrings(resultEntry.getKey().getVersion(), installedEntry.getKey().getVersion()) < 0) {
            resultItr.remove(); // result contains smaller version than installedOne, remove it
          }
          else {
            continue nextInstalledEntry;
          }
        }
      }
      result.put(installedEntry.getKey(), installedEntry.getValue());
    }

    return result;
  }

  protected int compareVersionStrings(final String v1str, final String v2str) {
    try {
      final Version v1 = versionParser.parseVersion(v1str);
      final Version v2 = versionParser.parseVersion(v2str);

      return v1.compareTo(v2);
    }
    catch (InvalidVersionSpecificationException e) {
      // fall back to "sane" human alike sorting of strings
      return new AlphanumComparator().compare(v1str, v2str);
    }
  }

  protected GAVCoordinate getActivatedPluginGav(final GAVCoordinate gav, final boolean strict) {
    // try exact match 1st
    if (activePlugins.containsKey(gav)) {
      return gav;
    }

    // if we are lax, try by GA
    if (!strict) {
      for (GAVCoordinate coord : activePlugins.keySet()) {
        if (coord.matchesByGA(gav)) {
          return coord;
        }
      }
    }

    // sad face here
    return null;
  }

  protected boolean isActivatedPlugin(final GAVCoordinate gav, final boolean strict) {
    return getActivatedPluginGav(gav, strict) != null;
  }

  protected PluginManagerResponse activatePlugin(final GAVCoordinate gav, final boolean strict,
                                                 final Set<GAVCoordinate> installedPluginsFilteredByGA)
  {
    final GAVCoordinate activatedGav = getActivatedPluginGav(gav, strict);
    if (activatedGav == null) {
      GAVCoordinate actualGAV = null;
      if (!strict) {
        actualGAV = findInstalledPluginByGA(installedPluginsFilteredByGA, gav);
      }
      if (actualGAV == null) {
        actualGAV = gav;
      }
      final PluginManagerResponse response = new PluginManagerResponse(actualGAV, PluginActivationRequest.ACTIVATE);
      try {
        activatePlugin(
            repositoryManager.resolveArtifact(actualGAV), response, installedPluginsFilteredByGA
        );
      }
      catch (final NoSuchPluginRepositoryArtifactException e) {
        reportMissingPlugin(response, e);
      }
      return response;
    }
    else {
      return new PluginManagerResponse(activatedGav, PluginActivationRequest.ACTIVATE);
    }
  }

  private GAVCoordinate findInstalledPluginByGA(final Set<GAVCoordinate> installedPluginsFilteredByGA,
                                                final GAVCoordinate gav)
  {
    if (installedPluginsFilteredByGA != null) {
      for (GAVCoordinate coord : installedPluginsFilteredByGA) {
        if (coord.matchesByGA(gav)) {
          return coord;
        }
      }
    }
    return null;
  }

  private void activatePlugin(final PluginRepositoryArtifact plugin,
                              final PluginManagerResponse response,
                              final Set<GAVCoordinate> installedPluginsFilteredByGA)
      throws NoSuchPluginRepositoryArtifactException
  {
    final GAVCoordinate pluginGAV = plugin.getCoordinate();
    final PluginMetadata metadata = plugin.getPluginMetadata();

    final PluginDescriptor descriptor = new PluginDescriptor(pluginGAV);
    descriptor.setPluginMetadata(metadata);

    final PluginResponse result = new PluginResponse(pluginGAV, PluginActivationRequest.ACTIVATE);
    result.setPluginDescriptor(descriptor);

    activePlugins.put(pluginGAV, descriptor);

    final List<GAVCoordinate> importList = new ArrayList<GAVCoordinate>();
    final List<GAVCoordinate> resolvedList = new ArrayList<GAVCoordinate>();
    for (final PluginDependency pd : metadata.getPluginDependencies()) {
      // here, a plugin might express a need for GAV1, but GAV2 might be already activated
      // since today we just "play" dependency resolution, we support GA resolution only
      // so, we say "relax version matching" and rely on luck for now it will work
      final GAVCoordinate gav = new GAVCoordinate(pd.getGroupId(), pd.getArtifactId(), pd.getVersion());
      final PluginManagerResponse dependencyActivationResponse = activatePlugin(
          gav, false, installedPluginsFilteredByGA
      );
      response.addPluginManagerResponse(dependencyActivationResponse);
      importList.add(dependencyActivationResponse.getOriginator());
      resolvedList.add(dependencyActivationResponse.getOriginator());
    }
    descriptor.setImportedPlugins(importList);
    descriptor.setResolvedPlugins(resolvedList);

    if (!response.isSuccessful()) {
      result.setAchievedGoal(PluginActivationResult.BROKEN);
    }
    else {
      try {
        createPluginInjector(plugin, descriptor);
        result.setAchievedGoal(PluginActivationResult.ACTIVATED);
      }
      catch (final Throwable e) {
        result.setThrowable(e);
      }
    }

    reportActivationResult(response, result);
  }

  void createPluginInjector(final PluginRepositoryArtifact plugin, final PluginDescriptor descriptor)
      throws NoSuchPluginRepositoryArtifactException
  {
    final String realmId = descriptor.getPluginCoordinates().toString();
    final ClassRealm containerRealm = container.getContainerRealm();
    ClassRealm pluginRealm;
    try {
      pluginRealm = containerRealm.createChildRealm(realmId);
    }
    catch (final DuplicateRealmException e1) {
      try {
        pluginRealm = containerRealm.getWorld().getRealm(realmId);
      }
      catch (final NoSuchRealmException e2) {
        throw new IllegalStateException();
      }
    }

    final List<URL> scanList = new ArrayList<URL>();

    final URL pluginURL = toURL(plugin);
    if (null != pluginURL) {
      pluginRealm.addURL(pluginURL);
      scanList.add(pluginURL);
    }

    for (final ClasspathDependency d : descriptor.getPluginMetadata().getClasspathDependencies()) {
      final GAVCoordinate gav =
          new GAVCoordinate(d.getGroupId(), d.getArtifactId(), d.getVersion(), d.getClassifier(), d.getType());

      final URL url = toURL(repositoryManager.resolveDependencyArtifact(plugin, gav));
      if (null != url) {
        pluginRealm.addURL(url);
        if (d.isHasComponents() || d.isShared()) {
          scanList.add(url);
        }
      }
    }

    for (final GAVCoordinate gav : descriptor.getResolvedPlugins()) {
      final String importId = gav.toString();
      for (final String classname : activePlugins.get(gav).getExportedClassnames()) {
        try {
          pluginRealm.importFrom(importId, classname);
        }
        catch (final NoSuchRealmException e) {
          // should never happen
        }
      }
    }

    final List<String> exportedClassNames = new ArrayList<String>();
    final List<RepositoryTypeDescriptor> repositoryTypes = new ArrayList<RepositoryTypeDescriptor>();
    final List<StaticResource> staticResources = new ArrayList<StaticResource>();

    final NexusResourceBundle resourceBundle = new NexusResourceBundle()
    {
      public List<StaticResource> getContributedResouces() {
        return staticResources;
      }
    };

    final Module resourceModule = new AbstractModule()
    {
      @Override
      protected void configure() {
        bind(NexusResourceBundle.class).annotatedWith(Names.named(realmId)).toInstance(resourceBundle);
      }
    };

    final List<PlexusBeanModule> beanModules = new ArrayList<PlexusBeanModule>();

    final ClassSpace pluginSpace = new URLClassSpace(pluginRealm);
    beanModules.add(new PlexusXmlBeanModule(pluginSpace, variables));

    final ClassSpace annSpace = new URLClassSpace(pluginRealm, scanList.toArray(new URL[scanList.size()]));
    beanModules.add(new NexusAnnotatedBeanModule(annSpace, variables, exportedClassNames, repositoryTypes));

    final List<Module> modules = new ArrayList<Module>();
    modules.add(resourceModule);
    modules.add(new PluginModule());
    modules.addAll(interceptorModules);

    container.addPlexusInjector(beanModules, modules.toArray(new Module[modules.size()]));

    for (final RepositoryTypeDescriptor r : repositoryTypes) {
      repositoryTypeRegistry.registerRepositoryTypeDescriptors(r);
    }

    final Enumeration<URL> e = pluginSpace.findEntries("static/", null, true);
    while (e.hasMoreElements()) {
      final URL url = e.nextElement();
      final String path = getPublishedPath(url);
      if (path != null) {
        staticResources.add(new PluginStaticResource(url, path,
            mimeSupport.guessMimeTypeFromPath(url.getPath())));
      }
    }

    descriptor.setExportedClassnames(exportedClassNames);
    descriptor.setRepositoryTypes(repositoryTypes);
    descriptor.setStaticResources(staticResources);
  }

  private URL toURL(final PluginRepositoryArtifact artifact) {
    try {
      return artifact.getFile().toURI().toURL();
    }
    catch (final MalformedURLException e) {
      return null; // should never happen
    }
  }

  private String getPublishedPath(final URL resourceURL) {
    final String path = resourceURL.toExternalForm();
    int index = path.indexOf("jar!/");
    if (index > 0) {
      return path.substring(index + 4);
    }
    index = path.indexOf("/static/");
    if (index > 0) {
      return path.substring(index);
    }
    return null;
  }

  private void reportMissingPlugin(final PluginManagerResponse response,
                                   final NoSuchPluginRepositoryArtifactException cause)
  {
    final GAVCoordinate gav = cause.getCoordinate();
    final PluginResponse result = new PluginResponse(gav, response.getRequest());
    result.setThrowable(cause);
    result.setAchievedGoal(PluginActivationResult.MISSING);

    response.addPluginResponse(result);
    pluginResponses.put(gav, result);
  }

  private void reportActivationResult(final PluginManagerResponse response, final PluginResponse result) {
    final Event<NexusPluginManager> pluginEvent;
    final GAVCoordinate gav = result.getPluginCoordinates();
    if (result.isSuccessful()) {
      pluginEvent = new PluginActivatedEvent(this, result.getPluginDescriptor());
    }
    else {
      pluginEvent = new PluginRejectedEvent(this, gav, result.getThrowable());
      activePlugins.remove(gav);
    }

    response.addPluginResponse(result);
    pluginResponses.put(gav, result);

    eventBus.post(pluginEvent);
  }
}
