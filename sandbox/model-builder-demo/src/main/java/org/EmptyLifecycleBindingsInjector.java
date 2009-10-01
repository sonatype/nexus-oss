package org;

import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.plugin.LifecycleBindingsInjector;
import org.apache.maven.model.Model;

/*
 * This is an optional component and only required if the model request has processPlugins=true which is not of interest
 * for dependency related tasks. I'm looking into teaching Plexus optional requirements to get rid of this empty class.
 */
public class EmptyLifecycleBindingsInjector
    implements LifecycleBindingsInjector
{

    public void injectLifecycleBindings( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        // no-op
    }

}
