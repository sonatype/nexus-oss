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

package org.sonatype.nexus.proxy.storage.local.fs;

import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageEOFException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.RemoteStorageEOFException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.RepositoryItemUidLock;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.item.uid.IsItemAttributeMetacontentAttribute;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.nexus.util.ItemPathUtils;
import org.sonatype.nexus.util.SystemPropertiesHelper;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.sonatype.nexus.proxy.ItemNotFoundException.reasonFor;

/**
 * The default FSPeer implementation, directly implementating it. There might be alternate implementations, like doing
 * 2nd level caching and so on.
 *
 * @author cstamas
 */
@Named
@Singleton
public class DefaultFSPeer
    extends AbstractLoggingComponent
    implements FSPeer
{

  private static final String HIDDEN_TARGET_SUFFIX = ".nx-upload";

  private static final String APPENDIX = "nx-tmp";

  private static final String REPO_TMP_FOLDER = ".nexus/tmp";

  @Override
  public boolean isReachable(final Repository repository, final File repositoryBaseDir,
                             final ResourceStoreRequest request, final File target)
      throws LocalStorageException
  {
    return target.exists() && target.canWrite();
  }

  @Override
  public boolean containsItem(final Repository repository, final File repositoryBaseDir,
                              final ResourceStoreRequest request, final File target)
      throws LocalStorageException
  {
    return target.exists();
  }

  @Override
  public File retrieveItem(final Repository repository, final File repositoryBaseDir,
                           final ResourceStoreRequest request, final File target)
      throws ItemNotFoundException, LocalStorageException
  {
    return target;
  }

  @Override
  public void storeItem(final Repository repository, final File repositoryBaseDir, final StorageItem item,
                        final File target, final ContentLocator cl)
      throws UnsupportedStorageOperationException, LocalStorageException
  {
    // create parents down to the file itself (this will make those if needed, otherwise return silently)
    mkParentDirs(repository, target);

    if (cl != null) {
      // we have _content_ (content or link), hence we store a file
      final File hiddenTarget = getHiddenTarget(repository, repositoryBaseDir, target, item);

      // NEXUS-4550: Part One, saving to "hidden" (temp) file
      // In case of error cleaning up only what needed
      // No locking needed, AbstractRepository took care of that
      FileOutputStream os = null;
      InputStream is = null;

      try {
        os = new FileOutputStream(hiddenTarget);

        is = cl.getContent();

        IOUtil.copy(is, os, getCopyStreamBufferSize());

        os.flush();
      }
      catch (EOFException e) // NXCM-4852: Upload premature end (thrown by Jetty
      // org.eclipse.jetty.io.EofException)
      {
        if (hiddenTarget != null) {
          hiddenTarget.delete();
        }

        throw new LocalStorageEOFException(String.format(
            "EOF during storing on path \"%s\" (while writing to hiddenTarget: \"%s\")",
            item.getRepositoryItemUid().toString(), hiddenTarget.getAbsolutePath()), e);
      }
      catch (RemoteStorageEOFException e) // NXCM-4852: Proxy remote peer response premature end (should be
      // translated by RRS)
      {
        if (hiddenTarget != null) {
          hiddenTarget.delete();
        }

        throw new LocalStorageEOFException(String.format(
            "EOF during caching on path \"%s\" (while writing to hiddenTarget: \"%s\")",
            item.getRepositoryItemUid().toString(), hiddenTarget.getAbsolutePath()), e);
      }
      catch (IOException e) {
        if (hiddenTarget != null) {
          hiddenTarget.delete();
        }

        throw new LocalStorageException(String.format(
            "Got exception during storing on path \"%s\" (while writing to hiddenTarget: \"%s\")",
            item.getRepositoryItemUid().toString(), hiddenTarget.getAbsolutePath()), e);
      }
      finally {
        IOUtil.close(is);

        IOUtil.close(os);
      }

      // NEXUS-4550: Part Two, moving the "hidden" (temp) file to final location
      // In case of error cleaning up both files
      // Locking is needed, AbstractRepository got shared lock only for destination

      // NEXUS-4550: FSPeer is the one that handles the rename in case of FS LS,
      // so we need here to claim exclusive lock on actual UID to perform the rename
      final RepositoryItemUidLock uidLock = item.getRepositoryItemUid().getLock();
      uidLock.lock(Action.create);

      try {
        handleRenameOperation(hiddenTarget, target);

        target.setLastModified(item.getModified());
      }
      catch (IOException e) {
        // if we ARE NOT handling attributes, do proper cleanup in case of IOEx
        // if we ARE handling attributes, leave backups in case of IOEx
        final boolean isCleanupNeeded =
            !item.getRepositoryItemUid().getBooleanAttributeValue(IsItemAttributeMetacontentAttribute.class);

        if (target != null && (isCleanupNeeded ||
            // NEXUS-4871 prevent zero length/corrupt files
            target.length() == 0)) {
          target.delete();
        }

        if (hiddenTarget != null && (isCleanupNeeded ||
            // NEXUS-4871 prevent zero length/corrupt files
            hiddenTarget.length() == 0)) {
          hiddenTarget.delete();
        }

        if (!isCleanupNeeded) {
          getLogger().warn(
              "No cleanup done for error that happened while trying to save attibutes of item {}, the backup is left as {}!",
              item.getRepositoryItemUid().toString(), hiddenTarget.getAbsolutePath());
        }

        throw new LocalStorageException(String.format(
            "Got exception during storing on path \"%s\" (while moving to final destination)",
            item.getRepositoryItemUid().toString()), e);
      }
      finally {
        uidLock.unlock();
      }
    }
    else {
      // we have no content, we talk about directory
      target.mkdir();

      target.setLastModified(item.getModified());
    }
  }

  @Override
  public void shredItem(final Repository repository, final File repositoryBaseDir,
                        final ResourceStoreRequest request, final File target)
      throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
  {
    if (getLogger().isDebugEnabled()) {
      getLogger().debug("Deleting file: " + target.getAbsolutePath());
    }
    if (target.isDirectory()) {
      try {
        FileUtils.deleteDirectory(target);
      }
      catch (IOException ex) {
        throw new LocalStorageException(String.format(
            "Could not delete directory in repository %s from path \"%s\"",
            RepositoryStringUtils.getHumanizedNameString(repository), target.getAbsolutePath()), ex);
      }
    }
    else if (target.isFile()) {
      try {
        FileUtils.forceDelete(target);
      }
      catch (IOException ex) {
        throw new LocalStorageException(String.format(
            "Could not delete file in repository %s from path \"%s\"",
            RepositoryStringUtils.getHumanizedNameString(repository), target.getAbsolutePath()));
      }
    }
    else {
      throw new ItemNotFoundException(reasonFor(request, repository,
          "Path %s not found in local storage of repository %s", request.getRequestPath(),
          RepositoryStringUtils.getHumanizedNameString(repository)));
    }
  }

  @Override
  public void moveItem(final Repository repository, final File repositoryBaseDir, final ResourceStoreRequest from,
                       final File fromTarget, final ResourceStoreRequest to, final File toTarget)
      throws ItemNotFoundException, UnsupportedStorageOperationException, LocalStorageException
  {
    if (fromTarget.exists()) {
      // create parents down to the file itself (this will make those if needed, otherwise return silently)
      mkParentDirs(repository, toTarget);

      try {
        org.sonatype.nexus.util.FileUtils.move(fromTarget, toTarget);
      }
      catch (IOException e) {
        getLogger().warn("Unable to move item, falling back to copy+delete: " + toTarget.getPath(),
            getLogger().isDebugEnabled() ? e : null);

        if (fromTarget.isDirectory()) {
          try {
            FileUtils.copyDirectoryStructure(fromTarget, toTarget);
          }
          catch (IOException ioe) {
            throw new LocalStorageException("Error during moveItem", ioe);
          }
        }
        else if (fromTarget.isFile()) {
          try {
            FileUtils.copyFile(fromTarget, toTarget);
          }
          catch (IOException ioe) {
            throw new LocalStorageException("Error during moveItem", ioe);
          }
        }
        else {
          // TODO throw exception?
          getLogger().error("Unexpected item kind: " + toTarget.getClass());
        }
        shredItem(repository, repositoryBaseDir, from, fromTarget);
      }
    }
    else {
      throw new ItemNotFoundException(reasonFor(from, repository,
          "Path %s not found in local storage of repository %s", from.getRequestPath(),
          RepositoryStringUtils.getHumanizedNameString(repository)));
    }
  }

  @Override
  public Collection<File> listItems(final Repository repository, final File repositoryBaseDir,
                                    final ResourceStoreRequest request, final File target)
      throws ItemNotFoundException, LocalStorageException
  {
    if (target.isDirectory()) {
      List<File> result = new ArrayList<File>();

      File[] files = target.listFiles(new FileFilter()
      {
        @Override
        public boolean accept(File pathname) {
          return !pathname.getName().endsWith(HIDDEN_TARGET_SUFFIX);
        }
      });

      if (files != null) {
        for (int i = 0; i < files.length; i++) {
          if (files[i].isFile() || files[i].isDirectory()) {
            String newPath = ItemPathUtils.concatPaths(request.getRequestPath(), files[i].getName());

            request.pushRequestPath(newPath);
            try {
              result.add(retrieveItem(repository, repositoryBaseDir, request, files[i]));
            }
            finally {
              request.popRequestPath();
            }
          }
        }
      }
      else {
        throw new LocalStorageException("Cannot list directory in repository " + repository + ", path "
            + target.getAbsolutePath());
      }

      return result;
    }
    else if (target.isFile()) {
      return null;
    }
    else {
      throw new ItemNotFoundException(reasonFor(request, repository,
          "Path %s not found in local storage of repository %s", request.getRequestPath(),
          RepositoryStringUtils.getHumanizedNameString(repository)));
    }
  }

  // ==

  protected File getHiddenTarget(final Repository repository, final File repositoryBaseDir, final File target,
                                 final StorageItem item)
      throws LocalStorageException
  {
    // NEXUS-5400: instead of putting "hidden" target in same dir structure as original file would reside (and
    // appending it
    // with some extra cruft), we place the file into repo-level tmp directory (/.nexus/tmp, REPO_TMP_FOLDER)
    // As since Nexus 2.0, due to attributes, it is required that whole repository from it's root must be kept on
    // same
    // volume (no subtree of it should reside on some other volume), meaning, rename would still happen
    // on same volume, hence is fast (is not copy+del on OS level).
    checkNotNull(target);

    try {
      final File repoTmpFolder = new File(repositoryBaseDir, REPO_TMP_FOLDER);
      mkDirs(repository, repoTmpFolder);

      // NEXUS-4955 add APPENDIX to make sure prefix is bigger the 3 chars
      return File.createTempFile(target.getName() + APPENDIX, HIDDEN_TARGET_SUFFIX, repoTmpFolder);
    }
    catch (IOException e) {
      throw new LocalStorageException(e.getMessage(), e);
    }
  }

  protected void mkParentDirs(Repository repository, File target)
      throws LocalStorageException
  {
    mkDirs(repository, target.getParentFile());
  }

  protected void mkDirs(final Repository repository, final File target)
      throws LocalStorageException
  {
    if (!target.exists() && !target.mkdirs()) {
      // re-check is it really a "good" parent?
      if (!target.isDirectory()) {
        throw new LocalStorageException(String.format(
            "Could not create the directory hierarchy in repository %s to write \"%s\"",
            RepositoryStringUtils.getHumanizedNameString(repository), target.getAbsolutePath()));
      }
    }
  }

  // ==

  public static final String FILE_COPY_STREAM_BUFFER_SIZE_KEY = "upload.stream.bufferSize";

  private int copyStreamBufferSize = -1;

  protected int getCopyStreamBufferSize() {
    if (copyStreamBufferSize == -1) {
      copyStreamBufferSize = SystemPropertiesHelper.getInteger(FILE_COPY_STREAM_BUFFER_SIZE_KEY, 4096);
    }

    return this.copyStreamBufferSize;
  }

  // ==

  public static final String RENAME_RETRY_COUNT_KEY = "rename.retry.count";

  public static final String RENAME_RETRY_DELAY_KEY = "rename.retry.delay";

  private int renameRetryCount = -1;

  private int renameRetryDelay = -1;

  protected int getRenameRetryCount() {
    if (renameRetryCount == -1) {
      renameRetryCount = SystemPropertiesHelper.getInteger(RENAME_RETRY_COUNT_KEY, 0);
    }

    return renameRetryCount;
  }

  protected int getRenameRetryDelay() {
    if (renameRetryDelay == -1) {
      renameRetryDelay = SystemPropertiesHelper.getInteger(RENAME_RETRY_DELAY_KEY, 0);
    }

    return renameRetryDelay;
  }

  protected void handleRenameOperation(File hiddenTarget, File target)
      throws IOException
  {
    // delete the target, this is required on windows
    if (target.exists()) {
      target.delete();
    }

    // first try
    boolean success = hiddenTarget.renameTo(target);

    // if retries enabled go ahead and start the retry process
    for (int i = 1; success == false && i <= getRenameRetryCount(); i++) {
      getLogger().debug("Rename operation attempt {} failed on {} --> {}, will wait {} ms and try again", i,
          hiddenTarget.getAbsolutePath(), target.getAbsolutePath(), getRenameRetryDelay());

      try {
        Thread.sleep(getRenameRetryDelay());
      }
      catch (InterruptedException e) {
      }

      // try to delete again...
      if (target.exists()) {
        target.delete();
      }

      // and rename again...
      success = hiddenTarget.renameTo(target);

      if (success) {
        getLogger().info("Rename operation succeeded after {} retries on {} --> {}", i,
            hiddenTarget.getAbsolutePath(), target.getAbsolutePath());
      }
    }

    if (!success) {
      try {
        FileUtils.rename(hiddenTarget, target);
      }
      catch (IOException e) {
        getLogger().error("Rename operation failed after {} retries in {} ms intervals {} --> {}",
            getRenameRetryCount(), getRenameRetryDelay(), hiddenTarget.getAbsolutePath(),
            target.getAbsolutePath());

        throw new IOException(String.format("Cannot rename file \"%s\" to \"%s\"! Message: %s",
            hiddenTarget.getAbsolutePath(), target.getAbsolutePath(), e.getMessage()), e);
      }
    }
  }
}
