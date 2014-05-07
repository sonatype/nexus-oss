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
package org.sonatype.nexus.rapture.internal.capability;

import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.nexus.rapture.RaptureSettings;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NonNls;

/**
 * {@link RaptureSettingsCapability} descriptor.
 *
 * @since 3.0
 */
@Named(RaptureSettingsCapabilityDescriptor.TYPE_ID)
@Singleton
public class RaptureSettingsCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{
  @NonNls
  public static final String TYPE_ID = "rapture.settings";

  public static final CapabilityType TYPE = CapabilityType.capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Rapture Settings")
    String name();

    @DefaultMessage("Debug allowed")
    String debugAllowedLabel();

    @DefaultMessage("Allow developer debugging")
    String debugAllowedHelp();

    @DefaultMessage("Authenticated user status interval")
    String statusIntervalAuthenticatedLabel();

    @DefaultMessage("Interval between status requests for authenticated users(seconds)")
    String statusIntervalAuthenticatedHelp();

    @DefaultMessage("Anonymous user status interval")
    String statusIntervalAnonymousLabel();

    @DefaultMessage("Interval between status requests for anonymous user (seconds)")
    String statusIntervalAnonymousHelp();

    @DefaultMessage("Session timeout")
    String sessionTimeoutLabel();

    @DefaultMessage(
        "Period of inactivity before session is timing out (minutes). A value of 0 will mean that session never expires"
    )
    String sessionTimeoutHelp();

    @DefaultMessage("Title")
    String titleLabel();

    @DefaultMessage("Browser page title")
    String titleHelp();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final List<FormField> formFields;

  public RaptureSettingsCapabilityDescriptor() {
    formFields = Lists.<FormField>newArrayList(
        new StringTextFormField(
            RaptureSettingsCapabilityConfiguration.TITLE,
            messages.titleLabel(),
            messages.titleHelp(),
            FormField.MANDATORY
        ).withInitialValue(RaptureSettings.DEFAULT_TITLE),
        new CheckboxFormField(
            RaptureSettingsCapabilityConfiguration.DEBUG_ALLOWED,
            messages.debugAllowedLabel(),
            messages.debugAllowedHelp(),
            FormField.OPTIONAL
        ).withInitialValue(RaptureSettings.DEFAULT_DEBUG_ALLOWED),
        new NumberTextFormField(
            RaptureSettingsCapabilityConfiguration.STATUS_INTERVAL_AUTHENTICATED,
            messages.statusIntervalAuthenticatedLabel(),
            messages.statusIntervalAuthenticatedHelp(),
            FormField.MANDATORY
        ).withInitialValue(RaptureSettings.DEFAULT_STATUS_INTERVAL_AUTHENTICATED),
        new NumberTextFormField(
            RaptureSettingsCapabilityConfiguration.STATUS_INTERVAL_ANONYMOUS,
            messages.statusIntervalAnonymousLabel(),
            messages.statusIntervalAnonymousHelp(),
            FormField.MANDATORY
        ).withInitialValue(RaptureSettings.DEFAULT_STATUS_INTERVAL_ANONYMOUS),
        new NumberTextFormField(
            RaptureSettingsCapabilityConfiguration.SESSION_TIMEOUT,
            messages.sessionTimeoutLabel(),
            messages.sessionTimeoutHelp(),
            FormField.MANDATORY
        ).withInitialValue(RaptureSettings.DEFAULT_SESSION_TIMEOUT)
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
  public Validator validator() {
    return validators().logical().and(
        validators().capability().uniquePer(TYPE),
        validators().value().isAPositiveInteger(TYPE, RaptureSettingsCapabilityConfiguration.STATUS_INTERVAL_AUTHENTICATED),
        validators().value().isAPositiveInteger(TYPE, RaptureSettingsCapabilityConfiguration.STATUS_INTERVAL_ANONYMOUS),
        validators().value().isAPositiveInteger(TYPE, RaptureSettingsCapabilityConfiguration.SESSION_TIMEOUT)
    );
  }

  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators().logical().and(
        validators().capability().uniquePerExcluding(id, TYPE),
        validators().value().isAPositiveInteger(TYPE, RaptureSettingsCapabilityConfiguration.STATUS_INTERVAL_AUTHENTICATED),
        validators().value().isAPositiveInteger(TYPE, RaptureSettingsCapabilityConfiguration.STATUS_INTERVAL_ANONYMOUS),
        validators().value().isAPositiveInteger(TYPE, RaptureSettingsCapabilityConfiguration.SESSION_TIMEOUT)
    );
  }

  @Override
  protected String renderAbout() throws Exception {
    return render(TYPE_ID + "-about.vm");
  }

  @Override
  public Set<Tag> getTags() {
    return Tag.tags(Tag.categoryTag("Security"));
  }

}
