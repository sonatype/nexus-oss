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

import com.google.common.base.Suppliers;

import static com.google.common.base.Preconditions.checkState;

/**
 * Miscellaneous methods to exercise transactional aspects.
 */
@SuppressWarnings("unused")
public class ExampleMethods
{
  public String nonTransactional() {
    checkState(UnitOfWork.currentTransaction() == null);
    return "success";
  }

  @Transactional
  public String transactional() {
    checkState(UnitOfWork.currentTransaction() != null);
    return "success";
  }

  @Transactional
  public String outer() {
    return inner();
  }

  @Transactional
  public String inner() {
    return transactional();
  }

  @Transactional
  public String rollbackOnCheckedException() throws IOException {
    throw new IOException();
  }

  @Transactional
  public String rollbackOnUncheckedException() throws IOException {
    throw new IllegalStateException();
  }

  private int countdown;

  public void setCountdownToSuccess(int countdown) {
    this.countdown = countdown;
  }

  @Transactional(ignore = IOException.class)
  public String ignoreCheckedException() throws IOException {
    throw new IOException();
  }

  @Transactional(ignore = RuntimeException.class)
  public String ignoreUncheckedException() throws IOException {
    throw new IllegalStateException();
  }

  @Transactional(retryOn = IOException.class)
  public String retryOnCheckedException() throws IOException {
    if (countdown-- > 0) {
      throw new IOException();
    }
    return "success";
  }

  @Transactional(retryOn = RuntimeException.class)
  public String retryOnUncheckedException() throws IOException {
    if (countdown-- > 0) {
      throw new IllegalStateException();
    }
    return "success";
  }

  // should throw IllegalStateException
  @Transactional
  public void beginWorkInTransaction() {
    UnitOfWork.begin(Suppliers.ofInstance((Transaction) null));
  }

  // should throw IllegalStateException
  @Transactional
  public void endWorkInTransaction() {
    UnitOfWork.end();
  }
}
