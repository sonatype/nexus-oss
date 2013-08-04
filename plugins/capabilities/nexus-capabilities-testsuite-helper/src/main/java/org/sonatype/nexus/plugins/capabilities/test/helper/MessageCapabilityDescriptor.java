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

package org.sonatype.nexus.plugins.capabilities.test.helper;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.plugins.capabilities.CapabilityDescriptor;
import org.sonatype.nexus.plugins.capabilities.CapabilityType;
import org.sonatype.nexus.plugins.capabilities.support.CapabilityDescriptorSupport;

import static org.sonatype.nexus.plugins.capabilities.CapabilityType.capabilityType;

@Named(MessageCapabilityDescriptor.TYPE_ID)
@Singleton
public class MessageCapabilityDescriptor
    extends CapabilityDescriptorSupport
    implements CapabilityDescriptor
{

  static final String TYPE_ID = "[message]";

  static final CapabilityType TYPE = capabilityType(TYPE_ID);

  static final String REPOSITORY = "repository";

  static final String MESSAGE = "message";

  static final RepoOrGroupComboFormField REPOSITORY_FIELD = new RepoOrGroupComboFormField(
      REPOSITORY, FormField.MANDATORY
  );

  static final StringTextFormField MESSAGE_FIELD = new StringTextFormField(
      MESSAGE, "Message", "Enter a message starting with XYZ", FormField.OPTIONAL, "XYZ.*"
  );

  protected MessageCapabilityDescriptor() {
    super(TYPE, "Message Capability", "What about me?", REPOSITORY_FIELD, MESSAGE_FIELD);
  }

}
