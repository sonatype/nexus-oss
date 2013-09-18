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

package org.sonatype.nexus.plugins.capabilities.internal.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.sonatype.nexus.capability.support.CapabilitiesPlugin;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptorRegistry;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityFormFieldResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResource;
import org.sonatype.nexus.plugins.capabilities.internal.rest.dto.CapabilityTypeResourceResponse;
import org.sonatype.sisu.goodies.common.ComponentSupport;
import org.sonatype.sisu.siesta.common.Resource;

import org.apache.shiro.authz.annotation.RequiresPermissions;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

@Named
@Singleton
@Path(CapabilityTypesResource.RESOURCE_URI)
@Produces({"application/xml", "application/json"})
public class CapabilityTypesResource
    extends ComponentSupport
    implements Resource
{

  public static final String RESOURCE_URI = CapabilitiesPlugin.REST_PREFIX + "/types";

  public static final String $INCLUDE_NOT_EXPOSED = "$includeNotExposed";

  private final CapabilityDescriptorRegistry capabilityDescriptorRegistry;

  @Inject
  public CapabilityTypesResource(final CapabilityDescriptorRegistry capabilityDescriptorRegistry) {
    this.capabilityDescriptorRegistry = capabilityDescriptorRegistry;
  }

  /**
   * Retrieve a list of capability types available.
   */
  @GET
  @Produces({APPLICATION_XML, APPLICATION_JSON})
  @RequiresPermissions(CapabilitiesPlugin.PERMISSION_PREFIX_TYPES + "read")
  public CapabilityTypeResourceResponse get(@QueryParam($INCLUDE_NOT_EXPOSED) Boolean includeNotExposed) {
    final CapabilityTypeResourceResponse envelope = new CapabilityTypeResourceResponse();

    final CapabilityDescriptor[] descriptors = capabilityDescriptorRegistry.getAll();

    if (descriptors != null) {
      for (final CapabilityDescriptor descriptor : descriptors) {
        if (((includeNotExposed != null && includeNotExposed) || descriptor.isExposed())) {
          final CapabilityTypeResource capabilityTypeResource = new CapabilityTypeResource();
          capabilityTypeResource.setId(descriptor.type().toString());
          capabilityTypeResource.setName(descriptor.name());
          capabilityTypeResource.setAbout(descriptor.about());

          envelope.addData(capabilityTypeResource);

          final List<FormField> formFields = descriptor.formFields();

          capabilityTypeResource.setFormFields(formFieldToDTO(formFields));
        }

      }
    }

    return envelope;
  }

  protected List<CapabilityFormFieldResource> formFieldToDTO(List<FormField> fields) {
    List<CapabilityFormFieldResource> dtoList = new ArrayList<>();

    for (FormField field : fields) {
      CapabilityFormFieldResource dto = new CapabilityFormFieldResource();
      dto.setHelpText(field.getHelpText());
      dto.setId(field.getId());
      dto.setLabel(field.getLabel());
      dto.setRegexValidation(field.getRegexValidation());
      dto.setRequired(field.isRequired());
      dto.setType(field.getType());
      if (field.getInitialValue() != null) {
        dto.setInitialValue(field.getInitialValue().toString());
      }

      dtoList.add(dto);
    }

    return dtoList;
  }

}
