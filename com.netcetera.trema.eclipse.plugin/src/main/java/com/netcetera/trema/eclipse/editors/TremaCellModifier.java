package com.netcetera.trema.eclipse.editors;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.Item;

import com.netcetera.trema.core.Status;
import com.netcetera.trema.core.api.IDatabase;
import com.netcetera.trema.core.api.ITextNode;
import com.netcetera.trema.core.api.IValueNode;
import com.netcetera.trema.eclipse.TremaUtilEclipse;



/**
 * The cell modifier for the Trema editor.
 */
public class TremaCellModifier implements ICellModifier {

  /** {@inheritDoc} */
  public boolean canModify(Object element, String property) {
    if (property == TremaEditor.PROP_SECOND_COLUMN) {
      return true;
    } else if (property == TremaEditor.PROP_THIRD_COLUMN) {
      if (element instanceof IValueNode) {
        return true;
      }
    } else if (property == TremaEditor.PROP_FIRST_COLUMN) {
      if (element instanceof ITextNode) {
        return true; 
      }
    }
    return false;
  }

  /** {@inheritDoc} */
  public Object getValue(Object element, String property) {
    if (property == TremaEditor.PROP_SECOND_COLUMN) {
      if (element instanceof IDatabase) {
        return ((IDatabase) element).getMasterLanguage();
      }
      if (element instanceof ITextNode) {
        return ((ITextNode) element).getContext();
      }
      if (element instanceof IValueNode) {
        return TremaUtilEclipse.makeLineEndingsVisible(((IValueNode) element).getValue(), true);
      }
    } else if (property == TremaEditor.PROP_THIRD_COLUMN) {
      if (element instanceof IValueNode) {
        Status status = ((IValueNode) element).getStatus();
        return Integer.valueOf(status.getPosition());
      }
    } else if (property == TremaEditor.PROP_FIRST_COLUMN) {
      if (element instanceof ITextNode) {
        return ((ITextNode) element).getKey();
      }
    }
    return null;
  }

  /** {@inheritDoc} */
  public void modify(Object element, String property, Object value) {
    // null indicates that the validator rejected the value
    Object elem = element;
    if (value == null) {
      return;
    }
    
    if (elem instanceof Item) {
      elem = ((Item) elem).getData();
    }
        
    if (property == TremaEditor.PROP_SECOND_COLUMN) {
      String textValue = (String) value;
      if (elem instanceof IDatabase) {
        ((IDatabase) elem).setMasterLanguage(textValue);
      } else if (elem instanceof ITextNode) {
        ((ITextNode) elem).setContext(textValue);
      } else if (elem instanceof IValueNode) {
        ((IValueNode) elem).setValue(TremaUtilEclipse.makeLineEndingsVisible(textValue, false));
      }
    } else if (property == TremaEditor.PROP_THIRD_COLUMN) {
      if (elem instanceof IValueNode) {
        IValueNode valueNode = (IValueNode) elem;
        valueNode.setStatus(Status.valueOf((Integer) value)); 
      }
    } else if (property == TremaEditor.PROP_FIRST_COLUMN) {
      if (elem instanceof ITextNode) {
        ((ITextNode) elem).setKey((String)value);
      }
    }
  }
  
}
