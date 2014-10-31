package com.netcetera.trema.eclipse.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.netcetera.trema.eclipse.TremaPlugin;



/**
 * A dialog allowing to compare 2 strings. The code was partially
 * copied from the org.eclipse.jdt.junit plugin.
 */
public class CompareDialog extends Dialog {
  
  private static final class CompareResultMergeViewer extends TextMergeViewer {
    private CompareResultMergeViewer(Composite parent, int style, CompareConfiguration configuration) {
      super(parent, style, configuration);
    }
    @Override
    protected void configureTextViewer(TextViewer textViewer) {
      if (textViewer instanceof SourceViewer) {
        ((SourceViewer) textViewer).configure(new SourceViewerConfiguration());   
      }
    }
  }
  
  private static class CompareElement implements ITypedElement, IEncodedStreamContentAccessor {
    private String content = null;
    
    public CompareElement(String content) {
      this.content = content;
    }
    public String getName() {
      return "<no name>";
    }
    public Image getImage() {
      return null;
    }
    public String getType() {
      return "txt";
    }
    public InputStream getContents() {
      try {
        return new ByteArrayInputStream(content.getBytes("UTF-8"));
      } catch (UnsupportedEncodingException e) {
        return new ByteArrayInputStream(content.getBytes());
      }
    }
    public String getCharset() {
      return "UTF-8";
    }
  }
  
  private String title = null;
  private String leftLabel = null;
  private String rightLabel = null;
  private String leftText = null;
  private String rightText = null;
  
  /**
   * Creates a new dialog instance.
   * @param parentShell the parent shell
   * @param title the title
   * @param leftLabel the label for the left text
   * @param rightLabel the label for the right text
   * @param leftText the left text to compare
   * @param rightText the right text to compare
   */
  public CompareDialog(Shell parentShell, String title, String leftLabel, String rightLabel,
                       String leftText, String rightText) {
    super(parentShell);
    this.title = title;
    this.leftLabel = leftLabel;
    this.rightLabel = rightLabel;
    this.leftText = leftText;
    this.rightText = rightText;
  }
  
  /** {@inheritDoc} */
  @Override
  protected void configureShell(Shell shell) {
    super.configureShell(shell);
    if (title != null) {
      shell.setText(title);
    }
  }  
  
  /** {@inheritDoc} */
  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    // just OK button is enough
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
  }
  
  /** {@inheritDoc} */
  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    
    Composite mainComposite = new Composite(area, SWT.NONE);
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    gridLayout.makeColumnsEqualWidth = true;
    mainComposite.setLayout(gridLayout);
    
    CompareViewerPane pane = new CompareViewerPane(mainComposite, SWT.BORDER | SWT.FLAT);
    GridData data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    data.widthHint = convertWidthInCharsToPixels(120);
    data.heightHint = convertHeightInCharsToPixels(12);
    pane.setLayoutData(data);
    pane.setImage(TremaPlugin.getDefault().getImage("icons/file.gif"));
    pane.setText(title);
    
    Control previewer = createPreviewer(pane);
    pane.setContent(previewer);
    GridData gridData = new GridData(GridData.FILL_BOTH);
    previewer.setLayoutData(gridData);
    
    applyDialogFont(area);
    
    return area;
  }
  
  private Control createPreviewer(Composite parent) {
    final CompareConfiguration compareConfiguration = new CompareConfiguration();
    compareConfiguration.setLeftLabel(leftLabel);
    compareConfiguration.setLeftEditable(false);
    compareConfiguration.setRightLabel(rightLabel);
    compareConfiguration.setRightEditable(false);
    compareConfiguration.setProperty(CompareConfiguration.IGNORE_WHITESPACE, Boolean.FALSE);
    
    TextMergeViewer fViewer = new CompareResultMergeViewer(parent, SWT.NONE, compareConfiguration);
    fViewer.setInput(new DiffNode(new CompareElement(leftText), new CompareElement(rightText)));
    
    Control control = fViewer.getControl();
    control.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        if (compareConfiguration != null) {
          compareConfiguration.dispose();
        }
      }
    });
    
    return control;
  }
  
}
