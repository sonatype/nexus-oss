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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Opens a transaction when entering a transactional method and closes it on exit.
 * Nested transactional methods proceed as normal inside the current transaction.
 *
 * @since 3.0
 */
class TransactionInterceptor
    implements MethodInterceptor
{
  public Object invoke(final MethodInvocation mi) throws Throwable {
    final UnitOfWork work = UnitOfWork.self();

    if (work.isActive()) {
      return mi.proceed();
    }

    try (final Transaction tx = work.acquireTransaction()) {
      final Transactional spec = mi.getMethod().getAnnotation(Transactional.class);
      return new TransactionalWrapper(spec, mi).proceedWithTransaction(tx);
    }
    finally {
      work.releaseTransaction();
    }
  }
}
