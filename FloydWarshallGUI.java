import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FloydWarshallGUI extends JFrame {
    private static final int INF = Integer.MAX_VALUE / 2;
    private JTextField sizeField;
    private JTable inputTable, resultTable;
    private DefaultTableModel inputTableModel, resultTableModel;
    private BSCS2_RODRIGO_fw fw;
    private GraphPanel graphPanel;
    private JButton addNodeButton, removeNodeButton, demoGraphButton;

    public FloydWarshallGUI() {
        fw = new BSCS2_RODRIGO_fw();
        setTitle("Floyd-Warshall Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(30, 30, 30));

        JPanel controlPanel = createControlPanel();
        JPanel inputPanel = createInputPanel();
        JPanel resultPanel = createResultPanel();
        graphPanel = new GraphPanel();
        
        JSplitPane matrixSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputPanel, resultPanel);
        matrixSplitPane.setResizeWeight(0.5);
        matrixSplitPane.setBackground(new Color(40, 40, 40));
        matrixSplitPane.setDividerSize(5);
        
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, matrixSplitPane, graphPanel);
        mainSplitPane.setResizeWeight(0.7);
        mainSplitPane.setBackground(new Color(40, 40, 40));
        mainSplitPane.setDividerSize(5);

        add(controlPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);

        updateTables(4);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(40, 40, 40));

        JLabel sizeLabel = new JLabel("Matrix Size (2-8):");
        sizeLabel.setForeground(Color.WHITE);
        sizeField = new JTextField("4", 5);
        sizeField.setBackground(new Color(50, 50, 50));
        sizeField.setForeground(Color.WHITE);
        sizeField.setCaretColor(Color.WHITE);

        JButton updateSizeButton = new JButton("Update Size");
        JButton computeButton = new JButton("Compute Shortest Paths");
        JButton clearButton = new JButton("Clear");
        addNodeButton = new JButton("Add Node");
        removeNodeButton = new JButton("Remove Last Node");
        demoGraphButton = new JButton("Show Demo Graph");

        styleButton(updateSizeButton, new Color(100, 150, 255));
        styleButton(computeButton, new Color(50, 200, 50));
        styleButton(clearButton, new Color(255, 100, 100));
        styleButton(addNodeButton, new Color(102, 205, 170));
        styleButton(removeNodeButton, new Color(220, 20, 60));
        styleButton(demoGraphButton, new Color(135, 206, 250));

        updateSizeButton.addActionListener(e -> {
            try {
                int size = Integer.parseInt(sizeField.getText());
                if (size >= 2 && size <= 8) {
                    updateTables(size);
                    graphPanel.updateGraph(new int[size][size]);
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a size between 2 and 8.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        });

        computeButton.addActionListener(e -> computeFloydWarshall());

        clearButton.addActionListener(e -> {
            updateTables(inputTableModel.getRowCount());
            graphPanel.updateGraph(new int[inputTableModel.getRowCount()][inputTableModel.getRowCount()]);
        });

        addNodeButton.addActionListener(e -> {
            int currentSize = inputTableModel.getRowCount();
            if (currentSize < 8) {
                int[][] currentGraph = getCurrentGraph();
                int[][] newGraph = fw.addNode(currentGraph);
                updateTables(newGraph.length);
                setGraphToTable(newGraph);
                graphPanel.updateGraph(newGraph);
            } else {
                JOptionPane.showMessageDialog(this, "Maximum size (8) reached.");
            }
        });

        removeNodeButton.addActionListener(e -> {
            int currentSize = inputTableModel.getRowCount();
            if (currentSize > 2) {
                int[][] currentGraph = getCurrentGraph();
                int[][] newGraph = fw.removeLastNode(currentGraph);
                updateTables(newGraph.length);
                setGraphToTable(newGraph);
                graphPanel.updateGraph(newGraph);
            } else {
                JOptionPane.showMessageDialog(this, "Minimum size (2) reached.");
            }
        });

        demoGraphButton.addActionListener(e -> {
            int[][] demoGraph = fw.getDemoGraph();
            makeGraphSymmetric(demoGraph); // Ensure demo graph is symmetric
            updateTables(demoGraph.length);
            setGraphToTable(demoGraph);
            graphPanel.updateGraph(demoGraph);
        });

        panel.add(sizeLabel);
        panel.add(sizeField);
        panel.add(updateSizeButton);
        panel.add(computeButton);
        panel.add(clearButton);
        panel.add(addNodeButton);
        panel.add(removeNodeButton);
        panel.add(demoGraphButton);
        return panel;
    }

    private void makeGraphSymmetric(int[][] graph) {
        int size = graph.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (graph[i][j] != INF) {
                    graph[j][i] = graph[i][j]; // Copy i->j to j->i
                } else if (graph[j][i] != INF) {
                    graph[i][j] = graph[j][i]; // Copy j->i to i->j
                }
            }
        }
    }

    private void styleButton(JButton button, Color baseColor) {
        button.setBackground(baseColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFont(new Font("Arial", Font.BOLD, 14));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(baseColor.brighter());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(baseColor);
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Input Adjacency Matrix (∞ for no edge)", 
            0, 0, null, Color.WHITE));
        panel.setBackground(new Color(40, 40, 40));
        
        inputTableModel = new DefaultTableModel();
        inputTable = new JTable(inputTableModel);
        inputTable.setRowHeight(30);
        inputTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputTable.setGridColor(new Color(80, 80, 80));
        inputTable.setShowGrid(true);
        inputTable.setBackground(new Color(50, 50, 50));
        inputTable.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(inputTable);
        scrollPane.setBackground(new Color(40, 40, 40));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Shortest Paths Matrix", 
            0, 0, null, Color.WHITE));
        panel.setBackground(new Color(40, 40, 40));
        
        resultTableModel = new DefaultTableModel();
        resultTable = new JTable(resultTableModel);
        resultTable.setRowHeight(30);
        resultTable.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultTable.setGridColor(new Color(80, 80, 80));
        resultTable.setShowGrid(true);
        resultTable.setEnabled(false);
        resultTable.setBackground(new Color(50, 50, 50));
        resultTable.setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(resultTable);
        scrollPane.setBackground(new Color(40, 40, 40));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private int[][] getCurrentGraph() {
        int size = inputTableModel.getRowCount();
        int[][] graph = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Object value = inputTableModel.getValueAt(i, j);
                String strValue = value != null ? value.toString() : "";
                if (strValue.trim().isEmpty() || strValue.equals("∞") || strValue.equalsIgnoreCase("INF")) {
                    graph[i][j] = INF;
                } else {
                    try {
                        graph[i][j] = Integer.parseInt(strValue.trim());
                    } catch (NumberFormatException e) {
                        graph[i][j] = INF;
                    }
                }
            }
        }
        makeGraphSymmetric(graph); // Ensure symmetry for undirected graph
        return graph;
    }

    private void setGraphToTable(int[][] graph) {
        makeGraphSymmetric(graph); // Ensure symmetry before updating table
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                String value = graph[i][j] == INF ? "∞" : String.valueOf(graph[i][j]);
                inputTableModel.setValueAt(value, i, j);
            }
        }
    }

    private void updateTables(int size) {
        inputTableModel.setRowCount(0);
        inputTableModel.setColumnCount(0);
        String[] columnNames = new String[size];
        for (int i = 0; i < size; i++) {
            columnNames[i] = String.valueOf(i);
        }
        inputTableModel.setDataVector(new Object[size][size], columnNames);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                inputTableModel.setValueAt("", i, j);
            }
        }

        resultTableModel.setRowCount(0);
        resultTableModel.setColumnCount(0);
        resultTableModel.setDataVector(new Object[size][size], columnNames);
        
        sizeField.setText(String.valueOf(size));
    }

    private void computeFloydWarshall() {
        try {
            int size = inputTableModel.getRowCount();
            int[][] graph = new int[size][size];
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    Object value = inputTableModel.getValueAt(i, j);
                    String strValue = value != null ? value.toString() : "";
                    if (strValue.trim().isEmpty() || strValue.equals("∞") || strValue.equalsIgnoreCase("INF")) {
                        graph[i][j] = INF;
                    } else {
                        graph[i][j] = Integer.parseInt(strValue.trim());
                    }
                }
            }
            
            int[][] result = fw.floydWarshall(graph);
            
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    String value = result[i][j] == INF ? "∞" : String.valueOf(result[i][j]);
                    resultTableModel.setValueAt(value, i, j);
                }
            }
            
            graphPanel.updateGraph(graph);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers or '∞'.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage());
        }
    }

    class GraphPanel extends JPanel {
        private int[][] graph;
        private ArrayList<Point> nodePositions;
        private ArrayList<Float> nodeScales;
        private int draggedEdgeI = -1, draggedEdgeJ = -1;
        private boolean isDragging = false;
        private int draggedNode = -1; // Tracks the node being dragged

        public GraphPanel() {
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), 
                "Graph Visualization", 
                0, 0, null, Color.WHITE));
            setBackground(new Color(30, 30, 30));
            graph = new int[0][0];
            nodePositions = new ArrayList<>();
            nodeScales = new ArrayList<>();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Check for node dragging first
                    int size = nodePositions.size();
                    for (int i = 0; i < size; i++) {
                        Point p = nodePositions.get(i);
                        float scale = nodeScales.get(i);
                        int nodeSize = (int) (40 * scale);
                        Rectangle nodeBounds = new Rectangle(p.x - nodeSize / 2, p.y - nodeSize / 2, nodeSize, nodeSize);
                        if (nodeBounds.contains(e.getPoint())) {
                            draggedNode = i;
                            isDragging = true;
                            repaint();
                            return;
                        }
                    }

                    // Check for edge dragging if no node is selected
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            if (graph[i][j] != INF) {
                                if (isNearEdge(e.getPoint(), i, j)) {
                                    draggedEdgeI = i;
                                    draggedEdgeJ = j;
                                    isDragging = true;
                                    repaint();
                                    return;
                                }
                            }
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging && draggedNode != -1) {
                        // Stop dragging node
                        draggedNode = -1;
                        isDragging = false;
                        repaint();
                    } else if (isDragging && draggedEdgeI != -1 && draggedEdgeJ != -1) {
                        // Handle edge weight change
                        int newWeight = promptNewWeight();
                        if (newWeight >= 0) {
                            graph[draggedEdgeI][draggedEdgeJ] = newWeight;
                            graph[draggedEdgeJ][draggedEdgeI] = newWeight; // Enforce symmetry
                            setGraphToTable(graph);
                            repaint();
                        }
                        draggedEdgeI = -1;
                        draggedEdgeJ = -1;
                        isDragging = false;
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging && draggedNode != -1) {
                        // Update position of dragged node
                        nodePositions.set(draggedNode, e.getPoint());
                        repaint();
                    } else if (isDragging && draggedEdgeI != -1 && draggedEdgeJ != -1) {
                        repaint();
                    }
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    int size = nodePositions.size();
                    boolean found = false;

                    // Check if mouse is over a node
                    for (int i = 0; i < size; i++) {
                        Point p = nodePositions.get(i);
                        float scale = nodeScales.get(i);
                        int nodeSize = (int) (40 * scale);
                        Rectangle nodeBounds = new Rectangle(p.x - nodeSize / 2, p.y - nodeSize / 2, nodeSize, nodeSize);
                        if (nodeBounds.contains(e.getPoint())) {
                            setCursor(new Cursor(Cursor.MOVE_CURSOR));
                            found = true;
                            break;
                        }
                    }

                    // Check if mouse is over an edge if not over a node
                    if (!found) {
                        for (int i = 0; i < size; i++) {
                            for (int j = 0; j < size; j++) {
                                if (graph[i][j] != INF) {
                                    if (isNearEdge(e.getPoint(), i, j)) {
                                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (found) break;
                        }
                    }

                    if (!found) {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            });

            Timer timer = new Timer(50, e -> {
                for (int i = 0; i < nodeScales.size(); i++) {
                    float scale = nodeScales.get(i);
                    scale = 1.0f + 0.1f * (float) Math.sin(System.currentTimeMillis() * 0.002 + i);
                    nodeScales.set(i, scale);
                }
                repaint();
            });
            timer.start();
        }

        private boolean isNearEdge(Point p, int i, int j) {
            Point p1 = nodePositions.get(i);
            Point p2 = nodePositions.get(j);
            double dist = pointToLineDistance(p, p1, p2);
            return dist < 15;
        }

        private double pointToLineDistance(Point p, Point p1, Point p2) {
            double x = p.x, y = p.y;
            double x1 = p1.x, y1 = p1.y;
            double x2 = p2.x, y2 = p2.y;
            double num = Math.abs((y2 - y1) * x - (x2 - x1) * y + x2 * y1 - y2 * x1);
            double den = Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
            if (den == 0) return Double.MAX_VALUE;

            double dot = ((x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)) / (den * den);
            if (dot < 0 || dot > 1) return Double.MAX_VALUE;

            return num / den;
        }

        private int promptNewWeight() {
            String input = JOptionPane.showInputDialog(this, "Enter new edge weight (or '∞' for infinity):");
            if (input == null) return -1; // Cancelled
            input = input.trim();
            if (input.equals("∞") || input.equalsIgnoreCase("INF")) return INF;
            try {
                int weight = Integer.parseInt(input);
                return weight >= 0 ? weight : INF;
            } catch (NumberFormatException e) {
                return INF;
            }
        }

        public void updateGraph(int[][] newGraph) {
            this.graph = newGraph;
            int size = graph.length;
            
            if (nodePositions.size() != size) {
                nodePositions.clear();
                nodeScales.clear();
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = Math.min(getWidth(), getHeight()) / 3;
                
                for (int i = 0; i < size; i++) {
                    double angle = 2 * Math.PI * i / size;
                    int x = (int) (centerX + radius * Math.cos(angle));
                    int y = (int) (centerY + radius * Math.sin(angle));
                    nodePositions.add(new Point(x, y));
                    nodeScales.add(1.0f);
                }
            }
            
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (graph.length == 0) return;

            for (int i = 0; i < graph.length; i++) {
                for (int j = i + 1; j < graph.length; j++) { // Only draw upper triangle to avoid duplicate edges
                    if (graph[i][j] != INF) {
                        Point p1 = nodePositions.get(i);
                        Point p2 = nodePositions.get(j);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.setColor(new Color(100, 150, 255));
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);

                        int midX = (p1.x + p2.x) / 2;
                        int midY = (p1.y + p2.y) / 2;
                        g2d.setColor(new Color(255, 69, 58));
                        g2d.setFont(new Font("Arial", Font.BOLD, 14));
                        g2d.drawString(String.valueOf(graph[i][j]), midX, midY);

                        if (isDragging && ((i == draggedEdgeI && j == draggedEdgeJ) || (i == draggedEdgeJ && j == draggedEdgeI))) {
                            g2d.setColor(new Color(255, 255, 0));
                            g2d.setStroke(new BasicStroke(3));
                            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                        }
                    }
                }
            }

            for (int i = 0; i < nodePositions.size(); i++) {
                Point p = nodePositions.get(i);
                float scale = nodeScales.get(i);
                int size = (int) (40 * scale);
                g2d.setColor(new Color(147, 112, 219));
                g2d.fillOval(p.x - size / 2 - 5, p.y - size / 2 - 5, size + 10, size + 10);
                g2d.setColor(new Color(138, 43, 226));
                g2d.fillOval(p.x - size / 2, p.y - size / 2, size, size);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(i), p.x - 5, p.y + 5);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(600, 400); // Larger panel for clarity
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FloydWarshallGUI().setVisible(true);
        });
    }
}

class BSCS2_RODRIGO_fw {
    private static final int INF = Integer.MAX_VALUE / 2;

    public int[][] floydWarshall(int[][] graph) {
        int n = graph.length;
        int[][] dist = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = graph[i][j];
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (dist[i][k] != INF && dist[k][j] != INF) {
                        dist[i][j] = Math.min(dist[i][j], dist[i][k] + dist[k][j]);
                    }
                }
            }
        }

        return dist;
    }

    public int[][] addNode(int[][] graph) {
        int n = graph.length;
        int[][] newGraph = new int[n + 1][n + 1];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newGraph[i][j] = graph[i][j];
            }
        }

        for (int i = 0; i < n + 1; i++) {
            newGraph[i][n] = INF;
            newGraph[n][i] = INF;
        }

        newGraph[n][n] = 0;

        return newGraph;
    }

    public int[][] removeLastNode(int[][] graph) {
        int n = graph.length;
        if (n <= 1) return graph;

        int[][] newGraph = new int[n - 1][n - 1];

        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                newGraph[i][j] = graph[i][j];
            }
        }

        return newGraph;
    }

    public int[][] getDemoGraph() {
        int[][] demoGraph = {
            {0, 5, INF, 10},
            {5, 0, 3, INF},
            {INF, 3, 0, 1},
            {10, INF, 1, 0}
        };
        return demoGraph; // Symmetric matrix for undirected graph
    }
}