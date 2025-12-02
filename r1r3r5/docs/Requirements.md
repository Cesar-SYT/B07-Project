
---

# 💨 SMART AIR - Required Features Documentation

[cite_start]SMART AIR is a kid-friendly Android app that helps children (ages 6-16) understand asthma, practice good inhaler technique, log symptoms/medicine use, and share parent-approved information with a healthcare provider via a concise, exportable report[cite: 3].

---

## 👤 1. Users & Roles

| Role | Primary Responsibilities | Access/Notes |
| :--- | :--- | :--- |
| **Child** | [cite_start]Logs medicines and symptoms; interacts with technique helpers[cite: 5]. | [cite_start]Sees only their own data[cite: 10]. |
| **Parent** | [cite_start]Links children; views dashboards; receives alerts; manages sharing; exports reports[cite: 6, 7]. | [cite_start]Controls all sharing settings[cite: 12]. [cite_start]Owns the account/permissions if a Child uses a child profile[cite: 57]. |
| **Provider** | [cite_start]Views the shared summary/report[cite: 8]. | [cite_start]Read-only access inside the app[cite: 8, 56]. [cite_start]Access is via a one-time invite code/link[cite: 16]. |

### [cite_start]Privacy Defaults [cite: 9]

* [cite_start]Child sees only their own data[cite: 10].
* [cite_start]By default, nothing is shared with a Provider[cite: 11].
* [cite_start]The Parent controls what a Provider can view per child via granular toggles[cite: 12].
* [cite_start]Changes to sharing take effect in real time and are reversible[cite: 13, 75].

---

## 📚 2. Glossary & Key Concepts

| Term | Definition | Thresholds/Defaults | Source |
| :--- | :--- | :--- | :--- |
| **Rescue Inhaler** | Quick-relief medicine for sudden symptoms. | [cite_start]N/A | [cite: 20] |
| **Controller Medicine** | Daily prevention medicine that reduces airway swelling. | [cite_start]N/A | [cite: 21] |
| **Peak-flow (PEF)** | A number the user manually enters to reflect how fast they can blow out. | [cite_start]N/A | [cite: 22, 23] |
| **Personal Best (PB)** | Highest healthy PEF used to define zones. | [cite_start]N/A | [cite: 25] |
| **Zones** | Defined by PEF relative to Personal Best (PB). | Green $\ge80\%$ PB; Yellow 50-79% PB; [cite_start]Red $<50\%$ PB[cite: 25, 106]. [cite_start]| [cite: 25, 106] |
| **Rapid Rescue Repeats** | Safety alert trigger condition. | [cite_start]$\ge3$ rescue uses within 3 hours (configurable)[cite: 31, 122]. [cite_start]| [cite: 31, 122] |
| **Low Canister** | Inventory alert condition. | [cite_start]$\le20\%$ remaining (configurable)[cite: 33]. [cite_start]| [cite: 33] |

---

## ✅ 3. Core Functional Requirements

### R1. Accounts, Roles & Onboarding

* [cite_start]**Sign-in & Recovery:** Email/password sign-in and credential recovery for Parent and Provider[cite: 43].
* [cite_start]**Child Accounts:** Child can (a) sign in with their own account or (b) use a child profile under a Parent account (no email required)[cite: 44, 57].
* [cite_start]**Role Routing:** On sign-in, each role lands on its correct home (Child / Parent / Provider)[cite: 45].
* [cite_start]**Onboarding:** Explains rescue vs controller, app purpose, privacy defaults, sharing controls, and Provider invites[cite: 47, 48].

### R2. Parent/Child Linking & Selective Sharing

* [cite_start]**Manage Children (Parent):** Parents can add/link/manage multiple children (name, DOB/age, optional notes)[cite: 61].
* [cite_start]**Granular Sharing:** Parent uses a "Share with Provider" selector with toggles for each data type[cite: 62]:
    * [cite_start]Rescue logs [cite: 63]
    * [cite_start]Controller adherence summary [cite: 65]
    * [cite_start]Symptoms [cite: 66]
    * [cite_start]Triggers [cite: 67]
    * [cite_start]Peak-flow (PEF) [cite: 69]
    * [cite_start]Triage incidents [cite: 70]
    * [cite_start]Summary charts (dashboard/report visuals) [cite: 73]
* **Invite Flow (Provider):** Parent generates a one-time invite code/link (valid 7 days); [cite_start]Parent can revoke or regenerate at any time[cite: 77].

### R3. Medicines, Technique & Motivation

* [cite_start]**Medicine Logs:** Must keep Rescue vs Controller logging distinct[cite: 82]. [cite_start]Each entry captures timestamp and dose count[cite: 83].
* [cite_start]**Technique Helper:** Provides step-by-step prompts (e.g., seal lips, hold $\sim10$s)[cite: 85]. [cite_start]Includes at least one embedded video/animation[cite: 87].
* [cite_start]**Inventory (Parent):** Tracks purchase date, amount left, expiry date, and replacement reminders[cite: 91].
* [cite_start]**Motivation (Streaks/Badges):** Streaks for consecutive planned controller days and technique-completed days[cite: 94]. [cite_start]Badges for achievements like 10 high-quality technique sessions or low rescue month ($\le4$ rescue days / 30 days)[cite: 95].

### R4. Safety & Control (PEF, Zones & Triage)

* [cite_start]**PEF & Zones:** Manual PEF entry is required[cite: 102]. [cite_start]PB is settable by Parent[cite: 104]. [cite_start]App computes today's zone and shows it on Child and Parent homes[cite: 105].
* [cite_start]**One-tap Triage:** A quick check for red flags (e.g., can't speak full sentences, blue lips)[cite: 108, 110, 112].
    * [cite_start]**Decision Card:** Shows "Call Emergency Now" for critical flags or "Start Home Steps for controllable symptoms (shows zone-aligned steps from the action plan)"[cite: 114, 115].
    * [cite_start]**Timer:** Default 10-minute timer with auto-escalation if not improving[cite: 116].
    * [cite_start]**Parent Alert:** Fired when a triage session starts and on escalation[cite: 117].
* [cite_start]**Safety Note:** "This is guidance, not a diagnosis. If in doubt, call emergency" is always visible in triage[cite: 120].

### R5. Symptoms, Triggers & History

* [cite_start]**Daily Check-in:** Includes logging night waking, activity limits, cough/wheeze[cite: 127].
* [cite_start]**Triggers:** Multiple tags per entry (e.g., exercise, cold air, smoke)[cite: 129].
* [cite_start]**History Browser:** Stores 3-6 months of data[cite: 130]. [cite_start]Can filter by symptom, trigger, and date range[cite: 131].
* [cite_start]**Export:** Selected range can be exported as PDF (and optional CSV)[cite: 132].

### R6. Parent Home, Notifications & Provider Report

* **Dashboard Tiles (Parent):**
    * [cite_start]Today's zone [cite: 137]
    * [cite_start]Last rescue time [cite: 138]
    * [cite_start]Weekly rescue count [cite: 139]
    * [cite_start]Trend snippet (default 7 days; toggle 30 days) [cite: 140]
* **Alerts (FCM, real-time):**
    * [cite_start]Red-zone day [cite: 143]
    * [cite_start]Rapid rescue repeats ($\ge3$ in 3 hours) [cite: 144]
    * [cite_start]"Worse after dose" [cite: 145]
    * [cite_start]Triage escalation [cite: 146]
    * [cite_start]Inventory low/expired [cite: 147]
* [cite_start]**Provider Report (PDF):** Parent-controlled window of 3-6 months[cite: 148]. [cite_start]Must include[cite: 149, 150, 151, 152]:
    * Rescue frequency and controller adherence ($\%$ of planned days completed)
    * Symptom burden (counts of problem days)
    * Zone distribution over time
    * Notable triage incidents (if sharing enabled)
    * [cite_start]At least one time-series chart and one categorical chart (bars/pie) [cite: 153]

---
