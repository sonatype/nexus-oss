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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.util.io.StreamSupport;

import com.bolyuba.nexus.plugin.npm.internal.Version;
import com.bolyuba.nexus.plugin.npm.service.HostedMetadataService;
import com.bolyuba.nexus.plugin.npm.service.PackageRoot;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;

public class RecreateMetadataWalkerProcessor
    extends AbstractWalkerProcessor
{
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ObjectMapper objectMapper;

  private final HostedMetadataService hostedMetadataService;

  private int processedPackages;

  private int processedPackageVersions;

  public RecreateMetadataWalkerProcessor(final HostedMetadataService hostedMetadataService)
  {
    this.objectMapper = new ObjectMapper();
    this.hostedMetadataService = hostedMetadataService;
    this.processedPackages = 0;
    this.processedPackageVersions = 0;
  }

  public int getProcessedPackages() {
    return processedPackages;
  }

  public int getProcessedPackageVersions() {
    return processedPackageVersions;
  }

  @Override
  public void processItem(final WalkerContext context, final StorageItem item)
      throws Exception
  {
    // nop
  }

  public void onCollectionExit(final WalkerContext context, final StorageCollectionItem coll)
      throws Exception
  {
    final TreeMap<Version, Map<String, Object>> packages = Maps.newTreeMap();

    // collect all the possible tarballs
    final Collection<StorageItem> files = coll.list();
    for (StorageItem file : files) {
      if (!(file instanceof StorageFileItem)) {
        // not a file, just skip it
        continue;
      }
      if (!file.getName().endsWith(".tgz")) {
        // not a tarball, just skip it
        continue;
      }
      try {
        final Map<String, Object> packageJson = extractPackageJson((StorageFileItem) file);
        if (packageJson == null || !packageJson.containsKey("name") || !packageJson.containsKey("version")) {
          // not found in tarball, not containing needed elements, just skip it but this time log it
          log.info("Not found or malformed package.json from {}", file.getRepositoryItemUid());
          continue;
        }
        final Version version = Version.parse(String.valueOf(packageJson.get("version")));
        packages.put(version, packageJson);
      }
      catch (Exception e) {
        log.info("Failed to extract or malformed package.json from {}", file.getRepositoryItemUid());
      }
    }

    // ie. dir empty or all extract failed?
    if (packages.isEmpty()) {
      return;
    }

    // rebuild package root as possible and push it into database
    final Map<String, Object> rootJson = Maps.newHashMap();
    rootJson.put("dist-tags", Maps.<String, Object>newHashMap());
    rootJson.put("versions", Maps.<String, Object>newHashMap());
    rootJson.put("x-nx-rebuilt", new Date());

    for (Version version : packages.keySet()) {
      try {
        processPackageJson(rootJson, packages.get(version));
      }
      catch (IllegalArgumentException e) {
        log.info("Failed to process package.json from {} version {}", coll.getPath(), version, e);
      }
    }

    // ie. all processPackageJson failed?
    if (((Map<String, Object>)rootJson.get("versions")).isEmpty()) {
      return;
    }

    final Map<String, Object> lastPackage = packages.get(packages.lastKey());
    final String lastVersion = String.valueOf(lastPackage.get("version"));
    final Map<String, Object> distTags = (Map<String, Object>) rootJson.get("dist-tags");
    distTags.remove(lastVersion);
    distTags.put("latest", lastVersion);

    final PackageRoot packageRoot = new PackageRoot(context.getRepository().getId(), rootJson);
    hostedMetadataService.consumePackageRoot(packageRoot);
    processedPackages = processedPackages + 1;
    processedPackageVersions = processedPackageVersions + packages.size();
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
    // TODO: some sanity check of package.json size? If so, for how much (as they grow as package gets "older")
    checkArgument(tarEntry.getSize() < 100000, "Too large package.json: %s bytes", tarEntry.getSize());
    // do this in memory, as package.json is expected to be fairly small (few kb at most)
    final ByteArrayOutputStream bos = new ByteArrayOutputStream(Ints.checkedCast(tarEntry.getSize()));
    StreamSupport.copy(tarArchiveInputStream, bos, StreamSupport.BUFFER_SIZE);

    // read JSON into a map
    final Map<String, Object> packageJson = objectMapper
        .readValue(bos.toByteArray(), new TypeReference<HashMap<String, Object>>() { });
    return packageJson;
  }

  private void processPackageJson(final Map<String, Object> rootJson, final Map<String, Object> packageJson)
      throws Exception
  {
    liftAttributes(rootJson, packageJson);
    final String version = String.valueOf(packageJson.get("version"));
    final Map<String, Object> distTags = (Map<String, Object>) rootJson.get("dist-tags");
    final Map<String, Object> versions = (Map<String, Object>) rootJson.get("versions");
    distTags.put(version, version);
    versions.put(version, packageJson);
  }

  private void liftAttributes(final Map<String, Object> higher, final Map<String, Object> lower) {
    liftAttributeOrEnforce(higher, lower, "name");
    liftAttribute(higher, lower, "description");
    liftAttribute(higher, lower, "keywords");
    liftAttribute(higher, lower, "homepage");
    // TODO: figure out what else?
  }

  private void liftAttributeOrEnforce(final Map<String, Object> higher, final Map<String, Object> lower,
                                      final String attributeKey)
  {
    if (higher.containsKey(attributeKey) && lower.containsKey(attributeKey)) {
      checkArgument(
          Objects.equals(higher.get(attributeKey), lower.get(attributeKey)),
          "inconsistent attribute: %s higher=%s, lower=%s",
          attributeKey,
          higher.get(attributeKey),
          lower.get(attributeKey)
      );
    }
    else {
      liftAttribute(higher, lower, attributeKey);
    }
  }

  private void liftAttribute(final Map<String, Object> higher, final Map<String, Object> lower,
                             final String attributeKey)
  {
    if (lower.containsKey(attributeKey)) {
      higher.put(attributeKey, lower.get(attributeKey));
    }
  }
}
