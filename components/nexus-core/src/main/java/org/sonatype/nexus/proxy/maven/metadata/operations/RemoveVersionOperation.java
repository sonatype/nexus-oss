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

import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;

/**
 * removes a version from Metadata
 *
 * @author Oleg Gusakov
 * @version $Id: RemoveVersionOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class RemoveVersionOperation
    implements MetadataOperation
{

  private String version;

  /**
   * @throws MetadataException
   */
  public RemoveVersionOperation(StringOperand data)
      throws MetadataException
  {
    setOperand(data);
  }

  public void setOperand(AbstractOperand data)
      throws MetadataException
  {
    if (data == null || !(data instanceof StringOperand)) {
      throw new MetadataException("Operand is not correct: expected StringOperand, but got "
          + (data == null ? "null" : data.getClass().getName()));
    }

    version = ((StringOperand) data).getOperand();
  }

  /**
   * remove version to the in-memory metadata instance
   */
  public boolean perform(Metadata metadata)
      throws MetadataException
  {
    if (metadata == null) {
      return false;
    }

    Versioning vs = metadata.getVersioning();

    if (vs == null) {
      return false;
    }

    if (vs.getVersions() != null && vs.getVersions().size() > 0) {
      List<String> vl = vs.getVersions();
      if (!vl.contains(version)) {
        return false;
      }
    }

    vs.removeVersion(version);
    vs.setLastUpdated(TimeUtil.getUTCTimestamp());

    return true;
  }

}
