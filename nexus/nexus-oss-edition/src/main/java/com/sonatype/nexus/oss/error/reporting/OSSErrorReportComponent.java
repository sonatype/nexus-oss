package com.sonatype.nexus.oss.error.reporting;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.error.report.ErrorReportComponent;

@Component( role = ErrorReportComponent.class )
public class OSSErrorReportComponent
    implements ErrorReportComponent
{

    private static final String COMPONENT = "Nexus";

    public String getComponent()
    {
        return COMPONENT;
    }

}
