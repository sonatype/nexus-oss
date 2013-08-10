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

import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryTypeDescriptor;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.data.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(role = RepositoryURLBuilder.class, hint = "RestletRepositoryUrlBuilder")
public class RestletRepositoryURLBuilder
    implements RepositoryURLBuilder
{
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Requirement
  private RepositoryRegistry repositoryRegistry;

  @Requirement
  private RepositoryTypeRegistry repositoryTypeRegistry;

  @Requirement
  private GlobalRestApiSettings globalRestApiSettings;

  public RestletRepositoryURLBuilder() {
    // nothing
  }

  /**
   * This constructor is used for testing only.
   */
  protected RestletRepositoryURLBuilder(final RepositoryRegistry repositoryRegistry,
                                        final RepositoryTypeRegistry repositoryTypeRegistry,
                                        final GlobalRestApiSettings globalRestApiSettings)
  {
    this.repositoryRegistry = repositoryRegistry;
    this.repositoryTypeRegistry = repositoryTypeRegistry;
    this.globalRestApiSettings = globalRestApiSettings;
  }

  @Override
  public String getRepositoryContentUrl(final String repositoryId)
      throws NoSuchRepositoryException
  {
    return getRepositoryContentUrl(repositoryId, false);
  }

  @Override
  public String getRepositoryContentUrl(final String repositoryId, final boolean forceBaseURL)
      throws NoSuchRepositoryException
  {
    return getRepositoryContentUrl(repositoryRegistry.getRepository(repositoryId), forceBaseURL);
  }

  @Override
  public String getRepositoryContentUrl(final Repository repository) {
    return getRepositoryContentUrl(repository, false);
  }

  @Override
  public String getRepositoryContentUrl(final Repository repository, final boolean forceBaseURL) {
    final boolean shouldForceBaseUrl = forceBaseURL ||
        (globalRestApiSettings.isEnabled()
            && globalRestApiSettings.isForceBaseUrl()
            && StringUtils.isNotBlank(globalRestApiSettings.getBaseUrl())
        );

    String baseURL;

    // if force, always use force
    if (shouldForceBaseUrl) {
      baseURL = globalRestApiSettings.isEnabled()?globalRestApiSettings.getBaseUrl():null;
    }
    // next check if this thread has a restlet request
    else if (Request.getCurrent() != null) {
      baseURL = Request.getCurrent().getRootRef().toString();
    }
    // as last resort, try to use the baseURL if set
    else {
      baseURL = globalRestApiSettings.getBaseUrl();
    }

    // if all else fails?
    if (StringUtils.isBlank(baseURL)) {
      logger.info("Not able to build content URL of the repository {}, baseUrl not set!",
          RepositoryStringUtils.getHumanizedNameString(repository));

      return null;
    }

    StringBuilder url = new StringBuilder(baseURL);

    if (!baseURL.endsWith("/")) {
      url.append("/");
    }

    final RepositoryTypeDescriptor rtd =
        repositoryTypeRegistry.getRepositoryTypeDescriptor(repository.getProviderRole(),
            repository.getProviderHint());

    url.append("content/").append(rtd.getPrefix()).append("/").append(repository.getPathPrefix());

    return url.toString();
  }

  @Override
  public String getExposedRepositoryContentUrl(final Repository repository) {
    return getExposedRepositoryContentUrl(repository, false);
  }

  @Override
  public String getExposedRepositoryContentUrl(final Repository repository, final boolean forceBaseURL) {
    if (!repository.isExposed()) {
      return null;
    }
    else {
      return getRepositoryContentUrl(repository);
    }
  }

}
