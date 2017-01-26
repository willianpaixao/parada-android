package br.com.mossteam.parada.db;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.replicator.Replication;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author Willian Paixao <willian@ufpa.br>
 * @version 1.0
 */
public class SyncManager {

    private final String DB_NAME = "parada";
    private final String syncURL = "http://162.243.161.58:4984/db";
    private Database database = null;
    private Manager manager = null;
    private Context context = null;
    private URL url;

    public SyncManager(Context context) {
        this.context = context;
        manager = getManager();
        database = getDatabase();
    }

    /**
     * Creates a new Couchbase Document object
     *
     * @return A new and empty document
     */
    public Document createDocument() {
        return getDatabase().createDocument();
    }

    public Database getDatabase() {
        if ((database == null) & (manager != null)) {
            try {
                manager = getManager();
                database = manager.getDatabase(DB_NAME);
            } catch (Exception e) {
                Log.e("Couchbase", e.toString());
            }
        }
        return database;
    }

    public Document getDocument(String documentId) {
        return database.getExistingDocument(documentId);
    }

    private Manager getManager() {
        try {
            if (manager == null) {
                manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
            }
        } catch (IOException e) {
            Log.e("Couchbase", e.toString());
        }
        return manager;
    }

    /**
     *
     * @param document Document to be updated.
     * @param updatedProperties Map with new set of properties.
     */
    public void updateDocument(Document document, HashMap<String, Object> updatedProperties) {
        try {
            document.putProperties(updatedProperties);
        } catch (CouchbaseLiteException e) {
            Log.e("Couchbase", e.toString());
        }
    }

    /**
     * Pushes local Documents to the Couchbase Server using a process called
     * Replication.
     *
     * @param token Facebook's access token.
     */
    public void push(String token) {

        // Check for internet connection
        if(!isConnected()) {
            return;
        }

        try {
            url = new URL(syncURL);
        } catch (MalformedURLException e) {
            Log.e("Couchbase", e.toString());
        }
        final Replication push = getDatabase().createPushReplication(url);
        Authenticator auth = AuthenticatorFactory.createFacebookAuthenticator(token);
        push.setAuthenticator(auth);
        push.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                Log.d("Couchbase", event.toString());
            }
        });
        Log.i("Couchbase", "Starting push replication.");
        push.start();

        final ProgressDialog progressDialog = ProgressDialog.show(context, "Please wait...", "Syncing", false);
        push.addChangeListener(new Replication.ChangeListener() {
            @Override
            public void changed(Replication.ChangeEvent event) {
                boolean active = push.getStatus() == Replication.ReplicationStatus.REPLICATION_ACTIVE;
                if(!active) {
                    progressDialog.dismiss();
                } else {
                    progressDialog.setMax(push.getCompletedChangesCount());
                    progressDialog.setProgress(push.getChangesCount());
                }
            }
        });
    }

    /**
     * Permanently deletes a database's file and all its attachments.
     * After calling this method Database reference is null.
     */
    public void deleteDatabase() {
        database = getDatabase();
        try {
            database.delete();
            database = null;
        } catch (CouchbaseLiteException e) {
            Log.e("Couchbase", e.toString());
        }
        Log.i("Couchbase", "Local database deleted.");
    }

    /*public void assignOwnerToListsIfNeeded(String userId) {
        Query query = getDatabase().createAllDocumentsQuery();
        QueryEnumerator queryEnumerator = null;
        try {
            queryEnumerator = query.run();
            if (queryEnumerator == null)
                return;
            while (queryEnumerator.hasNext()) {
                Document document = queryEnumerator.next().getDocument();
                String owner = (String) document.getProperty("owner");
                if (owner != null)
                    continue;
                HashMap<String, Object> properties = new HashMap<String, Object>();
                properties.putAll(document.getProperties());
                properties.put("owner", userId);
                document.putProperties(properties);
            }
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }*/

    /**
     *
     * @return true if there is internet connection
     */
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null &&
                info.isConnectedOrConnecting();
    }
}
