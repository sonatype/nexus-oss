package org.sample.plugin;

import java.io.InputStream;

import org.sonatype.plexus.plugin.Managed;

@Managed
public interface VirusScanner
{
    boolean hasVirus( InputStream is );
}
