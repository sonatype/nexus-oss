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
package org.sonatype.nexus.geb

import org.sonatype.nexus.geb.pages.HomePage

import geb.navigator.Navigator
import org.junit.Test


class SigninIT
    extends GebTestSupport
{

  @Test
  void 'Can sign in and out as admin'() {
    to HomePage // automatically applies an 'at' assertion to ensure we arrive at the page and can account for it being loaded

    //visibility checks
    assert loginModule.signinButton.displayed
    assert !loginModule.signoutButton.displayed

    //open signin dialog
    loginModule.signinButton.click()
    assert loginModule.username.displayed
    assert loginModule.password.displayed

    //interact with the form
    loginModule.username = 'admin'
    loginModule.password = 'admin123'
    loginModule.submitButton.click()

    //wait for the state to change
    waitFor { loginModule.signoutButton.displayed }
    assert !loginModule.signinButton.displayed
    
    //signout
    loginModule.signoutButton.click()
    
    waitFor { loginModule.signinButton.displayed }
    assert !loginModule.signoutButton.displayed
  }
  
  @Test
  void 'Can interact with the page using ExtJS'() {
    to HomePage
    def ids = idsForCQ('nx-header-signin')
    assert ids
    log.info ids
    Navigator navigator = $("#${ids[0]}")
    assert ids[0] == navigator.attr('id')
    
    log.info js.exec(ids[0], 'Bow down to Xenu', '''Ext.getCmp(arguments[0]).setText(arguments[1]);''')
  }
}
