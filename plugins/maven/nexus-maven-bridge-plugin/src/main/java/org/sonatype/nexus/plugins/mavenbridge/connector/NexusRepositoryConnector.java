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

package org.sonatype.nexus.plugins.mavenbridge.connector;

import java.util.Collection;

import org.sonatype.aether.spi.connector.ArtifactDownload;
import org.sonatype.aether.spi.connector.ArtifactUpload;
import org.sonatype.aether.spi.connector.MetadataDownload;
import org.sonatype.aether.spi.connector.MetadataUpload;
import org.sonatype.aether.spi.connector.RepositoryConnector;

public class NexusRepositoryConnector
    implements RepositoryConnector
{

  public void get(Collection<? extends ArtifactDownload> artifactDownloads,
                  Collection<? extends MetadataDownload> metadataDownloads)
  {
    // TODO Auto-generated method stub

  }

  public void put(Collection<? extends ArtifactUpload> artifactUploads,
                  Collection<? extends MetadataUpload> metadataUploads)
  {
    // TODO Auto-generated method stub

  }

  public void close() {
    // TODO Auto-generated method stub

  }

}
