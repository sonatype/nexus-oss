/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.testsuite.rapture;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.sonatype.nexus.bundle.launcher.NexusBundleConfiguration;
import org.sonatype.nexus.testsuite.support.NexusRunningParametrizedITSupport;
import org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.junit.runners.Parameterized;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.sonatype.nexus.testsuite.support.NexusStartAndStopStrategy.Strategy.EACH_TEST;

/**
 * @since 2.8
 */
@NexusStartAndStopStrategy(EACH_TEST)
public class SiestaLiteUiITSupport
    extends NexusRunningParametrizedITSupport
{

  private final WebDriverFactory driverFactory;

  @Parameterized.Parameters
  public static List<Object[]> drivers() {
    return Lists.<Object[]>newArrayList(
        new Object[]{firefox()}
        //new Object[]{chrome()},
        //new Object[]{remote(BrowserType.FIREFOX, "25", Platform.LINUX)}
    );
  }

  public SiestaLiteUiITSupport(final WebDriverFactory driverFactory) {
    super("${it.nexus.bundle.groupId}:${it.nexus.bundle.artifactId}:zip");
    this.driverFactory = driverFactory;
  }

  @Override
  protected NexusBundleConfiguration configureNexus(final NexusBundleConfiguration configuration) {
    return configuration.addPlugins(
        artifactResolver().resolvePluginFromDependencyManagement(
            "org.sonatype.nexus", "nexus-testsuite-ui-plugin"
        )
    );
  }

  protected void run(final String test) throws Exception {
    logger.info("Starting web driver");
    WebDriver driver = driverFactory.create();
    JavascriptExecutor js = (JavascriptExecutor) driver;
    logger.info("Started web driver {}", driver);

    try {
      loadTestHarness(driver);
      markTest(driver, test);
      runTest(driver, test);
      waitForTestToFinish(driver, js);

      String failure = logResults(getTestResults(test, js));

      assertThat(failure, failure == null);
    }
    finally {
      driver.quit();
    }
  }

  private String logResults(final Map resultAsMap) {
    String failure = null;
    List<Map> assertions = (List<Map>) resultAsMap.get("assertions");
    logger.info("----------------------------------------");
    for (Map assertion : assertions) {
      StringBuilder sb = new StringBuilder();
      sb.append(assertion.get("description").toString().replace("  ", " "));
      Object isException = assertion.get("isException");
      if (isException != null && (boolean) isException) {
        Object annotation = assertion.get("annotation");
        if (annotation != null) {
          sb.append(" -> ").append(annotation.toString().replace("  ", " "));
        }
      }
      Object passed = assertion.get("passed");
      String prefix = "      ";
      if (passed != null) {
        if ((boolean) passed) {
          prefix = "[PASS]";
        }
        else {
          prefix = "[FAIL]";
          failure = sb.toString();
        }

      }
      logger.info(prefix + " " + sb.toString());
    }
    logger.info("----------------------------------------");
    return (Boolean) resultAsMap.get("passed") ? failure : null;
  }

  private Map getTestResults(final String test, final JavascriptExecutor js) {
    return (Map) js.executeScript(
        "return Siesta.my.activeHarness.testsByURL['" + test + "'].getResults().toJSON();"
    );
  }

  private void loadTestHarness(final WebDriver driver) {
    driver.navigate().to(nexus().getUrl() + "static/rapture/nexus-ui-tests.html");
    driverWait(driver).until(elementToBeClickable(By.cssSelector(".x-btn a[title='Run checked']")));
  }

  private void markTest(final WebDriver driver, final String test) {
    WebElement testRow = findTest(driver, test);
    assertThat("Test " + test + " not found in test harness", testRow, notNullValue());
    WebElement checkbox = testRow.findElement(By.cssSelector(".x-tree-checkbox"));
    assertThat("Test " + test + " not found in test harness", checkbox, notNullValue());
    checkbox.click();
  }

  private WebElement findTest(final WebDriver driver, final String test) {
    List<WebElement> elements = driver.findElements(By.cssSelector(".tr-testgrid .x-grid-tree-node-leaf"));
    if (elements != null) {
      for (WebElement element : elements) {
        if (test.equals(element.getAttribute("data-recordid"))) {
          return element;
        }
      }
    }
    return null;
  }

  private void runTest(final WebDriver driver, final String test) {
    logger.info("Running {}", test);
    driver.findElement(By.cssSelector(".x-btn a[title='Run checked']")).click();
  }

  private void waitForTestToFinish(final WebDriver driver, final JavascriptExecutor js) {
    driverWait(driver).pollingEvery(1, TimeUnit.SECONDS).withTimeout(5, TimeUnit.MINUTES).until(
        new Predicate<WebDriver>()
        {
          @Override
          public boolean apply(@Nullable final WebDriver webDriver) {
            Object endDate = js.executeScript("return Siesta.my.activeHarness.endDate");
            return endDate != null;
          }
        }
    );
  }

  private FluentWait<WebDriver> driverWait(WebDriver driver) {
    return new FluentWait<>(driver).withTimeout(2, TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
  }

  protected static interface WebDriverFactory
  {
    WebDriver create();
  }

  protected static WebDriverFactory firefox() {
    return new WebDriverFactory()
    {
      @Override
      public WebDriver create() {
        return new FirefoxDriver();
      }
    };
  }

  protected static WebDriverFactory chrome() {
    return new WebDriverFactory()
    {
      @Override
      public WebDriver create() {
        return new ChromeDriver();
      }
    };
  }

  protected static WebDriverFactory remote(final String browser, final String version, final Platform platform) {
    return remote(new DesiredCapabilities(browser, version, platform));
  }

  protected static WebDriverFactory remote(final Capabilities capabilities) {
    return new WebDriverFactory()
    {
      @Override
      public WebDriver create() {
        try {
          return new RemoteWebDriver(
              new URL("http://adreghiciu:13a9920f-5910-42e8-9f54-854f638470b0@ondemand.saucelabs.com:80/wd/hub"),
              capabilities
          );
        }
        catch (MalformedURLException e) {
          throw Throwables.propagate(e);
        }
      }
    };
  }

}
