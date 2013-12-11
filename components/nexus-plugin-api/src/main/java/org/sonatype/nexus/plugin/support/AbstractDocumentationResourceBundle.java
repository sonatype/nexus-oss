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

package org.sonatype.nexus.plugin.support;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.sonatype.nexus.mime.MimeSupport;
import org.sonatype.nexus.web.WebResource;
import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.annotations.VisibleForTesting;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @deprecated Replace usage with {@link DocumentationBundleSupport}.
 */
@Deprecated
public abstract class AbstractDocumentationResourceBundle
    extends ComponentSupport
    implements DocumentationBundle
{
  private MimeSupport mimeSupport;

  protected AbstractDocumentationResourceBundle() {
    // empty
  }

  @VisibleForTesting
  protected AbstractDocumentationResourceBundle(final MimeSupport mimeSupport) {
    this.mimeSupport = mimeSupport;
  }

  @Inject
  public void setMimeSupport(final MimeSupport mimeSupport) {
    this.mimeSupport = checkNotNull(mimeSupport);
  }

  public List<WebResource> getResources() {
    List<WebResource> resources = new LinkedList<WebResource>();

    ZipFile zip = null;
    try {
      zip = getZipFile();

      if (zip != null) {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
          ZipEntry entry = entries.nextElement();

          if (entry.isDirectory()) {
            continue;
          }
          String name = entry.getName();
          if (!name.startsWith("docs")) {
            continue;
          }

          name = "/" + name;

          URL url = new URL("jar:file:" + zip.getName() + "!" + name);

          // to lessen clash possibilities, this way only within single plugin may be clashes, but one
          // plugin is usually developed by one team or one user so this is okay
          // system-wide clashes are much harder to resolve
          String path = "/" + getPluginId() + "/" + getPathPrefix() + name;

          resources.add(new DefaultWebResource(url, path, mimeSupport.guessMimeTypeFromPath(name)));
        }

        if (log.isTraceEnabled()) {
          log.trace("Discovered documentation for: {}", getPluginId());
          for (WebResource resource : resources) {
            log.trace("  {}", resource);
          }
        }
      }
      else {
        log.debug("No documentation discovered for: {}", getPluginId());
      }
    }
    catch (IOException e) {
      log.error("Error discovering plugin documentation {}", getPluginId(), e);
    }
    finally {
      if (zip != null) {
        try {
          zip.close();
        }
        catch (IOException e) {
          log.debug(e.getMessage(), e);
        }
      }
    }

    return resources;
  }

  public String getPathPrefix() {
    return "default";
  }

  public abstract String getDescription();

  public abstract String getPluginId();

  protected ZipFile getZipFile()
      throws IOException
  {
    return getZipFile(getClass());
  }

  protected ZipFile getZipFile(final Class<?> clazz)
      throws IOException, UnsupportedEncodingException
  {
    URL baseClass = clazz.getClassLoader().getResource(clazz.getName().replace('.', '/') + ".class");

    if ("file".equals(baseClass.getProtocol())) {
      // for now, assume that unpacked directory-based plugins do not contain documentation
      return null;
    }

    assert baseClass.getProtocol().equals("jar");

    String jarPath = baseClass.getPath().substring(5, baseClass.getPath().indexOf("!"));
    return new ZipFile(URLDecoder.decode(jarPath, "UTF-8"));
  }

}
