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
package org.sonatype.ldaptestsuite;

import java.lang.reflect.Field;

import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.schema.PartitionSchemaLoader;
import org.apache.directory.server.core.schema.SchemaService;
import org.apache.directory.server.schema.registries.DefaultRegistries;

public class SchemaPartitionAccessor
{

  public static Partition getSchemaPartition(SchemaService schemaService) throws Exception {
    Field field = SchemaService.class.getDeclaredField("schemaPartition");
    field.setAccessible(true);
    Partition partition = (Partition) field.get(schemaService);

    return partition;
  }

  public static PartitionSchemaLoader getSchemaLoader(DefaultRegistries registries) throws Exception {
    Field field = DefaultRegistries.class.getDeclaredField("schemaLoader");
    field.setAccessible(true);
    PartitionSchemaLoader schemaLoader = (PartitionSchemaLoader) field.get(registries);

    return schemaLoader;
  }

}
