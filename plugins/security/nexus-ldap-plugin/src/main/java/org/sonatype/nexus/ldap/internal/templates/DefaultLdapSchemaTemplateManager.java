/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.ldap.internal.templates;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.ldap.internal.persist.entity.Mapping;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;

@Named
@Singleton
public class DefaultLdapSchemaTemplateManager
    implements LdapSchemaTemplateManager
{
  private static final String TEMPLATES_RESOURCE = "/META-INF/templates/ldap-templates.xml";

  private List<LdapSchemaTemplate> templates = null;

  @SuppressWarnings("unchecked")
  public List<LdapSchemaTemplate> getSchemaTemplates() {
    if (this.templates == null) {
      XStream xstream = new XStream();
      xstream.setClassLoader(this.getClass().getClassLoader());

      xstream.aliasType("templates", ArrayList.class);
      xstream.aliasType("template", LdapSchemaTemplate.class);
      xstream.aliasType("userAndGroupConfig", Mapping.class);

      InputStream stream = null;
      try {
        stream = getClass().getResourceAsStream(TEMPLATES_RESOURCE);
        this.templates = (List<LdapSchemaTemplate>) xstream.fromXML(stream);
      }
      finally {
        IOUtils.closeQuietly(stream);
      }
    }
    return this.templates;
  }
}
