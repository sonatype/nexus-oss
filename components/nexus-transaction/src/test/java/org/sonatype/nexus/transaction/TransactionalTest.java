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
package org.sonatype.nexus.transaction;

import java.io.IOException;

import org.sonatype.sisu.litmus.testsupport.TestSupport;

import com.google.common.base.Suppliers;
import com.google.inject.Guice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test transactional behaviour.
 */
@SuppressWarnings("boxing")
public class TransactionalTest
    extends TestSupport
{
  ExampleMethods methods = Guice.createInjector(new TransactionModule()).getInstance(ExampleMethods.class);

  @Mock
  Transaction tx;

  @Before
  public void setUp() {
    UnitOfWork.begin(Suppliers.ofInstance(tx));
  }

  @After
  public void tearDown() {
    UnitOfWork.end();
  }

  @Test
  public void testNonTransactional() {

    methods.nonTransactional();
    methods.nonTransactional();
    methods.nonTransactional();

    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testTransactional() throws Exception {

    methods.transactional();
    methods.transactional();
    methods.transactional();

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testPauseResume() throws Exception {
    final Transaction tx2 = mock(Transaction.class);

    methods.transactional();

    final UnitOfWork work = UnitOfWork.pause();
    try {
      UnitOfWork.begin(Suppliers.ofInstance(tx2));
      try {
        methods.transactional();
      }
      finally {
        UnitOfWork.end();
      }
    }
    finally {
      UnitOfWork.resume(work);
    }
    methods.transactional();

    InOrder order = inOrder(tx, tx2);
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    order.verify(tx2).begin();
    order.verify(tx2).commit();
    order.verify(tx2).close();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testBatchTransactional() throws Exception {
    UnitOfWork.beginBatch(Suppliers.ofInstance(tx));
    try {
      methods.transactional();
      methods.transactional();
      methods.transactional();
    }
    finally {
      UnitOfWork.end();
    }

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).isActive();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).isActive();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).isActive();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testNested() throws Exception {
    when(tx.isActive()).thenReturn(true);

    methods.outer();
    methods.outer();
    methods.outer();

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx, times(2)).isActive();
    order.verify(tx).commit();
    order.verify(tx).close();
    order.verify(tx).begin();
    order.verify(tx, times(2)).isActive();
    order.verify(tx).commit();
    order.verify(tx).close();
    order.verify(tx).begin();
    order.verify(tx, times(2)).isActive();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testBatchNested() throws Exception {
    when(tx.isActive()).thenReturn(true, true, false, true, true, false, true, true, false);

    UnitOfWork.beginBatch(Suppliers.ofInstance(tx));
    try {
      methods.outer();
      methods.outer();
      methods.outer();
    }
    finally {
      UnitOfWork.end();
    }

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx, times(2)).isActive();
    order.verify(tx).commit();
    order.verify(tx).isActive();
    order.verify(tx).begin();
    order.verify(tx, times(2)).isActive();
    order.verify(tx).commit();
    order.verify(tx).isActive();
    order.verify(tx).begin();
    order.verify(tx, times(2)).isActive();
    order.verify(tx).commit();
    order.verify(tx).isActive();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test(expected = IOException.class)
  public void testRollbackOnCheckedException() throws Exception {
    try {
      methods.rollbackOnCheckedException();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testRollbackOnUncheckedException() throws Exception {
    try {
      methods.rollbackOnUncheckedException();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test(expected = IOException.class)
  public void testIgnoreCheckedException() throws Exception {
    try {
      methods.ignoreCheckedException();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).commit();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testIgnoreUncheckedException() throws Exception {
    try {
      methods.ignoreUncheckedException();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).commit();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test
  public void testRetrySuccessOnCheckedException() throws Exception {
    when(tx.allowRetry()).thenReturn(true);

    methods.setCountdownToSuccess(3);
    methods.retryOnCheckedException();

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx).rollback();
    order.verify(tx).allowRetry();
    order.verify(tx).begin();
    order.verify(tx).rollback();
    order.verify(tx).allowRetry();
    order.verify(tx).begin();
    order.verify(tx).rollback();
    order.verify(tx).allowRetry();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test(expected = IOException.class)
  public void testRetryFailureOnCheckedException() throws Exception {
    when(tx.allowRetry()).thenReturn(true).thenReturn(false);

    methods.setCountdownToSuccess(100);
    try {
      methods.retryOnCheckedException();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).allowRetry();
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).allowRetry();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test
  public void testRetrySuccessOnUncheckedException() throws Exception {
    when(tx.allowRetry()).thenReturn(true);

    methods.setCountdownToSuccess(3);
    methods.retryOnUncheckedException();

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx).rollback();
    order.verify(tx).allowRetry();
    order.verify(tx).begin();
    order.verify(tx).rollback();
    order.verify(tx).allowRetry();
    order.verify(tx).begin();
    order.verify(tx).rollback();
    order.verify(tx).allowRetry();
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test(expected = IllegalStateException.class)
  public void testRetryFailureOnUncheckedException() throws Exception {
    when(tx.allowRetry()).thenReturn(true).thenReturn(false);

    methods.setCountdownToSuccess(100);
    try {
      methods.retryOnUncheckedException();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).allowRetry();
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).allowRetry();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testCannotBeginWorkInTransaction() {
    methods.beginWorkInTransaction();
  }

  @Test(expected = IllegalStateException.class)
  public void testCannotEndWorkInTransaction() {
    methods.endWorkInTransaction();
  }
}
