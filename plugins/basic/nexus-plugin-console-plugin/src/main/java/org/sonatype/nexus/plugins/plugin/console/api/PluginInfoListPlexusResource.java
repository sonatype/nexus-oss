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

package org.sonatype.nexus.plugins.plugin.console.api;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.sonatype.nexus.plugins.plugin.console.PluginConsoleManager;
import org.sonatype.nexus.plugins.plugin.console.api.dto.DocumentationLinkDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoDTO;
import org.sonatype.nexus.plugins.plugin.console.api.dto.PluginInfoListResponseDTO;
import org.sonatype.nexus.plugins.plugin.console.model.DocumentationLink;
import org.sonatype.nexus.plugins.plugin.console.model.PluginInfo;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.AliasingListConverter;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.thoughtworks.xstream.XStream;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodSignature;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource publishing Nexus plugin details.
 */
@Component(role = PlexusResource.class, hint = "PluginInfoListPlexusResource")
@Path("/plugin_console/plugin_infos")
@Produces({"application/xml", "application/json"})
@Consumes({"application/xml", "application/json"})
public class PluginInfoListPlexusResource
    extends AbstractNexusPlexusResource
{
  @Requirement
  private PluginConsoleManager pluginConsoleManager;

  public PluginInfoListPlexusResource() {
    this.setReadable(true);
    this.setModifiable(false);
  }

  @Override
  public void configureXStream(XStream xstream) {
    super.configureXStream(xstream);

    xstream.processAnnotations(PluginInfoDTO.class);
    xstream.processAnnotations(PluginInfoListResponseDTO.class);

    xstream.registerLocalConverter(PluginInfoListResponseDTO.class, "data", new AliasingListConverter(
        PluginInfoDTO.class, "pluginInfo"));
  }

  @Override
  public Object getPayloadInstance() {
    return null;
  }

  @Override
  public String getResourceUri() {
    return "/plugin_console/plugin_infos";
  }

  @Override
  public PathProtectionDescriptor getResourceProtection() {
    return new PathProtectionDescriptor(getResourceUri(), "authcBasic,perms[nexus:pluginconsoleplugininfos]");
  }

  /**
   * Returns the list of known Nexus plugins with details describing them.
   */
  @Override
  @GET
  @ResourceMethodSignature(output = PluginInfoListResponseDTO.class)
  public Object get(Context context, Request request, Response response, Variant variant)
      throws ResourceException
  {
    PluginInfoListResponseDTO result = new PluginInfoListResponseDTO();

    for (PluginInfo pluginInfo : pluginConsoleManager.listPluginInfo()) {
      result.addPluginInfo(nexusToRestModel(request, pluginInfo));
    }

    return result;
  }

  private PluginInfoDTO nexusToRestModel(Request request, PluginInfo pluginInfo) {
    PluginInfoDTO result = new PluginInfoDTO();

    result.setName(pluginInfo.getName());
    result.setStatus(pluginInfo.getStatus());
    result.setVersion(pluginInfo.getVersion());
    result.setDescription(pluginInfo.getDescription());
    result.setSite(pluginInfo.getSite());

    List<DocumentationLinkDTO> docUrls = new ArrayList<DocumentationLinkDTO>();
    for (DocumentationLink doc : pluginInfo.getDocumentation()) {
      DocumentationLinkDTO docLink = new DocumentationLinkDTO();
      docLink.setLabel(doc.getLabel());
      docLink.setUrl(createRootReference(request, doc.getUrl() + "/docs/index.html").getTargetRef().toString());
      docUrls.add(docLink);
    }
    result.setDocumentation(docUrls);

    result.setScmVersion(StringUtils.isEmpty(pluginInfo.getScmVersion()) ? "N/A" : pluginInfo.getScmVersion());
    result.setScmTimestamp(StringUtils.isEmpty(pluginInfo.getScmTimestamp()) ? "N/A"
        : pluginInfo.getScmTimestamp());
    result.setFailureReason(pluginInfo.getFailureReason());

    return result;
  }
}
