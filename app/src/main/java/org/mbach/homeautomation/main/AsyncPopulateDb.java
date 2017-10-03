package org.mbach.homeautomation.main;

import android.content.Context;
import android.os.AsyncTask;

import org.mbach.homeautomation.db.OuiDB;

import java.lang.ref.WeakReference;

class AsyncPopulateDb extends AsyncTask<Void, Void, Void> {

    private final WeakReference<Context> weakContext;

    AsyncPopulateDb(Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        OuiDB ouiDB = new OuiDB(weakContext.get());
        ouiDB.populateFromLocalResource();
        return null;
    }
}