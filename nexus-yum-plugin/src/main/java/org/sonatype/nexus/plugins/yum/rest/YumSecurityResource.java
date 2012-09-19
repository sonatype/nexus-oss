package org.sonatype.nexus.plugins.yum.rest;

import javax.inject.Singleton;

import org.sonatype.security.realms.tools.AbstractStaticSecurityResource;
import org.sonatype.security.realms.tools.StaticSecurityResource;

@Singleton
public class YumSecurityResource extends AbstractStaticSecurityResource implements StaticSecurityResource {

  @Override
  protected String getResourcePath() {
    return "/META-INF/org.sonatype.nexus.plugins.yum-security.xml";
  }

}
