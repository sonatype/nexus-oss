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

package org.sonatype.nexus.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.sonatype.nexus.proxy.attributes.inspectors.DigestCalculatingInspector;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.plexus.rest.resource.RestletResponseCustomizer;

import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Tag;

import static org.sonatype.plexus.rest.resource.AbstractPlexusResource.addHttpResponseHeader;

public class StorageFileItemRepresentation
    extends StorageItemRepresentation
    implements RestletResponseCustomizer
{
  public StorageFileItemRepresentation(StorageFileItem file) {
    super(MediaType.valueOf(file.getMimeType()), file);

    setSize(file.getLength());

    if (file.getRepositoryItemAttributes().containsKey(DigestCalculatingInspector.DIGEST_SHA1_KEY)) {
      // Shield SHA1
      // {SHA1{xxxx}}
      final String tag =
          String.format("{SHA1{%s}}",
              file.getRepositoryItemAttributes().get(DigestCalculatingInspector.DIGEST_SHA1_KEY));
      setTag(new Tag(tag, false));
    }

    if (file.getItemContext().containsKey(AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY)) {
      String filename =
          file.getItemContext().get(AbstractResourceStoreContentPlexusResource.OVERRIDE_FILENAME_KEY).toString();

      setDownloadable(true);

      setDownloadName(filename);
    }
  }

  protected StorageFileItem getStorageItem() {
    return (StorageFileItem) super.getStorageItem();
  }

  public boolean isTransient() {
    return !getStorageItem().isReusableStream();
  }

  @Override
  public void write(OutputStream outputStream)
      throws IOException
  {
    InputStream is = null;

    try {
      is = getStorageItem().getInputStream();

      IOUtil.copy(is, outputStream);
    }
    catch (IOException e) {
      if ("EofException".equals(e.getClass().getSimpleName())) {
        // This is for Jetty's org.eclipse.jetty.io.EofException
        // https://issues.sonatype.org/browse/NEXUS-217
      }
      else if (e instanceof SocketException) {
        // https://issues.sonatype.org/browse/NEXUS-217
      }
      else {
        throw e;
      }
    }
    finally {
      IOUtil.close(is);
    }
  }

  /**
   * Adds "X-Content-Type-Options: nosniff" HTTP response header to disable IE for sniffing into response content to
   * determine content type (see NEXUS-5023).
   *
   * @param response Restlet response
   */
  @Override
  public void customize(final Response response) {
    addHttpResponseHeader(response, "X-Content-Type-Options", "nosniff");
  }

}
