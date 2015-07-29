package org.sonatype.nexus.geb

import org.sonatype.nexus.geb.pages.HomePage

import org.junit.Test


class SigninSpec
    extends BaseTest
{

  @Test
  void signin() {
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
  }
}
