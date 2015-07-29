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
/*
 This is the Geb configuration file.

 See: http://www.gebish.org/manual/current/configuration.html
*/

import java.util.logging.Level

import geb.driver.SauceLabsDriverFactory
import org.openqa.selenium.Dimension
import org.openqa.selenium.Platform
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.ie.InternetExplorerDriver
import org.openqa.selenium.phantomjs.PhantomJSDriver
import org.openqa.selenium.phantomjs.PhantomJSDriverService
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.safari.SafariDriver

reportsDir = "target/test-reports/geb"
// Port is not known until runtime, needs to be set in BaseTest.groovy
//baseUrl = System.getProperty('geb.build.baseUrl', 'http://localhost:9070/')

//enable waitFor behaviour for all 'at' checks
atCheckWaiting = true
waiting {
  presets {
    slow {
      timeout = 15
    }
  }
}
// Consult these documents for how to configure for SauceLabs
// http://www.gebish.org/manual/current/sauce-labs.html#saucelabs_integration
// https://saucelabs.com/docs/platforms/webdriver
def sauceBrowser = System.getProperty("geb.sauce.browser")
if (sauceBrowser) {
  driver = {
    def username = System.getProperty("GEB_SAUCE_LABS_USER")
    assert username
    def accessKey = System.getProperty("GEB_SAUCE_LABS_ACCESS_PASSWORD")
    assert accessKey
    WebDriver driver = new SauceLabsDriverFactory().create(sauceBrowser, username, accessKey)
    driver.manage().window().maximize()
    return driver
  }
  // increase default timeouts to account for remote execution
  waiting {
    timeout = 10
    retryInterval = 0.5
  }
}
else {
  driver = { configure(new FirefoxDriver()) }
}

Platform current = Platform.current
environments {

  // run as “mvn -Dgeb.env=chrome test”
  // See: http://code.google.com/p/selenium/wiki/ChromeDriver
  chrome {
    driver = { configure(new ChromeDriver()) }
  }

  // see https://code.google.com/p/selenium/wiki/SafariDriver
  safari {
    if (current.is(Platform.MAC)) {
      driver = { configure(new SafariDriver()) }
    }
    else {
      throw new IllegalStateException('Only runs on mac!')
    }
  }

  // see https://code.google.com/p/selenium/wiki/InternetExplorerDriver
  ie {
    if (current.is(Platform.WINDOWS)) {
      driver = { configure(new InternetExplorerDriver()) }
    }
    else {
      throw new IllegalStateException('Only runs on windows!')
    }
  }

  // see https://github.com/detro/ghostdriver
  phantom {
    String phantomJsBinary
    new File(System.getProperty('geb.workdir', 'target') + '/phantomjs-maven-plugin').eachFileRecurse { File file ->
      if (file.isFile() && file.name.matches('phantomjs(.exe)?')) {
        phantomJsBinary = file.absolutePath
        return false
      }
    }
    driver = {
      DesiredCapabilities capabilities = DesiredCapabilities.phantomjs()
      if (phantomJsBinary) {
        capabilities.setCapability('phantomjs.binary.path', phantomJsBinary)
      }
      capabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, ['--webdriver-loglevel=DEBUG'] as String[]);
      capabilities.
          setCapability(PhantomJSDriverService.PHANTOMJS_GHOSTDRIVER_CLI_ARGS, ["--logLevel=DEBUG"] as String[])
      RemoteWebDriver webDriver = new PhantomJSDriver(capabilities)
      webDriver.setLogLevel(Level.ALL)
      return configure(webDriver)
    }

    // increase default timeout to account for slower CI server
    waiting {
      timeout = 15
    }
  }
}

def configure(final RemoteWebDriver driver) {
  driver.manage().window().setSize(new Dimension(1280, 1024))
  println "Geb configured with driver: $driver"
  return driver
}
