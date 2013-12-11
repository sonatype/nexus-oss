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

package org.sonatype.nexus.web.internal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.web.TemplateRenderer;
import org.sonatype.nexus.web.WebUtils;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.VelocityException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link TemplateRenderer} using Apache Velocity.
 *
 * @since 2.8
 */
@Singleton
@Named
public class VelocityTemplateRenderer
    extends ComponentSupport
    implements TemplateRenderer
{
  private final Provider<VelocityEngine> velocityEngineProvider;

  private final WebUtils webUtils;

  private final String applicationVersion;

  @Inject
  public VelocityTemplateRenderer(final Provider<VelocityEngine> velocityEngineProvider,
                                  final WebUtils webUtils,
                                  final ApplicationStatusSource applicationStatusSource)
  {
    this.velocityEngineProvider = checkNotNull(velocityEngineProvider);
    this.webUtils = checkNotNull(webUtils);
    this.applicationVersion = checkNotNull(applicationStatusSource).getSystemStatus().getVersion();
  }

  @Override
  public void renderErrorPage(final HttpServletRequest request,
                              final HttpServletResponse response,
                              final int responseCode,
                              final String reasonPhrase,
                              final String errorDescription,
                              final Exception exception)
      throws IOException
  {
    checkNotNull(request);
    checkNotNull(response);
    checkArgument(responseCode >= 400);
    checkNotNull(errorDescription);

    final Map<String, Object> dataModel = Maps.newHashMap();
    dataModel.put("nexusRoot", webUtils.getAppRootUrl(request));
    dataModel.put("nexusVersion", applicationVersion);
    dataModel.put("statusCode", responseCode);
    dataModel.put("statusName", Strings.isNullOrEmpty(reasonPhrase) ? errorDescription : reasonPhrase);
    dataModel.put("errorDescription", StringEscapeUtils.escapeHtml(errorDescription));

    if (null != exception) {
      dataModel.put("errorStackTrace", StringEscapeUtils.escapeHtml(ExceptionUtils.getStackTrace(exception)));
    }

    if (Strings.isNullOrEmpty(reasonPhrase)) {
      response.setStatus(responseCode);
    }
    else {
      response.setStatus(responseCode, reasonPhrase);
    }

    render(template("/org/sonatype/nexus/web/internal/errorPageContentHtml.vm",
        VelocityTemplateRenderer.class.getClassLoader()), dataModel, response);
  }

  @Override
  public TemplateLocator template(final String name, final ClassLoader classLoader) {
    return new TemplateLocator()
    {
      @Override
      public String name() {
        return name;
      }

      @Override
      public ClassLoader classloader() {
        return classLoader;
      }
    };
  }

  @Override
  public void render(final TemplateLocator templateLocator,
                     final Map<String, Object> dataModel,
                     final HttpServletResponse response) throws IOException
  {
    render(getVelocityTemplate(templateLocator), dataModel, response);
  }

  // ==

  private void render(final Template template, final Map<String, Object> dataModel, final HttpServletResponse response)
      throws IOException
  {
    // ATM all templates render HTML
    response.setContentType("text/html");
    final Context context = new VelocityContext(dataModel);
    try (final OutputStream outputStream = response.getOutputStream()) {
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
      response.flushBuffer();
    }
    catch (IOException e) {
      // NEXUS-3442: IOEx should be propagated as is
      throw e;
    }
    catch (VelocityException e) {
      throw new IOException("Template processing error: " + e, e);
    }
  }

  private Template getVelocityTemplate(final TemplateLocator templateLocator) {
    // NOTE: Velocity's ClasspathResourceLoader goes for TCCL 1st, then would fallback to "system"
    // (in this case the classloader where Velocity is loaded) classloader, so we must set TCCL
    final ClassLoader original = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(templateLocator.classloader());
      return velocityEngineProvider.get().getTemplate(templateLocator.name());
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Cannot get the template with name " + templateLocator.name(), e);
    }
    finally {
      Thread.currentThread().setContextClassLoader(original);
    }
  }
}
