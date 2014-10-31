package com.netcetera.trema.eclipse.editors;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.dialogs.PatternFilter;

import com.netcetera.trema.core.XMLTextNode;
import com.netcetera.trema.core.XMLValueNode;


/**
 * Handles matching of the filtertext in the trema text and value nodes.
 */
public class TremaTreePatternFilter extends PatternFilter {
  
  @Override
  protected boolean isLeafMatch(Viewer viewer, Object element){
    if (element instanceof XMLTextNode){
      XMLTextNode node = (XMLTextNode)element;
      return wordMatches(node.getKey()+" "+node.getContext());  
    } else if (element instanceof XMLValueNode){
      // if the parent node matches, the children also match. This allows to actually see/expand them in the tree.
      // wouldn't make sense to find the parent and then not be able to expand to see what the children are
      XMLValueNode node = (XMLValueNode)element;
      if (wordMatches(node.getParent().getKey()+" "+node.getParent().getContext())){
        return true;
      }
      if (wordMatches(node.getValue()+" "+node.getStatus().getName()+" "+node.getLanguage())){
        return true;
      }          
    }
    return false;
  }
}
