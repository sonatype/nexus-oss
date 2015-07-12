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
package org.sonatype.nexus.kenai.internal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.CapabilityDescriptorSupport;
import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.capability.Tag;
import org.sonatype.nexus.capability.Taggable;
import org.sonatype.nexus.formfields.ComboboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.kenai.KenaiConfiguration;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Lists;

import static org.sonatype.nexus.capability.CapabilityType.capabilityType;
import static org.sonatype.nexus.capability.Tag.categoryTag;
import static org.sonatype.nexus.capability.Tag.tags;
import static org.sonatype.nexus.formfields.FormField.MANDATORY;

/**
 * {@link KenaiCapability} descriptor.
 *
 * @since 3.0
 */
@Named(KenaiCapabilityDescriptor.TYPE_ID)
@Singleton
public class KenaiCapabilityDescriptor
    extends CapabilityDescriptorSupport<KenaiConfiguration>
    implements Taggable
{

  /**
   * {@link KenaiCapability} type ID (kenai)
   */
  public static final String TYPE_ID = KenaiConstants.ID_PREFIX;

  /**
   * {@link KenaiCapability} type
   */
  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private interface Messages
      extends MessageBundle
  {

    @DefaultMessage("Kenai")
    String name();

    @DefaultMessage("Base URL")
    String baseUrlLabel();

    @DefaultMessage("The URL of the Kenai server. Example: https://java.net/")
    String baseUrlHelp();

    @DefaultMessage("Default Role")
    String defaultRoleLabel();

    @DefaultMessage("The role that will be assigned to all Kenai Realm users.")
    String defaultRoleHelp();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final List<FormField> formFields;

  @Inject
  public KenaiCapabilityDescriptor() {
    this.formFields = Lists.<FormField>newArrayList(
        new StringTextFormField(
            KenaiConfiguration.BASE_URL,
            messages.baseUrlLabel(),
            messages.baseUrlHelp(),
            MANDATORY
        ).withInitialValue("https://java.net/"),
        new ComboboxFormField(
            KenaiConfiguration.DEFAULT_ROLE,
            messages.defaultRoleLabel(),
            messages.defaultRoleHelp(),
            MANDATORY
        ).withStoreApi("coreui_Role.readReferences")
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
  protected KenaiConfiguration createConfig(final Map<String, String> properties) {
    return new KenaiConfiguration(properties);
  }

  @Override
  protected String renderAbout()
      throws Exception
  {
    return render(TYPE_ID + "-about.vm");
  }

  @Override
  public Set<Tag> getTags() {
    return tags(categoryTag("Security"));
  }

}
