package org.sonatype.nexus.geb.pages

import org.sonatype.nexus.geb.modules.LoginModule

import geb.Page

class HomePage
    extends Page
{
  static url = '/'

  static at = { title == 'Welcome - Sonatype Nexus' && featureLabel }

  static content = {
    featureLabel { $('label.nx-feature-name', text: 'Welcome') }
    loginModule { module LoginModule }
  }
}
