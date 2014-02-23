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

package org.sonatype.nexus.analytics.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.analytics.AnalyticsPlugin;
import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import org.jetbrains.annotations.NonNls;

import static org.sonatype.nexus.analytics.AnalyticsPlugin.CAPABILITY_CATEGORY_TAG;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.Tag.categoryTag;
import static org.sonatype.nexus.plugins.capabilities.Tag.tags;

/**
 * {@link CollectionCapability} descriptor.
 *
 * @since 2.8
 */
@Named(CollectionCapabilityDescriptor.TYPE_ID)
@Singleton
public class CollectionCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{
  @NonNls
  public static final String TYPE_ID = AnalyticsPlugin.ID_PREFIX + ".collection";

  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Analytics: Collection")
    String name();

    @DefaultMessage("Host Identifier")
    String hostIdLabel();

    @DefaultMessage("Random identifier to group analytics event data for a specific host.")
    String hostIdHelp();

    @DefaultMessage("Anonymization Salt")
    String saltLabel();

    @DefaultMessage("Random data used to anonymize data.")
    String saltHelp();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final FormField hostId;

  private final FormField salt;

  public CollectionCapabilityDescriptor() {
    this.hostId = new StringTextFormField(
        CollectionCapabilityConfiguration.HOST_ID,
        messages.hostIdLabel(),
        messages.hostIdHelp(),
        FormField.MANDATORY
    );

    this.salt = new StringTextFormField(
        CollectionCapabilityConfiguration.SALT,
        messages.saltLabel(),
        messages.saltHelp(),
        FormField.MANDATORY
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
    return Arrays.asList(
        hostId,
        salt
    );
  }

  @Override
  public Validator validator() {
    // Allow only one capability of this type
    return validators().capability().uniquePer(TYPE);
  }

  @Override
  protected String renderAbout() throws Exception {
    return render(TYPE_ID + "-about.vm");
  }

  @Override
  public Set<Tag> getTags() {
    return tags(categoryTag(CAPABILITY_CATEGORY_TAG));
  }

}
