package com.example.smartair;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.model.Child;
import com.example.smartair.model.UserRole;
import com.example.smartair.r3.SimpleMedicineLog;
import com.example.smartair.r5model.SymptomEntry;
import com.example.smartair.views.SimpleLineChart;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ProviderHomeActivity extends AppCompatActivity {

    private Spinner spinnerPatientSelector;
    private TextView txtProviderId; 

    private View cardRescueOverviewProvider;
    private View cardControllerOverviewProvider;
    private View cardSymptomsTriggersProvider;
    private View cardZoneDistributionProvider;

    private TextView txtSymptomsSummaryProvider;
    private TextView txtTopTriggersProvider;
    private TextView txtEventSample1;
    private SimpleLineChart viewRescueChart;

    private Button btnExportReportProvider;
    private Button btnSignout;

    private DatabaseReference usersRef;
    private final List<Child> patientList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private final List<String> patientNames = new ArrayList<>();

    private String currentProviderId;
    private String currentChildName = "Unknown Child";

    // Lists to hold data for export
    private final List<SimpleMedicineLog> exportMedicineLogs = new ArrayList<>();
    private final List<SymptomEntry> exportSymptomLogs = new ArrayList<>();
    private final List<PEFLogModel> exportPEFLogs = new ArrayList<>();
    private final List<Map<String, Object>> exportTriageLogs = new ArrayList<>();
    private int controllerDosesLast30Days = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_page);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null) {
            currentProviderId = user.getEmail().replace(".", ","); 
        } else {
            currentProviderId = "unknown_provider";
        }

        initViews();
        setupPatientSpinner();
        loadPatients(); 
        setupClickListeners();
    }

    private void initViews() {
        spinnerPatientSelector = findViewById(R.id.spinnerPatientSelector);
        txtProviderId = findViewById(R.id.txtProviderId);

        if (txtProviderId != null) {
            txtProviderId.setText("My Provider ID: " + currentProviderId);
        }

        cardRescueOverviewProvider = findViewById(R.id.cardRescueOverviewProvider);
        cardControllerOverviewProvider = findViewById(R.id.cardControllerOverviewProvider);
        cardSymptomsTriggersProvider = findViewById(R.id.cardSymptomsTriggersProvider);
        cardZoneDistributionProvider = findViewById(R.id.cardZoneDistributionProvider);

        txtSymptomsSummaryProvider = findViewById(R.id.txtSymptomsSummaryProvider);
        txtTopTriggersProvider = findViewById(R.id.txtTopTriggersProvider);
        txtEventSample1 = findViewById(R.id.txtEventSample1);
        viewRescueChart = findViewById(R.id.viewRescueChart);

        btnExportReportProvider = findViewById(R.id.btnExportReportProvider);
        btnSignout = findViewById(R.id.btnProviderSignOut);

        btnSignout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProviderHomeActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setupPatientSpinner() {
        spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                patientNames
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatientSelector.setAdapter(spinnerAdapter);
    }

    private void loadPatients() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patientList.clear();
                patientNames.clear();
                final List<String> keys = new ArrayList<>();

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Child child = childSnapshot.getValue(Child.class);
                    
                    if (child != null && child.getRole() == UserRole.CHILD) {
                        if (currentProviderId != null && currentProviderId.equals(child.getProviderId())) {
                             patientList.add(child);
                             patientNames.add(child.getDisplayName());
                             keys.add(childSnapshot.getKey());
                        }
                    }
                }
                
                spinnerAdapter.notifyDataSetChanged();
                
                if (patientList.isEmpty()) {
                    Toast.makeText(ProviderHomeActivity.this, "No patients linked to your ID.", Toast.LENGTH_LONG).show();
                } else {
                    // Load first patient data by default
                    loadPatientData(keys.get(0), patientList.get(0).getDisplayName());
                }
                
                spinnerPatientSelector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                         if (position >= 0 && position < keys.size()) {
                             loadPatientData(keys.get(position), patientList.get(position).getDisplayName());
                         }
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProviderHomeActivity.this, "Failed to load patients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPatientData(String childKey, String childName) {
        this.currentChildName = childName;
        // Clear previous export data
        exportMedicineLogs.clear();
        exportSymptomLogs.clear();
        exportPEFLogs.clear();
        exportTriageLogs.clear();
        controllerDosesLast30Days = 0;

        PEFDataRepository.getInstance().fetchSharingSettings(childKey, new PEFDataRepository.OnSharingSettingsFetchListener() {
            @Override
            public void onSuccess(SharingSettings settings) {
                updateUIBasedOnSettings(childKey, settings);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ProviderHomeActivity.this, "Error loading settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIBasedOnSettings(String childKey, SharingSettings settings) {
        // Rescue Logs (and calculate Controller Adherence)
        if (settings.isShareRescueLogs() || settings.isShareAdherence()) {
            if (settings.isShareRescueLogs()) cardRescueOverviewProvider.setVisibility(View.VISIBLE);
            if (settings.isShareAdherence()) cardControllerOverviewProvider.setVisibility(View.VISIBLE);
            loadRescueData(childKey, settings.isShareAdherence());
        } else {
            cardRescueOverviewProvider.setVisibility(View.GONE);
            cardControllerOverviewProvider.setVisibility(View.GONE);
        }

        // Symptoms & Triggers
        if (settings.isShareSymptoms() || settings.isShareTriggers()) {
            cardSymptomsTriggersProvider.setVisibility(View.VISIBLE);
            loadSymptomsAndTriggers(childKey, settings.isShareSymptoms(), settings.isShareTriggers());
        } else {
            cardSymptomsTriggersProvider.setVisibility(View.GONE);
        }
        
        // Zone Distribution (PEF)
        if (settings.isSharePEF()) {
            cardZoneDistributionProvider.setVisibility(View.VISIBLE);
            loadPEFData(childKey);
        } else {
            cardZoneDistributionProvider.setVisibility(View.GONE);
        }

        // Triage (if shared, load logs)
        if (settings.isShareTriage()) {
            loadTriageData(childKey);
        }
    }

    private void loadRescueData(String childKey, boolean isShareAdherence) {
        usersRef.child(childKey).child("medicine_logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int rescueCount = 0;
                long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
                // Show 30 days chart for provider
                long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 3600 * 1000);
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                List<SimpleLineChart.DataPoint> chartPoints = new ArrayList<>();

                exportMedicineLogs.clear(); // Ensure fresh list

                for (DataSnapshot child : snapshot.getChildren()) {
                    SimpleMedicineLog log = child.getValue(SimpleMedicineLog.class);
                    if (log != null) {
                        try {
                            Date date = sdf.parse(log.getTime());
                            if (date != null) {
                                if ("RESCUE".equalsIgnoreCase(log.getType())) {
                                    exportMedicineLogs.add(log); // Add to export list
                                    if (date.getTime() > oneWeekAgo) {
                                        rescueCount += log.getDoseCount();
                                    }
                                    if (date.getTime() > thirtyDaysAgo) {
                                        chartPoints.add(new SimpleLineChart.DataPoint(date.getTime(), log.getDoseCount()));
                                    }
                                } else if ("CONTROLLER".equalsIgnoreCase(log.getType())) {
                                    if (date.getTime() > thirtyDaysAgo) {
                                        controllerDosesLast30Days++;
                                    }
                                }
                            }
                        } catch (Exception e) {}
                    }
                }
                // Sort logs for report (Newest first)
                Collections.sort(exportMedicineLogs, (o1, o2) -> o2.getTime().compareTo(o1.getTime()));
                
                // Update text
                txtEventSample1.setText("Rescue use: " + rescueCount + " puffs this week.");
                
                if (isShareAdherence) {
                     TextView txtAdherence = cardControllerOverviewProvider.findViewById(R.id.txtAdherenceSummary); 
                     // FIX: Using R.id.txtAdherenceSummary which I just added to the layout
                     
                     if (txtAdherence != null) {
                         txtAdherence.setText("Controller: " + controllerDosesLast30Days + " doses in last 30 days.");
                     }
                }

                // Update Chart
                Collections.sort(chartPoints);
                viewRescueChart.setData(chartPoints);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPEFData(String childKey) {
        PEFDataRepository.getInstance().fetchPEFLogs(childKey, new PEFDataRepository.OnLogsFetchListener() {
            @Override
            public void onLogsFetched(List<PEFLogModel> logs) {
                exportPEFLogs.clear();
                exportPEFLogs.addAll(logs);
                // UI Update for Zone Distribution could go here
            }

            @Override
            public void onFailure(String error) {}
        });
    }

    private void loadTriageData(String childKey) {
        usersRef.child(childKey).child("triage_logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                exportTriageLogs.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Map<String, Object> log = (Map<String, Object>) child.getValue();
                    if (log != null) {
                        exportTriageLogs.add(log);
                    }
                }
                // Sort descending
                Collections.sort(exportTriageLogs, (o1, o2) -> {
                    Long t1 = (Long) o1.get("timestamp");
                    Long t2 = (Long) o2.get("timestamp");
                    return t2.compareTo(t1);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadSymptomsAndTriggers(String childKey, boolean shareSymptoms, boolean shareTriggers) {
        usersRef.child(childKey).child("symptomCheckins").limitToLast(50).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int symptomCount = 0;
                Set<String> triggers = new HashSet<>();
                long twoWeeksAgo = System.currentTimeMillis() - (14L * 24 * 3600 * 1000);
                
                exportSymptomLogs.clear(); // Ensure fresh list

                for (DataSnapshot child : snapshot.getChildren()) {
                    SymptomEntry entry = child.getValue(SymptomEntry.class);
                    if (entry != null) {
                        exportSymptomLogs.add(entry); // Add to export list
                        
                        if (entry.timestamp > twoWeeksAgo) {
                            symptomCount++;
                            if (entry.triggers != null) triggers.addAll(entry.triggers);
                        }
                    }
                }
                // Sort by timestamp descending
                Collections.sort(exportSymptomLogs, (o1, o2) -> Long.compare(o2.timestamp, o1.timestamp));

                if (shareSymptoms) {
                    txtSymptomsSummaryProvider.setText("Symptom entries: " + symptomCount + " in last 14 days");
                } else {
                    txtSymptomsSummaryProvider.setText("Symptoms not shared.");
                }

                if (shareTriggers) {
                    String t = triggers.isEmpty() ? "None" : String.join(", ", triggers);
                    txtTopTriggersProvider.setText("Recent triggers: " + t);
                } else {
                    txtTopTriggersProvider.setText("Triggers not shared.");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupClickListeners() {
        btnExportReportProvider.setOnClickListener(v -> showExportDialog());
    }

    private void showExportDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.symptom_export_sheet, null); // Reusing existing layout

        dialog.setContentView(sheet);
        dialog.show();

        RadioButton rbPdf = sheet.findViewById(R.id.radio_pdf);
        RadioButton rbCsv = sheet.findViewById(R.id.radio_csv);
        Button btnExport = sheet.findViewById(R.id.button_export);

        btnExport.setOnClickListener(v -> {
            if (exportMedicineLogs.isEmpty() && exportSymptomLogs.isEmpty() && exportPEFLogs.isEmpty() && exportTriageLogs.isEmpty()) {
                Toast.makeText(this, "No data available to export", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rbPdf.isChecked()) {
                exportPDF();
            } else if (rbCsv.isChecked()) {
                exportCSV();
            }
            dialog.dismiss();
        });
    }

    private void exportPDF() {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        int x = 40;
        int y = 50;
        int pageNumber = 1;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        titlePaint.setTextSize(18);
        titlePaint.setFakeBoldText(true);
        paint.setTextSize(12);

        // Title
        canvas.drawText("Patient Health Report: " + currentChildName, x, y, titlePaint);
        y += 40;

        // Section: Rescue Inhaler Chart
        titlePaint.setTextSize(14);
        canvas.drawText("Rescue Inhaler Trends (Last 30 Days)", x, y, titlePaint);
        y += 20;
        
        // Draw Chart Area
        Paint chartPaint = new Paint();
        chartPaint.setStyle(Paint.Style.STROKE);
        chartPaint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + 500, y + 150, chartPaint);
        
        if (!exportMedicineLogs.isEmpty()) {
            drawChartOnCanvas(canvas, x, y, 500, 150);
        } else {
            canvas.drawText("No data for chart.", x + 10, y + 75, paint);
        }
        
        y += 170;

        // Section: Rescue Inhaler Logs
        canvas.drawText("Rescue Inhaler Logs (Recent)", x, y, titlePaint);
        y += 25;
        
        if (exportMedicineLogs.isEmpty()) {
            canvas.drawText("No rescue logs shared or available.", x, y, paint);
            y += 20;
        } else {
            for (SimpleMedicineLog log : exportMedicineLogs) {
                 if (y > 780) { // New page if full
                     pdfDocument.finishPage(page);
                     pageNumber++;
                     pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                     page = pdfDocument.startPage(pageInfo);
                     canvas = page.getCanvas();
                     y = 50;
                 }
                 String line = "Date: " + log.getTime() + " | Doses: " + log.getDoseCount() + " | Feel: " + log.getFeel();
                 canvas.drawText(line, x, y, paint);
                 y += 20;
            }
        }
        y += 20;
        
        // Section: Controller Adherence
        canvas.drawText("Controller Adherence Summary", x, y, titlePaint);
        y += 25;
        canvas.drawText("Controller doses in last 30 days: " + controllerDosesLast30Days, x, y, paint);
        y += 20;

        // Section: Symptoms
        canvas.drawText("Symptoms & Triggers", x, y, titlePaint);
        y += 25;

        if (exportSymptomLogs.isEmpty()) {
            canvas.drawText("No symptom logs shared or available.", x, y, paint);
            y += 20;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            for (SymptomEntry entry : exportSymptomLogs) {
                if (y > 780) {
                     pdfDocument.finishPage(page);
                     pageNumber++;
                     pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                     page = pdfDocument.startPage(pageInfo);
                     canvas = page.getCanvas();
                     y = 50;
                }
                String date = sdf.format(new Date(entry.timestamp));
                String triggers = (entry.triggers == null || entry.triggers.isEmpty()) ? "None" : TextUtils.join(", ", entry.triggers);
                
                canvas.drawText("Date: " + date, x, y, paint);
                y += 15;
                canvas.drawText("   Sleep: " + entry.sleep + ", Activity: " + entry.activity + ", Cough: " + entry.cough, x, y, paint);
                y += 15;
                canvas.drawText("   Triggers: " + triggers, x, y, paint);
                y += 25;
            }
        }
        
        // Section: PEF Logs
        if (y > 760) {
             pdfDocument.finishPage(page);
             pageNumber++;
             pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
             page = pdfDocument.startPage(pageInfo);
             canvas = page.getCanvas();
             y = 50;
        }
        canvas.drawText("Peak Flow (PEF) & Zones", x, y, titlePaint);
        y += 25;
        if (exportPEFLogs.isEmpty()) {
            canvas.drawText("No PEF logs shared or available.", x, y, paint);
            y += 20;
        } else {
            for (PEFLogModel pef : exportPEFLogs) {
                if (y > 780) {
                     pdfDocument.finishPage(page);
                     pageNumber++;
                     pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                     page = pdfDocument.startPage(pageInfo);
                     canvas = page.getCanvas();
                     y = 50;
                }
                String val = pef.isComparison() ? "Pre: " + pef.getPreValue() + ", Post: " + pef.getPostValue() : "Value: " + pef.getPreValue();
                canvas.drawText(pef.getDateTime() + " | " + val + " | Zone: " + pef.getZone(), x, y, paint);
                y += 20;
            }
        }
        
        // Section: Triage Incidents
        if (y > 760) {
             pdfDocument.finishPage(page);
             pageNumber++;
             pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
             page = pdfDocument.startPage(pageInfo);
             canvas = page.getCanvas();
             y = 50;
        }
        canvas.drawText("Triage Incidents", x, y, titlePaint);
        y += 25;
        if (exportTriageLogs.isEmpty()) {
            canvas.drawText("No triage incidents shared or recorded.", x, y, paint);
            y += 20;
        } else {
             SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
             for (Map<String, Object> log : exportTriageLogs) {
                if (y > 780) {
                     pdfDocument.finishPage(page);
                     pageNumber++;
                     pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                     page = pdfDocument.startPage(pageInfo);
                     canvas = page.getCanvas();
                     y = 50;
                }
                long ts = (Long) log.get("timestamp");
                String action = (String) log.get("action");
                canvas.drawText(sdf.format(new Date(ts)) + " | " + action, x, y, paint);
                y += 20;
             }
        }

        pdfDocument.finishPage(page);
        savePdfFile(pdfDocument);
    }
    
    private void drawChartOnCanvas(Canvas canvas, int x, int y, int w, int h) {
        List<SimpleLineChart.DataPoint> points = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 3600 * 1000);

        for (SimpleMedicineLog log : exportMedicineLogs) {
            try {
                Date d = sdf.parse(log.getTime());
                if (d != null && d.getTime() > thirtyDaysAgo) {
                    points.add(new SimpleLineChart.DataPoint(d.getTime(), log.getDoseCount()));
                }
            } catch (Exception e) {}
        }
        Collections.sort(points);
        
        if (points.isEmpty()) return;

        long minX = points.get(0).timestamp;
        long maxX = points.get(points.size() - 1).timestamp;
        if (maxX == minX) maxX = minX + 1; // Avoid div by zero
        
        float maxY = 0;
        for (SimpleLineChart.DataPoint p : points) if (p.value > maxY) maxY = p.value;
        if (maxY == 0) maxY = 5;

        Paint linePaint = new Paint();
        linePaint.setColor(android.graphics.Color.BLUE);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);
        
        Paint pointPaint = new Paint();
        pointPaint.setColor(android.graphics.Color.BLUE);
        pointPaint.setStyle(Paint.Style.FILL);

        float prevX = -1, prevY = -1;
        
        for (SimpleLineChart.DataPoint p : points) {
            float drawX = x + ((p.timestamp - minX) / (float)(maxX - minX)) * w;
            // Invert Y
            float drawY = (y + h) - (p.value / maxY) * h;
            
            canvas.drawCircle(drawX, drawY, 3, pointPaint);
            
            if (prevX != -1) {
                canvas.drawLine(prevX, prevY, drawX, drawY, linePaint);
            }
            prevX = drawX;
            prevY = drawY;
        }
    }

    private void savePdfFile(PdfDocument pdfDocument) {
        String filename = "ProviderReport_" + currentChildName + "_" + System.currentTimeMillis() + ".pdf";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                OutputStream os = getContentResolver().openOutputStream(uri);
                pdfDocument.writeTo(os);
                os.close();
                Toast.makeText(this, "PDF Saved to Downloads", Toast.LENGTH_SHORT).show();
            } else {
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                pdfDocument.writeTo(fos);
                fos.close();
                Toast.makeText(this, "PDF Saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }

    private void exportCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Report for Patient: ").append(currentChildName).append("\n\n");
        
        sb.append("--- Controller Adherence ---\n");
        sb.append("Last 30 days doses: ").append(controllerDosesLast30Days).append("\n\n");
        
        sb.append("--- Rescue Medicine Logs ---\n");
        sb.append("Date,Doses,Feel\n");
        for (SimpleMedicineLog log : exportMedicineLogs) {
             sb.append(log.getTime()).append(",")
               .append(log.getDoseCount()).append(",")
               .append(log.getFeel()).append("\n");
        }
        
        sb.append("\n--- Symptoms & Triggers ---\n");
        sb.append("Date,Sleep,Activity,Cough,Triggers\n");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        for (SymptomEntry entry : exportSymptomLogs) {
             String date = sdf.format(new Date(entry.timestamp));
             String triggers = (entry.triggers == null) ? "" : TextUtils.join(";", entry.triggers);
             sb.append(date).append(",")
               .append(entry.sleep).append(",")
               .append(entry.activity).append(",")
               .append(entry.cough).append(",")
               .append(triggers).append("\n");
        }
        
        sb.append("\n--- PEF Logs ---\n");
        sb.append("Date,Value,Zone\n");
        for (PEFLogModel pef : exportPEFLogs) {
             String val = pef.isComparison() ? "Pre:" + pef.getPreValue() + ";Post:" + pef.getPostValue() : String.valueOf(pef.getPreValue());
             sb.append(pef.getDateTime()).append(",")
               .append(val).append(",")
               .append(pef.getZone()).append("\n");
        }
        
        sb.append("\n--- Triage Incidents ---\n");
        sb.append("Date,Action\n");
        for (Map<String, Object> log : exportTriageLogs) {
             long ts = (Long) log.get("timestamp");
             String action = (String) log.get("action");
             sb.append(sdf.format(new Date(ts))).append(",")
               .append(action).append("\n");
        }

        String filename = "ProviderReport_" + currentChildName + "_" + System.currentTimeMillis() + ".csv";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                OutputStream os = getContentResolver().openOutputStream(uri);
                os.write(sb.toString().getBytes());
                os.close();
                Toast.makeText(this, "CSV Saved to Downloads", Toast.LENGTH_SHORT).show();
            } else {
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes());
                fos.close();
                Toast.makeText(this, "CSV Saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
