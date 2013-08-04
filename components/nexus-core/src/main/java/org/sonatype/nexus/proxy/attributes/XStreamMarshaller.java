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

package org.sonatype.nexus.proxy.attributes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.attributes.internal.DefaultAttributes;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.converters.collections.MapConverter;

/**
 * The Nexus default marshaller: uses XStream to marshall complete StorageItem instances as XML.
 *
 * @author cstamas
 * @since 2.0
 * @deprecated Deprecated in favor of Jackson powered Marshaller, see {@link JacksonJSONMarshaller}.
 */
public class XStreamMarshaller
    implements Marshaller
{
  private final XStream xstream;

  public XStreamMarshaller() {
    this.xstream = new XStream();
    this.xstream.registerConverter(new MapConverter(xstream.getMapper()));
  }

  @Override
  public void marshal(final Attributes item, final OutputStream outputStream)
      throws IOException
  {
    final Map<String, String> attrs = new HashMap<String, String>(item.asMap());
    xstream.toXML(attrs, outputStream);
    outputStream.flush();
  }

  @Override
  public Attributes unmarshal(final InputStream inputStream)
      throws IOException
  {
    try {
      final Map<String, String> copy = (Map<String, String>) xstream.fromXML(inputStream);
      return new DefaultAttributes(copy);
    }
    catch (NullPointerException e) {
      // see NEXUS-3911: XPP3 throws sometimes NPE on "corrupted XMLs in some specific way"
      throw new InvalidInputException(
          "XPP3 thrown a NPE, see NEXUS-3911 for details, and input is claimed as corrupt.", e);
    }
    catch (XStreamException e) {
      // it is corrupt -- so says XStream, but see above and NEXUS-3911
      throw new InvalidInputException("XStream claimed file as corrupt.", e);
    }
  }

  // ==

  public String toString() {
    return "XStream";
  }
}
