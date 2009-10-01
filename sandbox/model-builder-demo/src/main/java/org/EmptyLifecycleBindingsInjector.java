package org;

import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.plugin.LifecycleBindingsInjector;
import org.apache.maven.model.Model;

/*
 * This is an optional component and will not be required when using plexus 1.3.0+.
 */
public class EmptyLifecycleBindingsInjector
    implements LifecycleBindingsInjector
{

    public void injectLifecycleBindings( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        // no-op
    }

}
