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
package com.sonatype.nexus.repository.nuget.odata;

import java.io.IOException;
import java.io.Reader;

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import static org.codehaus.plexus.util.xml.pull.XmlPullParser.CDSECT;
import static org.codehaus.plexus.util.xml.pull.XmlPullParser.END_DOCUMENT;
import static org.codehaus.plexus.util.xml.pull.XmlPullParser.END_TAG;
import static org.codehaus.plexus.util.xml.pull.XmlPullParser.ENTITY_REF;
import static org.codehaus.plexus.util.xml.pull.XmlPullParser.START_TAG;
import static org.codehaus.plexus.util.xml.pull.XmlPullParser.TEXT;

/**
 * Supports splicing of content into an XML stream while simultaneously extracting data.
 */
abstract class XmlSplicer
{
  // ----------------------------------------------------------------------

  private final MXParser parser = new MXParser();

  final StringBuilder xml;

  // ----------------------------------------------------------------------

  /**
   * @param xml Shared buffer
   */
  XmlSplicer(final StringBuilder xml) {
    this.xml = xml;
  }

  // ----------------------------------------------------------------------

  /**
   * Consumes XML from the given reader until the end of the document.
   *
   * @param reader XML reader
   */
  final void consume(final Reader reader)
      throws XmlPullParserException, IOException
  {
    final int[] lim = new int[2];
    parser.setInput(reader);

    for (int level = 0, event = parser.nextTag(); event != END_DOCUMENT; event = parser.nextToken()) {
      if (event == TEXT || event == CDSECT) {
        if (!parser.isWhitespace()) {
          xml.append(parser.getTextCharacters(lim), lim[0], lim[1]);
        }
      }
      else if (event == ENTITY_REF) {
        xml.append('&').append(parser.getTextCharacters(lim), lim[0], lim[1]).append(';');
      }
      else if (event == START_TAG) {
        xml.append(parser.getTextCharacters(lim), lim[0], lim[1]);
        started(parser.getName(), lim[1], level == 0);
        if (parser.isEmptyElementTag()) {
          event = parser.next();
          ended(parser.getName(), 0);
          if (level <= 0) {
            break;
          }
        }
        else {
          level++;
        }
      }
      else if (event == END_TAG) {
        xml.append(parser.getTextCharacters(lim), lim[0], lim[1]);
        ended(parser.getName(), lim[1]);
        if (--level <= 0) {
          break;
        }
      }
    }
  }

  // ----------------------------------------------------------------------

  /**
   * Retrieves a named attribute from the current start tag.
   *
   * @param name Attribute name
   * @return Attribute value
   */
  final String getAttribute(final String name) {
    return parser.getAttributeValue(null, name);
  }

  // ----------------------------------------------------------------------

  /**
   * Notifies that a start tag has been processed.
   *
   * @param name   Tag name
   * @param len    Tag length
   * @param isRoot if the tag is the root tag
   */
  abstract void started(final String name, final int len, final boolean isRoot)
      throws XmlPullParserException;

  /**
   * Notifies that an end tag has been processed.
   *
   * @param name Tag name
   * @param len  Tag length
   */
  abstract void ended(final String name, final int len)
      throws XmlPullParserException;

  // ----------------------------------------------------------------------
}
