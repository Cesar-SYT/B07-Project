package com.example.smartair;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartair.R;

import java.util.Arrays;
import java.util.List;

public class ProviderHomeActivity extends AppCompatActivity {

    private Spinner spinnerPatientSelector;

    private View cardRescueOverviewProvider;
    private View cardControllerOverviewProvider;
    private View cardSymptomsTriggersProvider;
    private View cardZoneDistributionProvider;

    private TextView txtSymptomsSummaryProvider;
    private TextView txtTopTriggersProvider;
    private TextView txtEventSample1;

    private Button btnExportReportProvider;

    // demo 患者列表
    private final List<String> demoPatients = Arrays.asList("Alex", "Jamie", "Chris");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.provider_page);  // 对应 provider_page.xml

        initViews();
        setupPatientSpinner();
        loadDemoDataForPatient(demoPatients.get(0)); // 默认第一个
        setupClickListeners();
    }

    private void initViews() {
        spinnerPatientSelector = findViewById(R.id.spinnerPatientSelector);

        cardRescueOverviewProvider = findViewById(R.id.cardRescueOverviewProvider);
        cardControllerOverviewProvider = findViewById(R.id.cardControllerOverviewProvider);
        cardSymptomsTriggersProvider = findViewById(R.id.cardSymptomsTriggersProvider);
        cardZoneDistributionProvider = findViewById(R.id.cardZoneDistributionProvider);

        txtSymptomsSummaryProvider = findViewById(R.id.txtSymptomsSummaryProvider);
        txtTopTriggersProvider = findViewById(R.id.txtTopTriggersProvider);
        txtEventSample1 = findViewById(R.id.txtEventSample1);

        btnExportReportProvider = findViewById(R.id.btnExportReportProvider);
    }

    private void setupPatientSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                demoPatients
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatientSelector.setAdapter(adapter);

        spinnerPatientSelector.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = demoPatients.get(position);
                loadDemoDataForPatient(selected);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // ignore
            }
        });
    }

    /** 根据当前选的 patient 填一点假数据，之后你可以换成 Firebase 的查询 */
    private void loadDemoDataForPatient(String patientName) {
        // 这里只是示例逻辑，你可以按真实字段改
        if ("Alex".equals(patientName)) {
            txtSymptomsSummaryProvider.setText("Symptom days: 2 in last 14 days");
            txtTopTriggersProvider.setText("Top triggers: exercise, cold air");
            txtEventSample1.setText("ER visit 10 days ago – follow up scheduled.");
        } else if ("Jamie".equals(patientName)) {
            txtSymptomsSummaryProvider.setText("Symptom days: 5 in last 14 days");
            txtTopTriggersProvider.setText("Top triggers: pollen, dust");
            txtEventSample1.setText("Rescue use 4 times this week – review controller dose.");
        } else {
            txtSymptomsSummaryProvider.setText("Symptom days: 0 in last 14 days");
            txtTopTriggersProvider.setText("Top triggers: –");
            txtEventSample1.setText("No recent critical events.");
        }
    }

    private void setupClickListeners() {
        btnExportReportProvider.setOnClickListener(v ->
                Toast.makeText(this, "Export report (to be implemented)", Toast.LENGTH_SHORT).show()
        );
    }
}
