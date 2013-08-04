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

import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;

/**
 * removes a Plugin from Metadata
 *
 * @author Oleg Gusakov
 * @version $Id: RemovePluginOperation.java 726701 2008-12-15 14:31:34Z hboutemy $
 */
public class RemovePluginOperation
    implements MetadataOperation
{

  private Plugin plugin;

  /**
   * @throws MetadataException
   */
  public RemovePluginOperation(PluginOperand data)
      throws MetadataException
  {
    setOperand(data);
  }

  public void setOperand(AbstractOperand data)
      throws MetadataException
  {
    if (data == null || !(data instanceof PluginOperand)) {
      throw new MetadataException("Operand is not correct: expected PluginOperand, but got "
          + (data == null ? "null" : data.getClass().getName()));
    }

    plugin = ((PluginOperand) data).getOperand();
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

    List<Plugin> plugins = metadata.getPlugins();

    if (plugins != null && plugins.size() > 0) {
      for (Iterator<Plugin> pi = plugins.iterator(); pi.hasNext(); ) {
        Plugin p = pi.next();

        if (p.getArtifactId().equals(plugin.getArtifactId())) {
          pi.remove();

          return true;
        }
      }
    }

    return false;
  }
}
