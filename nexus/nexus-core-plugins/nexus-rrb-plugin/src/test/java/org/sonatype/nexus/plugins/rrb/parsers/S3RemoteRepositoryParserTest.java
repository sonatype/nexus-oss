package org.sonatype.nexus.plugins.rrb.parsers;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.plugins.rrb.RepositoryDirectory;

public class S3RemoteRepositoryParserTest extends RemoteRepositoryParserTestAbstract {
    private S3RemoteRepositoryParser parser;

    @Before
    public void setUp() {
        String remoteUrl = "http://www.xxx.com"; // The exact names of the urls
        String localUrl = "http://local"; // doesn't matter in the tests
        parser = new S3RemoteRepositoryParser(remoteUrl, localUrl);
    }

    @Test
    public void testExtractLinks() throws Exception {
        // An S3 repo where the contents size is 15.
        // One of them is keyed as a robots.txt.
        StringBuilder indata = new StringBuilder(getExampleFileContent("/s3Example"));
        ArrayList<RepositoryDirectory> result = parser.extractLinks(indata);

        // Contents key:ed as robots.txt should not appear in the list of
        // RepositoryDirectory, which means that the result's size should be 14.
        assertEquals(14, result.size());
    }
}
