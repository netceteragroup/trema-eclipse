package com.netcetera.trema.eclipse;


import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * @author tzueblin
 * TODO: Is this class actually used?
 * 
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

  /**
   * @param configurer the configurer
   */
        public ApplicationWorkbenchWindowAdvisor(
                        IWorkbenchWindowConfigurer configurer) {
                super(configurer);
        }

  /*
   * (non-Jsdoc)
   * @see
   * org.eclipse.ui.application.WorkbenchWindowAdvisor#createActionBarAdvisor
   * (org.eclipse.ui.application.IActionBarConfigurer)
   */
        @Override
        public ActionBarAdvisor createActionBarAdvisor(
                        IActionBarConfigurer configurer) {
                return new ActionBarAdvisor(configurer);
        }

  /*
   * (non-Jsdoc)
   * @see org.eclipse.ui.application.WorkbenchWindowAdvisor#preWindowOpen()
   */
        @Override
        public void preWindowOpen() {
                IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
                configurer.setInitialSize(new Point(400, 300));
                configurer.setShowStatusLine(false);
                configurer.setShowCoolBar(true);
                configurer.setTitle("RCP Application");
        }
}

