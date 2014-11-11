package com.netcetera.trema.eclipse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.WorkbenchEncoding;

import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;
import com.netcetera.trema.eclipse.editors.DatabaseContainer;



/** A collection of utility methods for the Trema eclipse package. */
public final class TremaUtilEclipse {
  
  // ==================================================================
  //  some constants
  // ==================================================================
  
  /** The pattern defining valid keys. */
  public static final Pattern KEY_PATTERN = Pattern.compile("[a-zA-Z0-9_\\-.]+");
  
  /** The pattern defining valid languages. */
  public static final Pattern LANGUAGE_PATTERN = Pattern.compile("[a-zA-Z_\\-]+");
  
  /** Selection is empty. */
  public static final int SEL_NONE = 0;
  
  /** Selection contains <code>IDatabase</code> objects. */
  public static final int SEL_DATABASE = 1 << 1;
  
  /** Selection contains <code>ITextNode</code> objects. */
  public static final int SEL_TEXT_NODE = 1 << 2;
  
  /** Selection contains <code>IValueNode</code> objects. */
  public static final int SEL_VALUE_NODE = 1 << 3;
  
  /** Selection contains unknown objects. */
  public static final int SEL_UNKNOWN = 1 << 4;
  
  private static final String N1 = "\n";
  private static final String R1 = "\r";
  private static final String N2 = "\\n";
  private static final String R2 = "\\r";
  
  /**
   * Private default constructor to prevent instantiation.
   */
  private TremaUtilEclipse() {
    throw new InstantiationError("This class should not be instatiated.");
  }
  
  
  
  // ==================================================================
  //  selection analysis methods
  // ==================================================================
  
  /**
   * Analyzes the types of the objects in a given
   * <code>IStructuredSelection</code>.
   * @param selection the selection to be analyzed
   * @return an <code>int</code> that can be queried by further methods
   */
  public static int analyzeSelection(IStructuredSelection selection) {
    if (selection == null || StructuredSelection.EMPTY.equals(selection)) {
      return SEL_NONE;
    }

    int analysisResult = SEL_NONE;
    Iterator<?> i = selection.iterator();
    while (i.hasNext()) {
      Object selectedObject = i.next();
      if (selectedObject instanceof IDatabase) {
        analysisResult |= SEL_DATABASE;
      } else if (selectedObject instanceof ITextNode) {
        analysisResult |= SEL_TEXT_NODE;
      } else if (selectedObject instanceof IValueNode) {
        analysisResult |= SEL_VALUE_NODE;
      } else {
        analysisResult |= SEL_UNKNOWN;
      }
    }

    return analysisResult;
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>IDatabase</code> objects are in the
   * selection
   */
  public static boolean hasJustDatabases(int analysisResult) {
    return analysisResult == SEL_DATABASE;
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>ITextNode</code> objects are in the
   * selection
   */
  public static boolean hasJustTextNodes(int analysisResult) {
    return analysisResult == SEL_TEXT_NODE;
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>IValueNode</code> objects are in the
   * selection
   */
  public static boolean hasJustValueNodes(int analysisResult) {
    return analysisResult == SEL_VALUE_NODE;
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>ITextNode</code> <b>and</b>
   * <code>IValueNode</code> objects are in the selection
   */
  public static boolean hasJustTextAndValueNodes(int analysisResult) {
    return analysisResult == (SEL_TEXT_NODE | SEL_VALUE_NODE);
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>IDatabase</code> <b>and</b>
   * <code>ITextNode</code> objects are in the selection
   */
  public static boolean hasJustDatabasesAndTextNodes(int analysisResult) {
    return analysisResult == (SEL_DATABASE | SEL_TEXT_NODE);
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>IDatabase</code> <b>and</b>
   * <code>IValueNode</code> objects are in the selection
   */
  public static boolean hasJustDatabasesAndValueNodes(int analysisResult) {
    return analysisResult == (SEL_DATABASE | SEL_VALUE_NODE);
  }
  
  /**
   * Queries a selection analysis result <code>int</code>.
   * @param analysisResult the result to be queried
   * @return true if only <code>ITextNode</code> <b>or</b>
   * <code>IValueNode</code> objects are in the selection
   */
  public static boolean hasJustTextOrValueNodes(int analysisResult) {
    return hasJustTextNodes(analysisResult)
           || hasJustValueNodes(analysisResult) 
           || hasJustTextAndValueNodes(analysisResult);
  }
  
  // ==================================================================
  //  validation methods
  // ==================================================================
  
  /**
   * Validates a workspace path. The following criteria are
   * checked:
   * <ul>
   * <li>the path must not be <code>null</code> or empty
   * <li>the path must be a valid folder path, see
   * {@link org.eclipse.core.resources.IWorkspace#validatePath}.
   * <li>optional: a folder or a file with the same name must not
   * exists
   * <li>optional: the project appearing at the beginning of the path
   * must exist and be open
   * </ul>
   * @param path the path to be validated
   * @param checkExistence true if existence of folders and files with
   * the same name should be checked
   * @param checkProject true if the project should be checked
   * @return an appropriate error message or <code>null</code> if the
   * validation is successful.
   */
  public static String validateWorkspacePath(IPath path, boolean checkExistence, boolean checkProject) {
    if (path == null || path.isEmpty()) {
      return "Please specify a path.";
    }

    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IStatus result = workspace.validatePath(path.toOSString(), IResource.FOLDER | IResource.FILE);
    if (!result.isOK()) {
      return result.getMessage();
    }
    
    if (checkExistence) {
      if (workspace.getRoot().getFolder(path).exists()) {
        return "A folder with the same name already exists.";
      }
      if (workspace.getRoot().getFile(path).exists()) {
        return "A file with the same name already exists.";
      } 
    }
    
    if (checkProject) {
      String projectName = path.segment(0);
      IProject project = workspace.getRoot().getProject(projectName);
      if (projectName == null 
          || !project.exists()) {
        return "The specified project does not exist.";
      }
      if (!project.isOpen()) {
        return "The specified project is not open.";
      }
    }
    
    return null;
  }
  
  /**
   * Validates a file name. The following criteria are checked:
   * <ul>
   * <li>the file name must not be <code>null</code> or empty
   * <li>the file name must be a valid resource name, see
   * {@link org.eclipse.core.resources.IWorkspace#validateName}.
   * <li>optional: the file name must have one of a given set of 
   * extensions
   * </ul>
   * @param fileName the file name to be validated
   * @param validExtensions the valid extensions. May be
   * <code>null</code> or empty.
   * @return an appropriate error message or <code>null</code> if the
   * validation is successful.
   */
  public static String validateFileName(String fileName, String[] validExtensions) {
    if (fileName == null || fileName.length() == 0) {
      return "Please specify a file name.";
    }
    
    IStatus result = ResourcesPlugin.getWorkspace().validateName(fileName, IResource.FILE);
    if (!result.isOK()) {
      return result.getMessage();
    }
    
    if (validExtensions != null && validExtensions.length > 0) {
      int dotPosition = fileName.lastIndexOf('.');
      if (dotPosition == -1) {
        return "File extension must be one of: " + TremaEclipseUtil.arrayToString(validExtensions, ", ");
      }
      
      String extension = fileName.substring(dotPosition + 1);
      boolean validExtension = false;
      for (int i = 0; !validExtension && i < validExtensions.length; i++) {
        validExtension = extension.equalsIgnoreCase(validExtensions[i]);
      }
      if (!validExtension) {
        return "File extension must be one of: " + TremaEclipseUtil.arrayToString(validExtensions, ", ");
      }
    }
    
    return null;
  }
  
  /**
   * Validates a database key. The following criteria are checked:
   * <ul>
   * <li>the key must not be <code>null</code> or empty</li>
   * <li>the key must not contain illegal characters</li>
   * <li>optional: if the key is different from a given old key, it must
   * not be already contained in a given database</li>
   * </ul>
   * @param key the key to be validated.
   * @param oldKey the old key, may be <code>null</code>
   * @param db the database, may be <code>null</code>
   * @return an appropriate error message or <code>null</code> if the
   * validation is successful
   */
  public static String validateKey(String key, String oldKey, IDatabase db) {
    if (key == null || key.length() == 0) {
      return "Please specify a key.";
    }

    Matcher matcher = KEY_PATTERN.matcher(key);
    if (!matcher.matches()) {
      return "The key contains illegal characters.";
    }
    
    if (db != null) {
      if (!key.equals(oldKey) && db.existsTextNode(key)) {
        return "The key already exists in the database.";
      }
    }
    
    return null;
  }
  
  /**
   * Validates a language. The following criteria are checked:
   * <ul>
   * <li>the language must not be <code>null</code> or empty</li>
   * <li>the language must not contain illegal characters</li>
   * <li>optional: the language must not be contained in a given
   * set</li>
   * </ul>
   * @param language the language to be validated
   * @param presentLanguages the set of languages the language to be
   * checked should not be contained in, may be <code>null</code>
   * @return an appropriate error message or <code>null</code> if the
   * validation is successful
   */
  public static String validateLanguage(String language, Set<String> presentLanguages) {
    if (language == null || language.length() == 0) {
      return "Please specify a language.";
    }
    
    Matcher matcher = LANGUAGE_PATTERN.matcher(language);
    if (!matcher.matches()) {
      return "The language contains illegal characters.";
    }
    
    if (presentLanguages != null) {
      if (presentLanguages.contains(language)) {
        return (presentLanguages.size() == 1) ? "The language already exists for this key."
                                              : "The language already exists for at least one of the selected keys.";
      }
    }
    
    return null;
  }
  
  /**
   * Validates an encoding. The following criteria are checked:
   * <ul>
   * <li>the encoding must not be <code>null</code> or empty</li>
   * <li>the encoding must be supported</li>
   * </ul>
   * @param encoding the encoding to be validated
   * @return an appropriate error message or <code>null</code> if the
   * validation is successful
   */
  public static String validateEncoding(String encoding) {
    if (encoding == null || encoding.length() == 0) {
      return "Please specify an encoding.";
    }
    
    if (!isSupportedEncoding(encoding)) {
      return "The encoding \"" + encoding + "\" is not supported.";
    }
    
    return null;
  }
  
  /**
   * Checks whether a given encoding is supported or not.
   * @param encoding the encoding to be checked
   * @return true if the given encoding is supported
   */
  public static boolean isSupportedEncoding(String encoding) {
    try {
      new String(new byte[0], encoding);
      return true;
    } catch (UnsupportedEncodingException e) {
      return false;
    }
  }
  
  // ==================================================================
  //  various
  // ==================================================================
  
  /**
   * Extracts the text nodes of a given
   * <code>IStructuredSelection</code>.
   * @param selection the selection
   * @return the text nodes or an empty array if none.
   */
  public static ITextNode[] getTextNodes(IStructuredSelection selection) {
    if (selection == null) {
      return new ITextNode[0];
    }
    
    List<ITextNode> textNodeList = new ArrayList<ITextNode>();
    Iterator<?> iterator = selection.iterator();
    while (iterator.hasNext()) {
      Object selectedObject = iterator.next();
      if (selectedObject instanceof ITextNode) {
        textNodeList.add((ITextNode) selectedObject);
      }
    }
    return textNodeList.toArray(new ITextNode[textNodeList.size()]);
  }
  
  /**
   * Populates the combo with initial encoding texts.
   * 
   * @param initialText the encoding that should initially be displayed.
   * If <code>null</code> or not found, the preferred workbench
   * encoding and finally the default system encoding are tried.
   * @param combo the combo box to be populated
   */
  @SuppressWarnings("unchecked")
  public static void populateWithEncodings(String initialText, Combo combo) {
    
    SortedSet<String> encodings = new TreeSet<String>();
    String displayedEncoding = null;
    if (initialText != null && WorkbenchEncoding.getDefinedEncodings().contains(initialText)) {
      displayedEncoding = initialText;
    }    
    if (displayedEncoding == null) {
      displayedEncoding = WorkbenchEncoding.getWorkbenchDefaultEncoding();
    }
    encodings.add(displayedEncoding);
    encodings.addAll(WorkbenchEncoding.getDefinedEncodings());

    combo.setItems(encodings.toArray(new String[encodings.size()]));
    combo.setText(displayedEncoding);
  }
  
  /**
   * Convenience method to directly get the database from the Trema
   * table tree viewer input.
   * @param treeViewer the tree viewer
   * @return the database or <code>null</code> if no database could be
   * found
   */
  public static IDatabase getDatabase(TreeViewer treeViewer) {
    if (treeViewer == null) {
      return null;
    }
    Object input = treeViewer.getInput();
    if (input instanceof DatabaseContainer) {
      return ((DatabaseContainer) input).getDatabase();
    }
    return null;
  }
  
  /** 
   * Convenience method that wraps a Trema plugin error
   * <code>IStatus</code> around a given error message.
   * @param message the error message
   * @return the status
   */
  public static IStatus createErrorStatus(String message)  {
    return new org.eclipse.core.runtime.Status(IStatus.ERROR, TremaPlugin.getId(), IStatus.OK, message, null);
  }
  
  /** 
   * Convenience method that wraps a Trema plugin error
   * {@link IStatus} around a given {@link Throwable}.
   * @param e the {@link Throwable}
   * @return the status
   */
  public static IStatus createErrorStatus(Throwable e)  {
    return new org.eclipse.core.runtime.Status(IStatus.ERROR, TremaPlugin.getId(), IStatus.OK, e.getMessage(), e);
  }

  /**
   * Sets a given label's font style to bold.
   * @param label the label to process
   * @param device the device to create the font on
   */
  public static void setBoldFont(Label label, Device device) {
    FontData fontData = label.getFont().getFontData()[0];
    fontData.setStyle(SWT.BOLD);
    Font font = new Font(device, fontData);
    label.setFont(font);
  }
  
  /**
   * Turns line ending characters into visible "\\n" and "\\r" and vice
   * versa.
   * @param s the string to be processed
   * @param visible true if the line ending characters should be made
   * visible, false otherwise
   * @return the processed string or <code>null</code> if the given
   * string was <code>null</code>
   */
  public static String makeLineEndingsVisible(String s, boolean visible) {
    if (s == null) {
      return null;
    }
    
    String result = s;
    if (visible) {
      result = result.replace(N1, N2).replace(R1, R2);
    } else {
      result = result.replace(N2, N1).replace(R2, R1);
    }
    return result;
  }
  
}
