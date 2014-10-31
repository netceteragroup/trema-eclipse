package com.netcetera.trema.eclipse.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;



/**
 * An abstract dialog providing OK and Cancel buttons aswell as an
 * error message label with an error image. Though his is similar to an
 * {@link org.eclipse.jface.dialogs.InputDialog}, this class offers a
 * more flexible error message label.
 */
public abstract class TremaInputDialog extends Dialog {
  
  /** 
   * The default width in chars for "small" widgets such as a
   * language input text fieldor a status combo box.
   */
  public static final int DEFAULT_WIDTH_IN_CHARS = 12;
  
  /** The default background color used for the error message label. */
  protected static final Color DEFAULT_BACKGROUND = null;
  
  /** The title of this dialog. */
  private String title = null;
  
  /** The error message label. */
  private CLabel errorMessageLabel = null;
  
  /**
   * Creates a new modal Trema dialog instance.
   * @param parentShell the parent shell, or <code>null</code> to
   * create a top-level shell
   * @param title the title
   */
  public TremaInputDialog(Shell parentShell, String title) {
    super(parentShell);
    this.title = title;
  }
    
  /** {@inheritDoc} */
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    if (title != null) {
      shell.setText(title);
    }
  }

  /**
   * Creates the error message label under the given parent.
   * @param parent the parent composite. In most cases this should be
   * the dialog area.
   */
  protected void createErrorMessageLabel(Composite parent) {
    errorMessageLabel = new CLabel(parent, SWT.LEFT);
    GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
    errorMessageLabel.setLayoutData(data);
    errorMessageLabel.setFont(parent.getFont());
    errorMessageLabel.setBackground(DEFAULT_BACKGROUND);
  }
  
  /**
   * Sets the error message in the error message label.
   * <p>
   * If the error message label has not been created this method
   * has no effect.
   * @param errorMessage the error message to set or <code>null</code>
   * if no error message should be set
   */
  protected void setErrorMessage(String errorMessage) {
    if (errorMessageLabel != null) {
      if (errorMessage == null || errorMessage.length() == 0) {
        errorMessageLabel.setImage(null);
        errorMessageLabel.setText("");
        getButton(IDialogConstants.OK_ID).setEnabled(true);      
      } else {
        errorMessageLabel.setImage(
            PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK)
        );
        errorMessageLabel.setText(errorMessage);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
      }
      errorMessageLabel.getParent().update();
    }
  }
  
}

