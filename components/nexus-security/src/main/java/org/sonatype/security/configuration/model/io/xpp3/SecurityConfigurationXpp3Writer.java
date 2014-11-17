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
package org.sonatype.security.configuration.model.io.xpp3;

//---------------------------------/
//- Imported classes and packages -/
//---------------------------------/

import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;

import org.sonatype.security.configuration.model.SecurityConfiguration;

import org.codehaus.plexus.util.xml.pull.MXSerializer;
import org.codehaus.plexus.util.xml.pull.XmlSerializer;

/**
 * Class SecurityConfigurationXpp3Writer.
 *
 * @version $Revision$ $Date$
 */
@SuppressWarnings("all")
public class SecurityConfigurationXpp3Writer
{

  //--------------------------/
  //- Class/Member Variables -/
  //--------------------------/

  /**
   * Field NAMESPACE.
   */
  private static final String NAMESPACE = null;


  //-----------/
  //- Methods -/
  //-----------/

  /**
   * Method write.
   */
  public void write(Writer writer, SecurityConfiguration securityConfiguration)
      throws java.io.IOException
  {
    XmlSerializer serializer = new MXSerializer();
    serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
    serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
    serializer.setOutput(writer);
    serializer.startDocument(securityConfiguration.getModelEncoding(), null);
    writeSecurityConfiguration(securityConfiguration, "security-configuration", serializer);
    serializer.endDocument();
  } //-- void write( Writer, SecurityConfiguration )

  /**
   * Method write.
   */
  public void write(OutputStream stream, SecurityConfiguration securityConfiguration)
      throws java.io.IOException
  {
    XmlSerializer serializer = new MXSerializer();
    serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-indentation", "  ");
    serializer.setProperty("http://xmlpull.org/v1/doc/properties.html#serializer-line-separator", "\n");
    serializer.setOutput(stream, securityConfiguration.getModelEncoding());
    serializer.startDocument(securityConfiguration.getModelEncoding(), null);
    writeSecurityConfiguration(securityConfiguration, "security-configuration", serializer);
    serializer.endDocument();
  } //-- void write( OutputStream, SecurityConfiguration )

  /**
   * Method writeSecurityConfiguration.
   */
  private void writeSecurityConfiguration(SecurityConfiguration securityConfiguration, String tagName,
                                          XmlSerializer serializer)
      throws java.io.IOException
  {
    serializer.startTag(NAMESPACE, tagName);
    if (securityConfiguration.getVersion() != null) {
      serializer.startTag(NAMESPACE, "version").text(securityConfiguration.getVersion()).endTag(NAMESPACE, "version");
    }
    if (securityConfiguration.isAnonymousAccessEnabled() != false) {
      serializer.startTag(NAMESPACE, "anonymousAccessEnabled")
          .text(String.valueOf(securityConfiguration.isAnonymousAccessEnabled()))
          .endTag(NAMESPACE, "anonymousAccessEnabled");
    }
    if (securityConfiguration.getAnonymousUsername() != null) {
      serializer.startTag(NAMESPACE, "anonymousUsername").text(securityConfiguration.getAnonymousUsername())
          .endTag(NAMESPACE, "anonymousUsername");
    }
    if (securityConfiguration.getAnonymousPassword() != null) {
      serializer.startTag(NAMESPACE, "anonymousPassword").text(securityConfiguration.getAnonymousPassword())
          .endTag(NAMESPACE, "anonymousPassword");
    }
    if ((securityConfiguration.getRealms() != null) && (securityConfiguration.getRealms().size() > 0)) {
      serializer.startTag(NAMESPACE, "realms");
      for (Iterator iter = securityConfiguration.getRealms().iterator(); iter.hasNext(); ) {
        String realm = (String) iter.next();
        serializer.startTag(NAMESPACE, "realm").text(realm).endTag(NAMESPACE, "realm");
      }
      serializer.endTag(NAMESPACE, "realms");
    }
    if (securityConfiguration.getHashIterations() != 0) {
      serializer.startTag(NAMESPACE, "hashIterations").text(String.valueOf(securityConfiguration.getHashIterations()))
          .endTag(NAMESPACE, "hashIterations");
    }
    serializer.endTag(NAMESPACE, tagName);
  } //-- void writeSecurityConfiguration( SecurityConfiguration, String, XmlSerializer )

}
