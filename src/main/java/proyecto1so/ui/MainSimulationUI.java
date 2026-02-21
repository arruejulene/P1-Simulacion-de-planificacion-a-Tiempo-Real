package proyecto1so.ui;

import java.awt.BorderLayout;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Ellipse2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import proyecto1so.clock.GlobalClock;
import proyecto1so.cpu.CPUScheduler;
import proyecto1so.io.IOEventGenerator;
import proyecto1so.loader.LoadResult;
import proyecto1so.loader.ProcessLoader;
import proyecto1so.memory.SuspensionPolicy;
import proyecto1so.model.Process;
import proyecto1so.scheduler.EDFStrategy;
import proyecto1so.scheduler.FCFSStrategy;
import proyecto1so.scheduler.PriorityPreemptiveStrategy;
import proyecto1so.scheduler.RoundRobinStrategy;
import proyecto1so.scheduler.SRTStrategy;
import proyecto1so.scheduler.SchedulerStrategy;

public class MainSimulationUI extends JFrame {

    private static final String STRAT_RR = "RoundRobin";
    private static final String STRAT_FCFS = "FCFS";
    private static final String STRAT_SRT = "SRT";
    private static final String STRAT_PRIO = "PriorityPreemptive";
    private static final String STRAT_EDF = "EDF";
    private static final String PAGE_MEMORY = "Memory management";
    private static final String PAGE_MISSION = "Mission control";
    private static final String PAGE_METRICS = "Metrics";
    private static final String[] SAT_SUBSYSTEMS = {
            "TELEMETRY", "PAYLOAD", "ADCS", "THERMAL", "POWER", "COMMS", "NAV", "EPS", "OBC"
    };
    private static final String[] SAT_ACTIONS = {
            "CAPTURE", "FILTER", "SYNC", "CHECK", "CALIBRATE", "TRACK", "ENCODE", "DOWNLINK", "UPLINK"
    };

    // Space palette
    private static final Color SPACE_BG = new Color(8, 12, 28);
    private static final Color SPACE_PANEL = new Color(16, 24, 52);
    private static final Color SPACE_INPUT = new Color(22, 32, 70);
    private static final Color SPACE_TEXT = new Color(232, 235, 255);
    private static final Color SPACE_MUTED = new Color(176, 185, 230);
    private static final Color SPACE_ACCENT = new Color(167, 139, 250);
    private static final Color SPACE_ACCENT_2 = new Color(99, 102, 241);

    private final JTextField jsonPathField = new JTextField("sample-data/processes.json");
    private final JTextField cycleField = new JTextField("300");
    private final JCheckBox useJsonCheck = new JCheckBox("Use JSON", false);
    private final JComboBox<String> pageSelector = new JComboBox<>(
            new String[]{PAGE_MEMORY, PAGE_MISSION, PAGE_METRICS}
    );
    private final JComboBox<String> strategyCombo = new JComboBox<>(
            new String[]{STRAT_RR, STRAT_FCFS, STRAT_SRT, STRAT_PRIO, STRAT_EDF}
    );

    private final JLabel runningLabel = new JLabel("Running: -");
    private final JLabel modeLabel = new JLabel("Mode: USER");
    private final JLabel kpiLabel = new JLabel("CPU Util: 0.00% | Deadlines: 0/0");
    private final JLabel memoryCycleLabel = new JLabel("Cycle: 0");
    private final JLabel missionCycleLabel = new JLabel("Cycle: 0");
    private final JLabel metricsCycleLabel = new JLabel("Cycle: 0");
    private final JLabel missionRunningProcessLabel = new JLabel("-");
    private final JLabel missionRunningDeadlineLabel = new JLabel("DeadlineRem: -");

    private final JTextArea logArea = new JTextArea();

    private final ProcessTableModel readyModel = new ProcessTableModel();
    private final ProcessTableModel blockedModel = new ProcessTableModel();
    private final ProcessTableModel readySuspModel = new ProcessTableModel();
    private final ProcessTableModel blockedSuspModel = new ProcessTableModel();
    private final ProcessTableModel terminatedModel = new ProcessTableModel();
    private final MissionQueueTableModel missionReadyModel = new MissionQueueTableModel();
    private final MissionQueueTableModel missionBlockedModel = new MissionQueueTableModel();
    private final XYSeries cpuUtilSeries = new XYSeries("CPU Utilization (%)");
    private final XYSeries memUtilSeries = new XYSeries("Memory Utilization (%)");

    private GlobalClock clock;
    private CPUScheduler cpu;
    private IOEventGenerator ioGenerator;
    private Timer uiTimer;
    private CardLayout pageLayout;
    private JPanel pageContainer;

    private boolean started = false;
    private boolean clockStartedOnce = false;
    private int lastCpuUtilTickPlotted = -1;
    private int lastMemUtilTickPlotted = -1;
    private long randomSeed = 87364512L;
    private int nextPid = 1;
    private int irqCounter = 1;

    public MainSimulationUI() {
        super("RTOS Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(buildPageSelectorPanel(), BorderLayout.NORTH);
        add(buildPagesPanel(), BorderLayout.CENTER);

        buildSimulationEngine(300);
        setupUiTimer();
        setupFieldValidation();
        applySpaceTheme();
    }

    private JPanel buildPageSelectorPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        JPanel inner = new JPanel(new GridLayout(1, 2, 6, 6));
        inner.add(new JLabel("Page"));
        inner.add(pageSelector);
        top.add(inner, BorderLayout.WEST);

        pageSelector.addActionListener(e -> {
            String selected = (String) pageSelector.getSelectedItem();
            if (selected != null && pageLayout != null && pageContainer != null) {
                pageLayout.show(pageContainer, selected);
            }
        });
        return top;
    }

    private JPanel buildPagesPanel() {
        pageLayout = new CardLayout();
        pageContainer = new JPanel(pageLayout);
        pageContainer.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        pageContainer.add(buildMemoryManagementPage(), PAGE_MEMORY);
        pageContainer.add(buildMissionControlPage(), PAGE_MISSION);
        pageContainer.add(buildMetricsPage(), PAGE_METRICS);

        pageLayout.show(pageContainer, PAGE_MISSION);
        pageSelector.setSelectedItem(PAGE_MISSION);
        return pageContainer;
    }

    private JPanel buildMemoryManagementPage() {
        JPanel memoryPage = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.add(buildPageHeader("Memory management", memoryCycleLabel), BorderLayout.NORTH);
        top.add(buildMemoryUtilChartPanel(), BorderLayout.CENTER);
        memoryPage.add(top, BorderLayout.NORTH);
        memoryPage.add(buildCenterPanel(), BorderLayout.CENTER);
        return memoryPage;
    }

    private JPanel buildMissionControlPage() {
        JPanel missionPage = new JPanel(new BorderLayout(8, 8));
        JPanel top = new JPanel(new BorderLayout(6, 6));
        top.add(buildPageHeader("Mission control", missionCycleLabel), BorderLayout.NORTH);
        top.add(buildControlPanel(), BorderLayout.CENTER);
        missionPage.add(top, BorderLayout.NORTH);
        missionPage.add(buildMissionQueuesPanel(), BorderLayout.CENTER);
        return missionPage;
    }

    private JPanel buildMissionQueuesPanel() {
        JPanel center = new JPanel(new GridLayout(1, 3, 8, 8));
        center.add(wrapTable("Ready", new JTable(missionReadyModel)));

        JPanel middle = new JPanel(new GridLayout(3, 1, 6, 6));
        JPanel topSlot = buildRunningProcessPanel();
        JPanel centerSlot = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 28));
        JPanel bottomBlank = new JPanel();

        JButton emergencyBtn = createEmergencyButton();
        emergencyBtn.addActionListener(e -> triggerRandomExternalInterrupt());
        centerSlot.add(emergencyBtn);

        middle.add(topSlot);
        middle.add(centerSlot);
        middle.add(bottomBlank);
        center.add(middle);

        center.add(wrapTable("Blocked", new JTable(missionBlockedModel)));
        return center;
    }

    private JPanel buildRunningProcessPanel() {
        JPanel card = new JPanel(new GridLayout(3, 1, 2, 2));
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(SPACE_ACCENT, 1), "Running Process");
        tb.setTitleColor(SPACE_TEXT);
        card.setBorder(tb);
        missionRunningProcessLabel.setHorizontalAlignment(JLabel.CENTER);
        missionRunningProcessLabel.setFont(missionRunningProcessLabel.getFont().deriveFont(20f));
        missionRunningDeadlineLabel.setHorizontalAlignment(JLabel.CENTER);
        card.add(new JLabel(""));
        card.add(missionRunningProcessLabel);
        card.add(missionRunningDeadlineLabel);
        return card;
    }

    private JButton createEmergencyButton() {
        return new JButton("") {
            private Shape shape;
            {
                putClientProperty("emergencyButton", Boolean.TRUE);
                setToolTipText("Emergency (Interruption)");
                setForeground(Color.WHITE);
                setFont(getFont().deriveFont(12.5f));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setPreferredSize(new Dimension(132, 132));
                setMinimumSize(new Dimension(132, 132));
                setMaximumSize(new Dimension(132, 132));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int labelBand = 24;
                int circleAreaH = Math.max(1, h - labelBand);
                int d = Math.min(w, circleAreaH);
                int x = (w - d) / 2;
                int y = (circleAreaH - d) / 2;
                int pressOffset = getModel().isArmed() ? 1 : 0;

                // Squared base plate
                int plateMargin = 2;
                int plateArc = 16;
                int px = x + plateMargin;
                int py = y + plateMargin + pressOffset;
                int pw = d - (plateMargin * 2);
                int ph = d - (plateMargin * 2);

                GradientPaint plate = new GradientPaint(
                        px, py, new Color(37, 45, 67),
                        px, py + ph, new Color(20, 28, 48)
                );
                g2.setPaint(plate);
                g2.fillRoundRect(px, py, pw, ph, plateArc, plateArc);
                g2.setColor(new Color(129, 140, 165, 180));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(px, py, pw - 1, ph - 1, plateArc, plateArc);

                // Base shadow
                g2.setColor(new Color(0, 0, 0, 70));
                g2.fillOval(x + 10, y + 12 + pressOffset, d - 20, d - 20);

                // Metallic outer ring (dark -> light)
                GradientPaint ringOuter = new GradientPaint(
                        x, y + pressOffset, new Color(43, 52, 73),
                        x, y + d + pressOffset, new Color(129, 140, 165)
                );
                g2.setPaint(ringOuter);
                g2.fillOval(x + 6, y + 6 + pressOffset, d - 12, d - 12);

                // Ring inner bevel
                GradientPaint ringInner = new GradientPaint(
                        x, y + 7 + pressOffset, new Color(148, 163, 184),
                        x, y + d - 7 + pressOffset, new Color(51, 65, 85)
                );
                g2.setPaint(ringInner);
                g2.fillOval(x + 12, y + 12 + pressOffset, d - 24, d - 24);

                // Red glass dome core
                GradientPaint dome = new GradientPaint(
                        x, y + 14 + pressOffset, new Color(248, 113, 113),
                        x, y + d - 14 + pressOffset, new Color(127, 29, 29)
                );
                g2.setPaint(dome);
                g2.fillOval(x + 18, y + 18 + pressOffset, d - 36, d - 36);

                // Deep center tint for depth
                GradientPaint innerTint = new GradientPaint(
                        x, y + 22 + pressOffset, new Color(220, 38, 38, 160),
                        x, y + d - 18 + pressOffset, new Color(69, 10, 10, 175)
                );
                g2.setPaint(innerTint);
                g2.fillOval(x + 26, y + 26 + pressOffset, d - 52, d - 52);

                // Glass highlight
                g2.setColor(new Color(255, 255, 255, 95));
                g2.fillOval(x + 28, y + 26 + pressOffset, d - 56, (d - 36) / 3);

                // Fine outlines
                g2.setStroke(new BasicStroke(1.2f));
                g2.setColor(new Color(15, 23, 42, 180));
                g2.drawOval(x + 6, y + 6 + pressOffset, d - 13, d - 13);
                g2.setColor(new Color(190, 24, 24, 190));
                g2.drawOval(x + 18, y + 18 + pressOffset, d - 37, d - 37);

                // Label on square base
                String baseLabel = "EMERGENCY";
                g2.setFont(getFont().deriveFont(11.5f));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int tx = px + (pw - fm.stringWidth(baseLabel)) / 2;
                int ty = h - 8;
                g2.setColor(new Color(226, 232, 240));
                g2.drawString(baseLabel, tx, ty);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Border is painted in paintComponent.
            }

            @Override
            public boolean contains(int x, int y) {
                int labelBand = 24;
                int circleAreaH = Math.max(1, getHeight() - labelBand);
                int d = Math.min(getWidth(), circleAreaH);
                int ox = (getWidth() - d) / 2;
                int oy = (circleAreaH - d) / 2;
                if (shape == null || !shape.getBounds().equals(getBounds())) {
                    shape = new Ellipse2D.Float(ox, oy, d, d);
                }
                return shape.contains(x, y);
            }
        };
    }

    private JPanel buildPlaceholderPage(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JLabel("Coming soon"), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMetricsPage() {
        JPanel metricsPage = new JPanel(new BorderLayout(8, 8));
        metricsPage.add(buildPageHeader("Metrics", metricsCycleLabel), BorderLayout.NORTH);
        metricsPage.add(buildMetricsCenterPanel(), BorderLayout.CENTER);
        metricsPage.add(buildBottomPanel(), BorderLayout.SOUTH);
        return metricsPage;
    }

    private JPanel buildPageHeader(String title, JLabel cycleLabel) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        header.add(new JLabel(title), BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.add(cycleLabel);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildControlPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 6, 6));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel row1 = new JPanel(new GridLayout(1, 7, 6, 6));
        row1.add(new JLabel("JSON Path"));
        row1.add(jsonPathField);
        row1.add(useJsonCheck);
        row1.add(new JLabel("Cycle(ms)"));
        row1.add(cycleField);
        row1.add(new JLabel("Strategy"));
        strategyCombo.addActionListener(e -> applySelectedStrategy());
        row1.add(strategyCombo);

        JPanel row2 = new JPanel(new GridLayout(1, 1, 6, 6));
        JButton genRandomBtn = createRoundedActionButton("Generate 20 Random Processes");
        genRandomBtn.addActionListener(e -> generateRandomBatch());
        row2.add(genRandomBtn);

        JPanel row3 = new JPanel(new GridLayout(1, 5, 6, 6));
        JButton startBtn = createRoundedActionButton("Start");
        startBtn.addActionListener(e -> startSimulation());
        row3.add(startBtn);
        JButton stopBtn = createRoundedActionButton("Stop");
        stopBtn.addActionListener(e -> stopSimulation());
        row3.add(stopBtn);
        JButton resetBtn = createRoundedActionButton("Reset Engine");
        resetBtn.addActionListener(e -> resetSimulationEngine());
        row3.add(resetBtn);
        row3.add(runningLabel);
        row3.add(modeLabel);

        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        return panel;
    }

    private JPanel buildCenterPanel() {
        JPanel main = new JPanel(new GridLayout(2, 2, 8, 8));
        main.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        main.add(wrapTable("Ready", new JTable(readyModel)));
        main.add(wrapTable("Blocked", new JTable(blockedModel)));
        main.add(wrapTable("Ready Suspended", new JTable(readySuspModel)));
        main.add(wrapTable("Blocked Suspended", new JTable(blockedSuspModel)));
        return main;
    }

    private JPanel buildMemoryUtilChartPanel() {
        XYSeriesCollection dataset = new XYSeriesCollection(memUtilSeries);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Memory Utilization vs Time",
                "Cycle",
                "Memory Utilization (%)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        XYPlot plot = chart.getXYPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 105.0);
        range.setAutoRange(false);
        chart.setBackgroundPaint(SPACE_PANEL);
        chart.getTitle().setPaint(SPACE_TEXT);
        plot.setBackgroundPaint(SPACE_INPUT);
        plot.setDomainGridlinePaint(new Color(88, 98, 150));
        plot.setRangeGridlinePaint(new Color(88, 98, 150));
        plot.getDomainAxis().setLabelPaint(SPACE_TEXT);
        plot.getDomainAxis().setTickLabelPaint(SPACE_MUTED);
        plot.getRangeAxis().setLabelPaint(SPACE_TEXT);
        plot.getRangeAxis().setTickLabelPaint(SPACE_MUTED);
        plot.getRenderer().setSeriesPaint(0, SPACE_ACCENT);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setPreferredSize(new Dimension(800, 220));
        chartPanel.setBackground(SPACE_PANEL);
        chartPanel.setOpaque(true);

        JPanel wrapper = new JPanel(new BorderLayout());
        TitledBorder tb = BorderFactory.createTitledBorder("Real-time Memory Utilization");
        tb.setTitleColor(SPACE_TEXT);
        wrapper.setBorder(tb);
        wrapper.setBackground(SPACE_PANEL);
        wrapper.setOpaque(true);
        wrapper.add(chartPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildMetricsCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        panel.add(buildCpuUtilChartPanel());

        JPanel bottom = new JPanel(new GridLayout(1, 2, 8, 8));
        bottom.add(wrapTable("Terminated", new JTable(terminatedModel)));
        JPanel logPanel = new JPanel(new BorderLayout(0, 6));
        JLabel logLabel = new JLabel("Log");
        logLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
        logPanel.add(logLabel, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        bottom.add(logPanel);

        panel.add(bottom);
        return panel;
    }

    private JPanel buildCpuUtilChartPanel() {
        XYSeriesCollection dataset = new XYSeriesCollection(cpuUtilSeries);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "CPU Utilization vs Time",
                "Cycle",
                "CPU Utilization (%)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        XYPlot plot = chart.getXYPlot();
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 105.0);
        range.setAutoRange(false);
        chart.setBackgroundPaint(SPACE_PANEL);
        chart.getTitle().setPaint(SPACE_TEXT);
        plot.setBackgroundPaint(SPACE_INPUT);
        plot.setDomainGridlinePaint(new Color(88, 98, 150));
        plot.setRangeGridlinePaint(new Color(88, 98, 150));
        plot.getDomainAxis().setLabelPaint(SPACE_TEXT);
        plot.getDomainAxis().setTickLabelPaint(SPACE_MUTED);
        plot.getRangeAxis().setLabelPaint(SPACE_TEXT);
        plot.getRangeAxis().setTickLabelPaint(SPACE_MUTED);
        plot.getRenderer().setSeriesPaint(0, SPACE_ACCENT_2);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(false);
        chartPanel.setBackground(SPACE_PANEL);
        chartPanel.setOpaque(true);

        JPanel wrapper = new JPanel(new BorderLayout());
        TitledBorder tb = BorderFactory.createTitledBorder("Real-time CPU Utilization");
        tb.setTitleColor(SPACE_TEXT);
        wrapper.setBorder(tb);
        wrapper.setBackground(SPACE_PANEL);
        wrapper.setOpaque(true);
        wrapper.add(chartPanel, BorderLayout.CENTER);
        return wrapper;
    }

    private JButton createRoundedActionButton(String text) {
        return new JButton(text) {
            {
                putClientProperty("roundedAction", Boolean.TRUE);
                setForeground(Color.WHITE);
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isArmed() ? new Color(74, 48, 150) : new Color(92, 63, 176);
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.setColor(new Color(129, 102, 214));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // painted in paintComponent
            }
        };
    }

    private JPanel buildBottomPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
        p.add(kpiLabel, BorderLayout.CENTER);
        logArea.setEditable(false);
        return p;
    }

    private JScrollPane wrapTable(String title, JTable table) {
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        TitledBorder tb = BorderFactory.createTitledBorder(new LineBorder(SPACE_ACCENT, 1), title);
        tb.setTitleColor(SPACE_TEXT);
        scroll.setBorder(tb);
        return scroll;
    }

    private void buildSimulationEngine(int tickMs) {
        cpu = new CPUScheduler(6, SuspensionPolicy.LOWEST_PRIORITY);
        cpu.setTickDurationMs(tickMs);
        cpu.setStrategy(new RoundRobinStrategy(2));
        clock = new GlobalClock(tickMs);
        clock.addListener(cpu);
        ioGenerator = null;
        started = false;
        clockStartedOnce = false;
        cpuUtilSeries.clear();
        memUtilSeries.clear();
        lastCpuUtilTickPlotted = -1;
        lastMemUtilTickPlotted = -1;
        log("[ENGINE] New engine created (tickMs=" + tickMs + ")");
    }

    private void setupUiTimer() {
        uiTimer = new Timer(200, e -> refreshUiSnapshot());
        uiTimer.start();
    }

    private void startSimulation() {
        if (started) {
            JOptionPane.showMessageDialog(
                    this,
                    "Simulation ongoing.",
                    "Start Warning",
                    JOptionPane.WARNING_MESSAGE
            );
            log("[START] Simulation already running");
            return;
        }
        if (!applyCycleFromField()) {
            return;
        }
        if (clockStartedOnce) {
            JOptionPane.showMessageDialog(
                    this,
                    "User has to reset engine before starting.",
                    "Start Warning",
                    JOptionPane.WARNING_MESSAGE
            );
            log("[START] Clock thread cannot be restarted. Use Reset Engine first.");
            return;
        }

        int loadedCount = 0;
        boolean jsonUnavailableOrInvalid = false;
        if (useJsonCheck.isSelected()) {
            LoadResult result = loadProcessesFromJson(jsonPathField.getText());
            loadedCount = result.getProcesses().length;
            jsonUnavailableOrInvalid = loadedCount == 0 && result.getErrors().length > 0;
            if (jsonUnavailableOrInvalid) {
                JOptionPane.showMessageDialog(
                        this,
                        "JSON file unavaliable or invalid. Simulation will generate random processes.",
                        "JSON Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        } else {
            log("[START] JSON disabled by user");
        }

        if (loadedCount == 0) {
            int count = 20;
            addRandomProcesses(count);
            if (useJsonCheck.isSelected()) {
                log("[START] JSON empty/invalid, generated random processes: " + count);
            } else {
                log("[START] Generated random processes: " + count);
            }
        }

        applySelectedStrategy();
        clock.start();
        ioGenerator = new IOEventGenerator(cpu, 500, 1200, 1, 3, randomSeed ^ 0x13579BDFL);
        ioGenerator.start();
        started = true;
        clockStartedOnce = true;
        log("[START] Simulation started (random IO events enabled)");
    }

    private void stopSimulation() {
        if (!started) {
            log("[STOP] Simulation is not running");
            return;
        }
        clock.stopClock();
        if (ioGenerator != null) {
            ioGenerator.stopGenerator();
            ioGenerator = null;
        }
        started = false;
        log("[STOP] Clock stop requested");
    }

    private void resetSimulationEngine() {
        if (started) {
            stopSimulation();
        }
        Integer cycleMs = readCycleMsOrShowError();
        if (cycleMs == null) return;
        buildSimulationEngine(cycleMs);
        refreshUiSnapshot();
    }

    private void applySelectedStrategy() {
        SchedulerStrategy strategy = createStrategy((String) strategyCombo.getSelectedItem());
        cpu.setStrategy(strategy);
        log("[STRATEGY] Applied: " + cpu.getStrategyNameSnapshot());
    }

    private SchedulerStrategy createStrategy(String name) {
        if (STRAT_FCFS.equals(name)) return new FCFSStrategy();
        if (STRAT_SRT.equals(name)) return new SRTStrategy();
        if (STRAT_PRIO.equals(name)) return new PriorityPreemptiveStrategy();
        if (STRAT_EDF.equals(name)) return new EDFStrategy();
        return new RoundRobinStrategy(2);
    }

    private LoadResult loadProcessesFromJson(String path) {
        ProcessLoader loader = new ProcessLoader();
        LoadResult result = loader.loadFromJson(path);

        Process[] loaded = result.getProcesses();
        String[] errors = result.getErrors();
        for (int i = 0; i < errors.length; i++) {
            log("[JSON ERROR] " + errors[i]);
        }

        for (int i = 0; i < loaded.length; i++) {
            cpu.addProcess(loaded[i]);
            nextPid++;
        }
        if (loaded.length > 0) log("[JSON] Loaded processes: " + loaded.length);
        return result;
    }

    private void generateRandomBatch() {
        int count = 20;
        addRandomProcesses(count);
        JOptionPane.showMessageDialog(
                this,
                "Success: 20 random processes added to queue.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
        log("[RANDOM] Added processes: " + count);
    }

    private void addRandomProcesses(int count) {
        if (count <= 0) return;
        for (int i = 0; i < count; i++) {
            cpu.addProcess(nextRandomProcess());
        }
    }

    private Process nextRandomProcess() {
        String pid = nextSatelliteProcessName();
        int burst = randBetween(4, 20);
        int priority = randBetween(1, 5);
        int arrival = randBetween(1, 15);
        int deadlineOffset = randBetween(15, 90);
        int deadline = arrival + deadlineOffset;
        return new Process(pid, burst, arrival, priority, deadline);
    }

    private String nextSatelliteProcessName() {
        String subsystem = SAT_SUBSYSTEMS[randBetween(0, SAT_SUBSYSTEMS.length - 1)];
        String action = SAT_ACTIONS[randBetween(0, SAT_ACTIONS.length - 1)];
        return subsystem + "_" + action + "_" + (nextPid++);
    }

    private void triggerRandomExternalInterrupt() {
        int isrTicks = randBetween(1, 3);
        String irqName = "UI-IRQ-" + irqCounter++;
        boolean ok = cpu.triggerExternalInterrupt(irqName, isrTicks);
        log("[INT] " + irqName + " requested (isrTicks=" + isrTicks + ", accepted=" + ok + ")");
    }

    private void refreshUiSnapshot() {
        int tick = clock == null ? 0 : clock.getCurrentTick();
        Process running = cpu == null ? null : cpu.getCurrentProcessSnapshot();
        Process[] ready = cpu == null ? new Process[0] : cpu.snapshotReadyQueue();
        Process[] blocked = cpu == null ? new Process[0] : cpu.snapshotBlockedQueue();
        Process[] readySusp = cpu == null ? new Process[0] : cpu.snapshotReadySuspendedQueue();
        Process[] blockedSusp = cpu == null ? new Process[0] : cpu.snapshotBlockedSuspendedQueue();
        Process[] done = cpu == null ? new Process[0] : cpu.snapshotFinishedQueue();

        readyModel.setData(ready, tick);
        blockedModel.setData(blocked, tick);
        readySuspModel.setData(readySusp, tick);
        blockedSuspModel.setData(blockedSusp, tick);
        terminatedModel.setData(done, tick);
        missionReadyModel.setData(ready);
        missionBlockedModel.setData(blocked);

        memoryCycleLabel.setText("Cycle: " + tick);
        missionCycleLabel.setText("Cycle: " + tick);
        metricsCycleLabel.setText("Cycle: " + tick);
        runningLabel.setText("Running: " + (running == null ? "-" : running.getPid()));
        modeLabel.setText("Mode: " + (running == null ? "SO/IDLE" : "USER"));
        if (running == null) {
            missionRunningProcessLabel.setText("-");
            missionRunningDeadlineLabel.setText("DeadlineRem: -");
        } else {
            missionRunningProcessLabel.setText(running.getPid());
            int deadlineRem = running.getDeadlineRemaining(tick);
            missionRunningDeadlineLabel.setText(
                    "DeadlineRem: " + (deadlineRem == Integer.MAX_VALUE ? "-" : String.valueOf(deadlineRem))
            );
        }

        int totalTicks = cpu.getTotalTicksSnapshot();
        int busyTicks = cpu.getBusyTicksSnapshot();
        int met = cpu.getDeadlinesMetSnapshot();
        int missed = cpu.getDeadlinesMissedSnapshot();
        int inRam = cpu.getInRamCountSnapshot();
        int maxInRam = cpu.getMaxInRamSnapshot();
        double util = totalTicks <= 0 ? 0.0 : (100.0 * busyTicks / totalTicks);
        double memUtil = maxInRam <= 0 ? 0.0 : (100.0 * inRam / maxInRam);
        int totalDeadline = met + missed;
        double deadlineRate = totalDeadline <= 0 ? 0.0 : (100.0 * met / totalDeadline);
        int terminatedCount = done.length;
        double throughput = totalTicks <= 0 ? 0.0 : ((double) terminatedCount / (double) totalTicks);
        double avgResponse = 0.0;
        if (terminatedCount > 0) {
            double sumResponse = 0.0;
            for (int i = 0; i < terminatedCount; i++) {
                Process p = done[i];
                int firstRun = p.getFirstRunTick() == null ? p.getArrivalTime() : p.getFirstRunTick();
                sumResponse += (firstRun - p.getArrivalTime());
            }
            avgResponse = sumResponse / terminatedCount;
        }

        kpiLabel.setText(String.format(
                "CPU Util: %.2f%% | Deadline success: %.2f%% (%d/%d) | Throughput: %.4f processes/tick | Avg Response: %.2f ticks",
                util, deadlineRate, met, totalDeadline, throughput, avgResponse
        ));

        if (tick != lastCpuUtilTickPlotted) {
            cpuUtilSeries.add(tick, util);
            lastCpuUtilTickPlotted = tick;
        }
        if (tick != lastMemUtilTickPlotted) {
            memUtilSeries.add(tick, memUtil);
            lastMemUtilTickPlotted = tick;
        }
    }

    private Integer readCycleMsOrShowError() {
        try {
            int parsed = Integer.parseInt(cycleField.getText().trim());
            if (parsed <= 0) {
                showCycleError("Cycle(ms) must be a positive integer.");
                return null;
            }
            return parsed;
        } catch (Exception e) {
            showCycleError("Cycle(ms) must be a valid integer.");
            return null;
        }
    }

    private void setupFieldValidation() {
        cycleField.addActionListener(e -> applyCycleFromField());
        cycleField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                applyCycleFromField();
            }
        });
    }

    private boolean applyCycleFromField() {
        Integer cycleMs = readCycleMsOrShowError();
        if (cycleMs == null) return false;

        if (clock != null) clock.setTickMillis(cycleMs);
        if (cpu != null) cpu.setTickDurationMs(cycleMs);
        log("[CLOCK] Tick duration set to " + cycleMs + " ms");
        return true;
    }

    private void showCycleError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Cycle", JOptionPane.ERROR_MESSAGE);
    }

    private int randBetween(int min, int max) {
        if (max <= min) return min;
        int bound = (max - min) + 1;
        return min + nextInt(bound);
    }

    private int nextInt(int bound) {
        randomSeed = (randomSeed * 1103515245L + 12345L) & 0x7fffffffL;
        return (int) (randomSeed % bound);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void applySpaceTheme() {
        getContentPane().setBackground(SPACE_BG);
        styleRecursively(getContentPane());
        repaint();
    }

    private void styleRecursively(java.awt.Component c) {
        if (c instanceof JPanel) {
            c.setBackground(SPACE_PANEL);
            c.setForeground(SPACE_TEXT);
        } else if (c instanceof JLabel) {
            c.setForeground(SPACE_TEXT);
        } else if (c instanceof JTextField) {
            JTextField tf = (JTextField) c;
            tf.setBackground(SPACE_INPUT);
            tf.setForeground(SPACE_TEXT);
            tf.setCaretColor(SPACE_TEXT);
            tf.setBorder(new LineBorder(SPACE_ACCENT_2, 1));
        } else if (c instanceof JTextArea) {
            JTextArea ta = (JTextArea) c;
            ta.setBackground(SPACE_INPUT);
            ta.setForeground(SPACE_TEXT);
            ta.setCaretColor(SPACE_TEXT);
            ta.setBorder(new LineBorder(SPACE_ACCENT_2, 1));
        } else if (c instanceof JComboBox) {
            @SuppressWarnings("rawtypes")
            JComboBox cb = (JComboBox) c;
            cb.setBackground(SPACE_INPUT);
            cb.setForeground(SPACE_TEXT);
            cb.setOpaque(true);
            cb.setBorder(new LineBorder(SPACE_ACCENT_2, 1, true));
            cb.setRenderer(new DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(
                        javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    java.awt.Component comp = super.getListCellRendererComponent(
                            list, value, index, isSelected, cellHasFocus);
                    if (isSelected) {
                        comp.setBackground(new Color(91, 33, 182));
                        comp.setForeground(Color.WHITE);
                    } else {
                        comp.setBackground(SPACE_INPUT);
                        comp.setForeground(SPACE_TEXT);
                    }
                    return comp;
                }
            });
            cb.setUI(new BasicComboBoxUI() {
                @Override
                protected JButton createArrowButton() {
                    JButton arrow = new JButton("▼");
                    arrow.setForeground(SPACE_TEXT);
                    arrow.setBackground(SPACE_INPUT);
                    arrow.setBorder(new LineBorder(SPACE_ACCENT_2, 1, true));
                    arrow.setFocusPainted(false);
                    return arrow;
                }

                @Override
                public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    g.setColor(SPACE_INPUT);
                    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                }
            });
        } else if (c instanceof JCheckBox) {
            JCheckBox cb = (JCheckBox) c;
            cb.setBackground(SPACE_PANEL);
            cb.setForeground(SPACE_TEXT);
        } else if (c instanceof JButton) {
            JButton b = (JButton) c;
            Object rounded = b.getClientProperty("roundedAction");
            Object emergency = b.getClientProperty("emergencyButton");
            if (!Boolean.TRUE.equals(emergency) && (rounded == null || !Boolean.TRUE.equals(rounded))) {
                b.setBackground(SPACE_ACCENT);
                b.setForeground(SPACE_BG);
                b.setFocusPainted(false);
                b.setBorder(new LineBorder(new Color(221, 214, 254), 1, true));
                b.setOpaque(true);
                b.setContentAreaFilled(true);
            }
        } else if (c instanceof JTable) {
            JTable t = (JTable) c;
            t.setBackground(SPACE_INPUT);
            t.setForeground(SPACE_TEXT);
            t.setOpaque(true);
            t.setGridColor(new Color(88, 98, 150));
            t.setSelectionBackground(new Color(91, 33, 182));
            t.setSelectionForeground(Color.WHITE);
            t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    java.awt.Component comp = super.getTableCellRendererComponent(
                            table, value, isSelected, hasFocus, row, column);
                    if (isSelected) {
                        comp.setBackground(new Color(91, 33, 182));
                        comp.setForeground(Color.WHITE);
                    } else {
                        comp.setBackground(SPACE_INPUT);
                        comp.setForeground(SPACE_TEXT);
                    }
                    return comp;
                }
            });
            if (t.getTableHeader() != null) {
                t.getTableHeader().setBackground(new Color(35, 47, 94));
                t.getTableHeader().setForeground(SPACE_TEXT);
            }
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            sp.setBackground(SPACE_PANEL);
            sp.getViewport().setBackground(SPACE_INPUT);
            sp.getViewport().setOpaque(true);
        } else if (c instanceof ChartPanel) {
            c.setBackground(SPACE_PANEL);
            c.setForeground(SPACE_TEXT);
            ((ChartPanel) c).setOpaque(true);
        } else if (c instanceof JSplitPane) {
            c.setBackground(SPACE_PANEL);
        }

        if (c instanceof java.awt.Container) {
            java.awt.Component[] children = ((java.awt.Container) c).getComponents();
            for (int i = 0; i < children.length; i++) {
                styleRecursively(children[i]);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainSimulationUI ui = new MainSimulationUI();
            ui.setVisible(true);
        });
    }
}
