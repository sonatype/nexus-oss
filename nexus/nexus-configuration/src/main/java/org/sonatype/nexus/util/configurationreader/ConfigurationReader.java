package org.sonatype.nexus.util.configurationreader;

import java.io.IOException;
import java.io.Reader;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public interface ConfigurationReader<E>
{

    E read( Reader fr )
        throws IOException, XmlPullParserException;

}
