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
import org.sonatype.nexus.formfields.TextAreaFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NonNls;

/**
 * {@link BrandingCapability} descriptor.
 *
 * @since 3.0
 */
@Named(BrandingCapabilityDescriptor.TYPE_ID)
@Singleton
public class BrandingCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{
  @NonNls
  public static final String TYPE_ID = "rapture.branding";

  public static final CapabilityType TYPE = CapabilityType.capabilityType(TYPE_ID);

  private static interface Messages
      extends MessageBundle
  {
    @DefaultMessage("Branding")
    String name();

    @DefaultMessage("Enable header")
    String headerEnabledLabel();

    @DefaultMessage(
        "Select this checkbox in order to show the branding header that will include the HTML snipped bellow."
    )
    String headerEnabledHelp();

    @DefaultMessage("Header HTML snippet")
    String headerHtmlLabel();

    @DefaultMessage(
        "An HTML snippet to be included in branding header.<br/>"
            + "Use '$baseUrl' in order to insert Nexus base URL (e.g. referencing an image)"
    )
    String headerHtmlHelp();

    @DefaultMessage("Enable footer")
    String footerEnabledLabel();

    @DefaultMessage(
        "Select this checkbox in order to show the branding footer that will include the HTML snipped bellow."
    )
    String footerEnabledHelp();

    @DefaultMessage("Footer HTML snippet")
    String footerHtmlLabel();

    @DefaultMessage(
        "An HTML snippet to be included in branding footer.<br/>"
            + "Use '$baseUrl' in order to insert Nexus base URL (e.g. referencing an image)"
    )
    String footerHtmlHelp();
  }

  private static final Messages messages = I18N.create(Messages.class);

  private final List<FormField> formFields;

  public BrandingCapabilityDescriptor() {
    formFields = Lists.<FormField>newArrayList(
        new CheckboxFormField(
            BrandingCapabilityConfiguration.HEADER_ENABLED,
            messages.headerEnabledLabel(),
            messages.headerEnabledHelp(),
            FormField.OPTIONAL
        ).withInitialValue(true),
        new TextAreaFormField(
            BrandingCapabilityConfiguration.HEADER_HTML,
            messages.headerHtmlLabel(),
            messages.headerHtmlHelp(),
            FormField.OPTIONAL
        ),
        new CheckboxFormField(
            BrandingCapabilityConfiguration.FOOTER_ENABLED,
            messages.footerEnabledLabel(),
            messages.footerEnabledHelp(),
            FormField.OPTIONAL
        ).withInitialValue(true),
        new TextAreaFormField(
            BrandingCapabilityConfiguration.FOOTER_HTML,
            messages.footerHtmlLabel(),
            messages.footerHtmlHelp(),
            FormField.OPTIONAL
        )
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
    return validators().capability().uniquePer(TYPE);
  }

  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators().capability().uniquePerExcluding(id, TYPE);
  }

  @Override
  protected String renderAbout() throws Exception {
    return render(TYPE_ID + "-about.vm");
  }

  @Override
  public Set<Tag> getTags() {
    return Tag.tags(Tag.categoryTag("UI"));
  }

}
