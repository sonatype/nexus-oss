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

package org.sonatype.nexus.proxy.wastebasket;

import java.io.File;
import java.io.IOException;

import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.nexus.proxy.walker.AffirmativeStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.sisu.resource.scanner.Listener;
import org.sonatype.sisu.resource.scanner.Scanner;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role = Wastebasket.class)
public class DefaultWastebasket
    extends AbstractLoggingComponent
    implements SmartWastebasket
{
  private static final String TRASH_PATH_PREFIX = "/.nexus/trash";

  protected static final long ALL = -1L;

  // ==

  @Requirement
  private ApplicationConfiguration applicationConfiguration;

  protected ApplicationConfiguration getApplicationConfiguration() {
    return applicationConfiguration;
  }

  // ==

  @Requirement
  private Walker walker;

  protected Walker getWalker() {
    return walker;
  }

  // @TestAccessible
  void setWalker(final Walker walker) {
    this.walker = walker;
  }

  // ==

  @Requirement(hint = "serial")
  private Scanner scanner;

  protected Scanner getScanner() {
    return scanner;
  }

  // ==

  @Requirement
  private RepositoryRegistry repositoryRegistry;

  protected RepositoryRegistry getRepositoryRegistry() {
    return repositoryRegistry;
  }

  // ==

  private DeleteOperation deleteOperation = DeleteOperation.MOVE_TO_TRASH;

  // ==============================
  // Wastebasket iface

  public DeleteOperation getDeleteOperation() {
    return deleteOperation;
  }

  public void setDeleteOperation(final DeleteOperation deleteOperation) {
    this.deleteOperation = deleteOperation;
  }

  public Long getTotalSize() {
    Long totalSize = null;

    for (Repository repository : getRepositoryRegistry().getRepositories()) {
      Long repoWBSize = getSize(repository);

      if (repoWBSize != null) {
        totalSize += repoWBSize;
      }
    }

    return totalSize;
  }

  public void purgeAll()
      throws IOException
  {
    purgeAll(ALL);
  }

  public void purgeAll(final long age)
      throws IOException
  {
    for (Repository repository : getRepositoryRegistry().getRepositories()) {
      purge(repository, age);
    }

    // NEXUS-4078: deleting "legacy" trash too for now
    // NEXUS-4468 legacy was not being cleaned up
    final File basketFile =
        getApplicationConfiguration().getWorkingDirectory(AbstractRepositoryFolderCleaner.GLOBAL_TRASH_KEY);

    // check for existence, is this needed at all?
    if (basketFile.isDirectory()) {
      final long limitDate = System.currentTimeMillis() - age;
      getScanner().scan(basketFile, new Listener()
      {
        @Override
        public void onFile(File file) {
          if (age == ALL || file.lastModified() < limitDate) {
            file.delete();
          }
        }

        @Override
        public void onExitDirectory(File directory) {
          if (!basketFile.equals(directory) && directory.list().length == 0) {
            directory.delete();
          }
        }

        public void onEnterDirectory(File directory) {
        }

        public void onEnd() {
        }

        public void onBegin() {
        }
      });
    }
  }

  public Long getSize(final Repository repository) {
    return null;
  }

  public void purge(final Repository repository)
      throws IOException
  {
    purge(repository, ALL);
  }

  public void purge(final Repository repository, final long age)
      throws IOException
  {
    ResourceStoreRequest req = new ResourceStoreRequest(getTrashPath(repository, RepositoryItemUid.PATH_ROOT));
    // NEXUS-4642 shall not delete the directory, since causes a problem if this has been symlinked to another
    // directory.
    // walker and walk and changes for age
    if (repository.getLocalStorage().containsItem(repository, req)) {
      req.setRequestGroupLocalOnly(true);
      req.setRequestLocalOnly(true);
      DefaultWalkerContext ctx = new DefaultWalkerContext(repository, req, new AffirmativeStoreWalkerFilter());
      ctx.getProcessors().add(new WastebasketWalker(age));
      getWalker().walk(ctx);
    }
  }

  @Override
  public void delete(final LocalRepositoryStorage ls, final Repository repository, final ResourceStoreRequest request)
      throws LocalStorageException
  {
    final DeleteOperation operation;
    if (request.getRequestContext().containsKey(DeleteOperation.DELETE_OPERATION_CTX_KEY)) {
      operation = (DeleteOperation) request.getRequestContext().get(DeleteOperation.DELETE_OPERATION_CTX_KEY);
    }
    else {
      operation = getDeleteOperation();
    }

    delete(ls, repository, request, operation);
  }

  private void delete(final LocalRepositoryStorage ls, final Repository repository,
                      final ResourceStoreRequest request, final DeleteOperation type)
      throws LocalStorageException
  {
    try {
      if (DeleteOperation.MOVE_TO_TRASH.equals(type)) {
        ResourceStoreRequest trashed =
            new ResourceStoreRequest(getTrashPath(repository, request.getRequestPath()));
        ls.moveItem(repository, request, trashed);
      }

      ls.shredItem(repository, request);
    }
    catch (ItemNotFoundException e) {
      // silent
    }
    catch (UnsupportedStorageOperationException e) {
      // yell
      throw new LocalStorageException("Delete operation is unsupported!", e);
    }
  }

  public boolean undelete(final LocalRepositoryStorage ls, final Repository repository,
                          final ResourceStoreRequest request)
      throws LocalStorageException
  {
    try {
      ResourceStoreRequest trashed =
          new ResourceStoreRequest(getTrashPath(repository, request.getRequestPath()));
      ResourceStoreRequest untrashed =
          new ResourceStoreRequest(getUnTrashPath(repository, request.getRequestPath()));

      if (!ls.containsItem(repository, untrashed)) {
        ls.moveItem(repository, trashed, untrashed);
        return true;
      }
    }
    catch (ItemNotFoundException e) {
      // silent
    }
    catch (UnsupportedStorageOperationException e) {
      // yell
      throw new LocalStorageException("Undelete operation is unsupported!", e);
    }

    return false;
  }

  // ==============================
  // SmartWastebasket iface

  public void setMaximumSizeConstraint(final MaximumSizeConstraint constraint) {
    // TODO Implement this
  }

  // ==

  protected String getTrashPath(final Repository repository, final String path) {
    if (path.startsWith(TRASH_PATH_PREFIX)) {
      return path;
    }
    else if (path.startsWith(RepositoryItemUid.PATH_SEPARATOR)) {
      return TRASH_PATH_PREFIX + path;
    }
    else {
      return TRASH_PATH_PREFIX + RepositoryItemUid.PATH_SEPARATOR + path;
    }
  }

  protected String getUnTrashPath(final Repository repository, final String path) {
    String result = path;
    if (result.startsWith(TRASH_PATH_PREFIX)) {
      result = result.substring(TRASH_PATH_PREFIX.length(), result.length());
    }

    if (!result.startsWith(RepositoryItemUid.PATH_ROOT)) {
      result = RepositoryItemUid.PATH_ROOT + result;
    }

    return result;
  }
}
