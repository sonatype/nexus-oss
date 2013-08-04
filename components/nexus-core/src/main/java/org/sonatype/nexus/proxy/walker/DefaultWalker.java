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

package org.sonatype.nexus.proxy.walker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.proxy.AccessDeniedException;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.utils.RepositoryStringUtils;
import org.sonatype.scheduling.TaskInterruptedException;

import org.codehaus.plexus.component.annotations.Component;

/**
 * The Class Walker.
 *
 * @author cstamas
 */
@Component(role = Walker.class)
public class DefaultWalker
    extends AbstractLoggingComponent
    implements Walker
{
  public static final String WALKER_WALKED_COLLECTION_COUNT = Walker.class.getSimpleName() + ".collCount";

  public static final String WALKER_WALKED_FROM_PATH = Walker.class.getSimpleName() + ".fromPath";

  public static final String WALKER_THROTTLE_INFO = Walker.class.getSimpleName() + ".throttleInfo";

  public void walk(WalkerContext context)
      throws WalkerException
  {
    String fromPath = context.getResourceStoreRequest().getRequestPath();

    if (fromPath == null) {
      fromPath = RepositoryItemUid.PATH_ROOT;
    }

    // cannot walk out of service repos
    if (LocalStatus.OUT_OF_SERVICE == context.getRepository().getLocalStatus()) {
      getLogger().info("Cannot walk, repository {} is out of service.",
          RepositoryStringUtils.getHumanizedNameString(context.getRepository()));
    }
    else {
      context.getContext().put(WALKER_WALKED_FROM_PATH, fromPath);
      context.getContext().put(WALKER_THROTTLE_INFO, new DefaultThrottleInfo());

      if (getLogger().isDebugEnabled()) {
        getLogger().debug("Start walking on ResourceStore {} from path \"{}\".",
            RepositoryStringUtils.getHumanizedNameString(context.getRepository()), fromPath);
      }

      try {
        // user may call stop()
        beforeWalk(context);

        if (!context.isStopped()) {
          StorageItem item = null;

          item = context.getRepository().retrieveItem(true, context.getResourceStoreRequest());

          int collCount = 0;

          if (StorageCollectionItem.class.isAssignableFrom(item.getClass())) {
            try {
              WalkerFilter filter =
                  context.getFilter() != null ? context.getFilter() : new DefaultStoreWalkerFilter();
              collCount = walkRecursive(0, context, filter, (StorageCollectionItem) item);
              context.getContext().put(WALKER_WALKED_COLLECTION_COUNT, collCount);
            }
            catch (Exception e) {
              context.stop(e);
            }
          }
          else {
            try {
              WalkerFilter filter =
                  context.getFilter() != null ? context.getFilter() : new DefaultStoreWalkerFilter();
              walkItem(context, filter, item);
              context.getContext().put(WALKER_WALKED_COLLECTION_COUNT, 1);
            }
            catch (Exception e) {
              context.stop(e);
            }
          }

          if (!context.isStopped()) {
            afterWalk(context);
          }
        }
      }
      catch (ItemNotFoundException ex) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("ItemNotFound where walking should start, bailing out.", ex);
        }

        context.stop(ex);
      }
      catch (Exception ex) {
        getLogger().warn("Got exception while doing retrieve, bailing out.", ex);

        context.stop(ex);
      }
    }

    reportWalkEnd(context, fromPath);
  }

  protected void reportWalkEnd(WalkerContext context, String fromPath)
      throws WalkerException
  {
    if (context.isStopped()) {
      if (context.getStopCause() == null) {
        if (getLogger().isDebugEnabled()) {
          getLogger().debug("Walker was stopped programatically, not because of error.");
        }
      }
      else if (context.getStopCause() instanceof TaskInterruptedException) {
        getLogger().info(
            "Canceled walking on repository {} from path \"{}\", cause: {}",
            new Object[]{
                RepositoryStringUtils.getHumanizedNameString(context.getRepository()), fromPath,
                context.getStopCause().getMessage()
            });
      }
      else {
        // we have a cause, report any non-ItemNotFounds with stack trace

        if (context.getStopCause() instanceof ItemNotFoundException) {
          getLogger().debug(
              "Aborted walking on repository {} from path \"{}\", cause: {}",
              new Object[]{
                  RepositoryStringUtils.getHumanizedNameString(context.getRepository()),
                  fromPath, context.getStopCause().getMessage()
              });
        }
        else {
          getLogger().info(
              "Aborted walking on repository {} from path \"{}\", cause: {}",
              new Object[]{
                  RepositoryStringUtils.getHumanizedNameString(context.getRepository()),
                  fromPath, context.getStopCause().getMessage()
              });
        }

        throw new WalkerException(context, "Aborted walking on repository ID='"
            + context.getRepository().getId() + "' from path='" + fromPath + "'.");
      }
    }
    else {
      // regular finish, it was not stopped
      if (getLogger().isDebugEnabled()) {
        getLogger().debug(
            "Finished walking on ResourceStore '" + context.getRepository().getId() + "' from path '"
                + context.getContext().get(WALKER_WALKED_FROM_PATH) + "'.");
      }
    }
  }

  protected final int walkRecursive(int collCount, WalkerContext context, WalkerFilter filter,
                                    StorageCollectionItem coll)
      throws AccessDeniedException, IllegalOperationException, StorageException
  {
    if (context.isStopped()) {
      return collCount;
    }

    boolean shouldProcess = filter.shouldProcess(context, coll);

    boolean shouldProcessRecursively = filter.shouldProcessRecursively(context, coll);

    if (!shouldProcess && !shouldProcessRecursively) {
      return collCount;
    }

    // user may call stop()
    if (shouldProcess) {
      onCollectionEnter(context, coll);

      collCount++;
    }

    if (context.isStopped()) {
      return collCount;
    }

    Collection<StorageItem> ls = null;

    if (shouldProcessRecursively) {
      try {
        ls = context.getRepository().list(false, coll);

        if (context.getItemComparator() != null) {
          final ArrayList<StorageItem> list = new ArrayList<StorageItem>(ls);
          Collections.sort(list, context.getItemComparator());
          ls = list;
        }

        for (StorageItem i : ls) {
          if (!(i instanceof StorageCollectionItem)) {
            walkItem(context, filter, i);

            if (context.isStopped()) {
              return collCount;
            }
          }

          if (i instanceof StorageCollectionItem) {
            // user may call stop()
            collCount = walkRecursive(collCount, context, filter, (StorageCollectionItem) i);

            if (context.isStopped()) {
              return collCount;
            }
          }
        }
      }
      catch (ItemNotFoundException e) {
        getLogger().debug("ItemNotFound not found while walking it, skipping.", e);
      }
    }

    // user may call stop()
    if (shouldProcess) {
      onCollectionExit(context, coll);
    }

    return collCount;
  }

  protected void walkItem(WalkerContext context, WalkerFilter filter, StorageItem i) {
    if (filter.shouldProcess(context, i)) {
      // user may call stop()
      processItem(context, i);
    }
  }

  protected void beforeWalk(WalkerContext context) {
    context.getThrottleController().walkStarted(context);

    try {
      for (WalkerProcessor processor : context.getProcessors()) {
        if (processor.isActive()) {
          processor.beforeWalk(context);

          if (context.isStopped()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      context.stop(e);
    }
  }

  protected void onCollectionEnter(WalkerContext context, StorageCollectionItem coll) {
    try {
      for (WalkerProcessor processor : context.getProcessors()) {
        if (processor.isActive()) {
          processor.onCollectionEnter(context, coll);

          if (context.isStopped()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      context.stop(e);
    }
  }

  protected void processItem(WalkerContext context, StorageItem item) {
    try {
      final DefaultThrottleInfo info = (DefaultThrottleInfo) context.getContext().get(WALKER_THROTTLE_INFO);

      info.enterProcessItem();

      for (WalkerProcessor processor : context.getProcessors()) {
        if (processor.isActive()) {
          processor.processItem(context, item);

          if (context.isStopped()) {
            break;
          }
        }
      }

      info.exitProcessItem();

      if (!context.isStopped() && context.getThrottleController().isThrottled()) {
        final long throttleTime = context.getThrottleController().throttleTime(info);

        if (throttleTime > 0) {
          try {
            Thread.sleep(throttleTime);
          }
          catch (InterruptedException e) {
            throw new TaskInterruptedException("Thread \"" + Thread.currentThread().getName()
                + "\" is interrupted!", false);
          }
        }
      }
    }
    catch (Exception e) {
      context.stop(e);
    }
  }

  protected void onCollectionExit(WalkerContext context, StorageCollectionItem coll) {
    try {
      for (WalkerProcessor processor : context.getProcessors()) {
        if (processor.isActive()) {
          processor.onCollectionExit(context, coll);

          if (context.isStopped()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      context.stop(e);
    }
  }

  protected void afterWalk(WalkerContext context) {
    try {
      for (WalkerProcessor processor : context.getProcessors()) {
        if (processor.isActive()) {
          processor.afterWalk(context);

          if (context.isStopped()) {
            break;
          }
        }
      }
    }
    catch (Exception e) {
      context.stop(e);
    }

    context.getThrottleController().walkEnded(context,
        (DefaultThrottleInfo) context.getContext().get(WALKER_THROTTLE_INFO));
  }
}
