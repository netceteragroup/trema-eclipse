package com.netcetera.trema.eclipse.exporting;

import java.io.Writer;

import org.eclipse.core.runtime.IProgressMonitor;

import com.netcetera.trema.core.exporting.TremaCSVPrinter;



/**
 * Subclass of <code>CSVPrinter</code> to support progress monitors,
 * since a CSV export has turned out to be rather a time consuming
 * operation.
 */
public class MonitoringTremaCSVPrinter extends TremaCSVPrinter {
  
  private final IProgressMonitor monitor;
  
  /**
   * Create a printer that will print values to the given
   * stream. Comments will be
   * written using the default comment character '#'.
   * @param out the output writer to use
   * @param separator the csv separator to use
   * @param monitor the monitor to use while writing
   */
  public MonitoringTremaCSVPrinter(Writer out, char separator, IProgressMonitor monitor) {
    super(out, separator);
    this.monitor = monitor;
  }
  
  public void monitorBeginTask(int totalwork) {
    if (monitor != null) {
      monitor.beginTask("Exporting...", totalwork);
    }
  }
  
  public void monitorWorked(int work) {
    if (monitor != null) {
      monitor.worked(work);
    } 
  }
}
