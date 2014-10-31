
package com.netcetera.trema.eclipse;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;


public class TremaPreferenceInitializer extends AbstractPreferenceInitializer {

  @Override
  public void initializeDefaultPreferences() {
    IEclipsePreferences node = DefaultScope.INSTANCE.getNode("com.netcetera.trema.eclipse");
    node.put(TremaPreferencePage.MASTERVALUECONFLICT_RADIO_ID, TremaPreferencePage.MASTERVALUECONFLICT_RADIO_OPTION_FILE);
  }

}
