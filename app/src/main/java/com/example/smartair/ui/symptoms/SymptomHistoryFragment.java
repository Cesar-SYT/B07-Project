package com.example.smartair.ui.symptoms;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartair.R;
import com.example.smartair.ui.r5model.SymptomEntry;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.LongConsumer;
import android.graphics.pdf.PdfDocument;

public class SymptomHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private SymptomHistoryAdapter adapter;
    private DatabaseReference db;
    private FirebaseAuth auth;
    private Button btnFilter;
    private ExtendedFloatingActionButton fabExport;
    List<SymptomEntry> fullHistoryList;
    List<SymptomEntry> currentHistoryList;

    public SymptomHistoryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.symptom_history_fragment, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_history);
        btnFilter = view.findViewById(R.id.button_filter);
        fabExport = view.findViewById(R.id.fab_export);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference("Checkins");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SymptomHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        fullHistoryList = new ArrayList<>();

        // load full history from database
        String userId = auth.getCurrentUser().getUid();
        db.child(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    fullHistoryList.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        SymptomEntry entry = child.getValue(SymptomEntry.class);
                        if (entry != null) {
                            fullHistoryList.add(entry);
                        }
                    }

                    // sort the symptom entries with respect to timestamp
                    fullHistoryList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

                    // initialize currentHistoryList, for the case
                    // where the user does not choose any filter
                    currentHistoryList = new ArrayList<>(fullHistoryList);

                    adapter.updateList(fullHistoryList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        btnFilter.setOnClickListener(v ->
                openFilters());

        fabExport.setOnClickListener(v ->
                openExportSheet());

        return view;
    }

    private void openFilters() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View filterView = getLayoutInflater().inflate(
                R.layout.symptom_history_filter_sheet,
                null
        );

        dialog.setContentView(filterView);
        dialog.show();

        // folding and unfolding
        TextView title_symptom = filterView.findViewById(R.id.title_symptom);
        LinearLayout container_symptom = filterView.findViewById(R.id.container_symptom);

        TextView title_trigger = filterView.findViewById(R.id.title_trigger);
        LinearLayout container_trigger = filterView.findViewById(R.id.container_trigger);

        TextView title_date = filterView.findViewById(R.id.title_date);
        LinearLayout container_date = filterView.findViewById(R.id.container_date);

        TextView title_author = filterView.findViewById(R.id.title_author);
        LinearLayout container_author = filterView.findViewById(R.id.container_author);

        toggleSection(title_symptom, container_symptom);
        toggleSection(title_trigger, container_trigger);
        toggleSection(title_date, container_date);
        toggleSection(title_author, container_author);

        // find views inside the filter sheet
        CheckBox cbSleep = filterView.findViewById(R.id.filter_symptom_sleep);
        CheckBox cbActivity = filterView.findViewById(R.id.filter_symptom_activity);
        CheckBox cbCough = filterView.findViewById(R.id.filter_symptom_cough);

        CheckBox cbChild = filterView.findViewById(R.id.filter_author_child);
        CheckBox cbParent = filterView.findViewById(R.id.filter_author_parent);

        Button btnApply = filterView.findViewById(R.id.button_apply_filters);

        CheckBox cbExercise = filterView.findViewById(R.id.filter_trigger_exercise);
        CheckBox cbColdAir = filterView.findViewById(R.id.filter_trigger_coldair);
        CheckBox cbPets = filterView.findViewById(R.id.filter_trigger_pets);
        CheckBox cbSmoke = filterView.findViewById(R.id.filter_trigger_smoke);
        CheckBox cbIllness = filterView.findViewById(R.id.filter_trigger_illness);
        CheckBox cbPerfume = filterView.findViewById(R.id.filter_trigger_perfume);

        Button btnStart = filterView.findViewById(R.id.button_pick_start_date);
        Button btnEnd = filterView.findViewById(R.id.button_pick_end_date);

        final long[] startDate = {Long.MIN_VALUE};
        final long[] endDate = {Long.MAX_VALUE};

        btnStart.setOnClickListener(v_2 -> pickDate(date -> {
            startDate[0] = date;
            btnStart.setText("Start: " + formatDate(date));
        }));

        btnEnd.setOnClickListener(v_2 -> pickDate(date -> {
            endDate[0] = date;
            btnEnd.setText("End: " + formatDate(date));
        }));

        // apply filters
        btnApply.setOnClickListener(v -> {

            List<SymptomEntry> filteredHistory = new ArrayList<>();

            for (SymptomEntry e : fullHistoryList) {
                boolean ok = true;
                // filter by symptoms
                if (cbSleep.isChecked()) {
                    ok &= (e.sleep != null && !e.sleep.equals("Good"));
                }
                if (cbActivity.isChecked()) {
                    ok &= (e.activity != null && !e.activity.equals("Normal"));
                }
                if (cbCough.isChecked()) {
                    ok &= (e.cough != null && !e.cough.equals("No"));
                }

                // filter by triggers
                if (cbExercise.isChecked()) ok &= e.triggers.contains("Exercise");
                if (cbColdAir.isChecked()) ok &= e.triggers.contains("Cold air");
                if (cbPets.isChecked()) ok &= e.triggers.contains("Pets");
                if (cbSmoke.isChecked()) ok &= e.triggers.contains("Smoke");
                if (cbIllness.isChecked()) ok &= e.triggers.contains("Illness");
                if (cbPerfume.isChecked()) ok &= e.triggers.contains("Perfume");

                //filter by date range
                ok &= (e.timestamp >= startDate[0] && e.timestamp <= endDate[0]);

                // filter by author
                if (cbChild.isChecked() && !cbParent.isChecked()) {
                    ok &= "child".equals(e.enteredBy);
                }
                if (cbParent.isChecked() && !cbChild.isChecked()) {
                    ok &= "parent".equals(e.enteredBy);
                }
                if (ok) {
                    filteredHistory.add(e);
                }
            }
            adapter.updateList(filteredHistory);
            currentHistoryList = filteredHistory;
            dialog.dismiss();
        });
    }

    private void toggleSection(TextView title, LinearLayout container) {
        container.setVisibility(View.GONE);

        title.setOnClickListener(v -> {
            if (container.getVisibility() == View.VISIBLE) {
                container.setVisibility(View.GONE);
                title.setText(title.getText().toString().replace("▴", "▾"));
            } else {
                container.setVisibility(View.VISIBLE);
                title.setText(title.getText().toString().replace("▾", "▴"));
            }
        });
    }

    private void pickDate(LongConsumer callback) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, day, 0, 0, 0);
                    callback.accept(c.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private String formatDate(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(new Date(millis));
    }

    private void openExportSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = getLayoutInflater().inflate(R.layout.symptom_export_sheet, null);

        dialog.setContentView(sheet);
        dialog.show();

        RadioButton rbPdf = sheet.findViewById(R.id.radio_pdf);
        RadioButton rbCsv = sheet.findViewById(R.id.radio_csv);
        Button btnExport = sheet.findViewById(R.id.button_export);

        btnExport.setOnClickListener(v -> {
            if (currentHistoryList == null || currentHistoryList.isEmpty()) {
                Toast.makeText(getContext(), "No records to export", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rbPdf.isChecked()) {
                exportPDF(currentHistoryList);
            }
            else if (rbCsv.isChecked()) {
                exportCSV(currentHistoryList);
            }

            dialog.dismiss();
        });
    }

    private void exportPDF(List<SymptomEntry> entries) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint pageNumberPaint = new Paint();

        int x = 40;
        int y = 50;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create(); // A4尺寸
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("Symptom History Report", x, y, paint);

        y += 30;
        paint.setFakeBoldText(false);

        for (SymptomEntry entry : entries) {
            if (y > 760) { // if it is too long, start at another page
                canvas.drawText("Page " + pageNumber, 290, 802, pageNumberPaint);
                pdfDocument.finishPage(page);

                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }

            canvas.drawText("Date: " + formatDate(entry.timestamp), x, y, paint);
            y += 20;
            canvas.drawText("Sleep: " + entry.sleep + ", Activity: " + entry.activity + ", Cough: " + entry.cough, x, y, paint);
            y += 20;
            canvas.drawText("Triggers: " + String.join(", ", entry.triggers), x, y, paint);
            y += 20;
            canvas.drawText("Entered By: " + entry.enteredBy, x, y, paint);
            y += 30;
        }

        pdfDocument.finishPage(page);

        // save to file
        String filename = "symptom_report_" + System.currentTimeMillis() + ".pdf";

        try {
            // API 29+: save to Download
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/");

                Uri uri = requireContext().getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                pdfDocument.writeTo(os);
                os.close();
                Toast.makeText(getContext(), "PDF saved to Download folder", Toast.LENGTH_LONG).show();
            }
            // API 24–28: save to app external directory
            else {
                File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, filename);

                FileOutputStream fos = new FileOutputStream(file);
                pdfDocument.writeTo(fos);
                fos.close();

                Toast.makeText(getContext(),
                        "PDF Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "PDF export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }

    private void exportCSV(List<SymptomEntry> entries) {

        String filename = "symptom_report_" + System.currentTimeMillis() + ".csv";
        StringBuilder sb = new StringBuilder();

        // CSV Header
        sb.append("Date,Sleep,Activity,Cough,Triggers,EnteredBy\n");

        // Data rows
        for (SymptomEntry e : entries) {

            String date = formatDate(e.timestamp);
            String sleep = e.sleep == null ? "" : e.sleep;
            String activity = e.activity == null ? "" : e.activity;
            String cough = e.cough == null ? "" : e.cough;
            String enteredBy = e.enteredBy == null ? "" : e.enteredBy;

            String triggers;
            if (e.triggers == null || e.triggers.isEmpty()) {
                triggers = "";
            } else {
                // triggers are separated by ";"
                triggers = TextUtils.join(";", e.triggers);
            }

            sb.append(date).append(",")
                    .append("\"").append(sleep).append("\"").append(",")
                    .append("\"").append(activity).append("\"").append(",")
                    .append("\"").append(cough).append("\"").append(",")
                    .append("\"").append(triggers).append("\"").append(",")
                    .append(enteredBy).append("\n");
        }

        try {
            // API 29+: save to Download
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = requireContext().getContentResolver()
                        .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                os.write(sb.toString().getBytes());
                os.close();

                Toast.makeText(getContext(), "CSV saved to Download folder", Toast.LENGTH_LONG).show();
            }
            // API 24–28: save to app external directory
            else {
                File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, filename);

                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                fos.close();

                Toast.makeText(getContext(),
                        "CSV saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "CSV export failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}