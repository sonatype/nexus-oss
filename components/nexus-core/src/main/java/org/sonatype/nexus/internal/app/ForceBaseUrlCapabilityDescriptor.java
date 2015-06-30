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
package org.sonatype.nexus.internal.app;

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
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import static org.sonatype.nexus.capability.Tag.categoryTag;
import static org.sonatype.nexus.capability.Tag.tags;

/**
 * {@link ForceBaseUrlCapability} descriptor.
 *
 * @since 3.0
 */
@Named(ForceBaseUrlCapabilityDescriptor.TYPE_ID)
@Singleton
public class ForceBaseUrlCapabilityDescriptor
    extends CapabilityDescriptorSupport<ForceBaseUrlCapabilityConfiguration>
    implements Taggable
{
  public static final String TYPE_ID = "baseurl.force";

  public static final CapabilityType TYPE = CapabilityType.capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Base URL: Force")
    String name();
  }

  private static final Messages messages = I18N.create(Messages.class);

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
    return null;
  }

  @Override
  public Set<Tag> getTags() {
    return tags(categoryTag("Core"));
  }

  @Override
  protected ForceBaseUrlCapabilityConfiguration createConfig(final Map<String, String> properties) {
    return new ForceBaseUrlCapabilityConfiguration();
  }

  @Override
  protected String renderAbout() throws Exception {
    return render(TYPE_ID + "-about.vm");
  }
}
