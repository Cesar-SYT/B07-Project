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
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.Rect;

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
import java.util.HashMap;
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
    private TextView txtZoneSummaryProvider;
    private SimpleLineChart viewZoneDistributionChart;
    private SimpleLineChart viewRescueChart;
    private SimpleLineChart viewControllerChart;

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
    private final List<SimpleLineChart.DataPoint> exportChartPoints = new ArrayList<>();
    private final List<SimpleLineChart.DataPoint> exportControllerChartPoints = new ArrayList<>();


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

    static class SharingSettings {
        boolean shareRescueLogs = true;
        boolean shareAdherence = true;
        boolean shareSymptoms = true;
        boolean shareTriggers = true;
        boolean sharePEF = true;

        public SharingSettings(boolean b, boolean b1, boolean b2, boolean b3, boolean b4) {
        }

        public boolean isShareRescueLogs() { return shareRescueLogs; }
        public boolean isShareAdherence() { return shareAdherence; }
        public boolean isShareSymptoms() { return shareSymptoms; }
        public boolean isShareTriggers() { return shareTriggers; }
        public boolean isSharePEF() { return sharePEF; }
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
        viewControllerChart = findViewById(R.id.viewControllerChart);
        viewZoneDistributionChart = findViewById(R.id.viewZoneDistributionChart);
        txtZoneSummaryProvider = findViewById(R.id.txtZoneSummaryProvider);

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
        exportMedicineLogs.clear();
        exportSymptomLogs.clear();
        exportChartPoints.clear();
        exportControllerChartPoints.clear();

        SharingSettings mockSettings = new SharingSettings(true, true, true, true, true);
        updateUIBasedOnSettings(childKey, mockSettings);
    }

    private void updateUIBasedOnSettings(String childKey, SharingSettings settings) {
        // Rescue Logs
        if (settings.isShareRescueLogs()) {
            cardRescueOverviewProvider.setVisibility(View.VISIBLE);
            loadRescueData(childKey);
        } else {
            cardRescueOverviewProvider.setVisibility(View.GONE);
            viewRescueChart.setData(new ArrayList<>());
            txtEventSample1.setText("Rescue logs not shared.");
        }

        // Controller Adherence
        if (settings.isShareAdherence()) {
            cardControllerOverviewProvider.setVisibility(View.VISIBLE);
            loadControllerData(childKey);
        } else {
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
            loadPEFZoneDistribution(childKey);
        } else {
            cardZoneDistributionProvider.setVisibility(View.GONE);
        }
    }

    private float mapZoneToValue(String zone) {
        if (zone == null) return 0f;
        switch (zone.toUpperCase()) {
            case "GREEN":
                return 3f;
            case "YELLOW":
                return 2f;
            case "RED":
                return 1f;
            default:
                return 0f;  // Unknown
        }
    }

    private void loadPEFZoneDistribution(String childKey) {
        if (txtZoneSummaryProvider != null) {
            txtZoneSummaryProvider.setText("Loading PEF zones...");
        }

        usersRef.child(childKey)
                .child("pef_logs")
                .orderByChild("timestamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        int greenCount = 0;
                        int yellowCount = 0;
                        int redCount = 0;
                        int unknownCount = 0;

                        List<SimpleLineChart.DataPoint> zonePoints = new ArrayList<>();

                        for (DataSnapshot child : snapshot.getChildren()) {
                            PEFLogModel log = child.getValue(PEFLogModel.class);
                            if (log == null) continue;

                            String zone = log.getZone();
                            if (zone == null) zone = "Unknown";

                            long ts = log.getTimestamp();
                            if (ts == 0L) continue;

                            switch (zone.toUpperCase()) {
                                case "GREEN":
                                    greenCount++; break;
                                case "YELLOW":
                                    yellowCount++; break;
                                case "RED":
                                    redCount++; break;
                                default:
                                    unknownCount++; break;
                            }

                            float y = mapZoneToValue(zone);
                            zonePoints.add(new SimpleLineChart.DataPoint(ts, y));
                        }

                        Collections.sort(zonePoints);

                        if (txtZoneSummaryProvider != null) {
                            String summary = "PEF zones (all logs)  "
                                    + "Green: " + greenCount
                                    + "   Yellow: " + yellowCount
                                    + "   Red: " + redCount
                                    + "   Unknown: " + unknownCount;
                            txtZoneSummaryProvider.setText(summary);
                        }

                        if (viewZoneDistributionChart != null) {
                            viewZoneDistributionChart.setData(zonePoints);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (txtZoneSummaryProvider != null) {
                            txtZoneSummaryProvider.setText("Failed to load PEF zones: " + error.getMessage());
                        }
                        if (viewZoneDistributionChart != null) {
                            viewZoneDistributionChart.setData(new ArrayList<>());
                        }
                    }
                });
    }


    private void loadRescueData(String childKey) {
        usersRef.child(childKey).child("medicine_logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int rescueCount = 0;
                long oneWeekAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
                long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 3600 * 1000);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                exportChartPoints.clear();
                exportMedicineLogs.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    SimpleMedicineLog log = child.getValue(SimpleMedicineLog.class);
                    if (log != null && "RESCUE".equalsIgnoreCase(log.getType())) {
                        exportMedicineLogs.add(log);
                        try {
                            Date date = sdf.parse(log.getTime());
                            if (date != null) {
                                if (date.getTime() > oneWeekAgo) {
                                    rescueCount += log.getDoseCount();
                                }
                                if (date.getTime() > thirtyDaysAgo) {
                                    exportChartPoints.add(new SimpleLineChart.DataPoint(date.getTime(), log.getDoseCount()));
                                }
                            }
                        } catch (Exception e) {}
                    }
                }
                Collections.sort(exportMedicineLogs, (o1, o2) -> o2.getTime().compareTo(o1.getTime()));

                txtEventSample1.setText("Rescue use: " + rescueCount + " puffs this week.");

                Collections.sort(exportChartPoints);
                viewRescueChart.setData(exportChartPoints);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadControllerData(String childKey) {
        usersRef.child(childKey).child("medicine_logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                exportControllerChartPoints.clear();

                long now = System.currentTimeMillis();
                long thirtyDaysAgo = now - (30L * 24 * 3600 * 1000);
                long sevenDaysAgo = now - (7L * 24 * 3600 * 1000);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                Map<Long, Float> dailyControllerDoses = new HashMap<>();
                int controllerUseLast7Days = 0;

                for (DataSnapshot child : snapshot.getChildren()) {
                    SimpleMedicineLog log = child.getValue(SimpleMedicineLog.class);
                    if (log != null && "CONTROLLER".equalsIgnoreCase(log.getType())) {
                        try {
                            Date date = sdf.parse(log.getTime());
                            if (date != null) {
                                long logTime = date.getTime();
                                float currentDose = (float)log.getDoseCount();

                                if (logTime > sevenDaysAgo) {
                                    controllerUseLast7Days += log.getDoseCount();
                                }

                                if (logTime > thirtyDaysAgo) {
                                    long dayMillis = 24L * 3600 * 1000;
                                    long dayKey = (logTime / dayMillis) * dayMillis;

                                    dailyControllerDoses.put(dayKey, dailyControllerDoses.getOrDefault(dayKey, 0f) + currentDose);
                                }
                            }
                        } catch (Exception e) {}
                    }
                }

                for (Map.Entry<Long, Float> entry : dailyControllerDoses.entrySet()) {
                    exportControllerChartPoints.add(new SimpleLineChart.DataPoint(entry.getKey(), entry.getValue()));
                }

                Collections.sort(exportControllerChartPoints);

                exportControllerChartPoints.add(new SimpleLineChart.DataPoint(Long.MAX_VALUE, (float)controllerUseLast7Days));

                if (viewControllerChart != null) {
                    List<SimpleLineChart.DataPoint> dataForView = new ArrayList<>(exportControllerChartPoints);
                    dataForView.remove(dataForView.size() - 1);
                    viewControllerChart.setData(dataForView);
                }
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

                exportSymptomLogs.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    SymptomEntry entry = child.getValue(SymptomEntry.class);
                    if (entry != null) {
                        exportSymptomLogs.add(entry);

                        if (entry.timestamp > twoWeeksAgo) {
                            symptomCount++;
                            if (entry.triggers != null) triggers.addAll(entry.triggers);
                        }
                    }
                }
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
        View sheet = getLayoutInflater().inflate(R.layout.symptom_export_sheet, null);

        dialog.setContentView(sheet);
        dialog.show();

        RadioButton rbPdf = sheet.findViewById(R.id.radio_pdf);
        RadioButton rbCsv = sheet.findViewById(R.id.radio_csv);
        Button btnExport = sheet.findViewById(R.id.button_export);

        btnExport.setOnClickListener(v -> {
            if (exportMedicineLogs.isEmpty() && exportSymptomLogs.isEmpty()) {
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

        canvas.drawText("Patient Health Report: " + currentChildName, x, y, titlePaint);
        y += 40;

        titlePaint.setTextSize(14);
        canvas.drawText("Rescue Inhaler Logs (Recent)", x, y, titlePaint);
        y += 25;

        if (!exportChartPoints.isEmpty()) {
            final int CHART_WIDTH = 515;
            final int CHART_HEIGHT = 150;

            y = drawChartToPdf(canvas, paint, y, exportChartPoints, CHART_WIDTH, CHART_HEIGHT, Color.BLUE);
        } else {
            paint.setTextSize(12);
            canvas.drawText("No rescue log data for chart.", x, y, paint);
            y += 30;
        }

        if (exportMedicineLogs.isEmpty()) {
            canvas.drawText("No rescue logs shared or available.", x, y, paint);
            y += 20;
        } else {
            int logsCount = 0;
            titlePaint.setTextSize(12);
            canvas.drawText("Recent Log Entries:", x, y, titlePaint);
            y += 20;

            for (SimpleMedicineLog log : exportMedicineLogs) {
                if (y > 780 || logsCount >= 10) {
                    if (logsCount >= 10) break;

                    pdfDocument.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                    canvas.drawText("Rescue Inhaler Logs (Continued)", x, y, titlePaint);
                    y += 25;
                }
                String line = "Date: " + log.getTime() + " | Doses: " + log.getDoseCount() + " | Feel: " + log.getFeel();
                canvas.drawText(line, x, y, paint);
                y += 20;
                logsCount++;
            }
        }
        y += 20;

        titlePaint.setTextSize(14);
        canvas.drawText("Controller Adherence Trend (Recent)", x, y, titlePaint);
        y += 25;

        int controllerUseLast7Days = 0;
        List<SimpleLineChart.DataPoint> controllerDataForChart = new ArrayList<>(exportControllerChartPoints);

        if (!controllerDataForChart.isEmpty()) {
            SimpleLineChart.DataPoint lastPoint = controllerDataForChart.get(controllerDataForChart.size() - 1);
            if (lastPoint.x == Long.MAX_VALUE) {
                controllerUseLast7Days = (int) lastPoint.y;
                controllerDataForChart.remove(controllerDataForChart.size() - 1);
            }
        }

        paint.setTextSize(12);
        canvas.drawText("Total controller use in the last 7 days: " + controllerUseLast7Days + " doses.", x, y, paint);
        y += 30;

        if (!controllerDataForChart.isEmpty()) {
            final int CHART_WIDTH = 515;
            final int CHART_HEIGHT = 150;

            y = drawChartToPdf(canvas, paint, y, controllerDataForChart, CHART_WIDTH, CHART_HEIGHT, Color.parseColor("#4CAF50"));
        } else {
            paint.setTextSize(12);
            canvas.drawText("No controller adherence data for chart.", x, y, paint);
            y += 30;
        }
        y += 20;

        titlePaint.setTextSize(14);
        canvas.drawText("Symptoms & Triggers", x, y, titlePaint);
        y += 25;

        Map<String, Integer> triggerCounts = new HashMap<>();
        for (SymptomEntry entry : exportSymptomLogs) {
            if (entry.triggers != null) {
                for (String trigger : entry.triggers) {
                    if (!trigger.trim().isEmpty()) {
                        triggerCounts.put(trigger, triggerCounts.getOrDefault(trigger, 0) + 1);
                    }
                }
            }
        }

        if (!triggerCounts.isEmpty()) {
            if (y > 600) {
                pdfDocument.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
                titlePaint.setTextSize(14);
                canvas.drawText("Trigger Distribution (Continued)", x, y, titlePaint);
                y += 25;
            }

            y = drawPieChartToPdf(canvas, paint, titlePaint, y, triggerCounts);
        } else {
            paint.setTextSize(12);
            canvas.drawText("No trigger data available for pie chart.", x, y, paint);
            y += 30;
        }


        if (exportSymptomLogs.isEmpty()) {
            canvas.drawText("No symptom logs shared or available.", x, y, paint);
            y += 20;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            titlePaint.setTextSize(12);
            canvas.drawText("Recent Symptom Entries:", x, y, titlePaint);
            y += 20;

            for (SymptomEntry entry : exportSymptomLogs) {
                if (y > 780) {
                    pdfDocument.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, pageNumber).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                    canvas.drawText("Symptoms & Triggers (Continued)", x, y, titlePaint);
                    y += 25;
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

        pdfDocument.finishPage(page);
        savePdfFile(pdfDocument);
    }


    private int drawChartToPdf(Canvas canvas, Paint paint, int startY, List<SimpleLineChart.DataPoint> points, int width, int height, int lineColor) {

        if (points == null || points.size() < 2) {
            paint.setTextSize(12);
            canvas.drawText("Not enough data to draw chart.", 40, startY + 20, paint);
            return startY + height + 30;
        }

        int padding = 20;
        int chartWidth = width - 2 * padding;
        int chartHeight = height - 2 * padding;
        int xOffset = 40 + padding;
        int yStart = startY + height + 5;

        long minTime = points.get(0).x;
        long maxTime = points.get(points.size() - 1).x;
        float maxDose = 1f;
        for (SimpleLineChart.DataPoint p : points) {
            if (p.y > maxDose) maxDose = p.y;
        }

        maxDose *= 1.1f;

        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        canvas.drawLine(xOffset, yStart, xOffset + chartWidth, yStart, paint);
        canvas.drawLine(xOffset, yStart, xOffset, yStart - chartHeight, paint);

        paint.setTextSize(10);
        paint.setColor(Color.BLACK);
        canvas.drawText(String.valueOf((int) maxDose), xOffset - 30, yStart - chartHeight + 5, paint);
        canvas.drawText("0", xOffset - 30, yStart + 5, paint);


        paint.setColor(lineColor);
        paint.setStrokeWidth(3);

        float prevX = 0;
        float prevY = 0;

        for (int i = 0; i < points.size(); i++) {
            SimpleLineChart.DataPoint p = points.get(i);

            float normalizedTime = (maxTime == minTime) ? 0 : (float) (p.x - minTime) / (maxTime - minTime);
            float currentX = xOffset + normalizedTime * chartWidth;

            float normalizedDose = p.y / maxDose;
            float currentY = yStart - normalizedDose * chartHeight;

            if (i > 0) {
                canvas.drawLine(prevX, prevY, currentX, currentY, paint);
            }

            canvas.drawCircle(currentX, currentY, 4, paint);

            prevX = currentX;
            prevY = currentY;
        }

        return startY + height + 50;
    }

    private int drawPieChartToPdf(Canvas canvas, Paint dataPaint, Paint titlePaint, int startY, Map<String, Integer> triggerCounts) {
        int x = 40;
        int y = startY;

        titlePaint.setTextSize(14);
        canvas.drawText("Trigger Distribution", x, y, titlePaint);
        y += 30;

        int chartSize = 250;
        android.graphics.RectF oval = new android.graphics.RectF(x, y, x + chartSize, y + chartSize);
        int totalCount = triggerCounts.values().stream().mapToInt(Integer::intValue).sum();

        float startAngle = 0;
        int legendX = x + chartSize + 20;
        int legendY = y;
        int colorIndex = 0;

        int[] colorPalette = {
                Color.parseColor("#FF6384"),
                Color.parseColor("#36A2EB"),
                Color.parseColor("#FFCD56"),
                Color.parseColor("#4BC0C0"),
                Color.parseColor("#9966FF"),
                Color.parseColor("#FF9F40"),
                Color.parseColor("#C9CBCF")
        };

        dataPaint.setTextSize(12);

        List<String> sortedTriggers = new ArrayList<>(triggerCounts.keySet());
        Collections.sort(sortedTriggers, (t1, t2) -> triggerCounts.get(t2) - triggerCounts.get(t1));

        for (String trigger : sortedTriggers) {
            int count = triggerCounts.get(trigger);
            float sweepAngle = (float) count / totalCount * 360;
            int color = colorPalette[colorIndex % colorPalette.length];

            dataPaint.setColor(color);
            dataPaint.setStyle(Paint.Style.FILL);

            canvas.drawArc(oval, startAngle, sweepAngle, true, dataPaint);

            canvas.drawRect(legendX, legendY, legendX + 15, legendY + 15, dataPaint);

            String percent = String.format(Locale.getDefault(), "%.1f%%", (sweepAngle / 360f) * 100);
            dataPaint.setColor(Color.BLACK);
            canvas.drawText(trigger + " (" + count + " entries, " + percent + ")", legendX + 25, legendY + 12, dataPaint);

            startAngle += sweepAngle;
            legendY += 25;
            colorIndex++;
        }

        return Math.max(y + chartSize, legendY) + 40;
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
        // if no data
        if (exportMedicineLogs.isEmpty() && exportSymptomLogs.isEmpty()) {
            Toast.makeText(this, "No data available to export", Toast.LENGTH_SHORT).show();
            return;
        }

        String filename = "ProviderReport_" + currentChildName + "_" + System.currentTimeMillis() + ".csv";

        StringBuilder sb = new StringBuilder();

        sb.append("Patient: ").append(csvEscape(currentChildName)).append("\n");
        sb.append("Generated at: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date())).append("\n\n");

        sb.append("Rescue Inhaler Logs\n");
        sb.append("DateTime,Type,Doses,Feel\n");

        for (SimpleMedicineLog log : exportMedicineLogs) {
            String time = log.getTime();
            String type = log.getType();
            String doses = String.valueOf(log.getDoseCount());
            String feel = log.getFeel();

            sb.append(csvEscape(time)).append(",");
            sb.append(csvEscape(type)).append(",");
            sb.append(csvEscape(doses)).append(",");
            sb.append(csvEscape(feel)).append("\n");
        }

        sb.append("\n");

        sb.append("Symptom Logs\n");
        sb.append("DateTime,Sleep,Activity,Cough,Triggers\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        for (SymptomEntry entry : exportSymptomLogs) {
            String date = sdf.format(new Date(entry.timestamp));
            String sleep = String.valueOf(entry.sleep);
            String activity = String.valueOf(entry.activity);
            String cough = String.valueOf(entry.cough);

            String triggers;
            if (entry.triggers == null || entry.triggers.isEmpty()) {
                triggers = "None";
            } else {
                triggers = TextUtils.join(";", entry.triggers);
            }

            sb.append(csvEscape(date)).append(",");
            sb.append(csvEscape(sleep)).append(",");
            sb.append(csvEscape(activity)).append(",");
            sb.append(csvEscape(cough)).append(",");
            sb.append(csvEscape(triggers)).append("\n");
        }

        sb.append("\n");

        if (!exportControllerChartPoints.isEmpty()) {
            List<SimpleLineChart.DataPoint> controllerData = new ArrayList<>(exportControllerChartPoints);
            int controllerUseLast7Days = 0;

            if (!controllerData.isEmpty()) {
                SimpleLineChart.DataPoint last = controllerData.get(controllerData.size() - 1);
                if (last.x == Long.MAX_VALUE) {
                    controllerUseLast7Days = (int) last.y;
                    controllerData.remove(controllerData.size() - 1);
                }
            }

            sb.append("Controller Daily Doses (for chart)\n");
            sb.append("Date,DailyTotalDoses\n");

            for (SimpleLineChart.DataPoint dp : controllerData) {
                String day = sdf.format(new Date(dp.x));
                String dose = String.valueOf(dp.y);
                sb.append(csvEscape(day)).append(",");
                sb.append(csvEscape(dose)).append("\n");
            }

            sb.append("\n");
            sb.append("Total controller use in last 7 days:,")
                    .append(controllerUseLast7Days)
                    .append("\n\n");
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Toast.makeText(this, "Failed to create CSV file", Toast.LENGTH_SHORT).show();
                    return;
                }

                OutputStream os = getContentResolver().openOutputStream(uri);
                if (os == null) {
                    Toast.makeText(this, "Failed to open CSV output stream", Toast.LENGTH_SHORT).show();
                    return;
                }

                os.write(sb.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                Toast.makeText(this, "CSV saved to Downloads", Toast.LENGTH_SHORT).show();
            } else {
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir != null && !dir.exists()) dir.mkdirs();

                File file = new File(dir, filename);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(sb.toString().getBytes("UTF-8"));
                fos.flush();
                fos.close();

                Toast.makeText(this, "CSV Saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private String csvEscape(String value) {
        if (value == null) return "";
        if (value.contains("\"") || value.contains(",") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

}