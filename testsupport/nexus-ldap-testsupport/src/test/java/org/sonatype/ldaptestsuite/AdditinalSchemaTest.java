/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.ldaptestsuite;

import java.util.Map;

import org.apache.directory.server.schema.bootstrap.Schema;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;

public class AdditinalSchemaTest
    extends AbstractLdapTestEnvironment
{
  @Override
  protected LdapServerConfiguration buildConfiguration() {
    return LdapServerConfiguration.builder()
        .withWorkingDirectory(util.createTempDir())
        .withAdditionalSchemas("org.apache.directory.server.schema.bootstrap.NisSchema")
        .withPartitions(
            Partition.builder()
                .withNameAndSuffix("sonatype", "o=sonatype")
                .withIndexedAttributes("objectClass", "o")
                .withRootEntryClasses("top", "organization")
                .withLdifFile(util.resolveFile("src/test/resources/nis.ldif")).build())
        .build();
  }

  @Test
  public void additionalSchemas() {
    Map<String, Schema> schemas = getLdapServer().getDirectoryService().getRegistries().getLoadedSchemas();
    assertThat(schemas, hasKey("nis"));
    assertThat(schemas.get("nis").isDisabled(), is(false));
  }
}
