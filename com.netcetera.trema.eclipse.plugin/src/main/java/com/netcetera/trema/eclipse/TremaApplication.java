package com.netcetera.trema.eclipse;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;



/** The Trema RCP Application. */
public class TremaApplication implements IApplication {

  /** {@inheritDoc} */
  public Object start(IApplicationContext context) throws Exception {
    WorkbenchAdvisor workbenchAdvisor = new TremaWorkbenchAdvisor();
    Display display = PlatformUI.createDisplay();
    try {
      int returnCode = PlatformUI.createAndRunWorkbench(display, workbenchAdvisor);
      if (returnCode == PlatformUI.RETURN_RESTART) {
        return IApplication.EXIT_RESTART;
      } else {
        return IApplication.EXIT_OK;
      }
    } finally {
      display.dispose();
    }
  }

  /** {@inheritDoc} */
  public void stop() {
  }

}
