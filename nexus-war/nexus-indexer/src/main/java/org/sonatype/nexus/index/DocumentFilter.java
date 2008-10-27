package org.sonatype.nexus.index;

import org.apache.lucene.document.Document;

public interface DocumentFilter
{
    boolean accept( Document doc );
}
