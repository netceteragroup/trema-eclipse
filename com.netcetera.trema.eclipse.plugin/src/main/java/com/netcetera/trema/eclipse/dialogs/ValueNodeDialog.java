package com.netcetera.trema.eclipse.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.netcetera.trema.common.TremaUtil;
import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IValueNode;



/**
 * A dialog to edit or add a value node.
 */
public class ValueNodeDialog extends TremaInputDialog {
  
  /** 
   * Dialog for adding a value node. Input masks for the language, the
   * status and the value will be displayed.
   */
  public static final int TYPE_ADD = 1;
  
  /** 
   * Dialog for editing a single value node. Input mask for the
   * status and the value will be displayed, but not for the language.
   */
  public static final int TYPE_EDIT_SINGLE = 2;
  
  /** 
   * Dialog for editing multiple value nodes. Only an input mask for
   * the status will be displayed.
   */
  public static final int TYPE_EDIT_MULTI = 3;
  
  private int type = 0;
  private String language = null;
  private Status status = null;
  private String value = null;
  private IValueNode initialValueNode = null;
  private IInputValidator languageValidator = null;
  
  private Text languageText = null;
  private Combo statusCombo = null;
  private Text valueText = null;
  
  /**
   * Constructs a new value node dialog.
   * @param parentShell the parent shell of the dialog
   * @param title the title of the dialog
   * @param initialValueNode the value node providing the initial contents of
   * the input fields, may be <code>null</code>. Will be ignored if
   * <code>type == TYPE_ADD</code>
   * @param languageValidator the input validator for the language, may
   * be <code>null</code>
   * @param type the type of this dialog, one of <code>TYPE_ADD</code>, 
   * <code>TYPE_EDIT_SINGLE</code> or <code>TYPE_EDIT_MULTI</code>
   */
  public ValueNodeDialog(Shell parentShell, String title, final IValueNode initialValueNode,
                         IInputValidator languageValidator, int type) {
    super(parentShell, title);
    this.type = type;
    this.initialValueNode = initialValueNode;
    this.languageValidator = languageValidator;
  }
  
  /** {@inheritDoc} */
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    
    // do this here (and not in createDialogArea) because setting the
    // input fields may set the enablement on the ok button
    initContents();
  }
  
  /** 
   * Initializes the input fields of the dialog according to the dialog
   * type and provided initial value node.
   */
  private void initContents() {
    if (type == TYPE_ADD) {
      setStatusComboText(Status.INITIAL);
      getButton(IDialogConstants.OK_ID).setEnabled(false);
      languageText.setFocus();
    } else if (type == TYPE_EDIT_SINGLE) {
      initStatus();
      initValue();
      valueText.selectAll();
      valueText.setFocus();
    } else if (type == TYPE_EDIT_MULTI) {
      initStatus();
      statusCombo.setFocus();
    }
  }
  
  /**
   * Initializes the value according to the provided initial value node.
   * The language input text must not be <code>null</code> when calling
   * this method.
   */
  private void initValue() {
    if (initialValueNode != null) {
      setValueText(TremaUtil.emptyStringIfNull(initialValueNode.getValue()));
    }
  }
  
  /**
   * Initializes the status according to the provided initial value node.
   * The status combo must not be <code>null</code> when calling
   * this method.
   */
  private void initStatus() {
    if (initialValueNode != null && initialValueNode.getStatus() != null) {
      setStatusComboText(initialValueNode.getStatus());
    } else {
      setStatusComboText(Status.INITIAL);
    }
  }

  /** {@inheritDoc} */
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    Composite composite = new Composite(area, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    composite.setLayout(gridLayout);
    composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    
    if (type == TYPE_ADD) {
      // language label
      Label languageLabel = new Label(composite, SWT.NONE);
      languageLabel.setText("&Language:");
      
      // language text
      languageText = new Text(composite, SWT.BORDER | SWT.SINGLE);
      languageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      languageText.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          validateLanguageInput();
        }
      });
    }
    
    // status label
    Label statusLabel = new Label(composite, SWT.NONE);
    statusLabel.setText("&Status:");
    
    // status combo box
    statusCombo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN);
    GridData gridData = new GridData();
    gridData.widthHint = convertWidthInCharsToPixels(DEFAULT_WIDTH_IN_CHARS);
    statusCombo.setLayoutData(gridData);
    statusCombo.setItems(Status.getAvailableStatusNames());
    
    if (type != TYPE_EDIT_MULTI) {
      // value label
      Label valueLabel = new Label(composite, SWT.NONE);
      valueLabel.setText("&Value:");
      valueLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
      
      // value text
      valueText = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
      gridData = new GridData(GridData.FILL_BOTH);
      gridData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
      gridData.heightHint = valueText.getLineHeight() * 5;
      valueText.setLayoutData(gridData);
    }
    
    if (languageValidator != null) {
      createErrorMessageLabel(area);
    }
    
    applyDialogFont(area);
    
    return area;
  }
  
  /** {@inheritDoc} */
  protected void buttonPressed(int buttonId) {
    if (buttonId == IDialogConstants.OK_ID) {
      if (type == TYPE_ADD) {
        language = languageText.getText();
      }
      if (type != TYPE_EDIT_MULTI) {
        value = valueText.getText();
      }
      status = Status.valueOf(statusCombo.getText());
    } else {
      language = null;
      status = null;
      value = null;
    }
    super.buttonPressed(buttonId);
  }
  
  /** 
   * Validates the language input using the provided validator and sets
   * the error message accordingly. If no validator is present, the
   * error message is set to <code>null</code>.
   */
  protected void validateLanguageInput() {
    // visibility is set to protected in order to increase performance
    // when accessing this method from within annonymous subclasses
    String errorMessage = null;
    if (languageValidator != null) {
      errorMessage = languageValidator.isValid(languageText.getText());
    }
    setErrorMessage(errorMessage);
  }
  
  /**
   * Sets the status combo text to a given status.
   * @param status the status to set
   */
  private void setStatusComboText(Status status) {
    statusCombo.setText(status.getName());
  }
  
  /**
   * Sets the value text to a given value.
   * @param value the value to set
   */
  private void setValueText(String value) {
    valueText.setText(value);
  }
  
  /**
   * Gets the language typed into this dialog. This will return
   * <code>null</code> if the dialog type is not <code>TYPE_ADD</code>.
   * @return the language typed into this dialog.
   */
  public String getLanguage() {
    return language;
  }
  
  /**
   * Gets the status entered in this dialog.
   * @return the status entered in this dialog.
   */
  public Status getStatus() {
    return status;
  }
  
  /**
   * Gets the value typed into this dialog. This will return
   * <code>null</code> if the dialog type is
   * <code>TYPE_EDIT_MULTI</code>.
   * @return the value typed into this dialog.
   */
  public String getValue() {
    return value;
  }
  
}
