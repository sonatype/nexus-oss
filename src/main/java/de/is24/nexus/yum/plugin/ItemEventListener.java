package de.is24.nexus.yum.plugin;

import javax.inject.Singleton;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.plugin.Managed;


@Managed @Singleton
public interface ItemEventListener extends EventListener {
}
