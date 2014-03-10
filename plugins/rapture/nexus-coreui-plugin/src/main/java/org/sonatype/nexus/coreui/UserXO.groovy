/**
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

package org.sonatype.nexus.coreui

import groovy.transform.ToString
import org.apache.bval.constraints.NotEmpty
import org.sonatype.nexus.extdirect.model.Password
import org.sonatype.nexus.validation.Create
import org.sonatype.security.usermanagement.UserStatus

import javax.validation.constraints.NotNull

/**
 * User exchange object.
 *
 * @since 2.8
 */
@ToString(includePackage = false, includeNames = true)
class UserXO
{
  @NotNull
  @NotEmpty
  String id

  String realm

  @NotNull
  @NotEmpty
  String firstName

  @NotNull
  @NotEmpty
  String lastName

  @NotNull
  @NotEmpty
  String email

  @NotNull
  UserStatus status

  @NotNull(groups = Create.class)
  @NotEmpty(groups = Create.class)
  Password password

  @NotNull
  @NotEmpty
  Set<String> roles
}
