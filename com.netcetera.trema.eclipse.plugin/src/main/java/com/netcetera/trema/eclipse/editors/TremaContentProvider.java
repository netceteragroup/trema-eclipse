package com.netcetera.trema.eclipse.editors;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.netcetera.trema.core.api.IChildNode;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.IDatabaseListener;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;



/**
 * The content provider for the <code>TableTreeViewer</code> in the
 * table tree view of the Trema editor. In addition, this class acts as
 * change listener to reflect changes to the database in the table tree.
 */
public class TremaContentProvider implements ITreeContentProvider, IDatabaseListener {
  
  private final TremaEditor tremaEditor;
  private final TreeViewer tableTreeViewer;
  
  /**
   * Creates a new instance.
   * @param tremaEditor the parent Trema editor
   */
  public TremaContentProvider(TremaEditor tremaEditor) {
    this.tremaEditor = tremaEditor;
    this.tableTreeViewer = tremaEditor.getTreeViewer();
  }

  /** {@inheritDoc} */
  public Object[] getChildren(Object parentElement) {
    if (parentElement instanceof DatabaseContainer) {
      DatabaseContainer dbContainer = (DatabaseContainer) parentElement;
      return new Object[] {dbContainer.getDatabase()};
    } else if (parentElement instanceof IDatabase) {
      IDatabase db = (IDatabase) parentElement;
      return db.getTextNodes();
    } else if (parentElement instanceof ITextNode) {
      ITextNode textNode = (ITextNode) parentElement;
      return textNode.getValueNodes();
    }
    return null;
  }
  
  /** {@inheritDoc} */
  public Object getParent(Object element) {
    if (element instanceof IChildNode) {
      return ((IChildNode<?>) element).getParent();
    }
    return null;
  }
  
  /** {@inheritDoc} */
  public boolean hasChildren(Object element) {
    Object[] children = getChildren(element);
    return children != null && children.length > 0;
  }
  
  /** {@inheritDoc} */
  public Object[] getElements(Object inputElement) {
    return getChildren(inputElement);
  }
  
  /** {@inheritDoc} */
  public void dispose() {
    // nothing to dispose of
  }
  
  /** {@inheritDoc} */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    // nothing to be done
  }
  
  /** {@inheritDoc} */
  public void masterLanguageChanged(IDatabase db) {
    tableTreeViewer.update(db, new String[] {TremaEditor.PROP_SECOND_COLUMN});
    tremaEditor.tableTreeModified();
  }
  
  /** {@inheritDoc} */
  public void textNodeChanged(IDatabase db, ITextNode textNode) {
    tableTreeViewer.update(textNode, new String[] {TremaEditor.PROP_FIRST_COLUMN, TremaEditor.PROP_SECOND_COLUMN});
    tremaEditor.tableTreeModified();
  }
  
  /** {@inheritDoc} */
  public void textNodeAdded(IDatabase db, ITextNode textNode) {
    tableTreeViewer.refresh(db);
    tableTreeViewer.expandToLevel(textNode, 1);
    tableTreeViewer.setSelection(new StructuredSelection(textNode));
    tableTreeViewer.reveal(textNode);
    tremaEditor.tableTreeModified(true);
  }
  
  /** {@inheritDoc} */
  public void textNodesRemoved(IDatabase db, ITextNode[] textNodes, int index) {
    tableTreeViewer.refresh(db);
    // set an intuitive selection
    int dbSize = db.getSize();
    if (dbSize == 0) {
      tableTreeViewer.setSelection(new StructuredSelection(db));
    } else if (index < db.getSize()) {
      tableTreeViewer.setSelection(new StructuredSelection(db.getTextNode(index)));
    } else {
      tableTreeViewer.setSelection(new StructuredSelection(db.getTextNode(dbSize - 1)));
    }
    tremaEditor.tableTreeModified(true);
  }
  
  /** {@inheritDoc} */
  public void textNodesMoved(IDatabase db, ITextNode[] textNodes) {
    tableTreeViewer.refresh(db);
    tremaEditor.tableTreeModified();
  }
  
  /** {@inheritDoc} */
  public void valueNodeChanged(IValueNode valueNode) {
     tableTreeViewer.update(valueNode, null);
     tremaEditor.tableTreeModified();
  }
  
  /** {@inheritDoc} */
  public void valueNodeAdded(IValueNode valueNode) {
    tableTreeViewer.refresh(valueNode.getParent());
    tableTreeViewer.reveal(valueNode);
    tremaEditor.tableTreeModified();
  }
  
  /** {@inheritDoc} */
  public void valueNodeRemoved(IValueNode valueNode) {
    tableTreeViewer.refresh(valueNode.getParent());
    tableTreeViewer.setSelection(new StructuredSelection(valueNode.getParent()));
    tremaEditor.tableTreeModified();
  }
  
}
