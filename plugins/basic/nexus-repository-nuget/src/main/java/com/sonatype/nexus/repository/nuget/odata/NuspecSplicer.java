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
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Extracts NuGet package metadata from a NuSpec specification.
 */
public final class NuspecSplicer
    extends XmlSplicer
{
  private static final List<String> ACCEPTED_TAGS = ImmutableList.of("id", "version", "authors", "description",
      "title", "releaseNotes", "summary", "tags",
      "projectUrl", "iconUrl", "licenseUrl",
      "copyright", "language", "requireLicenseAcceptance");

  final Map<String, String> data = new HashMap<String, String>();

  NuspecSplicer() {
    super(new StringBuilder());
  }

  /**
   * Extracts the .nuspec metdata from .nuspec XML in an input stream
   *
   * @param nuspecInputStream .nuspec input stream
   * @return Metadata in key-value form
   */
  public static Map<String, String> extractNuspecData(final InputStream nuspecInputStream)
      throws XmlPullParserException, IOException
  {
    final NuspecSplicer splicer = new NuspecSplicer();
    splicer.consume(ReaderFactory.newXmlReader(nuspecInputStream));
    return splicer.populateItemData();
  }

  @Override
  void started(final String name, final int len, final boolean isRoot)
      throws XmlPullParserException
  {
    if (isRoot && !"package".equals(name)) {
      throw new XmlPullParserException(
          "Parsed xml has an unexpected start tag: '" + name + "' (expected 'package')"
      );
    }
    if ("dependency".equals(name)) {
      final String deps = data.get("DEPENDENCIES");
      final String d = getAttribute("id") + ':' + getAttribute("version");
      data.put("DEPENDENCIES", null == deps ? d : deps + '|' + d);
    }
    xml.setLength(0);
  }

  @Override
  void ended(final String name, final int len) {
    if (ACCEPTED_TAGS.contains(name)) {
      data.put(name.toUpperCase(Locale.ENGLISH), xml.substring(0, xml.length() - len));
    }
  }

  private Map<String, String> populateItemData()
      throws XmlPullParserException
  {
    final String id = data.get("ID");
    if (null == id || id.length() == 0) {
      throw new XmlPullParserException("Missing id");
    }
    final String version = data.get("VERSION");
    if (null == version || version.length() == 0) {
      throw new XmlPullParserException("Missing version");
    }

    if (version.contains("-")) // http://docs.nuget.org/docs/reference/Versioning#Prerelease_Versions
    {
      data.put("ISPRERELEASE", "true");
    }

    if (!data.containsKey("TITLE")) {
      data.put("TITLE", id);
    }
    if (!data.containsKey("SUMMARY")) {
      data.put("SUMMARY", data.get("DESCRIPTION"));
    }
    if (!data.containsKey("REQUIRELICENSEACCEPTANCE")) {
      data.put("REQUIRELICENSEACCEPTANCE", "false");
    }


    return data;
  }
}
