package com.netcetera.trema.eclipse.importing;

import org.eclipse.core.runtime.IProgressMonitor;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IImportSource;
import com.netcetera.trema.core.importing.ChangesAnalyzer;
import com.netcetera.trema.eclipse.TremaPreferencePage;



/**
 * Determines the changes between an imported source and a database
 * and classifies them as conflicting or non-conflicting.
 */
public class MonitoringChangesAnalyzer extends ChangesAnalyzer {
  
  private IProgressMonitor monitor;
  
  /**
   * Creates a new instance.
   * @param importSource the import source
   * @param db the database
   * @throws IllegalArgumentException if there is a mismatch between
   * the master languages
   */
  public MonitoringChangesAnalyzer(IImportSource importSource, IDatabase db) throws IllegalArgumentException {
    super(importSource, db);
    setUseMasterValueFromFile(TremaPreferencePage.getUseMasterValueFromFile());
  }
  
  /**
   * Sets the progress monitor to be used in this analyzer.
   * 
   * @param monitor the monitor
   */
  public void setMonitor(IProgressMonitor monitor) {
    this.monitor = monitor;
  }

  public void monitorBeginTask(int totalwork) {
    if (monitor != null) {
      monitor.beginTask("Analyzing changes...", totalwork);
    }
  }
  
  public void monitorWorked(int work) {
    if (monitor != null) {
      monitor.worked(1);
    }
  }
}
