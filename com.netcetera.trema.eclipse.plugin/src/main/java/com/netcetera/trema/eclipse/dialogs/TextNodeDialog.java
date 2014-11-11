package com.netcetera.trema.eclipse.dialogs;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.eclipse.TremaEclipseUtil;
import com.netcetera.trema.eclipse.TremaPlugin;
import com.netcetera.trema.eclipse.validators.LanguageValidator;



/**
 * A dialog to edit or add a text node. If a text node has been added,
 * this dialog remembers the spcified context and languages and will
 * display them initially the next time an 'add' dialog opens.
 */
public class TextNodeDialog extends TremaInputDialog {
  
  /** 
   * Dialog for adding a text node. Input masks for the key, the
   * context and the initial languages will be displayed.
   */
  public static final int TYPE_ADD = 1;  
  
  /** 
   * Dialog for editing a single text node. Input masks for the key and
   * the context will be displayed.
   */
  public static final int TYPE_EDIT_SINGLE = 2;
  
  /** 
   * Dialog for editing multiple text nodes. Only an input mask for
   * the context will be displayed.
   */
  public static final int TYPE_EDIT_MULTI = 3;
  
  /** The dialog settings key for storing the specified context. */
  private static final String DS_KEY_CONTEXT = "textNodeDialog.context";
  
  /** The dialog settings key for storing the specified languages. */
  private static final String DS_KEY_LANGUAGES = "textNodeDialog.languages";
  
  /**
   * Dialog settings for this dialog. Used for caching the specified
   * context and languages for a dialog of type
   * <code>TYPE_ADD</code>.
   */
  private IDialogSettings dialogSettings = null;
  
  private int type = 0;
  private String key = null;
  private String context = null;
  private SortedSet<String> languages = null;
  private ITextNode initialTextNode = null;
  private IInputValidator keyValidator = null;
  
  private Text keyText = null;
  private Text contextText = null;
  private Text languageText = null;
  private List languageList = null;
  private Button languageAddButton = null;
  private Button languageRemoveButton = null;

  /**
   * Creates a new text node dialog.
   * @param parentShell the parent shell of the dialog
   * @param title the title  of the dialog
   * @param initialTextNode the text node providing the initial
   * contents of the input fields, may be <code>null</code>. Will be
   * ignored if <code>type == TYPE_ADD</code>.
   * @param keyValidator the validator to validate the specified key
   * @param type the type of the text node dialog, one of
   * <code>TYPE_ADD</code>, <code>TYPE_EDIT_SINGLE</code> or
   * <code>TYPE_EDIT_MULTI</code>
   */
  public TextNodeDialog(Shell parentShell, String title, final ITextNode initialTextNode,
                        IInputValidator keyValidator, int type) {
    super(parentShell, title);
    this.type = type;
    this.initialTextNode = initialTextNode;
    this.keyValidator = keyValidator;
    dialogSettings = TremaPlugin.getDefault().getDialogSettings();
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
      getButton(IDialogConstants.OK_ID).setEnabled(false);
      String storedContext = dialogSettings.get(DS_KEY_CONTEXT);
      if (storedContext != null) {
        setContextText(storedContext);
      }
      initLanguages();
      keyText.setFocus();
    } else if (type == TYPE_EDIT_SINGLE) {
      initKey();
      initContext();
      if (keyText.getText().equals("")) {
        getButton(IDialogConstants.OK_ID).setEnabled(false);
      }
      keyText.selectAll();
      keyText.setFocus();
    } else if (type == TYPE_EDIT_MULTI) {
      initContext();
      contextText.selectAll();
      contextText.setFocus();
    }
  }
  
  /**
   * Initializes the key according to the provided initial text node.
   * The key input text must not be <code>null</code> when calling
   * this method.
   */
  private void initKey() {
    if (initialTextNode != null) {
      setKeyText(TremaEclipseUtil.emptyStringIfNull(initialTextNode.getKey()));
    }
  }

  /**
   * Initializes the context according to the provided initial text
   * node. The context input text must not be <code>null</code> when
   * calling this method.
   */
  private void initContext() {
    if (initialTextNode != null) {
      setContextText(TremaEclipseUtil.emptyStringIfNull(initialTextNode.getContext()));
    }
  }
  
  /** Initializes the languages using the language cache if possible. */
  private void initLanguages() {
    languages = new TreeSet<String>();
    
    // possibly use the cache
    String[] storedLanguages = dialogSettings.getArray(DS_KEY_LANGUAGES);
    if (storedLanguages != null) {
      languages.addAll(Arrays.asList(storedLanguages));
    }
    updateLanguageList();
  }
  
  /** {@inheritDoc} */
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);

    Composite mainComposite = new Composite(area, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    mainComposite.setLayout(gridLayout);
    
    if (type != TYPE_EDIT_MULTI) {
      // key label
      Label keyLabel = new Label(mainComposite, SWT.NONE);
      keyLabel.setText("&Key:");
      
      // key text
      keyText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
      keyText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
      keyText.addModifyListener(new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          validateKeyInput();
        }
      });
    }
    
    // context label
    Label contextLabel = new Label(mainComposite, SWT.NONE);
    contextLabel.setText("&Context:");
    
    // context text
    contextText = new Text(mainComposite, SWT.BORDER | SWT.SINGLE);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    gridData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
    contextText.setLayoutData(gridData);
    
    if (type == TYPE_ADD) {
      createLanguageArea(mainComposite);
    }
    
    if (keyValidator != null) {
      createErrorMessageLabel(area);
    }

    applyDialogFont(area);
    
    return area;
  }
  
  /**
   * Creates the language selection area used for
   * <code>TYPE_ADD</code>.
   * @param parent the parent composite
   */
  private void createLanguageArea(Composite parent) {
    GridData data;
    Label languageLabel = new Label(parent, SWT.NONE);
    languageLabel.setText("&Languages:");
    languageLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
    
    // composite for displaying the language selection
    Composite languageComposite = new Composite(parent, SWT.NONE);
    GridLayout languageGridLayout = new GridLayout();
    languageGridLayout.numColumns = 2;
    languageGridLayout.marginWidth = 0;
    languageGridLayout.marginHeight = 0;
    languageComposite.setLayout(languageGridLayout);
    
    // text for inputting a language
    languageText = new Text(languageComposite, SWT.BORDER | SWT.SINGLE);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.widthHint = convertWidthInCharsToPixels(DEFAULT_WIDTH_IN_CHARS);
    languageText.setLayoutData(data);
    languageText.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        Shell shell = getShell();
        if (shell != null) {
          shell.setDefaultButton(languageAddButton);
        }
      }
      public void focusLost(FocusEvent e) {
        Shell shell = getShell();
        if (shell != null) {
          shell.setDefaultButton(getButton(IDialogConstants.OK_ID));
        }
      }
    });
    languageText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        updateLanguageAddButton();
      }
    });
    
    // add button
    languageAddButton = new Button(languageComposite, SWT.PUSH);
    languageAddButton.setText("&Add");
    languageAddButton.setEnabled(false);
    languageAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    languageAddButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        validateAndAddLanguage();
      }
    });
    
    // list displaying currently selected languages
    languageList = new List(languageComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
    GridData listLayoutData = new GridData(GridData.FILL_HORIZONTAL);
    listLayoutData.heightHint = languageList.getItemHeight() * 5;
    languageList.setLayoutData(listLayoutData);
    languageList.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        updateLanguageRemoveButton();
      }
    });
    
    // remove button
    languageRemoveButton = new Button(languageComposite, SWT.PUSH);
    languageRemoveButton.setText("&Remove");
    languageRemoveButton.setEnabled(false);
    languageRemoveButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL));
    languageRemoveButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        removeSelectedLanguages();
      }
    });
  }

  /** Updates the enablement of the language add button. */
  private void updateLanguageAddButton() {
    languageAddButton.setEnabled(!languageText.getText().equals(""));
  }
  
  /** Updates the enablement of the language remove button. */
  private void updateLanguageRemoveButton() {
    languageRemoveButton.setEnabled(languageList.getSelectionCount() > 0);
  }
  
  /** Removes the selected languages from the language list. */
  private void removeSelectedLanguages() {
    String[] toRemove = languageList.getSelection();
    for (int i = 0; i < toRemove.length; i++) {
      languages.remove(toRemove[i]);
    }
    updateLanguageList();
    updateLanguageRemoveButton();
  }
  
  /** 
   * Validates the language in the language text field. If the
   * validation succeeds the language is added to the language list,
   * otherwise an error message is displayed.
   */ 
  private void validateAndAddLanguage() {
    String language = languageText.getText();
    String errorMessage = new LanguageValidator().isValid(language);
    if (errorMessage != null) {
      MessageDialog.openError(getShell(), "Trema", errorMessage);
      languageText.selectAll();
      languageText.setFocus();
    } else {
      languages.add(language);
      updateLanguageList();
      languageText.setText("");
      languageText.setFocus();
    }
  }
  
  /** Updates the language list from the internal language set. */
  private void updateLanguageList() {
    languageList.setItems(languages.toArray(new String[languages.size()]));
  }
  
  /** {@inheritDoc} */
  protected void buttonPressed(int buttonId) {
    if (buttonId == IDialogConstants.OK_ID) {
      context = contextText.getText();
      if (type != TYPE_EDIT_MULTI) {
        key = keyText.getText();
      }
      if (type == TYPE_ADD) {
        // cache the context and the languages
        dialogSettings.put(DS_KEY_CONTEXT, context);
        dialogSettings.put(DS_KEY_LANGUAGES, languages.toArray(new String[languages.size()]));
      }
    } else {
      key = null;
      context = null;

    }
    super.buttonPressed(buttonId);
  }
  
  /** 
   * Validates the key input using the provided validator and sets
   * the error message accordingly. If no validator is present, the
   * error message is set to <code>null</code>.
   */
  private void validateKeyInput() {
    String errorMessage = null;
    if (keyValidator != null) {
      errorMessage = keyValidator.isValid(keyText.getText());
    }
    setErrorMessage(errorMessage);
  }
  
  /**
   * Sets the key text to a given key.
   * @param key the key to set
   */
  private void setKeyText(String key) {
    keyText.setText(key);
  }
  
  /**
   * Sets the context text to a given context.
   * @param context the context to set
   */
  private void setContextText(String context) {
    contextText.setText(context);
  }
  
  /**
   * Gets the key typed into this dialog. This will return
   * <code>null</code> if the dialog type is
   * <code>TYPE_EDIT_MULTI</code>.
   * @return the key typed into this dialog.
   */
  public String getKey() {
    return key;
  }
  
  /**
   * Gets the languages typed into this dialog. This will return
   * <code>null</code> if the dialog type is not
   * <code>TYPE_EDIT_ADD</code>.
   * @return the languages typed into this dialog.
   */  
  public String[] getLanguages() {
    return languages.toArray(new String[languages.size()]);
  }
  
  /**
   * Gets the context typed into this dialog.
   * @return the context typed into this dialog.
   */
  public String getContext() {
    return context;
  }
  
}

