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

package org.sonatype.nexus.security.filter.authz;

import java.io.IOException;
import java.util.regex.Matcher;

import javax.inject.Inject;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.RequestContext;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

import org.apache.shiro.web.util.WebUtils;

/**
 * A filter that maps the targetId from the Request.
 *
 * @author cstamas
 */
public class NexusTargetMappingAuthorizationFilter
    extends AbstractNexusAuthorizationFilter
{
  @Inject
  private RepositoryRouter rootRouter;

  private String pathReplacement;

  public String getPathReplacement() {
    if (pathReplacement == null) {
      pathReplacement = "";
    }

    return pathReplacement;
  }

  public void setPathReplacement(String pathReplacement) {
    this.pathReplacement = pathReplacement;
  }

  public String getResourceStorePath(ServletRequest request) {
    String path = WebUtils.getPathWithinApplication((HttpServletRequest) request);

    if (getPathPrefix() != null) {
      Matcher m = this.getPathPrefixPattern().matcher(path);

      if (m.matches()) {
        path = getPathReplacement();

        // TODO: hardcoded currently
        if (path.contains("@1")) {
          path = path.replaceAll("@1", Matcher.quoteReplacement(m.group(1)));
        }

        if (path.contains("@2")) {
          path = path.replaceAll("@2", Matcher.quoteReplacement(m.group(2)));
        }
        // and so on... this will be reworked to be dynamic
      }
      else {
        throw new IllegalArgumentException(
            "The request path does not matches the incoming request? This is misconfiguration in web.xml!");
      }
    }

    return path;
  }

  protected ResourceStoreRequest getResourceStoreRequest(ServletRequest request, boolean localOnly) {
    ResourceStoreRequest rsr = new ResourceStoreRequest(getResourceStorePath(request), localOnly);

    rsr.getRequestContext().put(RequestContext.CTX_AUTH_CHECK_ONLY, true);

    return rsr;
  }

  @Override
  protected String getHttpMethodAction(ServletRequest request) {

    String method = ((HttpServletRequest) request).getMethod().toLowerCase();

    if ("put".equals(method)) {
      // heavy handed thing
      // doing a LOCAL ONLY request to check is this exists?
      try {
        rootRouter.retrieveItem(getResourceStoreRequest(request, true));
      }
      catch (ItemNotFoundException e) {
        // the path does not exists, it is a CREATE
        method = "post";
      }
      catch (AccessDeniedException e) {
        // no access for read, so chances are post or put doesnt matter
        method = "post";
      }
      catch (Exception e) {
        // huh?
        throw new IllegalStateException("Got exception during target mapping!", e);
      }

      // the path exists, this is UPDATE
      return super.getHttpMethodAction(method);
    }
    else {
      return super.getHttpMethodAction(request);
    }
  }

  @Override
  public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
      throws IOException
  {
    // let check the mappedValues 1st
    boolean result = false;

    if (mappedValue != null) {
      result = super.isAccessAllowed(request, response, mappedValue);

      // if we are not allowed at start, forbid it
      if (!result) {
        return false;
      }
    }

    String actionVerb = getHttpMethodAction(request);
    Action action = Action.valueOf(actionVerb);

    if (null == action) {
      return false;
    }

    return rootRouter.authorizePath(getResourceStoreRequest(request, false), action);
  }
}
