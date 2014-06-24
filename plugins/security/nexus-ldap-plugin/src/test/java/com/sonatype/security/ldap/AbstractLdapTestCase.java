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
package com.sonatype.security.ldap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sonatype.ldaptestsuite.LdapServer;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.codec.Base64;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.InterpolationFilterReader;

import static org.apache.shiro.codec.CodecSupport.PREFERRED_ENCODING;

public abstract class AbstractLdapTestCase
    extends AbstractMultipleLdapTestEnvironment
{

  @Override
  protected void customizeContext(Context ctx) {
    super.customizeContext(ctx);
    // ldif to load
    ctx.put("test-ldif", this.getTestLdifPath());
    ctx.put("ldapResourcePath", "target/test-classes/" + this.getClass().getName().replace('.', '/'));
  }

  @Override
  public void setUp()
      throws Exception
  {

    super.setUp();

    Map<String, String> interpolationMap = new HashMap<String, String>();

    for (Entry<String, LdapServer> entry : this.getLdapServerMap().entrySet()) {
      interpolationMap.put(entry.getKey() + "-ldap-port", Integer.toString(entry.getValue().getPort()));
    }

    getConfHomeDir().mkdirs();

    copyResource(this.getSecurityConfigXmlFilePath(), getSecurityConfiguration());
    copyResource(this.getSecurityXmlFilePath(), getNexusSecurityConfiguration());

    try (InterpolationFilterReader reader =
             new InterpolationFilterReader(new InputStreamReader(
                 getClass().getResourceAsStream(this.getLdapConfigXmlFilePath())), interpolationMap)) {
      IOUtils.copy(reader, new FileOutputStream(new File(getConfHomeDir(), "ldap.xml")));
    }
  }

  private void copy(InputStream is, OutputStream os)
      throws IOException
  {
    try (InputStream in = is;
         OutputStream out = os) {
      IOUtils.copy(in, out);
    }
  }

  protected String getSecurityConfigXmlFilePath() {
    return this.getFilePath("security-configuration.xml");
  }

  protected String getLdapConfigXmlFilePath() {
    return this.getFilePath("ldap.xml");
  }

  protected String getSecurityXmlFilePath() {
    return this.getFilePath("security.xml");
  }

  private Object getTestLdifPath() {
    return "target/test-classes" + this.getFilePath("default-ldap.ldif");
  }

  private String getFilePath(String filename) {
    // first check if the file is defined for the class
    String resourcePath = this.getClass().getName().replace('.', '/');
    resourcePath += "-" + filename;

    // now check if it exists
    if (ClassLoader.getSystemResource(resourcePath) != null) {
      return resourcePath;
    }
    else {
      return "/defaults/" + filename;
    }
  }

  public static String encodeBase64(final String value) {
    try {
      return Base64.encodeToString(value.getBytes(PREFERRED_ENCODING));
    }
    catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

}
