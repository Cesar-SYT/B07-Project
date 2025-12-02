
# SMART AIR - Requirements Checklist (Detailed)

This document tracks the implementation status of each feature outlined in `requirements.md`.

---

## 1. Users & Roles

- [x] **Child Role**
  - [x] Logs medicines and symptoms
    - `app/src/main/java/com/example/smartair/r3/SimpleMedicineLog.java`
    - `app/src/main/java/com/example/smartair/symptoms/SymptomCheckinChildFragment.java`
  - [x] Interacts with technique helpers
    - `app/src/main/java/com/example/smartair/r3/TechniqueHelperActivity.java`
  - [x] Sees only their own data (Handled via Firebase rules and passing child's UID)
- [x] **Parent Role**
  - [x] Links children
    - `app/src/main/java/com/example/smartair/ManageChildrenActivity.java`
  - [x] Views dashboards
    - `app/src/main/java/com/example/smartair/ParentHomeActivity.java`
  - [x] Receives alerts
    - `app/src/main/java/com/example/smartair/ParentHomeActivity.java` (in `checkForAlerts`)
  - [x] Manages sharing
    - `app/src/main/java/com/example/smartair/SharingActivity.java`
  - [x] Exports reports
    - `app/src/main/java/com/example/smartair/ProviderHomeActivity.java` (Reused for provider report export)
- [x] **Provider Role**
  - [x] Views the shared summary/report
    - `app/src/main/java/com/example/smartair/ProviderHomeActivity.java`
  - [x] Read-only access (Handled by UI design in `ProviderHomeActivity`)
  - [x] Access via one-time invite code/link
    - `app/src/main/java/com/example/smartair/SharingActivity.java` (Code generation)
    - `app/src/main/java/com/example/smartair/ProviderHomeActivity.java` (Code redemption)

### Privacy Defaults

- [x] Child sees only their own data.
- [x] By default, nothing is shared with a Provider.
- [x] The Parent controls what a Provider can view via granular toggles.
- [x] Changes to sharing take effect in real time.

---

## 2. Glossary & Key Concepts

- [x] **Rescue Inhaler**: `app/src/main/java/com/example/smartair/r3/MedicineType.java`
- [x] **Controller Medicine**: `app/src/main/java/com/example/smartair/r3/MedicineType.java`
- [x] **Peak-flow (PEF)**: `app/src/main/java/com/example/smartair/DisplayPEF.java`
- [x] **Personal Best (PB)**: `app/src/main/java/com/example/smartair/SafetyAndControl.java`
- [x] **Zones**: `app/src/main/java/com/example/smartair/DisplayPEF.java`
- [x] **Rapid Rescue Repeats**: `app/src/main/java/com/example/smartair/ParentHomeActivity.java`
- [x] **Low Canister**: `app/src/main/java/com/example/smartair/r3/InventoryActivity.java`

---

## 3. Core Functional Requirements

### R1. Accounts, Roles & Onboarding

- [x] **Sign-in & Recovery**: Email/password sign-in and credential recovery.
  - `app/src/main/java/com/example/smartair/login/LoginFragment.java`
- [x] **Child Accounts**: Child can sign in with their own account or use a child profile.
  - `app/src/main/java/com/example/smartair/login/LoginFragment.java`
  - `app/src/main/java/com/example/smartair/register/RegisterParentFragment.java`
- [x] **Role Routing**: On sign-in, each role lands on its correct home.
  - `app/src/main/java/com/example/smartair/MainActivity.java`
- [x] **Onboarding**: Explains app purpose and features.
  - `app/src/main/java/com/example/smartair/onboarding/OnboardingFragment.java`

### R2. Parent/Child Linking & Selective Sharing

- [x] **Manage Children (Parent)**: Parents can add/link/manage multiple children.
  - `app/src/main/java/com/example/smartair/ManageChildrenActivity.java`
- [x] **Granular Sharing**
  - [x] Rescue logs
  - [x] Controller adherence summary
  - [x] Symptoms
  - [x] Triggers
  - [x] Peak-flow (PEF)
  - [x] Triage incidents
  - [x] Summary charts
    - All implemented in: `app/src/main/java/com/example/smartair/SharingActivity.java`
- [x] **Invite Flow (Provider)**: Parent generates a one-time invite code/link (valid 7 days); Parent can revoke or regenerate.
  - Implemented in `SharingActivity.java` (Generation & Expiry Check Logic) and `ProviderHomeActivity.java` (Redemption).

### R3. Medicines, Technique & Motivation

- [x] **Medicine Logs**: Distinct Rescue vs Controller logging with timestamp and dose.
  - `app/src/main/java/com/example/smartair/r3/SimpleMedicineLog.java`
- [x] **Technique Helper**: Step-by-step prompts and embedded video/animation.
  - `app/src/main/java/com/example/smartair/r3/TechniqueHelperActivity.java`
- [x] **Inventory (Parent)**: Tracks purchase date, amount left, expiry date, and replacement reminders.
  - `app/src/main/java/com/example/smartair/r3/InventoryActivity.java`
- [x] **Motivation (Streaks/Badges)**
  - [x] Streaks for consecutive planned controller days and technique-completed days.
    - `app/src/main/java/com/example/smartair/r3/MotivationActivity.java`
  - [x] Badges for achievements like 10 high-quality technique sessions or low rescue month.
    - `app/src/main/java/com/example/smartair/r3/BadgeType.java`
    - `app/src/main/java/com/example/smartair/r3/R3Service.java` (Logic for awarding)

### R4. Safety & Control (PEF, Zones & Triage)

- [x] **PEF & Zones**: Manual PEF entry, Parent-settable PB, and zone computation.
  - `app/src/main/java/com/example/smartair/DisplayPEF.java`
  - `app/src/main/java/com/example/smartair/SafetyAndControl.java`
- [x] **One-tap Triage**
  - [x] Decision Card: "Call Emergency Now" or "Start Home Steps".
    - `app/src/main/java/com/example/smartair/TriageDetail.java`
  - [x] Timer: 10-minute timer with auto-escalation.
    - `app/src/main/java/com/example/smartair/TriageDetail.java`
  - [x] Parent Alert: Fired on triage start and escalation.
    - `app/src/main/java/com/example/smartair/TriageDetail.java`
- [x] **Safety Note**: Always visible in triage.
  - `app/src/main/res/layout/activity_triage_detail.xml`

### R5. Symptoms, Triggers & History

- [x] **Daily Check-in**: Logging night waking, activity limits, cough/wheeze.
  - `app/src/main/java/com/example/smartair/symptoms/SymptomCheckinChildFragment.java`
- [x] **Triggers**: Multiple tags per entry.
  - `app/src/main/java/com/example/smartair/symptoms/SymptomCheckinChildFragment.java`
- [x] **History Browser**: Stores and filters data.
  - **Note**: Stores data indefinitely. Filtering by symptom, trigger, and date range is present.
  - `app/src/main/java/com/example/smartair/symptoms/SymptomHistoryChildFragment.java`
- [x] **Export**: Export as PDF and CSV.
  - `app//src/main/java/com/example/smartair/ProviderHomeActivity.java`

### R6. Parent Home, Notifications & Provider Report

- [x] **Dashboard Tiles (Parent)**
  - [x] Today's zone
  - [x] Last rescue time
  - [x] Weekly rescue count
  - [x] Trend snippet (7/30 days)
    - All implemented in: `app/src/main/java/com/example/smartair/ParentHomeActivity.java`
- [x] **Alerts (FCM, real-time)**
  - [x] Red-zone day
  - [x] Rapid rescue repeats
  - [x] "Worse after dose"
    - Implemented in `app/src/main/java/com/example/smartair/r3/R3ServiceImpl.java`
  - [x] Triage escalation
  - [x] Inventory low/expired
    - Implemented in various activities like `ParentHomeActivity`, `TriageDetail`, and `InventoryActivity`.
- [x] **Provider Report (PDF)**
  - [x] Rescue frequency and controller adherence
  - [x] Symptom burden
  - [x] Zone distribution over time
  - [x] Notable triage incidents
  - [x] At least one time-series chart and one categorical chart.
    - All implemented in: `app/src/main/java/com/example/smartair/ProviderHomeActivity.java` (PDF/CSV export)
