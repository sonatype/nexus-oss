package org.sonatype.nexus.plugins.yum.plugin.m2yum;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.AbstractIdContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;

@Component(role = ContentClass.class, hint = M2YumContentClass.ID)
public class M2YumContentClass extends AbstractIdContentClass {
  private static final int STAGING_VALIDATION_METHOD_INDEX = 2;
  public static final String ID = "maven2yum";

  @Override
  public String getId() {
    if (isStagingRepoValidation()) {
      return "maven2";
    }
    return ID;
  }

  protected boolean isStagingRepoValidation() {
    try {
      throw new RuntimeException();
    } catch (RuntimeException e) {
      final StackTraceElement[] elements = e.getStackTrace();
      if (elements.length > STAGING_VALIDATION_METHOD_INDEX && isThrownByStagingValidationMethod(elements[STAGING_VALIDATION_METHOD_INDEX])) {
        return true;
      }
    }
    return false;
  }

  protected boolean isThrownByStagingValidationMethod(StackTraceElement elem) {
    return "com.sonatype.nexus.staging.api.AbstractStagingProfilePlexusResource".equals(elem.getClassName())
        && "validateProfile".equals(elem.getMethodName());
  }

  @Override
  public boolean isCompatible(ContentClass contentClass) {
    return ID.equals(contentClass.getId()) || Maven2ContentClass.ID.equals(contentClass.getId());
  }
}
