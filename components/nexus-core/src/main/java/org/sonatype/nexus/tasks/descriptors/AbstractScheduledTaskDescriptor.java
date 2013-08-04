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

package org.sonatype.nexus.tasks.descriptors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepoComboFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.formfields.StringTextFormField;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractBooleanPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractNumberPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractRepositoryOrGroupPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractRepositoryPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractStringPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

public abstract class AbstractScheduledTaskDescriptor
    implements ScheduledTaskDescriptor
{
  public boolean isExposed() {
    return true;
  }

  @Deprecated
  public List<ScheduledTaskPropertyDescriptor> getPropertyDescriptors() {
    return Collections.emptyList();
  }

  /**
   * Helper method, that will convert from old api to new api, saving plugin devs
   * some headaches
   */
  public List<FormField> formFields() {
    if (getPropertyDescriptors().size() > 0) {
      List<FormField> formFields = new ArrayList<FormField>();

      for (ScheduledTaskPropertyDescriptor prop : getPropertyDescriptors()) {
        if (prop instanceof AbstractBooleanPropertyDescriptor) {
          formFields.add(new CheckboxFormField(prop.getId(), prop.getName(), prop.getHelpText(), prop.isRequired()));
        }
        else if (prop instanceof AbstractNumberPropertyDescriptor) {
          formFields.add(new NumberTextFormField(prop.getId(), prop.getName(), prop.getHelpText(), prop.isRequired(),
              prop.getRegexValidation()));
        }
        else if (prop instanceof AbstractStringPropertyDescriptor) {
          formFields.add(new StringTextFormField(prop.getId(), prop.getName(), prop.getHelpText(), prop.isRequired(),
              prop.getRegexValidation()));
        }
        else if (prop instanceof AbstractRepositoryOrGroupPropertyDescriptor) {
          formFields.add(
              new RepoOrGroupComboFormField(prop.getId(), prop.getName(), prop.getHelpText(), prop.isRequired(),
                  prop.getRegexValidation()));
        }
        else if (prop instanceof AbstractRepositoryPropertyDescriptor) {
          formFields.add(new RepoComboFormField(prop.getId(), prop.getName(), prop.getHelpText(), prop.isRequired(),
              prop.getRegexValidation()));
        }
      }

      return formFields;
    }
    else {
      return Collections.emptyList();
    }
  }
}
