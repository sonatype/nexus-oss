/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.repository.storage;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.sonatype.sisu.goodies.common.ComponentSupport;

import com.google.common.base.Throwables;
import com.orientechnologies.orient.core.command.OCommandResultListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLAsynchQuery;
import com.orientechnologies.orient.core.sql.query.OSQLNonBlockingQuery;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Helper class to wrap Orient {@link OSQLAsynchQuery} into {@link Iterable}s.
 *
 * @since 3.0
 */
public class OrientAsyncHelper
    extends ComponentSupport
{
  private static final int BUFFER_SIZE = 128;

  private static final long TIMEOUT_SECONDS = 60L;

  /**
   * Executes with passed in {@link ODatabaseDocumentTx} connection the passed in {@link String} SELECT SQL command
   * and returns a true asynchronous backed {@link Iterable} of {@link ODocument} instance, that can be later
   * transformed into whatever caller needs. This means that this wrapper supports ANY SQL commands, even those
   * performing aggregation on results, as long as iterator is not accumulated into a real array or list.
   */
  public static Iterable<ODocument> asyncIterable(final ODatabaseDocumentTx db,
                                                  final String selectQuery,
                                                  @Nullable final Map<String, Object> parameters)
  {
    checkNotNull(db);
    checkNotNull(selectQuery);
    final ArrayBlockingQueue<ODocument> queue = new ArrayBlockingQueue<>(BUFFER_SIZE);

    db.command(new OSQLNonBlockingQuery<ODocument>(selectQuery, new OCommandResultListener()
    {
      @Override
      public boolean result(final Object iRecord) {
        final ODocument doc = (ODocument) iRecord;
        try {
          queue.offer(doc, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          throw Throwables.propagate(e);
        }
        return true;
      }

      @Override
      public void end() {
        try {
          queue.offer(SENTINEL, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          throw Throwables.propagate(e);
        }
      }
    })).execute(parameters);

    return new OrientAsyncIterableWrapper(queue);
  }

  private static final ODocument SENTINEL = new ODocument();

  private static final class OrientAsyncIterableWrapper
      implements Iterable<ODocument>, Iterator<ODocument>
  {
    private final BlockingQueue<ODocument> queue;

    private ODocument next;

    public OrientAsyncIterableWrapper(final BlockingQueue<ODocument> queue)
    {
      this.queue = queue;
    }

    @Override
    public Iterator<ODocument> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      try {
        next = queue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        throw Throwables.propagate(e);
      }
      return next != SENTINEL;
    }

    @Override
    public ODocument next() {
      if (next == SENTINEL) {
        throw new NoSuchElementException("Iterator depleted");
      }
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Method not supported");
    }
  }
}
