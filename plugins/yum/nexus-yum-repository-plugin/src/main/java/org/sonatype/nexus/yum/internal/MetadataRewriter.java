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
package org.sonatype.nexus.yum.internal;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.DigesterUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.sonatype.nexus.yum.Yum.PATH_OF_REPOMD_XML;

/**
 * Utilities for rewriting YUM metadata.
 *
 * @since 2.11
 */
public class MetadataRewriter
{

  private static final Logger log = LoggerFactory.getLogger(MetadataRewriter.class);

  private MetadataRewriter() {
  }

  /**
   * Rewrites locations in primary.xml after a merge.
   * Locations are wrongly written as file urls of merged group repository base dirs, which will be rewritten to be
   * relative to group repository.
   *
   * @param repository                 containing yum repository
   * @param memberRepositoriesBaseDirs list of merged group repository base dirs
   * @return true if primary.xml/repomd.xml was changed
   */
  public static boolean rewritePrimaryLocationsAfterMerge(final Repository repository,
                                                          final List<File> memberRepositoriesBaseDirs)
  {
    log.debug("Checking if {}:primary.xml locations should be rewritten after merge", repository.getId());
    return rewritePrimaryLocations(
        repository,
        new Processor()
        {
          @Override
          public boolean process(final Element location) {
            String xmlBase = location.getAttribute("xml:base");
            if (xmlBase != null) {
              String href = location.getAttribute("href");
              if (!xmlBase.endsWith("/")) {
                xmlBase += "/";
              }
              href = xmlBase + href;
              for (File memberReposBaseDir : memberRepositoriesBaseDirs) {
                String memberRepoDirPath = memberReposBaseDir.getPath();
                int pos = href.indexOf(memberRepoDirPath);
                if (pos > 0) {
                  if (pos + memberRepoDirPath.length() <= href.length()) {
                    href = href.substring(pos + memberRepoDirPath.length());
                    if (href.startsWith("/")) {
                      href = href.substring(1);
                    }
                  }
                  location.setAttribute("href", href);
                  location.removeAttribute("xml:base");
                  return true;
                }
              }
            }
            return false;
          }
        }
    );
  }

  /**
   * Rewrites locations in primary.xml after it had been proxied.
   * All locations that have an xml:base + url matching repository url will be changed to be relative to repository.
   *
   * @param repository containing yum repository
   * @return true if primary.xml/repomd.xml was changed
   */
  public static boolean rewritePrimaryLocationsAfterProxy(final ProxyRepository repository) {
    log.debug("Checking if {}:primary.xml locations should be rewritten after being proxied", repository.getId());
    final String repositoryUrl = repository.getRemoteUrl();
    return rewritePrimaryLocations(
        repository,
        new Processor()
        {
          @Override
          public boolean process(final Element location) {
            String xmlBase = location.getAttribute("xml:base");
            if (xmlBase != null) {
              String href = location.getAttribute("href");
              if (!xmlBase.endsWith("/")) {
                xmlBase += "/";
              }
              href = xmlBase + href;
              if (href.startsWith(repositoryUrl)) {
                href = href.substring(repositoryUrl.length());
                if (href.startsWith("/")) {
                  href = href.substring(1);
                }
                location.setAttribute("href", href);
                location.removeAttribute("xml:base");
                return true;
              }
            }
            return false;
          }
        }
    );
  }

  /**
   * Remove references to sqlite from repomd.xml
   *
   * @param repository containing repomd.xml
   * @return true if repomd.xml was changed
   */
  public static boolean removeSqliteFromRepoMD(final Repository repository) {
    log.debug("Checking if sqlite databases should be removed from {}:repomd.xml", repository.getId());
    try {
      boolean changed = false;
      Document doc;
      StorageFileItem repoMDItem = (StorageFileItem) repository.retrieveItem(
          false, new ResourceStoreRequest("/" + PATH_OF_REPOMD_XML)
      );
      try (InputStream in = new BufferedInputStream(repoMDItem.getInputStream())) {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        doc = documentBuilder.parse(in);
        List<Element> elementsToRemove = Lists.newArrayList();
        NodeList dataNodes = doc.getElementsByTagName("data");
        for (int i = 0; i < dataNodes.getLength(); i++) {
          Element data = (Element) dataNodes.item(i);
          if (data.getAttribute("type").endsWith("_db")) {
            elementsToRemove.add(data);
            changed = true;
          }
        }
        for (Element element : elementsToRemove) {
          element.getParentNode().removeChild(element);
        }
      }

      if (changed) {
        log.debug("Removing sqlite databases from {}:repomd.xml", repository.getId());
        ByteArrayOutputStream repoMDOut = new ByteArrayOutputStream();
        try (OutputStream out = new BufferedOutputStream(repoMDOut)) {
          Transformer transformer = TransformerFactory.newInstance().newTransformer();
          transformer.transform(new DOMSource(doc), new StreamResult(out));
        }
        storeItem(repository, PATH_OF_REPOMD_XML, repoMDOut.toByteArray(), "application/xml");
      }

      return changed;
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Process all locations from primary.xml. If any of location is changed will save the changed primary.xml and update
   * repomd.xml according to new primary.xml.
   *
   * @param repository containing primary.xml
   * @param processor  location processor
   * @return true if primary.xml/repomd.xml was changed
   */
  public static boolean rewritePrimaryLocations(final Repository repository, final Processor processor) {
    try {
      byte[] repoMDContent = readRepoMD(repository);
      byte[] primaryContent = processPrimary(repository, processor, repoMDContent);

      if (primaryContent != null) {
        storePrimary(repository, repoMDContent, primaryContent);
        return true;
      }
      return false;
    }
    catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

  /**
   * Store primary.xml and update content of repomd.xml accordingly.
   *
   * @param repository     repository containing primary.xml/repomd.xml
   * @param repoMDContent  repomd.xml content
   * @param primaryContent primary.xml
   */
  private static void storePrimary(final Repository repository,
                                   final byte[] repoMDContent,
                                   final byte[] primaryContent)
      throws Exception
  {
    log.debug("Rewriting {}:primary.xml locations", repository.getId());

    int openSize = primaryContent.length;
    String openSha256 = DigesterUtils.getDigest("SHA-256", new ByteArrayInputStream(primaryContent));

    byte[] primaryCompressedContent = compress(primaryContent);

    int compressedSize = primaryCompressedContent.length;
    String compressedSha256 = DigesterUtils.getDigest("SHA-256", new ByteArrayInputStream(primaryCompressedContent));

    String primaryNameOld = null;
    String primaryNameNew = null;

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    Document doc = documentBuilder.parse(new ByteArrayInputStream(repoMDContent));
    NodeList dataElements = doc.getElementsByTagName("data");
    for (int i = 0; i < dataElements.getLength(); i++) {
      Element data = (Element) dataElements.item(i);
      if ("primary".equals(data.getAttribute("type"))) {
        String checksum = data.getElementsByTagName("checksum").item(0).getTextContent();
        data.getElementsByTagName("checksum").item(0).setTextContent(String.valueOf(compressedSha256));
        data.getElementsByTagName("open-checksum").item(0).setTextContent(String.valueOf(openSha256));
        data.getElementsByTagName("size").item(0).setTextContent(String.valueOf(compressedSize));
        data.getElementsByTagName("open-size").item(0).setTextContent(String.valueOf(openSize));
        Element location = (Element) data.getElementsByTagName("location").item(0);
        String href = primaryNameOld = primaryNameNew = location.getAttribute("href");
        if (href.contains(checksum)) {
          location.setAttribute("href", primaryNameNew = href.replace(checksum, compressedSha256));
        }
      }
    }

    ByteArrayOutputStream repoMDOut = new ByteArrayOutputStream();
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.transform(new DOMSource(doc), new StreamResult(repoMDOut));

    if (!Objects.equals(primaryNameOld, primaryNameNew)) {
      repository.deleteItem(false, new ResourceStoreRequest("/" + primaryNameOld));
    }
    storeItem(repository, primaryNameNew, primaryCompressedContent, "application/x-gzip");
    storeItem(repository, PATH_OF_REPOMD_XML, repoMDOut.toByteArray(), "application/xml");
  }

  private static void storeItem(final Repository repository,
                                final String path,
                                final byte[] content,
                                final String mimeType)
      throws Exception
  {
    DefaultStorageFileItem item = new DefaultStorageFileItem(
        repository,
        new ResourceStoreRequest("/" + path),
        true,
        true,
        new PreparedContentLocator(new ByteArrayInputStream(content), mimeType, ContentLocator.UNKNOWN_LENGTH)
    );

    repository.storeItem(false, item);
  }

  /**
   * GZip provided bytes.
   *
   * @param bytes to be compressed
   * @return compressed bytes
   */
  private static byte[] compress(final byte[] bytes)
      throws Exception
  {
    byte[] primaryCompressedBytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         OutputStream gzos = new GZIPOutputStream(baos)) {
      IOUtils.copy(new ByteArrayInputStream(bytes), gzos);
      gzos.close();
      primaryCompressedBytes = baos.toByteArray();
    }
    return primaryCompressedBytes;
  }

  /**
   * Read and process all location entries using provided processor.
   *
   * @param repository    repository containing primary.xml
   * @param processor     location processor
   * @param repoMDContent repomx.xml content
   * @return content of primary.xml after processing
   */
  private static byte[] processPrimary(final Repository repository,
                                       final Processor processor,
                                       final byte[] repoMDContent)
      throws Exception
  {
    RepoMD repoMD = new RepoMD(new ByteArrayInputStream(repoMDContent));

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

    byte[] primaryContent = null;

    StorageFileItem primaryItem = (StorageFileItem) repository.retrieveItem(
        false, new ResourceStoreRequest("/" + repoMD.getLocation("primary"))
    );
    try (InputStream primaryIn = new GZIPInputStream(new BufferedInputStream(primaryItem.getInputStream()))) {
      Document doc = documentBuilder.parse(primaryIn);
      NodeList locations = doc.getElementsByTagName("location");
      if (locations != null) {
        boolean changed = false;
        for (int i = 0; i < locations.getLength(); i++) {
          Element location = (Element) locations.item(i);
          if (processor.process(location)) {
            changed = true;
          }
        }
        if (changed) {
          Transformer transformer = TransformerFactory.newInstance().newTransformer();
          ByteArrayOutputStream primaryOut = new ByteArrayOutputStream();
          transformer.transform(new DOMSource(doc), new StreamResult(primaryOut));
          primaryOut.close();
          primaryContent = primaryOut.toByteArray();
        }
      }
    }
    return primaryContent;
  }

  /**
   * Read content of repomd.xml.
   *
   * @param repository repository containing repomd.xml
   * @return content of repomd.xml
   */
  private static byte[] readRepoMD(final Repository repository)
      throws Exception
  {
    StorageFileItem repoMDItem = (StorageFileItem) repository.retrieveItem(
        false, new ResourceStoreRequest("/" + PATH_OF_REPOMD_XML)
    );
    byte[] repoMDBytes;
    try (InputStream in = repoMDItem.getInputStream()) {
      repoMDBytes = IOUtils.toByteArray(in);
    }
    return repoMDBytes;
  }

  /**
   * Location processor.
   */
  private static interface Processor
  {
    boolean process(Element location);
  }

}
