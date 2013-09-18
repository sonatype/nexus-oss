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

package org.sonatype.nexus.plugins.capabilities.test.helper;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.maven1.Maven1ContentClass;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.proxy.repository.WebSiteRepository;

import com.google.common.collect.Lists;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

/**
 * @since 2.7
 */
@Named(RepositoryComboCapabilityDescriptor.TYPE_ID)
@Singleton
public class RepositoryComboCapabilityDescriptor
    extends CapabilityDescriptorSupport
{

  static final String TYPE_ID = "[repositoryCombo]";

  static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private final List<FormField> formFields;

  protected RepositoryComboCapabilityDescriptor() {
    formFields = Lists.<FormField>newArrayList(
        new RepositoryCombobox("all", "All", "?", FormField.OPTIONAL),
        new RepositoryCombobox("hosted", "Hosted", "?", FormField.OPTIONAL)
            .withAnyOfFacets(HostedRepository.class),
        new RepositoryCombobox("proxy", "Proxy", "?", FormField.OPTIONAL)
            .withAnyOfFacets(ProxyRepository.class),
        new RepositoryCombobox("hosted+proxy", "Hosted and Proxy", "?", FormField.OPTIONAL)
            .withAnyOfFacets(HostedRepository.class, ProxyRepository.class),
        new RepositoryCombobox("group", "Groups", "?", FormField.OPTIONAL)
            .withAnyOfFacets(GroupRepository.class),
        new RepositoryCombobox("virtual", "Virtual", "?", FormField.OPTIONAL)
            .withAnyOfFacets(ShadowRepository.class),
        new RepositoryCombobox("maven", "Maven", "?", FormField.OPTIONAL)
            .withAnyOfFacets(MavenRepository.class),
        new RepositoryCombobox("site", "Sites", "?", FormField.OPTIONAL)
            .withAnyOfFacets(WebSiteRepository.class),
        new RepositoryCombobox("maven1", "Maven 1", "?", FormField.OPTIONAL)
            .withAnyOfFacets(MavenRepository.class)
            .withAnyOfContentClasses(Maven1ContentClass.ID),
        new RepositoryCombobox("maven2", "Maven 2", "?", FormField.OPTIONAL)
            .withAnyOfFacets(MavenRepository.class)
            .withAnyOfContentClasses(Maven2ContentClass.ID)
    );
  }

  @Override
  public CapabilityType type() {
    return TYPE;
  }

  @Override
  public String name() {
    return "Repository Combos";
  }

  @Override
  public List<FormField> formFields() {
    return formFields;
  }

}
