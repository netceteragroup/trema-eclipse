package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLConfiguration extends TextSourceViewerConfiguration {
  private XMLDoubleClickStrategy doubleClickStrategy;
  private XMLTagScanner tagScanner;
  private XMLScanner scanner;
  private ColorManager colorManager;

  /**
   * Constructor.
   * 
   * @param colorManager the color manager
   */
  public XMLConfiguration(ColorManager colorManager) {
    this.colorManager = colorManager;
  }
  /** {@inheritDoc} */
  public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
    return new String[] {
        IDocument.DEFAULT_CONTENT_TYPE,
        XMLPartitionScanner.XML_COMMENT,
        XMLPartitionScanner.XML_TAG };
  }
  /** {@inheritDoc} */
  public ITextDoubleClickStrategy getDoubleClickStrategy(
      ISourceViewer sourceViewer,
      String contentType) {
    if (doubleClickStrategy == null) {
      doubleClickStrategy = new XMLDoubleClickStrategy();
    }
    return doubleClickStrategy;
  }

  /**
   * Gets the XML Scanner.
   * 
   * @return XMLScanner the xml scanner
   */
  protected XMLScanner getXMLScanner() {
    if (scanner == null) {
      scanner = new XMLScanner(colorManager);
      scanner.setDefaultReturnToken(new Token(
          new TextAttribute(
              colorManager.getColor(IXMLColorConstants.DEFAULT))));
    }
    return scanner;
  }
  /**
   * Gets the XMLTagScanner.
   * 
   * @return XMLTagScanner the xml tag scanner
   */
  protected XMLTagScanner getXMLTagScanner() {
    if (tagScanner == null) {
      tagScanner = new XMLTagScanner(colorManager);
      tagScanner.setDefaultReturnToken(
          new Token(
              new TextAttribute(
                  colorManager.getColor(IXMLColorConstants.TAG))));
    }
    return tagScanner;
  }

  /** {@inheritDoc} */
  public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
    PresentationReconciler reconciler = new PresentationReconciler();

    DefaultDamagerRepairer dr =
      new DefaultDamagerRepairer(getXMLTagScanner());
    reconciler.setDamager(dr, XMLPartitionScanner.XML_TAG);
    reconciler.setRepairer(dr, XMLPartitionScanner.XML_TAG);

    dr = new DefaultDamagerRepairer(getXMLScanner());
    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

    NonRuleBasedDamagerRepairer ndr =
      new NonRuleBasedDamagerRepairer(
          new TextAttribute(
              colorManager.getColor(IXMLColorConstants.XML_COMMENT)));
    reconciler.setDamager(ndr, XMLPartitionScanner.XML_COMMENT);
    reconciler.setRepairer(ndr, XMLPartitionScanner.XML_COMMENT);

    return reconciler;
  }

}