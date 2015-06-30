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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.codehaus.plexus.util.ReaderFactory;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NuspecSplicerTest
    extends TestSupport
{
  private final Map<String, String> basicData = new HashMap<String, String>();

  @Before
  public void prepareData() {
    basicData.put("ID", "adobereader");
    basicData.put("VERSION", "10.1.0");
    basicData.put("TITLE", "AdobeReader");
    basicData.put("AUTHORS", "Adobe");
    basicData.put("SUMMARY", "Adobe Reader - View and interact with PDF files");
    basicData.put("DESCRIPTION",
        "Adobe Reader is the global standard for reliably viewing, printing, and commenting on PDF documents."
            + " It's the only PDF file viewer that can open and interact with all types of PDF content, including"
            + " forms and multimedia. \n| Please install with chocolatey (http://nuget.org/List/Packages/chocolatey).");
    basicData.put("REQUIRELICENSEACCEPTANCE", "false");
    basicData.put("LANGUAGE", "en-US");
    basicData.put("PROJECTURL", "http://www.adobe.com/products/reader.html");
    basicData.put("LICENSEURL", "http://www.adobe.com/products/eulas/pdfs/Reader10_combined-20100625_1419.pdf");
    basicData.put("TAGS", "adobereader pdf reader chocolatey admin");
    basicData.put("DEPENDENCIES", "chocolatey:0.9.8.2");
  }

  @Test
  public void testNuspecParsing()
      throws Exception
  {
    final InputStream resourceAsStream = getClass().getResourceAsStream("/nuspec/adobereader.nuspec");

    final NuspecSplicer splicer = new NuspecSplicer();
    splicer.consume(ReaderFactory.newXmlReader(resourceAsStream));

    assertThat(splicer.data, is(basicData));
  }
}
