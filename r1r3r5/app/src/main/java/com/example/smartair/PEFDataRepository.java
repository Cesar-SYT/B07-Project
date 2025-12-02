package com.example.smartair;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository for handling PEF data operations (Firebase storage & Parent Config retrieval).
 */
public class PEFDataRepository {

    private static PEFDataRepository instance;
    private final DatabaseReference rootRef;

    private PEFDataRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference("users");
    }

    public static synchronized PEFDataRepository getInstance() {
        if (instance == null) {
            instance = new PEFDataRepository();
        }
        return instance;
    }

    // ... (existing methods)
    public void savePEFLogToFirebase(String childKey, PEFLogModel pefLog, OnSaveListener listener) {
        if (childKey == null || childKey.isEmpty()) {
            if (listener != null) listener.onFailure("Invalid Child Key");
            return;
        }

        DatabaseReference logsRef = rootRef.child(childKey).child("pef_logs");
        String pushId = logsRef.push().getKey();
        
        if (pushId != null) {
            pefLog.setLogId(pushId);
            logsRef.child(pushId).setValue(pefLog)
                    .addOnSuccessListener(aVoid -> {
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
        } else {
            if (listener != null) listener.onFailure("Failed to generate ID");
        }
    }

    public void saveParentConfiguredPB(String childKey, float pbValue, OnSaveListener listener) {
        if (childKey == null || childKey.isEmpty()) {
            if (listener != null) listener.onFailure("Invalid Child Key");
            return;
        }

        rootRef.child(childKey).child("personal_best_pef").setValue(pbValue)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    public void fetchParentConfiguredPB(String childKey, OnPBFetchListener listener) {
        if (childKey == null || childKey.isEmpty()) {
            listener.onFailure("Invalid User ID");
            return;
        }

        rootRef.child(childKey).child("personal_best_pef").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float val = snapshot.getValue(Float.class);
                    if (val != null) {
                        listener.onSuccess(val);
                    } else {
                        Integer intVal = snapshot.getValue(Integer.class);
                        if (intVal != null) {
                            listener.onSuccess(intVal.floatValue());
                        } else {
                            listener.onSuccess(0f);
                        }
                    }
                } else {
                    listener.onSuccess(0f);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void fetchPEFLogs(String childKey, OnLogsFetchListener listener) {
        rootRef.child(childKey).child("pef_logs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<PEFLogModel> logs = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    PEFLogModel log = child.getValue(PEFLogModel.class);
                    if (log != null) {
                        logs.add(log);
                    }
                }
                Collections.sort(logs, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                listener.onLogsFetched(logs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    /**
     * Fetches only the most recent PEF log for a child.
     */
    public void fetchLatestPEFLog(String childKey, OnLatestLogFetchListener listener) {
        if (childKey == null || childKey.isEmpty()) {
            listener.onFailure("Invalid Child Key");
            return;
        }

        Query latestLogQuery = rootRef.child(childKey).child("pef_logs").orderByChild("timestamp").limitToLast(1);
        latestLogQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // The result is a map with one entry, so iterate to get it
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        PEFLogModel latestLog = childSnapshot.getValue(PEFLogModel.class);
                        listener.onSuccess(latestLog);
                        return; // Exit after first one
                    }
                }
                // No log found
                listener.onSuccess(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void saveSharingSettings(String childKey, SharingSettings settings, OnSaveListener listener) {
        if (childKey == null || childKey.isEmpty()) {
            if (listener != null) listener.onFailure("Invalid Child Key");
            return;
        }
        rootRef.child(childKey).child("sharing_settings").setValue(settings)
                .addOnSuccessListener(aVoid -> {
                    if (listener != null) listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (listener != null) listener.onFailure(e.getMessage());
                });
    }

    public void fetchSharingSettings(String childKey, OnSharingSettingsFetchListener listener) {
        if (childKey == null || childKey.isEmpty()) {
            listener.onFailure("Invalid Child Key");
            return;
        }
        rootRef.child(childKey).child("sharing_settings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    SharingSettings settings = snapshot.getValue(SharingSettings.class);
                    if (settings != null) {
                        listener.onSuccess(settings);
                    } else {
                        listener.onSuccess(new SharingSettings()); // Return default if exists but is null/malformed
                    }
                } else {
                    listener.onSuccess(new SharingSettings()); // Return default if not set yet
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    // --- Interfaces ---
    public interface OnSaveListener {
        void onSuccess();
        void onFailure(String error);
    }
    public interface OnPBFetchListener {
        void onSuccess(float pbValue);
        void onFailure(String errorMsg);
    }
    public interface OnLogsFetchListener {
        void onLogsFetched(List<PEFLogModel> logs);
        void onFailure(String error);
    }
    public interface OnSharingSettingsFetchListener {
        void onSuccess(SharingSettings settings);
        void onFailure(String error);
    }
    public interface OnLatestLogFetchListener {
        void onSuccess(PEFLogModel latestLog);
        void onFailure(String error);
    }
}
