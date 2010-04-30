package org.sonatype.nexus.test.utils;

import com.thoughtworks.xstream.XStream;

public interface XStreamConfigurator
{
    void configure( XStream xstream );
}
