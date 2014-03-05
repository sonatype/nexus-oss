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

package org.sonatype.nexus.quartz.internal.capability;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.quartz.QuartzPlugin;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import org.jetbrains.annotations.NonNls;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.Tag.categoryTag;
import static org.sonatype.nexus.plugins.capabilities.Tag.tags;
import static org.sonatype.nexus.quartz.QuartzPlugin.CAPABILITY_CATEGORY_TAG;

/**
 * {@link SchedulerCapability} descriptor.
 *
 * @since 2.8
 */
@Named(SchedulerCapabilityDescriptor.TYPE_ID)
@Singleton
public class SchedulerCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{
  @NonNls
  public static final String TYPE_ID = QuartzPlugin.ID_PREFIX + ".scheduler";

  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Quartz: Scheduler")
    String name();

    @DefaultMessage("Active")
    String activeLabel();

    @DefaultMessage("If active, jobs will be executed as scheduled. If not active, no jobs will be executed, scheduler is in \'stand-by\' mode.")
    String activeHelp();

    @DefaultMessage("Thread Pool Size")
    String threadPoolSizeLabel();

    @DefaultMessage("Size of the thread pool to be used with Quartz Scheduler. Is applied when scheduler is created (this capability disabled/enabled or on Nexus reboot).")
    String threadPoolSizeHelp();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final FormField active;

  private final FormField threadPoolSize;

  public SchedulerCapabilityDescriptor() {
    this.active = new CheckboxFormField(
        SchedulerCapabilityConfiguration.ACTIVE,
        messages.activeLabel(),
        messages.activeHelp(),
        FormField.OPTIONAL // Bug? Checkbox not sent when unchecked
    );
    this.threadPoolSize = new NumberTextFormField(
        SchedulerCapabilityConfiguration.THREAD_POOL_SIZE,
        messages.threadPoolSizeLabel(),
        messages.threadPoolSizeHelp(),
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
        active,
        threadPoolSize
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
