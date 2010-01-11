package org.sonatype.nexus.plugins.rrb.parsers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class HtmlRemoteRepositoryParserTest extends RemoteRepositoryParserTestAbstract {
    private HtmlRemoteRepositoryParser parser;

    @Before
    public void setUp() {
        String remoteUrl = "http://www.xxx.com"; // The exact names of the urls
        String localUrl = "http://local"; // doesn't matter in the tests
        parser = new HtmlRemoteRepositoryParser(remoteUrl, localUrl);
    }

    @Test
    public void testExtractLinks() throws Exception {
        // htmlExample is an html repo with three sub directories
        StringBuilder indata = new StringBuilder(getExampleFileContent("/htmlExample"));
        ArrayList<RepositoryDirectory> result = parser.extractLinks(indata);

        assertEquals(3, result.size());
    }
}
