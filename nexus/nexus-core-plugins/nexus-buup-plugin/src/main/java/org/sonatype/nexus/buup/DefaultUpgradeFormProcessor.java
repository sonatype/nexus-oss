package org.sonatype.nexus.buup;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.buup.api.dto.UpgradeFormRequest;

@Component( role = UpgradeFormProcessor.class )
public class DefaultUpgradeFormProcessor
    implements UpgradeFormProcessor
{
    public boolean processForm( UpgradeFormRequest form )
    {
        // TODO: implement this
        // verify mails etc, send email to sonatype, etc
        return form.isAcceptsTermsAndConditions();
    }
}
