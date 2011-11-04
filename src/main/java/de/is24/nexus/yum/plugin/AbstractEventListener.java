package de.is24.nexus.yum.plugin;

import javax.inject.Inject;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;


public abstract class AbstractEventListener implements ItemEventListener {
  @Inject
  public void initialize(ApplicationEventMulticaster eventMulticaster) {
    eventMulticaster.addEventListener(this);
  }

}
