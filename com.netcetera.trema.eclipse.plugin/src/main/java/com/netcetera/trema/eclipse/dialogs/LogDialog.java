package com.netcetera.trema.eclipse.dialogs;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;



/**
 * Dialog to display read-only multi-line text, such as a log file.
 */
public class LogDialog extends Dialog {

  private Text textText = null;
  
  private String title = null;
  private String label = null;
  private String text = null;
  
  /**
   * Creates a new dialog instance.
   * @param parentShell the parent shell
   * @param title the title of the dialog
   * @param label the label, may be <code>null</code>
   * @param text the text to be displayed
   */
  public LogDialog(Shell parentShell, String title, String label, String text) {
    super(parentShell);
    this.title = title;
    this.label = label;
    this.text = text;
  }
    
  /** {@inheritDoc} */
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    if (title != null) {
      shell.setText(title);
    }
  }

  /** {@inheritDoc} */
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    if (label != null) {
      Label textLabel = new Label(area, SWT.NONE);
      textLabel.setText(label);
    }

    textText = new Text(area, SWT.V_SCROLL | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
    textText.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.widthHint = convertWidthInCharsToPixels(80);
    gridData.heightHint = convertHeightInCharsToPixels(30);
    textText.setLayoutData(gridData);
    textText.setText(text);

    return area;
  }

  /** {@inheritDoc} */
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }

}
