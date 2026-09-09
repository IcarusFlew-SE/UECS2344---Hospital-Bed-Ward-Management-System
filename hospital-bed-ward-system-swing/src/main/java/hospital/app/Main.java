package hospital.app;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

import hospital.controller.HospitalController;
import hospital.data.HospitalDataStore;
import hospital.data.DataLoader;
import hospital.model.*;

// Main - application entry point and UI wiring
public class Main {

    public static void main(String[] args) {
        AppTheme.applyLookAndFeel();
        SwingUtilities.invokeLater(Main::createAndShow);
    }

    private static void createAndShow() {
        HospitalDataStore ds = new HospitalDataStore();
        HospitalController hc = new HospitalController(ds);
        seedSampleData(ds);

        List<User> allUsers = ds.findAllUsers();
        List<Patient> patients = new ArrayList<>();
        List<Nurse> nurses = new ArrayList<>();
        List<Doctor> doctors = new ArrayList<>();
        for (User u : allUsers) {
            if (u instanceof Patient p) patients.add(p);
            if (u instanceof Nurse n) nurses.add(n);
            if (u instanceof Doctor d) doctors.add(d);
        }
        List<Ward> wards = ds.findAllWards();

        // Login 
        JFrame splash = buildSplashFrame();

        LoginUI loginDlg = new LoginUI(splash, allUsers);
        loginDlg.setVisible(true);

        User currentUser = loginDlg.getLoggedInUser();
        if (currentUser == null) {
            System.exit(0);
        }
        splash.dispose();

        // Build UI panels 
        AdmissionUI admissionUI = new AdmissionUI(hc, patients, doctors, wards);
        TransferUI transferUI = new TransferUI(hc, ds, wards, patients);
        WardStatusUI wardStatusUI = new WardStatusUI(hc, wards, nurses);
        ReportUI reportUI = new ReportUI(hc);
        NurseAssignmentUI nurseUI = new NurseAssignmentUI(hc, ds, wards);
        NotificationUI notifUI = new NotificationUI(ds, currentUser.getUserId());
        AccountUI accountUI = new AccountUI(hc, allUsers);

         // Tab pane
         JTabbedPane tabs = new JTabbedPane();
         tabs.setFont(AppTheme.FONT_BODY.deriveFont(Font.BOLD));

         tabs.addTab(" Admit Patient", admissionUI);
         tabs.addTab(" Transfer / Cancel", transferUI);
         tabs.addTab(" Ward Status", wardStatusUI);
         tabs.addTab(" Reports", reportUI);
         tabs.addTab(" Assign Nurse", nurseUI);
         tabs.addTab(" Notifications", notifUI);
         tabs.addTab(" Accounts", accountUI);

         // Apply RBAC immediately for the logged-in user
         applyRbac(currentUser, tabs, admissionUI, transferUI, wardStatusUI, reportUI, nurseUI, notifUI, accountUI);

         // Header bar (pass tabs so it can update RBAC when user switches)
         JPanel headerBar = buildHeaderBar(currentUser, allUsers, tabs, admissionUI, transferUI, wardStatusUI, reportUI, nurseUI, notifUI, accountUI);

        // Reload live data whenever a tab is focused
        tabs.addChangeListener(e -> {
            Component sel = tabs.getSelectedComponent();
            if (sel == transferUI) transferUI.loadActiveAdmissions();
            if (sel == wardStatusUI) wardStatusUI.viewAllWardStatus();
            if (sel == notifUI) notifUI.viewNotifications();
            if (sel == accountUI) accountUI.refreshTable();
        });

        // Root panel
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AppTheme.SURFACE);
        root.add(headerBar, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);

        // Main frame
        JFrame frame = new JFrame("Hospital Bed & Ward Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.setSize(1000, 720);
        frame.setMinimumSize(new Dimension(860, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Header bar with dynamic RBAC on user role switch
    private static JPanel buildHeaderBar(User initialUser, List<User> allUsers,
            JTabbedPane tabs, AdmissionUI admissionUI, TransferUI transferUI, 
            WardStatusUI wardStatusUI, ReportUI reportUI, NurseAssignmentUI nurseUI, 
            NotificationUI notifUI, AccountUI accountUI) {

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppTheme.PRIMARY);
        bar.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

        // Left – branding
        JLabel brand = new JLabel("🏥  Hospital Bed & Ward Management System");
        brand.setFont(AppTheme.FONT_SECTION.deriveFont(14f));
        brand.setForeground(AppTheme.TEXT_ON_DARK);

        // Right – user session info
        JPanel sessionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        sessionPanel.setOpaque(false);

        JLabel actingLabel = new JLabel("Logged in as:");
        actingLabel.setFont(AppTheme.FONT_SMALL);
        actingLabel.setForeground(new Color(0xA8C4D4));

        JComboBox<User> userSwitcher = new JComboBox<>();
        for (User u : allUsers) userSwitcher.addItem(u);
        userSwitcher.setSelectedItem(initialUser);
        userSwitcher.setFont(AppTheme.FONT_BODY);
        userSwitcher.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int idx, boolean isSel, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, idx, isSel, hasFocus);
                if (value instanceof User u)
                    setText(u.getName() + "  (" + u.getClass().getSimpleName() + ")");
                setFont(AppTheme.FONT_BODY);
                return this;
            }
        });
        userSwitcher.setPreferredSize(new Dimension(220, 26));

        JLabel roleTag = new JLabel("[" + initialUser.getClass().getSimpleName().toUpperCase() + "]");
        roleTag.setFont(AppTheme.FONT_SMALL.deriveFont(Font.BOLD));
        roleTag.setForeground(AppTheme.ACCENT_LIGHT);

        userSwitcher.addActionListener(e -> {
            User sel = (User) userSwitcher.getSelectedItem();
            if (sel == null) return;
            roleTag.setText("[" + sel.getClass().getSimpleName().toUpperCase() + "]");
            notifUI.setCurrentUserId(sel.getUserId());
            // Apply RBAC dynamically when user role switches
            applyRbac(sel, tabs, admissionUI, transferUI, wardStatusUI, reportUI, nurseUI, notifUI, accountUI);
        });

        sessionPanel.add(actingLabel);
        sessionPanel.add(userSwitcher);
        sessionPanel.add(roleTag);

        bar.add(brand, BorderLayout.WEST);
        bar.add(sessionPanel, BorderLayout.EAST);
        return bar;
    }

    // RBAC helper – control tab access based on user role and permissions
    private static void applyRbac(User user, JTabbedPane tabs,
            AdmissionUI admissionUI, TransferUI transferUI,
            WardStatusUI wardStatusUI, ReportUI reportUI,
            NurseAssignmentUI nurseUI, NotificationUI notifUI, AccountUI accountUI) {

        List<String> perms = user.getPermissions();
        boolean isAdmin   = perms.contains("MANAGE_USERS");
        boolean isDoctor  = perms.contains("ADMIT_PATIENT");
        boolean isNurse   = perms.contains("UPDATE_BED_STATUS") && !isAdmin;
        boolean isPatient = perms.contains("VIEW_OWN_ADMISSION") && !isAdmin;

        // Tab 0: Admit Patient – Doctor + Admin
        tabs.setEnabledAt(0, isDoctor || isAdmin);
        // Tab 1: Transfer/Cancel – Doctor + Admin + Patient (view-only)
        tabs.setEnabledAt(1, isDoctor || isAdmin || isPatient);
        // Tab 2: Ward Status – Nurse + Admin + Doctor
        tabs.setEnabledAt(2, isNurse || isAdmin || isDoctor);
        // Tab 3: Reports – Admin only
        tabs.setEnabledAt(3, isAdmin);
        // Tab 4: Assign Nurse – Admin only
        tabs.setEnabledAt(4, isAdmin);
        // Tab 5: Notifications – All users
        tabs.setEnabledAt(5, true);
        // Tab 6: Accounts – Admin only
        tabs.setEnabledAt(6, isAdmin);

        // Focus the first enabled tab
        for (int i = 0; i < tabs.getTabCount(); i++) {
            if (tabs.isEnabledAt(i)) { tabs.setSelectedIndex(i); break; }
        }
    }

    private static JFrame buildSplashFrame() {
        JFrame f = new JFrame();
        f.setUndecorated(true);
        f.setVisible(false);
        return f;
    }

    // Demo data
    private static void seedSampleData(HospitalDataStore ds) {
        try {
            DataLoader.SeedData data = DataLoader.loadSeedData();
            
            // Save all wards
            for (Ward w : data.wards) {
                ds.saveWard(w);
            }
            
            // Save all users (patients, nurses, doctors, admins)
            for (Patient p : data.patients) {
                ds.saveUser(p);
            }
            for (Nurse n : data.nurses) {
                ds.saveUser(n);
            }
            for (Doctor d : data.doctors) {
                ds.saveUser(d);
            }
            for (Admin a : data.admins) {
                ds.saveUser(a);
            }
            
            System.out.println("Demo seed data loaded successfully:");
            System.out.println("  - Wards: " + data.wards.size());
            System.out.println("  - Patients: " + data.patients.size());
            System.out.println("  - Nurses: " + data.nurses.size());
            System.out.println("  - Doctors: " + data.doctors.size());
            System.out.println("  - Admins: " + data.admins.size());
        } catch (Exception e) {
            System.err.println("Failed to load seed data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}