
package com.netcetera.trema.eclipse;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class TremaPreferencePage extends FieldEditorPreferencePage implements
    IWorkbenchPreferencePage {

  public static final String MASTERVALUECONFLICT_RADIO_ID = "masterlanguagechangesdefault";
  public static final String MASTERVALUECONFLICT_RADIO_OPTION_FILE = "file";
  public static final String MASTERVALUECONFLICT_RADIO_OPTION_DATABASE = "db";

  public TremaPreferencePage() {
    super(GRID);
  }

  @Override
  protected void createFieldEditors() {
    addField(new RadioGroupFieldEditor(MASTERVALUECONFLICT_RADIO_ID,
        "When the master value of a text has changed on import:", 1, new String[][]{
            {"Use the master value from the imported file by default",
                MASTERVALUECONFLICT_RADIO_OPTION_FILE},
            {"Use the master value from the database by default",
                MASTERVALUECONFLICT_RADIO_OPTION_DATABASE}},
        getFieldEditorParent()));
  }

  public void init(IWorkbench workbench) {
    setPreferenceStore(TremaPlugin.getDefault().getPreferenceStore());
  }

  public static boolean getUseMasterValueFromFile() {
    IPreferenceStore prefStore = TremaPlugin.getDefault().getPreferenceStore();
    String preference = prefStore.getString(TremaPreferencePage.MASTERVALUECONFLICT_RADIO_ID);
    return !MASTERVALUECONFLICT_RADIO_OPTION_DATABASE.equals(preference);
  }

}
