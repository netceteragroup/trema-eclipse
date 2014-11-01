package com.netcetera.trema.eclipse.m2e;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMaven;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

public class TremaBuildParticipant
 extends MojoExecutionBuildParticipant
{

  public TremaBuildParticipant(MojoExecution execution) {
    super(execution, true);
  }

  @Override
  public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
    IMaven maven = MavenPlugin.getMaven();
    BuildContext buildContext = getBuildContext();

    // check if the trm file has changed
    File trmFile = maven.getMojoParameterValue(getSession(), getMojoExecution(), "tremaFile",
        File.class);
    if (!buildContext.hasDelta(trmFile)) {
      return null;
    }

    // execute mojo
    Set<IProject> result = super.build(kind, monitor);
    // since buildcontext.newFileOutputStream is used to write files,
    // eclipse/m2e will be aware of the new files that were created by the mojo
    return result;
  }
}
