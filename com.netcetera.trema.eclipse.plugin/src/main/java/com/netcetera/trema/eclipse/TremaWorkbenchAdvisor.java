package com.netcetera.trema.eclipse;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;



/** 
 * Workbench advisor for the Trema RCP application. 
 */

// TODO tzueblin Dec 1, 2008: Replace deprecated stuff in here.
public class TremaWorkbenchAdvisor extends WorkbenchAdvisor {

  private WorkbenchActionBuilder actionBuilder = null;

  /** {@inheritDoc} */
  public String getInitialWindowPerspectiveId() {
    return "com.netcetera.trema.eclipse.perspectives.tremaPerspective";
  }
  
  /** {@inheritDoc} */
  @SuppressWarnings("deprecation")
  public void preWindowOpen(IWorkbenchWindowConfigurer configurer) {
    super.preWindowOpen(configurer);
    configurer.setTitle("Trema Editor");
    configurer.setShowCoolBar(true);
    configurer.setShowStatusLine(true);
  }
  
  /** {@inheritDoc} */
  @SuppressWarnings("deprecation")
  public void fillActionBars(IWorkbenchWindow window, IActionBarConfigurer configurer, int flags) {
    super.fillActionBars(window, configurer, flags);
    
    if (actionBuilder == null) {
      actionBuilder = new WorkbenchActionBuilder(window);
    }
    
    actionBuilder.makeAndPopulateActions(configurer);
  }
  
}
