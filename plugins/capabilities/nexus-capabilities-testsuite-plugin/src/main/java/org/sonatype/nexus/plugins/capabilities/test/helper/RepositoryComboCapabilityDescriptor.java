/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.capability.Tag;
import org.sonatype.nexus.capability.Taggable;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepositoryCombobox;
import org.sonatype.nexus.repository.types.GroupType;
import org.sonatype.nexus.repository.types.HostedType;
import org.sonatype.nexus.repository.types.ProxyType;
import org.sonatype.nexus.repository.types.VirtualType;

import com.google.common.collect.Lists;

import static org.sonatype.nexus.capability.CapabilityType.capabilityType;
import static org.sonatype.nexus.capability.Tag.categoryTag;
import static org.sonatype.nexus.capability.Tag.tags;

/**
 * A test/demo capability descriptor for using {@link RepositoryCombobox}.
 *
 * @since 2.7
 */
@Named(RepositoryComboCapabilityDescriptor.TYPE_ID)
@Singleton
public class RepositoryComboCapabilityDescriptor
    extends TestCapabilityDescriptor
    implements Taggable
{

  static final String TYPE_ID = "[repositoryCombo]";

  static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private final List<FormField> formFields;

  protected RepositoryComboCapabilityDescriptor() {
    formFields = Lists.<FormField>newArrayList(
        new RepositoryCombobox("all", "All", "?", FormField.OPTIONAL)
            .includeAnEntryForAllRepositories(),
        new RepositoryCombobox("hosted", "Hosted", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(HostedType.NAME),
        new RepositoryCombobox("proxy", "Proxy", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(ProxyType.NAME),
        new RepositoryCombobox("hosted+proxy", "Hosted and Proxy", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(HostedType.NAME, ProxyType.NAME),
        new RepositoryCombobox("group", "Groups", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(GroupType.NAME),
        new RepositoryCombobox("!group", "Any except Groups", "?", FormField.OPTIONAL)
            .excludingAnyOfTypes(GroupType.NAME),
        new RepositoryCombobox("virtual", "Virtual", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(VirtualType.NAME),
        new RepositoryCombobox("maven", "Maven", "?", FormField.OPTIONAL)
            .includingAnyOfFormats("maven2"),
        new RepositoryCombobox("hosted+maven", "Hosted and Maven", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(HostedType.NAME)
            .includingAnyOfFormats("maven2"),
        new RepositoryCombobox("hosted!maven", "Hosted except Maven", "?", FormField.OPTIONAL)
            .includingAnyOfTypes(HostedType.NAME)
            .excludingAnyOfFormats("maven2")
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

  @Override
  public Set<Tag> getTags() {
    return tags(categoryTag(Tag.REPOSITORY));
  }

}
