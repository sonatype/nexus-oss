package de.is24.nexus.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component(role = ContentClass.class, hint = M2YumContentClass.ID)
public class M2YumContentClass extends AbstractIdContentClass {
  public static final String ID = "maven2yum";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isCompatible(ContentClass contentClass) {
    return ID.equals(contentClass.getId()) || Maven2ContentClass.ID.equals(contentClass.getId());
  }
}
