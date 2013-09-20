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

package org.sonatype.nexus.plugins.ui.rest;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.ui.BuildNumberService;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor.UiContribution;
import org.sonatype.plexus.rest.ReferenceFactory;
import org.sonatype.plexus.rest.representation.VelocityRepresentation;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.sisu.velocity.Velocity;

import com.google.common.collect.Lists;
import org.apache.velocity.VelocityContext;
import org.codehaus.plexus.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

@Named("indexTemplate")
@Singleton
@Typed(ManagedPlexusResource.class)
public class IndexTemplatePlexusResource
    extends AbstractPlexusResource
    implements ManagedPlexusResource
{
  private ApplicationStatusSource applicationStatusSource;

  private ReferenceFactory referenceFactory;

  private Map<String, NexusIndexHtmlCustomizer> bundles;

  private Velocity velocity;

  private BuildNumberService buildNumberService;

  private Set<UiContributor> rJsContributors;

  String templateFilename;

  @Inject
  public IndexTemplatePlexusResource(final Map<String, NexusIndexHtmlCustomizer> bundles, final ApplicationStatusSource applicationStatusSource,
                                     final ReferenceFactory referenceFactory,
                                     final @Named("${index.template.file:-templates/index.vm}") String templateFilename,
                                     final Velocity velocity, final BuildNumberService buildNumberService,
                                     final Set<UiContributor> uiContributors)
  {
    this();

    this.bundles = bundles;
    this.applicationStatusSource = applicationStatusSource;
    this.referenceFactory = referenceFactory;
    this.templateFilename = templateFilename;
    this.velocity = velocity;
    this.buildNumberService = buildNumberService;
    this.rJsContributors = uiContributors;
  }

  public IndexTemplatePlexusResource() {
    setReadable(true);

    setModifiable(false);
  }

  @Override
  public Object getPayloadInstance() {
    // RO resource
    return null;
  }

  @Override
  public String getResourceUri() {
    return "/index.html";
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    // unprotected
    return null;
  }

  public List<Variant> getVariants() {
    List<Variant> result = super.getVariants();

    result.clear();

    result.add(new Variant(MediaType.APPLICATION_XHTML_XML));

    return result;
  }

  public Representation get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    return render(context, request, response, variant);
  }

  protected VelocityRepresentation render(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    getLogger().debug("Rendering index");

    final SystemStatus systemStatus = applicationStatusSource.getSystemStatus();

    Map<String, Object> templatingContext = new HashMap<String, Object>();

    templatingContext.put("serviceBase", "service/local");

    templatingContext.put("contentBase", "content");

    templatingContext.put("nexusVersion", systemStatus.getVersion());

    templatingContext.put("nexusRoot", referenceFactory.getContextRoot(request).toString());

    // gather plugin stuff

    Map<String, Object> topContext = new HashMap<String, Object>(templatingContext);

    Map<String, Object> pluginContext = null;

    List<String> pluginPreHeadContributions = new ArrayList<String>();
    List<String> pluginPostHeadContributions = new ArrayList<String>();

    List<String> pluginPreBodyContributions = new ArrayList<String>();
    List<String> pluginPostBodyContributions = new ArrayList<String>();

    List<String> pluginJsFiles = new ArrayList<String>();


    for (String key : bundles.keySet()) {
      pluginContext = new HashMap<String, Object>(topContext);

      NexusIndexHtmlCustomizer bundle = bundles.get(key);
      getLogger().debug("Processing customizations: {} -> {}", key, bundle);

      pluginContext.put("bundle", bundle);

      // pre HEAD

      String preHeadTemplate = bundle.getPreHeadContribution(pluginContext);

      evaluateIfNeeded(pluginContext, preHeadTemplate, pluginPreHeadContributions);

      // post HEAD

      String postHeadTemplate = bundle.getPostHeadContribution(pluginContext);
      if (!StringUtils.isEmpty(postHeadTemplate)) {
        final Document html = Jsoup.parse(postHeadTemplate);
        final Elements scripts = html.select("script");
        for (Element script : scripts) {
          final String src = script.attr("src");
          if (!src.isEmpty()) {
            pluginJsFiles.add(src);
            script.remove();
          }
        }
        postHeadTemplate = html.head().children().toString();
        evaluateIfNeeded(pluginContext, postHeadTemplate, pluginPostHeadContributions);
      }

      // pre BODY

      String preBodyTemplate = bundle.getPreBodyContribution(pluginContext);

      evaluateIfNeeded(pluginContext, preBodyTemplate, pluginPreBodyContributions);

      // post BODY

      String postBodyTemplate = bundle.getPostBodyContribution(pluginContext);

      evaluateIfNeeded(pluginContext, postBodyTemplate, pluginPostBodyContributions);
    }

    templatingContext.put("appName", systemStatus.getAppName());
    templatingContext.put("formattedAppName", systemStatus.getFormattedAppName());

    templatingContext.put("pluginPreHeadContributions", pluginPreHeadContributions);
    templatingContext.put("pluginPostHeadContributions", pluginPostHeadContributions);

    templatingContext.put("pluginPreBodyContributions", pluginPreBodyContributions);
    templatingContext.put("pluginPostBodyContributions", pluginPostBodyContributions);

    templatingContext.put("pluginJsFiles", pluginJsFiles);

    final String query = request.getResourceRef().getQuery();
    final boolean debugMode = query != null && query.contains("debug");
    templatingContext.put("debug", debugMode);

    List<UiContributor.UiContribution> contributions = Lists.newArrayList();
    for (UiContributor rJs : rJsContributors) {
      UiContribution contribution = rJs.contribute(debugMode);
      if (contribution.isEnabled()) {
        contributions.add(contribution);
      }
    }
    templatingContext.put("rJsContributions", contributions);

    templatingContext.put("buildQualifier", buildNumberService.getBuildNumber());

    return new VelocityRepresentation(context, templateFilename, getClass().getClassLoader(), templatingContext,
        MediaType.TEXT_HTML);
  }

  protected void evaluateIfNeeded(Map<String, Object> context, String template,
                                  List<String> results)
      throws ResourceException
  {
    if (!StringUtils.isEmpty(template)) {
      StringWriter result = new StringWriter();

      try {
        if (velocity.getEngine().evaluate(new VelocityContext(context), result, getClass().getName(), template)) {
          results.add(result.toString());
        }
        else {
          throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
              "Was not able to interpolate (check the logs for Velocity messages about the reason)!");
        }
      }
      catch (Exception e) {
        throw new ResourceException(
            Status.SERVER_ERROR_INTERNAL,
            "Got Exception exception during Velocity invocation!",
            e);
      }
    }
  }
}
