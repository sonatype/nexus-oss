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
package com.sonatype.nexus.ldap.internal.ssl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.sonatype.nexus.ldap.formfield.LdapServerCombobox;
import com.sonatype.nexus.ldap.internal.capabilities.LdapValidators;
import com.sonatype.nexus.ssl.plugin.SSLPlugin;

import org.sonatype.nexus.capability.support.CapabilityDescriptorSupport;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityIdentity;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.Tag;
import org.sonatype.nexus.plugins.capabilities.Taggable;
import org.sonatype.nexus.plugins.capabilities.Validator;
import org.sonatype.sisu.goodies.i18n.I18N;
import org.sonatype.sisu.goodies.i18n.MessageBundle;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sonatype.nexus.ldap.internal.ssl.LdapCapabilityConfiguration.LDAP_SERVER_ID;
import static org.sonatype.nexus.formfields.FormField.MANDATORY;
import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;
import static org.sonatype.nexus.plugins.capabilities.Tag.categoryTag;
import static org.sonatype.nexus.plugins.capabilities.Tag.tags;

/**
 * {@link LdapCapability} descriptor.
 *
 * @since 2.4
 */
@Named(LdapCapabilityDescriptor.TYPE_ID)
@Singleton
public class LdapCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements Taggable
{

  /**
   * {@link LdapCapability} type ID (ssl.key.ldap)
   */
  public static final String TYPE_ID = SSLPlugin.ID_PREFIX + ".key.ldap";

  /**
   * {@link LdapCapability} type
   */
  public static final CapabilityType TYPE = capabilityType(TYPE_ID);

  private final LdapValidators ldapValidators;

  private static interface Messages
      extends MessageBundle
  {

    @DefaultMessage("SSL: LDAP")
    String name();

    @DefaultMessage("LDAP Server")
    String ldapServerLabel();

    @DefaultMessage("Select an LDAP server to enable Nexus SSL Trust Store for")
    String ldapServerHelp();

  }

  private static final Messages messages = I18N.create(Messages.class);

  private final List<FormField> formFields;

  @Inject
  public LdapCapabilityDescriptor(final LdapValidators ldapValidators) {
    this.ldapValidators = checkNotNull(ldapValidators);

    this.formFields = Lists.<FormField>newArrayList(
        new LdapServerCombobox(
            LDAP_SERVER_ID,
            messages.ldapServerLabel(),
            messages.ldapServerHelp(),
            MANDATORY
        )
    );
  }

  @Override
  public Validator validator() {
    return validators().logical().and(
        validators().capability().uniquePer(TYPE, LDAP_SERVER_ID),
        ldapServerExists()
    );
  }

  @Override
  public Validator validator(final CapabilityIdentity id) {
    return validators().logical().and(
        validators().capability().uniquePerExcluding(id, TYPE, LDAP_SERVER_ID),
        ldapServerExists()
    );
  }

  public Validator ldapServerExists() {
    return ldapValidators.ldapServerExists(LDAP_SERVER_ID);
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
