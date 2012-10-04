/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
 package org.sonatype.nexus.plugins.shell.task;

import static java.util.Arrays.asList;
import static org.sonatype.nexus.formfields.FormField.MANDATORY;
import java.util.List;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;


@Component(
  role = ScheduledTaskDescriptor.class, hint = "ShellExecutionTask", description = ShellExecutionTaskDescriptor.NAME
)
public class ShellExecutionTaskDescriptor extends AbstractScheduledTaskDescriptor {
  public static final String ID = "ShellExecutionTask";
  public static final String NAME = "Shell Execution Task";
  public static final String COMMAND_FIELD_ID = "shellTaskCommand";

  private final StringTextFormField commandField = new StringTextFormField(
    COMMAND_FIELD_ID,
    "Command to execute in the shell",
    "This should be the complete command with arguments to be executed in the shell. It should be exactly one command nothing more. No shell script code is allowed.",
    MANDATORY);

  public String getId() {
    return ID;
  }

  public String getName() {
    return NAME;
  }

  @Override
  public List<FormField> formFields() {
    return asList((FormField) commandField);
  }

}
