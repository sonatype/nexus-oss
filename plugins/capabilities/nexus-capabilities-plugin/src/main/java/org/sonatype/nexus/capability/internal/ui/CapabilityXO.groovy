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

package org.sonatype.nexus.capability.internal.ui

import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty
import org.sonatype.nexus.validation.Update

import javax.validation.constraints.NotNull

/**
 * Capability exchange object.
 *
 * @since 3.0
 */
@ToString(includePackage = false, includeNames = true)
class CapabilityXO
{
  @NotEmpty(groups = Update.class)
  String id

  @NotEmpty
  String typeId

  @NotNull
  Boolean enabled

  String notes

  Map<String, String> properties

  Boolean active
  Boolean error
  String description
  String state
  String stateDescription
  String status
  String typeName
  Map<String, String> tags
}
