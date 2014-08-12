/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rapture.internal;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.rapture.internal.ui.StateComponent;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.nexus.webresources.GeneratedWebResource;
import org.sonatype.nexus.webresources.WebResource;
import org.sonatype.nexus.webresources.WebResourceBundle;
import org.sonatype.nexus.webresources.WebResourceService;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.goodies.template.TemplateEngine;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Rapture {@link WebResourceBundle}.
 *
 * Provides resources:
 * <ul>
 *   <li>{@code /index.html}</li>
 *   <li>{@code /static/rapture/bootstrap.js}</li>
 *   <li>{@code /static/rapture/app.js}</li>
 * </ul>
 *
 * @since 3.0
 */
@Named
@Singleton
public class RaptureWebResourceBundle
    extends ComponentSupport
    implements WebResourceBundle
{
  private final Provider<SystemStatus> systemStatusProvider;

  private final Provider<HttpServletRequest> servletRequestProvider;

  private final Provider<WebResourceService> webResourceServiceProvider;

  private final Provider<StateComponent> stateComponentProvider;

  private final TemplateEngine templateEngine;

  private final Gson gson;

  @Inject
  public RaptureWebResourceBundle(final Provider<SystemStatus> systemStatusProvider,
                                  final Provider<HttpServletRequest> servletRequestProvider,
                                  final Provider<WebResourceService> webResourceServiceProvider,
                                  final Provider<StateComponent> stateComponentProvider,
                                  final TemplateEngine templateEngine)
  {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
    this.servletRequestProvider = checkNotNull(servletRequestProvider);
    this.webResourceServiceProvider = checkNotNull(webResourceServiceProvider);
    this.stateComponentProvider = checkNotNull(stateComponentProvider);
    this.templateEngine = checkNotNull(templateEngine);

    // FIXME: Should be updated to use Jackson?
    gson = new GsonBuilder().setPrettyPrinting().create();
  }

  @Override
  public List<WebResource> getResources() {
    return ImmutableList.of(
        index_html(),
        bootstrap_js(),
        app_js()
    );
  }

  private abstract class TemplateWebResource
      extends GeneratedWebResource
  {
    private URL template(final String name) {
      URL template = getClass().getResource(name);
      checkState(template != null, "Missing template: %s", name);
      return template;
    }

    protected byte[] render(final String template, final TemplateParameters parameters) throws IOException {
      log.trace("Rendering template: {}, with params: {}", template, parameters);
      return templateEngine.render(this, template(template), parameters).getBytes();
    }
  }

  /**
   * The index.html resource.
   */
  private WebResource index_html() {
    return new TemplateWebResource()
    {
      @Override
      public String getPath() {
        return "/index.html";
      }

      @Override
      public String getContentType() {
        return HTML;
      }

      @Override
      protected byte[] generate() throws IOException {
        return render("index.vm", new TemplateParameters()
                .set("baseUrl", BaseUrlHolder.get())
                .set("debug", isDebug())
                .set("urlSuffix", generateUrlSuffix())
        );
      }
    };
  }

  /**
   * The bootstrap.js resource.
   */
  private WebResource bootstrap_js() {
    return new TemplateWebResource()
    {
      @Override
      public String getPath() {
        return "/static/rapture/bootstrap.js";
      }

      @Override
      public String getContentType() {
        return JAVASCRIPT;
      }

      @Override
      protected byte[] generate() throws IOException {
        return render("bootstrap.vm", new TemplateParameters()
                .set("baseUrl", BaseUrlHolder.get())
                .set("debug", isDebug())
                .set("namespaces", getNamespaces())
        );
      }
    };
  }

  /**
   * The app.js resource.
   */
  private WebResource app_js() {
    return new TemplateWebResource()
    {
      @Override
      public String getPath() {
        return "/static/rapture/app.js";
      }

      @Override
      public String getContentType() {
        return JAVASCRIPT;
      }

      @Override
      protected byte[] generate() throws IOException {
        return render("app.vm", new TemplateParameters()
                .set("baseUrl", BaseUrlHolder.get())
                .set("debug", isDebug())
                .set("state", gson.toJson(getState()))
                .set("pluginConfigs", getPluginConfigs())
        );
      }
    };
  }

  /**
   * Generate URL suffix to use on all requests when loading the index.
   */
  private String generateUrlSuffix() {
    StringBuilder buff = new StringBuilder();
    String version = systemStatusProvider.get().getVersion();
    buff.append("_v=").append(version);

    // if version is a SNAPSHOT, then append additional timestamp to disable cache
    if (version.endsWith("SNAPSHOT")) {
      buff.append("&_dc=").append(System.currentTimeMillis());
    }

    // when debug, add parameter
    if (isDebug()) {
      buff.append("&debug=true");
    }

    return buff.toString();
  }

  /**
   * Check if ?debug parameter is given on the request.
   */
  private boolean isDebug() {
    HttpServletRequest request = servletRequestProvider.get();
    String value = request.getParameter("debug");

    // not set
    if (value == null) {
      return false;
    }

    // ?debug
    if (value.trim().length() == 0) {
      return true;
    }

    // ?debug=<flag>
    return Boolean.parseBoolean(value);
  }

  /**
   * Returns the initial state for the application.
   */
  private Map<String, Object> getState() {
    return stateComponentProvider.get().getValues(Maps.<String, String>newHashMap());
  }

  private static final String NS_PREFIX = "/static/rapture/NX/";

  private static final String PLUGIN_CONFIG_SUFFIX = "/app/PluginConfig.js";

  /**
   * Find all plugin configs.
   */
  private List<String> getPluginConfigs() {
    List<String> classNames = Lists.newArrayList();

    for (String path : webResourceServiceProvider.get().getPaths()) {
      if (path.startsWith(NS_PREFIX) && path.endsWith(PLUGIN_CONFIG_SUFFIX)) {
        // rebuild the class name which has NX. prefix and minus the .js suffix
        String name = path.substring(NS_PREFIX.length() - "NX/".length(), path.length() - ".js".length());
        // convert path to class-name
        name = name.replace("/", ".");
        classNames.add(name);
      }
    }

    return classNames;
  }

  /**
   * Determine all plugin namespaces.
   */
  private List<String> getNamespaces() {
    List<String> namespaces = Lists.newArrayList();
    for (String className : getPluginConfigs()) {
      int i = className.indexOf(".app.PluginConfig");
      String ns = className.substring(0, i);
      if (!ns.equals("NX")) {
        // add namespace if its not the default
        namespaces.add(ns);
      }
    }
    return namespaces;
  }
}
