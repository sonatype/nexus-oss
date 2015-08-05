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
package org.sonatype.nexus.geb.modules

import geb.Module


class LoginModule
    extends Module
{
  static content = {
    signinButton(required: false) { $('span.x-btn-inner-center', text: 'Sign in') }
    signoutButton(required: false) { $('span.x-btn-inner-center', text: 'Sign out') }
    username(required: false) { $('.x-window input', name: 'username') }
    password(required: false) { $('.x-window input', name: 'password') }
    submitButton(required: false) { $('.x-window a', text: 'Sign in') }
    cancelButton(required: false) { $('.x-window a', text: 'Cancel') }
  }
}
