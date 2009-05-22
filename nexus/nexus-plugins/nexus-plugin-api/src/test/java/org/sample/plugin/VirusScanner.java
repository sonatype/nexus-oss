package org.sample.plugin;

import java.io.InputStream;

import org.sonatype.nexus.plugins.Managed;

@Managed
public interface VirusScanner
{
    boolean hasVirus( InputStream is );
}
