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

package org.sonatype.nexus.obr.metadata;

import java.io.IOException;
import java.io.InputStream;

import org.sonatype.nexus.configuration.application.NexusConfiguration;
import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.obr.ObrPluginConfiguration;
import org.sonatype.nexus.obr.util.ObrUtils;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.osgi.impl.bundle.obr.resource.BundleInfo;
import org.osgi.service.obr.Resource;

/**
 * Bindex based {@link ObrMetadataSource} component.
 */
@Component(role = ObrMetadataSource.class, hint = "obr-bindex", description = "bindex")
public class DefaultObrMetadataSource
    extends AbstractLogEnabled
    implements ObrMetadataSource
{
  @Requirement
  private ObrPluginConfiguration obrConfiguration;

  @Requirement
  private NexusConfiguration nexusConfiguration;

  @Requirement
  private MimeSupport mimeSupport;

  public ObrResourceReader getReader(final ObrSite site)
      throws StorageException
  {
    try {
      return new DefaultObrResourceReader(site, obrConfiguration.isBundleCacheActive());
    }
    catch (final IOException e) {
      throw new StorageException(e);
    }
  }

  public Resource buildResource(final StorageFileItem item) {
    if (!ObrUtils.acceptItem(item)) {
      return null; // ignore non-OBR resource items
    }

    InputStream is = null;

    try {
      is = item.getInputStream();
      if (is != null) {
        final RepositoryItemUid uid = item.getRepositoryItemUid();
        final BundleInfo info = new BundleInfo(null, is, "file:" + uid.getPath(), item.getLength());
        if (info.isOSGiBundle()) {
          return info.build();
        }
      }
    }
    catch (final Exception e) {
      getLogger().warn("Unable to generate OBR metadata for item " + item.getRepositoryItemUid(), e);
    }
    finally {
      IOUtil.close(is);
    }

    return null;
  }

  public ObrResourceWriter getWriter(final RepositoryItemUid uid)
      throws StorageException
  {
    try {
      return new DefaultObrResourceWriter(uid, nexusConfiguration.getTemporaryDirectory(), mimeSupport);
    }
    catch (final IOException e) {
      throw new StorageException(e);
    }
  }
}
