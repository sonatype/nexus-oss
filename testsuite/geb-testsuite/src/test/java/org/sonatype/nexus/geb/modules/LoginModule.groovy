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
