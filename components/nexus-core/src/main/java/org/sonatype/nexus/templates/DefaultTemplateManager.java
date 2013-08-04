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

package org.sonatype.nexus.templates;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = TemplateManager.class)
public class DefaultTemplateManager
    implements TemplateManager
{
  @Requirement(role = TemplateProvider.class)
  private List<TemplateProvider> providers;

  public TemplateSet getTemplates() {
    return getTemplates(null);
  }

  public Template getTemplate(Object clazz, String id)
      throws NoSuchTemplateIdException
  {
    return getTemplates(clazz).getTemplateById(id);
  }

  // ==

  protected TemplateSet getTemplates(Object clazz) {
    TemplateSet result = new TemplateSet(clazz);

    for (TemplateProvider provider : providers) {
      TemplateSet chunk = provider.getTemplates(clazz);

      result.addAll(chunk);
    }

    return result;
  }
}
