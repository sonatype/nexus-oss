/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testTransactional() throws Exception {
    methods.transactional();

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test
  public void testNested() throws Exception {
    methods.outer();

    InOrder order = inOrder(tx);
    order.verify(tx).begin();
    order.verify(tx).commit();
    order.verify(tx).close();
    verifyNoMoreInteractions(tx);
  }

  @Test(expected = IOException.class)
  public void testImplicitCommit() throws Exception {
    try {
      methods.implicitIgnore();
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
  public void testImplicitRollback() throws Exception {
    try {
      methods.implicitRollback();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test
  public void testImplicitRetrySuccess() throws Exception {
    when(tx.allowRetry()).thenReturn(true);
    methods.setCountdownToSuccess(3);
    methods.implicitRetry();

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

  @Test(expected = ExampleRetryException.class)
  public void testImplicitRetryFail() throws Exception {
    when(tx.allowRetry()).thenReturn(true).thenReturn(false);
    methods.setCountdownToSuccess(100);
    try {
      methods.implicitRetry();
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
  public void testExplicitCommit() throws Exception {
    try {
      methods.explicitIgnore();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).commit();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test(expected = IOException.class)
  public void testExplicitRollback() throws Exception {
    try {
      methods.explicitRollback();
    }
    finally {
      InOrder order = inOrder(tx);
      order.verify(tx).begin();
      order.verify(tx).rollback();
      order.verify(tx).close();
      verifyNoMoreInteractions(tx);
    }
  }

  @Test
  public void testExplicitRetrySuccess() throws Exception {
    when(tx.allowRetry()).thenReturn(true);
    methods.setCountdownToSuccess(3);
    methods.explicitRetry();

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
  public void testExplicitRetryFail() throws Exception {
    when(tx.allowRetry()).thenReturn(true).thenReturn(false);
    methods.setCountdownToSuccess(100);
    try {
      methods.explicitRetry();
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
