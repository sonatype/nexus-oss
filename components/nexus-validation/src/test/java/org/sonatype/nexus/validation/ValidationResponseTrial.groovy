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
package org.sonatype.nexus.validation

import org.sonatype.sisu.litmus.testsupport.TestSupport

import org.junit.Test

/**
 * Trials of {@link ValidationResponse}
 */
class ValidationResponseTrial
    extends TestSupport
{
  @Test
  void 'string representation'() {
    def response = new ValidationResponse()
    response.addError(new ValidationMessage('key', 'foo'))
    response.addError(new ValidationMessage('key', 'bar', new Throwable('TEST')))
    response.addError(new ValidationMessage('key', 'baz'))
    response.addWarning(new ValidationMessage('key', 'ick'))
    response.addWarning(new ValidationMessage('key', 'qux', new Throwable('TEST')))
    response.addWarning(new ValidationMessage('key', 'poo'))
    log response
  }

  @Test
  void 'string representation no messages'() {
    log new ValidationResponse()
  }
}
