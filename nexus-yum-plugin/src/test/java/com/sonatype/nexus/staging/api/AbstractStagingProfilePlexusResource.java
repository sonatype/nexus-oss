package com.sonatype.nexus.staging.api;

import org.sonatype.nexus.plugins.yum.plugin.m2yum.M2YumContentClass;

public class AbstractStagingProfilePlexusResource {
  public String validateProfile() {
    return new M2YumContentClass().getId();
  }
}
