# UECS2344---Hospital-Bed-Ward-Management-System
Healthcare Management System (Hospital Bed &amp; Ward)

---
## Introduction


---
### Core Functionalities

The system is implemented with the following functionalities:

**1. User Registration & Profile Management**
The system authorizes users to create, update, deactivate, and manage user accounts and personal information

**2. Transaction Management**
The system shall allow users to create, modify, search, and manage transactions according to the context 
*Bed & Ward: Admit Patients and Assign Beds* 

**3. Record Update & Cancellation**
The system shall allow authorized users to modify or cancel existing records while maintaining data integrity

**4. Status Tracking**
The system provide real-time or updated status information for patients, services, or healthcare resources

**Hospital Ward**
  - Bed Available
  - Occupied
  - Cleaning
  - Reserved

**5. Report Generation**
The system generates reports to assist management and healthcare staff
Possible reports include:
  - Daily appointment report
  - Monthly patient admissions
  - Bed occupancy report

**6. Healthcare Operation Management**
The system supports operational workflow in allocation of beds and monitoring ward capacity

**7. Role-Based Access Control (RBAC)**
Different users have different permissions:
| Role | Permissions |
| :--- | :--- |
| Patient | View own records and bookings |
| Doctor | Manage consultations and patient history |
| Nurse | Update patient status |
| Admin | Full system access |
*This functionality demonstrates inheritance and polymorphism*

**8. Alert & Notification System**
The system automatically notify users when important event occur:
**When bed occupancy reaches capacity**: Notifications may be simulated using Console messages or pop-up dialogs

---
