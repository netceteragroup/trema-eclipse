package com.netcetera.trema.eclipse.editors.xmleditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;



/** 
 * The classes of this package are based on the Eclipse PDE sample XML
 * text editor project.
 */
public class XMLDocumentProvider extends FileDocumentProvider {

  /** {@inheritDoc} */
  protected IDocument createDocument(Object element) throws CoreException {
    IDocument document = super.createDocument(element);
    if (document != null) {
      IDocumentPartitioner partitioner =
        new FastPartitioner(new XMLPartitionScanner(),
            new String[] {
          XMLPartitionScanner.XML_TAG,
          XMLPartitionScanner.XML_COMMENT });
      partitioner.connect(document);
      document.setDocumentPartitioner(partitioner);
    }
    return document;
  }
}