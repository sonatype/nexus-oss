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
package org.sonatype.nexus.repository.httpbridge.internal;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sonatype.nexus.repository.Repository;
import org.sonatype.nexus.repository.httpbridge.HttpResponseSender;
import org.sonatype.nexus.repository.manager.RepositoryManager;
import org.sonatype.nexus.repository.view.Response;
import org.sonatype.nexus.repository.view.ViewFacet;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Repository view servlet.
 *
 * @since 3.0
 */
@Named
@Singleton
public class ViewServlet
  extends HttpServlet
{
  private final RepositoryManager repositoryManager;

  private final Map<String,HttpResponseSender> responseSenders;

  @Inject
  public ViewServlet(final RepositoryManager repositoryManager,
                     final Map<String,HttpResponseSender> responseSenders)
  {
    this.repositoryManager = checkNotNull(repositoryManager);
    this.responseSenders = checkNotNull(responseSenders);
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException
  {
    // resolve repository for request
    Repository repo = repository(req);

    // dispatch request
    ViewFacet facet = repo.facet(ViewFacet.class);
    Response response;
    try {
      response = facet.getRouter().dispatch(new HttpRequest(repo, req));
    }
    catch (Exception e) {
      Throwables.propagateIfPossible(e, ServletException.class, IOException.class);
      throw new ServletException(e);
    }

    // send response
    sender(repo).send(response, resp);
  }

  /**
   * Find repository for given request.
   */
  @Nonnull
  private Repository repository(final HttpServletRequest request) {
    String key = "TODO"; // decode from request
    Repository repo = repositoryManager.get(key);
    checkState(repo != null, "No repository with key: %s", key);
    //noinspection ConstantConditions
    return repo;
  }

  /**
   * Find sender for repository format.
   */
  @Nonnull
  private HttpResponseSender sender(final Repository repository) {
    String format = repository.getFormat().getValue();
    HttpResponseSender sender = responseSenders.get(format);
    checkState(sender != null, "No HTTP response sender for format: %s", format);
    //noinspection ConstantConditions
    return sender;
  }
}
