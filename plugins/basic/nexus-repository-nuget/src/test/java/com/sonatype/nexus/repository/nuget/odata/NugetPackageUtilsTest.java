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
import java.util.Map;

import com.sonatype.nexus.repository.nuget.internal.NugetPackageException;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import org.junit.Test;

import static com.sonatype.nexus.repository.nuget.internal.NugetProperties.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class NugetPackageUtilsTest
    extends TestSupport
{
  @Test
  public void extractMetadataFromPackage() throws IOException, NugetPackageException {
    final InputStream packageStream = getClass().getResourceAsStream("/SONATYPE.TEST.1.0.nupkg");

    final Map<String, String> metadata = NugetPackageUtils.packageMetadata(packageStream);

    // Nuspec fields
    assertThat(metadata.get(ID), is(equalTo("SONATYPE.TEST")));
    assertThat(metadata.get(VERSION), is(equalTo("1.0")));
    assertThat(metadata.get(TITLE), is(equalTo("SONATYPE.TEST")));
    assertThat(metadata.get(AUTHORS), is(equalTo("Sonatype, Inc.")));
    assertThat(metadata.get(LICENSE_URL), is(equalTo("http://www.eclipse.org/legal/epl-v10.html")));
    assertThat(metadata.get(ICON_URL), is(equalTo(null)));
    assertThat(metadata.get(PROJECT_URL), is(equalTo("http://www.sonatype.com/")));
    assertThat(metadata.get(REQUIRE_LICENSE_ACCEPTANCE), is(equalTo("false")));
    assertThat(metadata.get(DESCRIPTION), is(equalTo("Example NuGet package.")));
    assertThat(metadata.get(SUMMARY), is(equalTo("Example.")));
    assertThat(metadata.get(LANGUAGE), is(equalTo("en-US")));
    assertThat(metadata.get(TAGS), is(equalTo("sonatype example")));
    assertThat(metadata.get(DEPENDENCIES), is(equalTo(null)));

    // Package metrics
    assertThat(metadata.get(PACKAGE_SIZE), is(equalTo("2165")));
    assertThat(metadata.get(PACKAGE_HASH),
        is(equalTo("Hy5FP+mjtRqJmQfOqO19QLR+W71NGufkmOhQxcPomQMG1HOTILCnTpdMXJD4u9716DO7a0LzMI1qR7paZiDE9A==")));
    assertThat(metadata.get(PACKAGE_HASH_ALGORITHM), is(equalTo("SHA512")));
  }

  // TODO: Add a test for when an InputStream contains a corrupt/non-package
}