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
package org.sonatype.nexus.plugins.ruby.group;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.sonatype.nexus.plugins.ruby.NexusStorage;
import org.sonatype.nexus.plugins.ruby.RubyGroupRepository;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.FileContentLocator;
import org.sonatype.nexus.proxy.item.PreparedContentLocator;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.GroupItemNotFoundException;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.ruby.BundlerApiFile;
import org.sonatype.nexus.ruby.DependencyFile;
import org.sonatype.nexus.ruby.RubygemsFile;
import org.sonatype.nexus.ruby.RubygemsGateway;
import org.sonatype.nexus.ruby.SpecsIndexType;
import org.sonatype.nexus.ruby.SpecsIndexZippedFile;
import org.sonatype.nexus.ruby.layout.ProxyStorage;

import org.codehaus.plexus.util.IOUtil;

public class GroupNexusStorage
    extends NexusStorage
    implements ProxyStorage
{
  private final RubygemsGateway gateway;

  private final RubyGroupRepository repository;

  public GroupNexusStorage(RubyGroupRepository repository, RubygemsGateway gateway) {
    super(repository);
    this.repository = repository;
    this.gateway = gateway;
  }

  @Override
  public void retrieve(DependencyFile file) {
    doRetrieve(file);
  }

  @Override
  public void retrieve(SpecsIndexZippedFile file) {
    doRetrieve(file);
  }

  private void doRetrieve(RubygemsFile file) {
    try {
      file.set(setup(file));
    }
    catch (ItemNotFoundException e) {
      file.markAsNotExists();
    }
    catch (IOException | IllegalOperationException | UnsupportedStorageOperationException e) {
      file.setException(e);
    }
  }

  private StorageItem setup(RubygemsFile file)
      throws ItemNotFoundException, UnsupportedStorageOperationException, IOException, IllegalOperationException
  {
    ResourceStoreRequest req = new ResourceStoreRequest(file.storagePath());
    // TODO is synchronized really needed
    synchronized (repository) {
      List<StorageItem> items = repository.doRetrieveItems(req);
      if (items.size() == 1) {
        return items.get(0);
      }
      return store(file, items);
    }
  }

  private StorageItem store(RubygemsFile file, List<StorageItem> items)
      throws UnsupportedStorageOperationException, IllegalOperationException, IOException
  {
    StorageFileItem localItem = null;
    try {
      localItem = (StorageFileItem) repository.getLocalStorage().retrieveItem(repository,
          new ResourceStoreRequest(file.storagePath()));
    }
    catch (ItemNotFoundException e) {
      // Ignored. there are situations like a freshly created repo
    }

    boolean outdated = true; // outdated is true if there are no local-specs
    if (localItem != null) {
      // using the timestamp from the file since localSpecsItem.getModified() produces something but
      // not from .nexus/attributes/* file !!!
      long modified = ((FileContentLocator) localItem.getContentLocator()).getFile().lastModified();
      outdated = false;
      for (StorageItem item : items) {
        outdated = outdated || (item.getModified() > modified);
      }
    }

    if (outdated) {
      switch (file.type()) {
        case DEPENDENCY:
          return merge((DependencyFile) file, items);
        case SPECS_INDEX_ZIPPED:
          return merge((SpecsIndexZippedFile) file, items);
        default:
          throw new RuntimeException("BUG: should never reach here: " + file);
      }
    }
    else {
      return localItem;
    }
  }

  private StorageItem merge(SpecsIndexZippedFile file, List<StorageItem> items)
      throws UnsupportedStorageOperationException, IOException, IllegalOperationException
  {
    List<InputStream> streams = new LinkedList<InputStream>();
    try {
      for (StorageItem item : items) {
        streams.add(new GZIPInputStream(((StorageFileItem) item).getInputStream()));
      }
      return storeSpecsIndex(file, gateway.mergeSpecs(streams, file.specsType() == SpecsIndexType.LATEST));
    }
    finally {
      if (streams != null) {
        for (InputStream i : streams) {
          IOUtil.close(i);
        }
      }
    }
  }

  private StorageItem storeSpecsIndex(SpecsIndexZippedFile file, InputStream content)
      throws UnsupportedStorageOperationException, IOException, IllegalOperationException
  {
    OutputStream out = null;
    try {
      ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
      out = new GZIPOutputStream(gzipped);
      IOUtil.copy(content, out);
      // need to close gzip stream here
      out.close();
      ContentLocator cl = new PreparedContentLocator(new ByteArrayInputStream(gzipped.toByteArray()),
          "application/x-gzip",
          gzipped.size());
      DefaultStorageFileItem item =
          new DefaultStorageFileItem(repository,
              new ResourceStoreRequest(file.storagePath()),
              true, true, cl);
      repository.storeItem(false, item);
      return item;
    }
    finally {
      IOUtil.close(out);
    }
  }

  private StorageItem merge(DependencyFile file, List<StorageItem> dependencies)
      throws UnsupportedStorageOperationException, IllegalOperationException, IOException
  {
    List<InputStream> streams = new LinkedList<InputStream>();
    InputStream content = null;
    try {
      for (StorageItem item : dependencies) {
        streams.add(((StorageFileItem) item).getInputStream());
      }
      content = gateway.mergeDependencies(streams, true);
      ContentLocator cl = new PreparedContentLocator(content,
          file.type().mime(),
          PreparedContentLocator.UNKNOWN_LENGTH);

      DefaultStorageFileItem item =
          new DefaultStorageFileItem(repository,
              new ResourceStoreRequest(file.storagePath()),
              true, true, cl);
      repository.storeItem(false, item);
      return item;
    }
    finally {
      IOUtil.close(content);
      for (InputStream is : streams) {
        IOUtil.close(is);
      }
    }
  }

  @Override
  public void retrieve(BundlerApiFile file) {
    try {
      // mimic request as coming directly to ProxyRepository
      repository.doRetrieveItems(new ResourceStoreRequest(file.storagePath()));
      file.set(null);
    }
    catch (GroupItemNotFoundException | IOException e) {
      file.setException(e);
    }
  }

  @Override
  public boolean isExpired(DependencyFile file) {
    return true;
  }
}