package org.sonatype.nexus.error.reporting.modifier;

import static org.sonatype.sisu.pr.Modifier.Priority.FILLIN;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.swizzle.IssueSubmissionRequest;
import org.sonatype.nexus.error.report.ErrorReportComponent;
import org.sonatype.sisu.pr.Modifier;

@Component( role = Modifier.class )
public class ComponentModifier
    implements Modifier
{

    @Requirement
    ErrorReportComponent component;

    @Override
    public IssueSubmissionRequest modify( final IssueSubmissionRequest request )
    {
        request.setComponent( component.getComponent() );
        return request;
    }

    @Override
    public int getPriority()
    {
        return FILLIN.priority();
    }
}
