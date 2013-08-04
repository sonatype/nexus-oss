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

package org.sonatype.nexus.jsecurity.realms;

import org.sonatype.security.realms.privileges.PrivilegePropertyDescriptor;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = PrivilegePropertyDescriptor.class, hint = "TargetPrivilegeRepositoryTargetPropertyDescriptor")
public class TargetPrivilegeRepositoryTargetPropertyDescriptor
    implements PrivilegePropertyDescriptor
{
  public static final String ID = "repositoryTargetId";

  public String getHelpText() {
    return "The repository target associated with this privilege.";
  }

  public String getId() {
    return ID;
  }

  public String getName() {
    return "Repository Target";
  }

  public String getType() {
    return "repotarget";
  }
}
