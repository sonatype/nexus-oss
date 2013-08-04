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

package org.sonatype.nexus.proxy.maven.metadata.operations;

import org.apache.maven.artifact.repository.metadata.Metadata;

/**
 * adds version to metadata
 *
 * @author Oleg Gusakov
 * @version $Id: SetVersionOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class SetVersionOperation
    implements MetadataOperation
{
  private String version;

  /**
   * @throws MetadataException
   */
  public SetVersionOperation(StringOperand data)
      throws MetadataException
  {
    setOperand(data);
  }

  public void setOperand(AbstractOperand data)
      throws MetadataException
  {
    if (data == null || !(data instanceof StringOperand)) {
      throw new MetadataException("Operand is not correct: expected SnapshotOperand, but got "
          + (data == null ? "null" : data.getClass().getName()));
    }

    version = ((StringOperand) data).getOperand();
  }

  /**
   * add version to the in-memory metadata instance
   */
  public boolean perform(Metadata metadata)
      throws MetadataException
  {
    if (metadata == null) {
      return false;
    }

    String vs = metadata.getVersion();

    if (vs == null) {
      if (version == null) {
        return false;
      }
    }
    else if (vs.equals(version)) {
      return false;
    }

    metadata.setVersion(version);

    return true;
  }

}
