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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.SystemStatus;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.nexus.webresources.WebResourceService;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@code /index.html}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class IndexHtmlWebResource
    extends TemplateWebResource
{
  private final Provider<SystemStatus> systemStatusProvider;

  private final Provider<HttpServletRequest> servletRequestProvider;

  private final Provider<WebResourceService> webResourceServiceProvider;

  @Inject
  public IndexHtmlWebResource(final Provider<SystemStatus> systemStatusProvider,
                              final Provider<HttpServletRequest> servletRequestProvider,
                              final Provider<WebResourceService> webResourceServiceProvider)
  {
    this.systemStatusProvider = checkNotNull(systemStatusProvider);
    this.servletRequestProvider = checkNotNull(servletRequestProvider);
    this.webResourceServiceProvider = checkNotNull(webResourceServiceProvider);
  }

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
    String baseUrl = BaseUrlHolder.get();
    log.trace("Base URL: {}", baseUrl);

    boolean debug = isDebug();
    log.trace("Debug: {}", debug);

    String urlSuffix = generateUrlSuffix();
    log.trace("URL suffix: {}", urlSuffix);

    List<String> preloadScripts = findPreloadScripts();
    log.trace("Preload scripts: {}", preloadScripts);

    return render("index.vm", new TemplateParameters()
            .set("baseUrl", baseUrl)
            .set("debug", debug)
            .set("urlSuffix", urlSuffix)
            .set("preloadScripts", preloadScripts)
    );
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

  private List<String> findPreloadScripts() {
    //List<String> paths = Lists.newArrayList();
    //for (String path : webResourceServiceProvider.get().getPaths()) {
    //  // HACK: skip default framework shit
    //  if (path.contains("baseapp-prod.js") || path.contains("extdirect-prod.js")) {
    //    continue;
    //  }
    //  if (path.startsWith("/static/rapture/") && path.endsWith("-prod.js")) {
    //    // strip "/" prefix
    //    path = path.substring(1, path.length());
    //    paths.add(path);
    //  }
    //}
    //return paths;

    return ImmutableList.of(
        "static/rapture/nexus-rapture-plugin-prod.js",
        "static/rapture/nexus-coreui-plugin-prod.js"
    );
  }
}
