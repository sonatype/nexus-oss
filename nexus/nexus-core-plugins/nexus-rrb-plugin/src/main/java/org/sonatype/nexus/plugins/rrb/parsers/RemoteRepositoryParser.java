package org.sonatype.nexus.plugins.rrb.parsers;

import java.util.ArrayList;

import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public interface RemoteRepositoryParser
{

    public ArrayList<RepositoryDirectory> extractLinks( StringBuilder indata );
}
