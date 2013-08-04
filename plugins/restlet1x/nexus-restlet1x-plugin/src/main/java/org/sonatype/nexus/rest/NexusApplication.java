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

import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.RetargetableRestlet;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.ProtectedPathManager;
import org.sonatype.sisu.goodies.eventbus.EventBus;

import com.google.common.eventbus.Subscribe;
import com.thoughtworks.xstream.XStream;
import org.apache.shiro.util.AntPathMatcher;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Router;
import org.restlet.service.StatusService;

/**
 * Nexus REST Application. This will ultimately replace the two applications we have now, and provide us plugin UI
 * extension capability.
 *
 * @author cstamas
 */
@Component(role = Application.class, hint = "nexus")
public class NexusApplication
    extends PlexusRestletApplicationBridge
{
  @Requirement
  private EventBus eventBus;

  @Requirement
  private ProtectedPathManager protectedPathManager;

  /**
   * HACK directly injecting indexTemplate managed resource broke Nexus startup here, wasn't resolvable. It's only
   * available in the collection later. This should be generalized for all "ManagedPlexusResource" (all resources
   * that
   * are bound from /,  not /service/local) but we need a little bit of order here to have resources being able to
   * override the default UI. (e.g. licensing)
   */
  @Requirement
  private Map<String, ManagedPlexusResource> managedResources;

  @Requirement(hint = "licenseTemplate", optional = true)
  private ManagedPlexusResource licenseTemplateResource;

  @Requirement(hint = "enterLicenseTemplate", optional = true)
  private ManagedPlexusResource enterLicenseTemplateResource;

  @Requirement(hint = "content")
  private ManagedPlexusResource contentResource;

  @Requirement(hint = "StatusPlexusResource")
  private ManagedPlexusResource statusPlexusResource;

  @Requirement(hint = "CommandPlexusResource")
  private ManagedPlexusResource commandPlexusResource;

  @Requirement(role = NexusResourceBundle.class)
  private List<NexusResourceBundle> nexusResourceBundles;

  @Requirement(role = NexusApplicationCustomizer.class)
  private List<NexusApplicationCustomizer> customizers;

  @Requirement(role = ErrorReportingManager.class)
  private ErrorReportingManager errorManager;

  @Requirement(role = StatusService.class)
  private StatusService statusService;

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

    // Add error manager to context
    getContext().getAttributes().put(ErrorReportingManager.class.getName(), errorManager);

    // SERVICE (two always connected, unrelated to isStarted)

    attach(getApplicationRouter(), false, statusPlexusResource);

    attach(getApplicationRouter(), false, commandPlexusResource);

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
