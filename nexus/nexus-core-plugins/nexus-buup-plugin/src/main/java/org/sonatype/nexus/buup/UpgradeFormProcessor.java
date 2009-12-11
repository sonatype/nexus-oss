package org.sonatype.nexus.buup;

import org.sonatype.nexus.buup.api.dto.UpgradeFormRequest;

public interface UpgradeFormProcessor
{
    boolean processForm( UpgradeFormRequest form );
}
