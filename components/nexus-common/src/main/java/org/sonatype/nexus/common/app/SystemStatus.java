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
package org.sonatype.nexus.common.app;

/**
 * System status.
 */
public class SystemStatus
{
  private String version = "unknown";

  private String editionShort = "OSS";

  private SystemState state;

  private boolean licenseInstalled = false;

  private boolean licenseExpired = false;

  private boolean trialLicense = false;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getEditionShort() {
    return editionShort;
  }

  public void setEditionShort(String editionUserAgent) {
    this.editionShort = editionUserAgent;
  }

  public SystemState getState() {
    return state;
  }

  public void setState(SystemState status) {
    this.state = status;
  }

  public boolean isNexusStarted() {
    return SystemState.STARTED.equals(getState());
  }

  public boolean isLicenseInstalled() {
    return licenseInstalled;
  }

  public void setLicenseInstalled(final boolean licenseInstalled) {
    this.licenseInstalled = licenseInstalled;
  }

  public boolean isLicenseExpired() {
    return licenseExpired;
  }

  public void setLicenseExpired(final boolean licenseExpired) {
    this.licenseExpired = licenseExpired;
  }

  public boolean isTrialLicense() {
    return trialLicense;
  }

  public void setTrialLicense(final boolean trialLicense) {
    this.trialLicense = trialLicense;
  }
}
