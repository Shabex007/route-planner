import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

public class RoutePlanner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Route Planner");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 800);

            MapPanel mapPanel = new MapPanel();
            frame.add(mapPanel, BorderLayout.CENTER);

            // Toolbar with buttons
            JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);

            JButton addMarkerBtn = createStyledButton("Add Marker", new Color(46, 125, 50));
            JButton drawRouteBtn = createStyledButton("Draw Route", new Color(30, 136, 229));
            JButton clearBtn = createStyledButton("Clear All", new Color(198, 40, 40));

            addMarkerBtn.addActionListener(e -> {
                mapPanel.setMode(MapPanel.Mode.ADD_MARKER);
                setActiveButton(addMarkerBtn, new Color(46, 125, 50));
                drawRouteBtn.setBackground(new Color(30, 136, 229));
            });

            drawRouteBtn.addActionListener(e -> {
                mapPanel.setMode(MapPanel.Mode.DRAW_ROUTE);
                setActiveButton(drawRouteBtn, new Color(30, 136, 229));
                addMarkerBtn.setBackground(new Color(46, 125, 50));
            });

            clearBtn.addActionListener(e -> {
                mapPanel.clearAll();
                addMarkerBtn.setBackground(new Color(46, 125, 50));
                drawRouteBtn.setBackground(new Color(30, 136, 229));
            });

            toolBar.add(addMarkerBtn);
            toolBar.add(drawRouteBtn);
            toolBar.add(clearBtn);

            frame.add(toolBar, BorderLayout.NORTH);
            frame.setVisible(true);
        });
    }

    private static JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private static void setActiveButton(JButton button, Color activeColor) {
        button.setBackground(activeColor.darker());
    }
}

class MapPanel extends JPanel {
    enum Mode {
        ADD_MARKER, DRAW_ROUTE, NONE
    }

    private Mode currentMode = Mode.NONE;
    private List<Point> markers = new ArrayList<>();
    private List<List<Point>> routes = new ArrayList<>();
    private List<Point> currentRoute = new ArrayList<>();
    private Image backgroundImage;
    private final Color[] markerColors = {
            new Color(231, 76, 60), // Red
            new Color(41, 128, 185), // Blue
            new Color(39, 174, 96), // Green
            new Color(155, 89, 182), // Purple
            new Color(241, 196, 15) // Yellow
    };
    private int currentColorIndex = 0;

    public MapPanel() {
        setBackground(new Color(240, 240, 240));

        // Load a background image (replace with your own image path)
        try {
            backgroundImage = new ImageIcon("map.jpg").getImage();
        } catch (Exception e) {
            System.out.println("Using blank background");
            backgroundImage = null;
        }

        // Mouse listeners for adding markers and drawing routes
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentMode == Mode.ADD_MARKER) {
                    markers.add(e.getPoint());
                    currentColorIndex = (currentColorIndex + 1) % markerColors.length;
                    repaint();
                } else if (currentMode == Mode.DRAW_ROUTE && !currentRoute.isEmpty()) {
                    currentRoute.add(e.getPoint());
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (currentMode == Mode.DRAW_ROUTE) {
                    currentRoute = new ArrayList<>();
                    currentRoute.add(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (currentMode == Mode.DRAW_ROUTE && !currentRoute.isEmpty()) {
                    routes.add(new ArrayList<>(currentRoute));
                    currentRoute.clear();
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (currentMode == Mode.DRAW_ROUTE && !currentRoute.isEmpty()) {
                    currentRoute.add(e.getPoint());
                    repaint();
                }
            }
        });
    }

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public void clearAll() {
        markers.clear();
        routes.clear();
        currentRoute.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(new Color(230, 230, 230));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // Draw grid if no background image
            g2d.setColor(new Color(220, 220, 220));
            int gridSize = 20;
            for (int x = 0; x < getWidth(); x += gridSize) {
                g2d.drawLine(x, 0, x, getHeight());
            }
            for (int y = 0; y < getHeight(); y += gridSize) {
                g2d.drawLine(0, y, getWidth(), y);
            }
        }

        // Draw routes
        g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (List<Point> route : routes) {
            drawRoute(g2d, route);
        }

        // Draw current route being drawn
        if (!currentRoute.isEmpty()) {
            g2d.setColor(new Color(231, 76, 60, 200)); // Semi-transparent red
            g2d.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawRoute(g2d, currentRoute);
        }

        // Draw markers
        for (int i = 0; i < markers.size(); i++) {
            Point marker = markers.get(i);
            Color color = markerColors[i % markerColors.length];
            drawLocationPin(g2d, marker, color);
        }
    }

    private void drawRoute(Graphics2D g2d, List<Point> route) {
        if (route.size() < 2)
            return;

        // Create gradient for the route
        Point start = route.get(0);
        Point end = route.get(route.size() - 1);
        GradientPaint gradient = new GradientPaint(
                start.x, start.y, new Color(52, 152, 219),
                end.x, end.y, new Color(41, 128, 185),
                true);
        g2d.setPaint(gradient);

        Point prev = route.get(0);
        for (int i = 1; i < route.size(); i++) {
            Point current = route.get(i);
            g2d.drawLine(prev.x, prev.y, current.x, current.y);
            prev = current;
        }
    }

    private void drawLocationPin(Graphics2D g2d, Point point, Color color) {
        int pinWidth = 24;
        int pinHeight = 34;
        int x = point.x - pinWidth / 2;
        int y = point.y - pinHeight;

        // Save original transform
        AffineTransform oldTransform = g2d.getTransform();

        // Create pin shape
        GeneralPath pin = new GeneralPath();
        pin.moveTo(x + pinWidth / 2, y);
        pin.curveTo(
                x + pinWidth, y,
                x + pinWidth, y + pinHeight * 0.7f,
                x + pinWidth / 2, y + pinHeight);
        pin.curveTo(
                x, y + pinHeight * 0.7f,
                x, y,
                x + pinWidth / 2, y);
        pin.closePath();

        // Draw pin shadow
        g2d.translate(2, 2);
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fill(pin);
        g2d.setTransform(oldTransform);

        // Draw pin body
        g2d.setColor(color);
        g2d.fill(pin);

        // Draw pin outline
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(pin);

        // Draw pin center dot
        g2d.setColor(Color.WHITE);
        g2d.fillOval(point.x - 4, point.y - pinHeight / 2 - 4, 8, 8);
    }
}