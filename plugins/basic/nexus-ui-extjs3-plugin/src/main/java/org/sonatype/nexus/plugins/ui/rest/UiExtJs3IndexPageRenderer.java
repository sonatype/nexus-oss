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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.plugins.rest.NexusIndexHtmlCustomizer;
import org.sonatype.nexus.plugins.ui.BuildNumberService;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor;
import org.sonatype.nexus.plugins.ui.contribution.UiContributor.UiContribution;
import org.sonatype.nexus.webresources.IndexPageRenderer;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.codehaus.plexus.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.google.common.base.Preconditions.checkNotNull;

@Named
@Singleton
public class UiExtJs3IndexPageRenderer
    extends ComponentSupport
    implements IndexPageRenderer
{
  private final ApplicationStatusSource applicationStatusSource;

  private final Map<String, NexusIndexHtmlCustomizer> bundles;

  private final Provider<VelocityEngine> velocityEngineProvider;

  private final BuildNumberService buildNumberService;

  private final Set<UiContributor> rJsContributors;

  @Inject
  public UiExtJs3IndexPageRenderer(final Map<String, NexusIndexHtmlCustomizer> bundles,
                                   final ApplicationStatusSource applicationStatusSource,
                                   final Provider<VelocityEngine> velocityEngineProvider,
                                   final BuildNumberService buildNumberService,
                                   final Set<UiContributor> uiContributors)
  {
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.bundles = checkNotNull(bundles);
    this.velocityEngineProvider = checkNotNull(velocityEngineProvider);
    this.buildNumberService = checkNotNull(buildNumberService);
    this.rJsContributors = checkNotNull(uiContributors);
  }

  @Override
  public void render(final HttpServletRequest request, final HttpServletResponse response, final String appRootUrl)
      throws IOException
  {
    log.debug("Rendering index");
    final SystemStatus systemStatus = applicationStatusSource.getSystemStatus();
    final Map<String, Object> templatingContext = Maps.newHashMap();
    templatingContext.put("serviceBase", "service/local");
    templatingContext.put("contentBase", "content");
    templatingContext.put("nexusVersion", systemStatus.getVersion());
    templatingContext.put("nexusRoot", appRootUrl);

    // gather plugin stuff
    final Map<String, Object> topContext = Maps.newHashMap(templatingContext);
    List<String> pluginPreHeadContributions = Lists.newArrayList();
    List<String> pluginPostHeadContributions = Lists.newArrayList();
    List<String> pluginPreBodyContributions = Lists.newArrayList();
    List<String> pluginPostBodyContributions = Lists.newArrayList();
    List<String> pluginJsFiles = Lists.newArrayList();

    Map<String, Object> pluginContext;
    for (String key : bundles.keySet()) {
      pluginContext = Maps.newHashMap(topContext);
      final NexusIndexHtmlCustomizer bundle = bundles.get(key);
      log.debug("Processing customizations: {} -> {}", key, bundle);
      pluginContext.put("bundle", bundle);
      // pre HEAD
      final String preHeadTemplate = bundle.getPreHeadContribution(pluginContext);
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
      final String preBodyTemplate = bundle.getPreBodyContribution(pluginContext);
      evaluateIfNeeded(pluginContext, preBodyTemplate, pluginPreBodyContributions);
      // post BODY
      final String postBodyTemplate = bundle.getPostBodyContribution(pluginContext);
      evaluateIfNeeded(pluginContext, postBodyTemplate, pluginPostBodyContributions);
    }

    templatingContext.put("appName", systemStatus.getAppName());
    templatingContext.put("formattedAppName", systemStatus.getFormattedAppName());

    templatingContext.put("pluginPreHeadContributions", pluginPreHeadContributions);
    templatingContext.put("pluginPostHeadContributions", pluginPostHeadContributions);

    templatingContext.put("pluginPreBodyContributions", pluginPreBodyContributions);
    templatingContext.put("pluginPostBodyContributions", pluginPostBodyContributions);

    templatingContext.put("pluginJsFiles", pluginJsFiles);

    final String query = request.getQueryString();
    final boolean debugMode = query != null && query.contains("debug");
    templatingContext.put("debug", debugMode);

    List<UiContribution> contributions = Lists.newArrayList();
    for (UiContributor rJs : rJsContributors) {
      UiContribution contribution = rJs.contribute(debugMode);
      if (contribution.isEnabled()) {
        contributions.add(contribution);
      }
    }
    templatingContext.put("rJsContributions", contributions);
    templatingContext.put("buildQualifier", buildNumberService.getBuildNumber());

    render(getTemplate("/org/sonatype/nexus/plugins/ui/rest/index.vm"), templatingContext, response);
  }

  /**
   * Evaluates template "snippets" that will be used to construct complete template.
   */
  private void evaluateIfNeeded(Map<String, Object> context, String template,
                                List<String> results)
  {
    if (!Strings.isNullOrEmpty(template)) {
      final StringWriter result = new StringWriter();
      if (velocityEngineProvider.get()
          .evaluate(new VelocityContext(context), result, getClass().getName(), template)) {
        results.add(result.toString());
      }
      else {
        throw new IllegalStateException(
            "Was not able to interpolate " + template + " (check the logs for Velocity messages about the reason)!");
      }
    }
  }

  private void render(final Template template, final Map<String, Object> dataModel, final HttpServletResponse response)
      throws IOException
  {
    // ATM all templates render HTML
    response.setContentType("text/html");

    final OutputStream outputStream = response.getOutputStream();

    final Context context = new VelocityContext(dataModel);
    try {
      final Writer tmplWriter;
      // Load the template
      if (template.getEncoding() == null) {
        tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
      }
      else {
        tmplWriter = new BufferedWriter(new OutputStreamWriter(outputStream, template.getEncoding()));
      }

      // Process the template
      template.merge(context, tmplWriter);
      tmplWriter.flush();
    }
    catch (IOException e) {
      // NEXUS-3442
      // IOEx should be propagated as is
      throw e;
    }
    catch (Exception e) {
      // All other (Velocity exceptions are RuntimeExcptions!) to be wrapped, but preserve cause too
      throw new IOException("Template processing error: " + e.getMessage(), e);
    }
  }

  private Template getTemplate(final String templateName) {
    // NOTE: Velocity's ClasspathResourceLoader goes for TCCL 1st, then would fallback to "system"
    // (in this case the classloader where Velocity is loaded) classloader
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    try {
      if (templateName.startsWith("/")) {
        return velocityEngineProvider.get().getTemplate(templateName);
      }
      else {
        return velocityEngineProvider.get().getTemplate(
            getClass().getPackage().getName().replace(".", "/") + "/" + templateName);
      }
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Cannot get the template with name " + String.valueOf(templateName), e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }
}
