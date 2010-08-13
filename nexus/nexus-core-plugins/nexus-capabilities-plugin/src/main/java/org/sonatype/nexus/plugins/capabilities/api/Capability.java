package org.sonatype.nexus.plugins.capabilities.api;

import java.util.Map;

public interface Capability
{

    String id();

    void create( Map<String, String> properties );

    void load( Map<String, String> properties );

    void update( Map<String, String> properties );

    void remove();

}
