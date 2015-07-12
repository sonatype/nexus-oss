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
package org.sonatype.nexus.rutauth.internal.capability;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilityDescriptorSupport;
import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.capability.Tag;
import org.sonatype.nexus.capability.Taggable;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.rutauth.internal.RutAuthConstants;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Lists;

import static org.sonatype.nexus.capability.CapabilityType.capabilityType;
import static org.sonatype.nexus.capability.Tag.categoryTag;
import static org.sonatype.nexus.capability.Tag.tags;

/**
 * {@link RutAuthCapability} descriptor.
 *
 * @since 2.7
 */
@Named(RutAuthCapabilityDescriptor.TYPE_ID)
@Singleton
public class RutAuthCapabilityDescriptor
    extends CapabilityDescriptorSupport<RutAuthCapabilityConfiguration>
    implements Taggable
{
  public static final String TYPE_ID = RutAuthConstants.ID_PREFIX;

  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Rut Auth")
    String name();

    @DefaultMessage("HTTP Header name")
    String httpHeaderLabel();

    @DefaultMessage(
        "Handled HTTP Header should contain the name of the header that is used to source the principal of already authenticated user.")
    String httpHeaderHelp();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final List<FormField> formFields;

  public RutAuthCapabilityDescriptor() {
    formFields = Lists.<FormField>newArrayList(
        new StringTextFormField(
            RutAuthCapabilityConfiguration.HTTP_HEADER,
            messages.httpHeaderLabel(),
            messages.httpHeaderHelp(),
            FormField.MANDATORY)
    );
  }

  @Override
  public CapabilityType type() {
    return TYPE;
  }

  @Override
  public String name() {
    return messages.name();
  }

  @Override
  public List<FormField> formFields() {
    return formFields;
  }

  @Override
  protected RutAuthCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new RutAuthCapabilityConfiguration(properties);
  }

  @Override
  protected String renderAbout() throws Exception {
    return render(TYPE_ID + "-about.vm");
  }

  @Override
  public Set<Tag> getTags() {
    return tags(categoryTag("Security"));
  }

}
