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

package org.sonatype.nexus.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;

@Component(role = ScheduledTaskPropertyDescriptor.class, hint = "RepositoryOrGroup",
    instantiationStrategy = "per-lookup")
public class RepositoryOrGroupPropertyDescriptor
    extends AbstractRepositoryOrGroupPropertyDescriptor
{
  public static final String ID = "repositoryId";

  public RepositoryOrGroupPropertyDescriptor() {
    setHelpText("Select the repository or repository group to assign to this task.");
    setRequired(true);
  }

  public String getId() {
    return ID;
  }

  public String getName() {
    return "Repository/Group";
  }
}
