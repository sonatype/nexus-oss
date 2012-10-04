/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
 package org.sonatype.nexus.plugins.yum.version.alias.rest;

import static org.restlet.data.MediaType.TEXT_PLAIN;
import static org.restlet.data.Method.POST;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.plexus.component.annotations.Component;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.yum.config.YumConfiguration;
import org.sonatype.nexus.plugins.yum.version.alias.AliasNotFoundException;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;


/**
 * Resource providing an aliases view on the repositories provided by Nexus.
 * That means, that you can configure serveral aliases for artifact versions.
 * E.g. you can introduce "trunk", "testing" and "production" aliases for the
 * versions "91.0.0", "90.0.0" and "89.0.0" and can access the RPMs via
 * http://localhost:8080/nexus/service/local/yum-alias/<repo-id>/alias.rpm
 *
 * @author sherold
 *
 */
@Component(role = PlexusResource.class, hint = "RepositoryVersionAliasResource")
@Path(RepositoryVersionAliasResource.RESOURCE_URI)
@Produces({ "application/xml", "application/json", "text/plain" })
public class RepositoryVersionAliasResource extends AbstractPlexusResource implements PlexusResource {
  public static final String URL_PREFIX = "yum/alias";
  private static final String PATH_PATTERN_TO_PROTECT = "/" + URL_PREFIX + "/**";
  public static final String REPOSITORY_ID_PARAM = "repositoryId";
  public static final String ALIAS_PARAM = "alias";
  public static final String RESOURCE_URI = "/" + URL_PREFIX + "/{" + REPOSITORY_ID_PARAM + "}/{" + ALIAS_PARAM + "}";

  @Inject
  private YumConfiguration aliasMapper;

  public RepositoryVersionAliasResource() {
    setModifiable(true);
  }

  @Override
  public Object get(Context context, Request request, Response response, Variant variant) throws ResourceException {
    final String repositoryId = getAttributeAsString(request, REPOSITORY_ID_PARAM);
    String alias = getAttributeAsString(request, ALIAS_PARAM);
    if (alias == null) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find empty alias");
    }

    try {
      return new StringRepresentation(aliasMapper.getVersion(repositoryId, alias));
    } catch (AliasNotFoundException e) {
      throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
        "Could not find alias " + alias + " for repository " + repositoryId, e);
    }
  }

  @Override
  public Object post(Context context, Request request, Response response, Object payload) throws ResourceException {
    final String repositoryId = getAttributeAsString(request, REPOSITORY_ID_PARAM);
    final String alias = getAttributeAsString(request, ALIAS_PARAM);

    if ((payload == null) || !String.class.isAssignableFrom(payload.getClass())) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please provide a valid artifact version.");
    }

    aliasMapper.setAlias(repositoryId, alias, payload.toString());

    return new StringRepresentation(payload.toString(), TEXT_PLAIN);
  }

  private String getAttributeAsString(final Request request, final String attrName) {
    final Object attrValue = request.getAttributes().get(attrName);
    return (attrValue != null) ? attrValue.toString() : null;
  }

  @Override
  public String getResourceUri() {
    return RESOURCE_URI;
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(PATH_PATTERN_TO_PROTECT, "authcBasic,perms[nexus:yumAlias]");
  }

  @Override
  public Object getPayloadInstance(Method method) {
    if (POST.equals(method)) {
      return new String();
    }
    return null;
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

}
