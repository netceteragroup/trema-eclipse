package com.netcetera.trema.eclipse.m2e;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

public class TremaProjectConfigurator
 extends AbstractProjectConfigurator
{

  @Override
  public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade,
      MojoExecution execution,
      IPluginExecutionMetadata executionMetadata) {
    return new TremaBuildParticipant(execution);
  }

  /*
   * (non-Jsdoc)
   * @see
   * org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator#configure
   * (org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest,
   * org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void configure(ProjectConfigurationRequest arg0, IProgressMonitor arg1)
      throws CoreException {
    // TODO Auto-generated function stub

  }

}
