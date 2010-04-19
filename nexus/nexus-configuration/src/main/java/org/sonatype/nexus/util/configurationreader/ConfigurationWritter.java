package org.sonatype.nexus.util.configurationreader;

import java.io.IOException;
import java.io.Writer;

public interface ConfigurationWritter<E>
{

    void write( Writer fr, E configuration )
        throws IOException;

}
