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
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.rapture.internal.ui.StateComponent;
import org.sonatype.nexus.web.BaseUrlHolder;
import org.sonatype.nexus.webresources.WebResourceService;
import org.sonatype.sisu.goodies.template.TemplateParameters;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Provides {@code /static/rapture/app.js}.
 *
 * @since 3.0
 */
@Named
@Singleton
public class AppJsWebResource
    extends TemplateWebResource
{

  private static final String NS_PREFIX = "/static/rapture/NX/";

  private static final String PLUGIN_CONFIG_SUFFIX = "/app/PluginConfig.js";

  private final ApplicationStatusSource applicationStatusSource;

  private final StateComponent stateComponent;

  private final Provider<WebResourceService> webResourceServiceProvider; // avoid circular dep

  private final Gson gson;

  @Inject
  public AppJsWebResource(final ApplicationStatusSource applicationStatusSource,
                          final StateComponent stateComponent,
                          final Provider<WebResourceService> webResourceServiceProvider)
  {
    this.applicationStatusSource = checkNotNull(applicationStatusSource);
    this.stateComponent = checkNotNull(stateComponent);
    this.webResourceServiceProvider = checkNotNull(webResourceServiceProvider);

    gson = new GsonBuilder().setPrettyPrinting().create();
  }

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
    List<String> classNames = getPluginConfigClassNames();
    if (classNames.isEmpty()) {
      log.warn("Did not detect any rapture plugin configurations");
    }
    else {
      log.debug("Plugin config class names: {}", classNames);
    }

    return render("app.vm", new TemplateParameters()
        .set("pluginConfigClassNames", join(classNames))
        .set("baseUrl", BaseUrlHolder.get())
        .set("status", applicationStatusSource.getSystemStatus())
        .set("state", gson.toJson(stateComponent.getValues(Maps.<String, String>newHashMap())))
    );
  }

  /**
   * Returns a list of all {@code NX._package_.app.PluginConfig} extjs classes.
   */
  private List<String> getPluginConfigClassNames() {
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
   * Joins the list of strings quoted for javascript list members (in side of [ ... ]).
   */
  private String join(final List<String> list) {
    StringBuilder buff = new StringBuilder();
    Iterator<String> iter = list.iterator();
    while (iter.hasNext()) {
      String pkg = iter.next();
      buff.append("'").append(pkg).append("'");
      if (iter.hasNext()) {
        buff.append(",");
      }
    }
    return buff.toString();
  }
}
