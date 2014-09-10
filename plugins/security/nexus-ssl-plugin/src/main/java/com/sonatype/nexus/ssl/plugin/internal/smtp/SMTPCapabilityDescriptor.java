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
package com.sonatype.nexus.ssl.plugin.internal.smtp;

import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.ssl.plugin.SSLPlugin;

import org.sonatype.nexus.capability.CapabilityType;
import org.sonatype.nexus.capability.Tag;
import org.sonatype.nexus.capability.Taggable;
import org.sonatype.nexus.capability.Validator;
import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Lists;

import static org.sonatype.nexus.capability.CapabilityType.capabilityType;
import static org.sonatype.nexus.capability.Tag.categoryTag;
import static org.sonatype.nexus.capability.Tag.tags;

/**
 * {@link SMTPCapability} descriptor.
 *
 * @since ssl 1.0
 */
@Named(SMTPCapabilityDescriptor.TYPE_ID)
@Singleton
public class SMTPCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{

  /**
   * {@link SMTPCapability} type ID (ssl.key.smtp)
   */
  public static final String TYPE_ID = SSLPlugin.ID_PREFIX + ".key.smtp";

  /**
   * {@link SMTPCapability} type
   */
  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("SSL: SMTP")
    String name();

  }

  private static final Messages messages = I18N.create(Messages.class);

  @Override
  public Validator validator() {
    return validators().capability().uniquePer(
        SMTPCapabilityDescriptor.TYPE
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
    return Lists.newArrayList();
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
