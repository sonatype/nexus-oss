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

package org.sonatype.nexus.rest;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.internal.DevModeResources;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.rest.internal.DevModeResourceFinder;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.RetargetableRestlet;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.ProtectedPathManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.XStream;
import org.apache.shiro.util.AntPathMatcher;
import org.restlet.Directory;
import org.restlet.Router;
import org.restlet.service.StatusService;

/**
 * Nexus REST Application. This will ultimately replace the two applications we have now, and provide us plugin UI
 * extension capability.
 *
 * @author cstamas
 */
@Named("nexus")
@Singleton
public class NexusApplication
    extends PlexusRestletApplicationBridge
{
  private final EventBus eventBus;

  private final ProtectedPathManager protectedPathManager;

  /**
   * HACK directly injecting indexTemplate managed resource broke Nexus startup here, wasn't resolvable. It's only
   * available in the collection later. This should be generalized for all "ManagedPlexusResource" (all resources
   * that
   * are bound from /,  not /service/local) but we need a little bit of order here to have resources being able to
   * override the default UI. (e.g. licensing)
   */
  private final Map<String, ManagedPlexusResource> managedResources;

  private final ManagedPlexusResource licenseTemplateResource;

  private final ManagedPlexusResource enterLicenseTemplateResource;

  private final ManagedPlexusResource contentResource;

  private final ManagedPlexusResource statusPlexusResource;

  private final List<NexusResourceBundle> nexusResourceBundles;

  private final List<NexusApplicationCustomizer> customizers;

  private final StatusService statusService;

  private final MimeSupport mimeSupport;

  @Inject
  public NexusApplication(final EventBus eventBus,
                          final ProtectedPathManager protectedPathManager,
                          final Map<String, ManagedPlexusResource> managedResources,
                          final @Named("licenseTemplate") @Nullable ManagedPlexusResource licenseTemplateResource,
                          final @Named("enterLicenseTemplate") @Nullable ManagedPlexusResource enterLicenseTemplateResource,
                          final @Named("content") ManagedPlexusResource contentResource,
                          final @Named("StatusPlexusResource") ManagedPlexusResource statusPlexusResource,
                          final List<NexusResourceBundle> nexusResourceBundles,
                          final List<NexusApplicationCustomizer> customizers,
                          final StatusService statusService,
                          final MimeSupport mimeSupport)
  {
    this.eventBus = eventBus;
    this.protectedPathManager = protectedPathManager;
    this.managedResources = managedResources;
    this.licenseTemplateResource = licenseTemplateResource;
    this.enterLicenseTemplateResource = enterLicenseTemplateResource;
    this.contentResource = contentResource;
    this.statusPlexusResource = statusPlexusResource;
    this.nexusResourceBundles = nexusResourceBundles;
    this.customizers = customizers;
    this.statusService = statusService;
    this.mimeSupport = mimeSupport;
  }

  // HACK: Too many places were using new NexusApplication() ... fuck it
  @VisibleForTesting
  public NexusApplication() {
    this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  @Subscribe
  public void onEvent(final NexusStartedEvent evt) {
    recreateRoot(true);
    afterCreateRoot((RetargetableRestlet) getRoot());
  }

  @Subscribe
  public void onEvent(final NexusStoppedEvent evt) {
    recreateRoot(false);
  }

  /**
   * Adding this as config change listener.
   */
  @Override
  protected void doConfigure() {
    // NEXUS-2883: turning off Range support for now
    getRangeService().setEnabled(false);

    // adding ourselves as listener
    eventBus.register(this);
  }

  /**
   * Configuring xstream with our aliases.
   */
  @Override
  public XStream doConfigureXstream(XStream xstream) {
    return org.sonatype.nexus.rest.model.XStreamConfigurator.configureXStream(xstream);
  }

  @Override
  protected Router initializeRouter(Router root, boolean isStarted) {
    // ========
    // SERVICE

    // service router
    Router applicationRouter = new Router(getContext());

    // attaching filter to a root on given URI
    attach(root, false, "/service/" + AbstractNexusPlexusResource.NEXUS_INSTANCE_LOCAL, applicationRouter);

    // return the swapped router
    return applicationRouter;
  }

  @Override
  protected void afterCreateRoot(RetargetableRestlet root) {
    // customizers
    for (NexusApplicationCustomizer customizer : customizers) {
      customizer.customize(this, root);
    }
  }

  /**
   * "Decorating" the root with our resources.
   *
   * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
   */
  @Override
  protected void doCreateRoot(Router root, boolean isStarted) {
    if (!isStarted) {
      return;
    }

    // set our StatusService
    setStatusService(statusService);

    // SERVICE (two always connected, unrelated to isStarted)

    attach(getApplicationRouter(), false, statusPlexusResource);

    // ==========
    // INDEX.HTML and WAR contents
    // To redirect "uncaught" requests to indexTemplateResource
    ManagedPlexusResource indexRedirectingResource = managedResources.get("IndexRedirectingPlexusResource");
    attach(root, true, "", new NexusPlexusResourceFinder(getContext(), indexRedirectingResource));
    attach(root, true, "/", new NexusPlexusResourceFinder(getContext(), indexRedirectingResource));

    // the indexTemplateResource
    attach(root, false, managedResources.get("indexTemplate"));
    if (licenseTemplateResource != null) {
      attach(root, false, licenseTemplateResource);
    }
    if (enterLicenseTemplateResource != null) {
      attach(root, false, enterLicenseTemplateResource);
    }

    // publish the WAR contents
    Directory rootDir = new NexusDirectory(getContext(), "war:///");
    rootDir.setListingAllowed(false);
    rootDir.setNegotiateContent(false);
    attach(root, false, "/", rootDir);

    // ================
    // STATIC RESOURCES

    if (nexusResourceBundles.size() > 0) {
      for (NexusResourceBundle bundle : nexusResourceBundles) {
        List<StaticResource> resources = bundle.getContributedResouces();

        if (resources != null) {
          for (StaticResource resource : resources) {
            attach(root, false, resource.getPath(), new StaticResourceFinder(getContext(), resource));
          }
        }
      }
    }

    // Attach to "/static" in case that dev mode is on
    // The finder will only be called if the path under static is not found (as those are more specific)
    if (DevModeResources.hasResourceLocations()) {
      attach(root, false, "/static", new DevModeResourceFinder(mimeSupport, getContext(), "/static"));
    }

    // =======
    // CONTENT

    // prepare for browser diversity :)
    BrowserSensingFilter bsf = new BrowserSensingFilter(getContext());

    // mounting it
    attach(root, false, "/content", bsf);

    bsf.setNext(new NexusPlexusResourceFinder(getContext(), contentResource));

    // protecting the content service manually
    this.protectedPathManager.addProtectedResource("/content"
        + contentResource.getResourceProtection().getPathPattern(), "noSessionCreation,"
        + contentResource.getResourceProtection().getFilterExpression());

    // protecting service resources with "wall" permission
    this.protectedPathManager.addProtectedResource("/service/local/**",
        "noSessionCreation,authcBasic,perms[nexus:permToCatchAllUnprotecteds]");
  }

  private final AntPathMatcher shiroAntPathMatcher = new AntPathMatcher();

  @Override
  protected void handlePlexusResourceSecurity(PlexusResource resource) {
    PathProtectionDescriptor descriptor = resource.getResourceProtection();

    if (descriptor == null) {
      return;
    }

    // sanity check: path protection descriptor path and resource URI must align
    if (!shiroAntPathMatcher.match(descriptor.getPathPattern(), resource.getResourceUri())) {
      throw new IllegalStateException(String.format(
          "Plexus resource %s would attach to URI=%s but protect path=%s that does not matches URI!",
          resource.getClass().getName(), resource.getResourceUri(),
          descriptor.getPathPattern()));
    }

    String filterExpression = descriptor.getFilterExpression();
    if (filterExpression != null && !filterExpression.contains("authcNxBasic")) {
      // don't create session unless the user logs in from the UI
      filterExpression = "noSessionCreation," + filterExpression;
    }

    this.protectedPathManager.addProtectedResource("/service/local" + descriptor.getPathPattern(), filterExpression);
  }

  @Override
  protected void attach(Router router, boolean strict, PlexusResource resource) {
    handlePlexusResourceSecurity(resource);
    attach(router, strict, resource.getResourceUri(), new NexusPlexusResourceFinder(getContext(), resource));
  }
}
