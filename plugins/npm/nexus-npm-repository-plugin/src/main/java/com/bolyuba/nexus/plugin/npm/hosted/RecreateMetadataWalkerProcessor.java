/*
 * Copyright (c) 2007-2014 Sonatype, Inc. and Georgy Bolyuba. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package com.bolyuba.nexus.plugin.npm.hosted;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.walker.AbstractFileWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.util.io.StreamSupport;

import com.bolyuba.nexus.plugin.npm.NpmRepository;
import com.bolyuba.nexus.plugin.npm.service.HostedMetadataService;
import com.bolyuba.nexus.plugin.npm.service.PackageRequest;
import com.bolyuba.nexus.plugin.npm.service.PackageRoot;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecreateMetadataWalkerProcessor
    extends AbstractFileWalkerProcessor
{
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final HostedMetadataService hostedMetadataService;

  private int processedFileItemCount;

  public RecreateMetadataWalkerProcessor(final HostedMetadataService hostedMetadataService) {
    this.hostedMetadataService = hostedMetadataService;
    this.processedFileItemCount = 0;
  }

  public int getProcessedFileItemCount() {
    return processedFileItemCount;
  }

  @Override
  protected void processFileItem(final WalkerContext context, final StorageFileItem file) throws Exception {
    if (!file.getName().endsWith(".tgz")) {
      return;
    }
    final Map<String, Object> packageJson = extractPackageJson(file);
    if (packageJson == null || !packageJson.containsKey("name") || !packageJson.containsKey("version")) {
      log.info("Failed to extract or malformed package.json from {}", file.getRepositoryItemUid());
      return;
    }
    try {
      final PackageRoot packageRoot = new PackageRoot(context.getRepository().getId(),
          Maps.<String, Object>newHashMap());
      final Map<String, Object> rootRaw = packageRoot.getRaw();
      liftAttributes(packageJson, rootRaw);
      // TODO: dist-tags, but how? Would need ordering by version or publish date?
      rootRaw.put("x-nexus-rebuilt", new Date());
      final Map<String, Object> versions = Maps.newHashMap();
      versions.put((String) packageJson.get("version"), packageJson);
      rootRaw.put("versions", versions);
      hostedMetadataService.consumePackageRoot(packageRoot);
      processedFileItemCount = processedFileItemCount + 1;
    }
    catch (Exception e) {
      log.warn("Problem processing package {}", file.getRepositoryItemUid(), e);
    }
  }

  private void liftAttributes(final Map<String, Object> packageJson, final Map<String, Object> rootRaw) {
    liftAttribute(packageJson, rootRaw, "name");
    liftAttribute(packageJson, rootRaw, "description");
    liftAttribute(packageJson, rootRaw, "keywords");
    liftAttribute(packageJson, rootRaw, "homepage");
    // TODO: figure out what else?
  }

  private void liftAttribute(final Map<String, Object> packageJson, final Map<String, Object> rootRaw,
                             final String attributeKey)
  {
    if (packageJson.containsKey(attributeKey)) {
      rootRaw.put(attributeKey, packageJson.get(attributeKey));
    }
  }

  private Map<String, Object> extractPackageJson(final StorageFileItem file) throws IOException {
    TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(file.getInputStream());
    TarArchiveEntry tarEntry = tarArchiveInputStream.getNextTarEntry();
    while (tarEntry != null && !"package.json".equals(tarEntry.getName())) {
      tarEntry = tarArchiveInputStream.getNextTarEntry();
    }
    if (tarEntry == null || !"package.json".equals(tarEntry.getName()) || !tarEntry.isFile()) {
      return null;
    }
    // TODO: some sanity check of package.json size?
    // do this in memory, as package.json is expected to be fairly small (few kb at most)
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(Ints.checkedCast(tarEntry.getSize()));
    StreamSupport.copy(tarArchiveInputStream, bos, StreamSupport.BUFFER_SIZE);

    PackageRoot packageRoot = hostedMetadataService.parsePackageRoot(
        new PackageRequest(file.getResourceStoreRequest()),
        new ByteArrayContentLocator(bos.toByteArray(), NpmRepository.JSON_MIME_TYPE)
    );
    return packageRoot.getRaw();
  }
}
