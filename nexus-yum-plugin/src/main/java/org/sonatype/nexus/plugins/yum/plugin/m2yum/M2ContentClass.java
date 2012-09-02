package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * Override default maven2 content class to implement compability between
 * {@link org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass} and
 * {@link M2YumContentClass}
 * 
 * @author sherold
 * 
 */
@Component(role = ContentClass.class, hint = M2ContentClass.ID)
public class M2ContentClass extends AbstractIdContentClass {
  public static final String ID = "maven2";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isCompatible(ContentClass contentClass) {
    return ID.equals(contentClass.getId()) || M2YumContentClass.ID.equals(contentClass.getId());
  }
}
