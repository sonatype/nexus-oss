package org.sonatype.nexus.buup.checks;

import java.io.File;
import java.io.IOException;

/**
 * A component that tries to see is the directory writable or not.
 * 
 * @author cstamas
 */
public interface FSPermissionChecker
{
    void checkFSPermissionsOnDirectory( File dir )
        throws IOException;
}
