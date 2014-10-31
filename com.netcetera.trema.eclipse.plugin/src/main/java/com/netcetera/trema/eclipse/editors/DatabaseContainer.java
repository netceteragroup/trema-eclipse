package com.netcetera.trema.eclipse.editors;

import com.netcetera.trema.core.api.IDatabase;



/**
 * A wrapper for an <code>IDatabase</code> used as input for the table
 * tree viewer in order to have a single <code>IDatabase</code> node
 * displayed as root.
 */
public final class DatabaseContainer {
  
  private IDatabase db = null;
  
  /**
   * Constructor.
   * 
   * @param db the database
   */
  public DatabaseContainer(IDatabase db) {
    this.db = db;
  }
  
  /**
   * Gets the database.
   * 
   * @return the database.
   */
  public IDatabase getDatabase() {
    return db;
  }
  
}
