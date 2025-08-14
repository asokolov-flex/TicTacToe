package com.xowin;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game implements GameScreen {

    private static final Random random = new Random();

    private int width;

    private int height;

    private static int cellSize;

    private int timerStep = 0;

    private boolean showGrid = true;

    private boolean showCoordinates = false;

    private boolean isMessageShown = false;

    private JPanel rootPanel;

    private Image backgroundImg;

    private CellLabel[][] labelCells;

    private ScheduledExecutorService timer;

    private int turnTimerMs = 0;

    private int score = 0;

    private final Map<Color, java.awt.Color> colorMap = new HashMap<>();

    private JLabel dialogLabel = new JLabel();

    private java.awt.Color dialogBackGround = null;

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String userClassName = System.getProperty("sun.java.command");

        Class<?> userClass = ClassLoader.getSystemClassLoader().loadClass(userClassName);

        Object userInstance = userClass.getConstructor().newInstance();

        ((Game) userInstance).start();
    }

    public void start() {
        JFrame jFrame = new JFrame();

        jFrame.setDefaultCloseOperation(3);

        this.mapColors();

        this.initialize();

        jFrame.addKeyListener(new GameKeyListener());

        jFrame.setTitle("JavaRush Game");

        jFrame.setResizable(false);

        jFrame.setUndecorated(true);

        jFrame.getContentPane().add(this.createSwingContent(), "Center");

        jFrame.pack();

        jFrame.setLocationRelativeTo((Component) null);

        jFrame.setVisible(true);

    }

    private JPanel createSwingContent() {
        this.createSwingBorderImage();

        this.rootPanel = new GameRootPanel();

        Dimension prefSize = new Dimension(this.width * cellSize + 250, this.height * cellSize + 110 + 140);

        this.rootPanel.setPreferredSize(prefSize);

        this.rootPanel.setBorder(BorderFactory.createEmptyBorder(110, 125, 140, 125));

        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                CellLabel currentCell = this.labelCells[y][x];

                if (currentCell != null) {
                    currentCell.setPreferredSize(new Dimension(cellSize, cellSize));

                    this.rootPanel.add(currentCell);

                }

                if (this.showGrid && currentCell != null) {
                    currentCell.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 1, false));

                }
            }

        }

        return this.rootPanel;

    }

    private void createSwingBorderImage() {
        URL imageUrl = Thread.currentThread().getContextClassLoader().getResource("screen.png");

        try {
            this.backgroundImg = ImageIO.read(Objects.requireNonNull(imageUrl)).getScaledInstance(this.width * cellSize + 250, this.height * cellSize + 110 + 140, 0);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public void setScreenSize(int width, int height) {
        this.width = width < 3 ? 3 : (width > 100 ? 100 : width);

        this.height = height < 3 ? 3 : (height > 100 ? 100 : height);

        cellSize = 800 / this.width < 600 / this.height ? 800 / this.width : 600 / this.height;

        this.labelCells = new CellLabel[this.height][this.width];

        for (int y = 0; y < this.height; ++y) {
            for (int x = 0; x < this.width; ++x) {
                this.labelCells[y][x] = new CellLabel(x, y);
            }
        }
    }

    public int getScreenWidth() {
        return this.width;
    }

    public int getScreenHeight() {
        return this.height;
    }

    public void setCellColor(int x, int y, Color color) {
        if (color != null && color != Color.NONE) {
            this.labelCells[y][x].setBackground(this.toAwtColor(color));
        }
    }

    public Color getCellColor(int x, int y) {
        return this.toEngineColor(this.labelCells[y][x].getBackground());
    }

    public void showGrid(boolean isShow) {
        this.showGrid = isShow;
    }

    public void showCoordinates(boolean isShow) {
        this.showCoordinates = isShow;
    }

    public void setCellValue(int x, int y, String value) {
        CellLabel currentCell = this.labelCells[y][x];

        if (!currentCell.getText().equals(value)) {
            float cellFontSize = value.length() <= 4 ? (float) cellSize * 0.4F : (float) (cellSize / value.length());

            currentCell.setFont(currentCell.getFont().deriveFont(cellFontSize));

            currentCell.setText(value);
        }
    }

    public String getCellValue(int x, int y) {
        String text = this.labelCells[y][x].getText();
        return text != null ? text : "";
    }

    public void setCellNumber(int x, int y, int value) {
        this.setCellValue(x, y, String.valueOf(value));
    }

    public int getCellNumber(int x, int y) {
        String value = this.getCellValue(x, y);

        if (value != null && !value.isEmpty()) {
            int result = 0;

            try {
                result = Integer.valueOf(value);

            } catch (NumberFormatException var6) {
            }

            return result;

        } else {
            return 0;
        }
    }

    public void setCellTextColor(int x, int y, Color color) {
        this.labelCells[y][x].setForeground(this.toAwtColor(color));
    }

    public Color getCellTextColor(int x, int y) {
        return this.toEngineColor(this.labelCells[y][x].getForeground());
    }

    public void setTurnTimer(int timeMs) {
        this.turnTimerMs = timeMs;

        if (this.timer != null) {
            this.timer.shutdownNow();

        }

        if (timeMs > 0) {
            this.timer = Executors.newSingleThreadScheduledExecutor();

            Runnable runnable = new TimerTask() {
                public void run() {
                    if (!Game.this.isMessageShown) {
                        Game.this.onTurn(++Game.this.timerStep);

                        if (Game.this.rootPanel != null) {
                            Game.this.rootPanel.repaint();

                        }

                    }

                }
            };

            this.timer.scheduleAtFixedRate(runnable, (long) this.turnTimerMs, (long) this.turnTimerMs, TimeUnit.MILLISECONDS);
        }
    }

    public void stopTurnTimer() {
        if (this.timer != null) {
            this.timer.shutdownNow();
        }

    }

    public int getRandomNumber(int max) {
        return random.nextInt(max);

    }

    public int getRandomNumber(int min, int max) {
        return random.nextInt(max - min) + min;

    }

    public void initialize() {

    }

    public void onMouseLeftClick(int x, int y) {

    }

    public void onMouseRightClick(int x, int y) {

    }

    public void onKeyPress(Key key) {

    }

    public void onKeyReleased(Key key) {

    }

    public void onTurn(int step) {

    }

    public void setCellTextSize(int x, int y, int size) {
        size = size > 100 ? 100 : size;

        double fontSize = (double) cellSize * ((double) size / (double) 100.0F);

        this.labelCells[y][x].setFont(this.labelCells[y][x].getFont().deriveFont((float) fontSize));

    }

    public int getCellTextSize(int x, int y) {
        return this.labelCells[y][x].getFont().getSize() * 100 / cellSize;
    }

    public void setCellValueEx(int x, int y, Color cellColor, String value) {
        this.setCellValue(x, y, value);
        this.setCellColor(x, y, cellColor);
    }

    public void setCellValueEx(int x, int y, Color cellColor, String value, Color textColor) {
        this.setCellValueEx(x, y, cellColor, value);
        this.setCellTextColor(x, y, textColor);
    }

    public void setCellValueEx(int x, int y, Color cellColor, String value, Color textColor, int textSize) {
        this.setCellValueEx(x, y, cellColor, value, textColor);
        this.setCellTextSize(x, y, textSize);
    }

    public void showMessageDialog(Color cellColor, String message, Color textColor, int textSize) {
        this.isMessageShown = true;

        this.dialogLabel = new JLabel(message, 0);

        this.dialogLabel.setFont(new Font("Verdana", 1, textSize));

        this.dialogLabel.setForeground(this.toAwtColor(textColor));

        this.dialogBackGround = cellColor == Color.NONE ? null : this.toAwtColor(cellColor);

        if (this.rootPanel != null) {
            this.rootPanel.repaint();

        }

    }

    private void hideMessageDialog() {
        this.isMessageShown = false;

        this.dialogLabel = new JLabel();

        this.dialogBackGround = null;

        if (this.rootPanel != null) {
            this.rootPanel.repaint();

        }

    }

    public void setScore(int score) {
        this.score = score;

        if (this.rootPanel != null) {

        }

    }

    public void setLives(int lives) {

    }

    private void mapColors() {
        this.colorMap.put(Color.NONE, new java.awt.Color(0.0F, 0.0F, 0.0F, 0.0F));
        this.colorMap.put(Color.TRANSPARENT, new java.awt.Color(0.0F, 0.0F, 0.0F, 0.0F));
        this.colorMap.put(Color.ALICEBLUE, new java.awt.Color(0.9411765F, 0.972549F, 1.0F));
        this.colorMap.put(Color.ANTIQUEWHITE, new java.awt.Color(0.98039216F, 0.92156863F, 0.84313726F));
        this.colorMap.put(Color.AQUA, new java.awt.Color(0.0F, 1.0F, 1.0F));
        this.colorMap.put(Color.AQUAMARINE, new java.awt.Color(0.49803922F, 1.0F, 0.83137256F));
        this.colorMap.put(Color.AZURE, new java.awt.Color(0.9411765F, 1.0F, 1.0F));
        this.colorMap.put(Color.BEIGE, new java.awt.Color(0.9607843F, 0.9607843F, 0.8627451F));
        this.colorMap.put(Color.BISQUE, new java.awt.Color(1.0F, 0.89411765F, 0.76862746F));
        this.colorMap.put(Color.BLACK, new java.awt.Color(0.0F, 0.0F, 0.0F));
        this.colorMap.put(Color.BLANCHEDALMOND, new java.awt.Color(1.0F, 0.92156863F, 0.8039216F));
        this.colorMap.put(Color.BLUE, new java.awt.Color(0.0F, 0.0F, 1.0F));
        this.colorMap.put(Color.BLUEVIOLET, new java.awt.Color(0.5411765F, 0.16862746F, 0.8862745F));
        this.colorMap.put(Color.BROWN, new java.awt.Color(0.64705884F, 0.16470589F, 0.16470589F));
        this.colorMap.put(Color.BURLYWOOD, new java.awt.Color(0.87058824F, 0.72156864F, 0.5294118F));
        this.colorMap.put(Color.CADETBLUE, new java.awt.Color(0.37254903F, 0.61960787F, 0.627451F));
        this.colorMap.put(Color.CHARTREUSE, new java.awt.Color(0.49803922F, 1.0F, 0.0F));
        this.colorMap.put(Color.CHOCOLATE, new java.awt.Color(0.8235294F, 0.4117647F, 0.11764706F));
        this.colorMap.put(Color.CORAL, new java.awt.Color(1.0F, 0.49803922F, 0.3137255F));
        this.colorMap.put(Color.CORNFLOWERBLUE, new java.awt.Color(0.39215687F, 0.58431375F, 0.92941177F));
        this.colorMap.put(Color.CORNSILK, new java.awt.Color(1.0F, 0.972549F, 0.8627451F));
        this.colorMap.put(Color.CRIMSON, new java.awt.Color(0.8627451F, 0.078431375F, 0.23529412F));
        this.colorMap.put(Color.CYAN, new java.awt.Color(0.0F, 1.0F, 1.0F));
        this.colorMap.put(Color.DARKBLUE, new java.awt.Color(0.0F, 0.0F, 0.54509807F));
        this.colorMap.put(Color.DARKCYAN, new java.awt.Color(0.0F, 0.54509807F, 0.54509807F));
        this.colorMap.put(Color.DARKGOLDENROD, new java.awt.Color(0.72156864F, 0.5254902F, 0.043137256F));
        this.colorMap.put(Color.DARKGRAY, new java.awt.Color(0.6627451F, 0.6627451F, 0.6627451F));
        this.colorMap.put(Color.DARKGREEN, new java.awt.Color(0.0F, 0.39215687F, 0.0F));
        this.colorMap.put(Color.DARKGREY, new java.awt.Color(0.6627451F, 0.6627451F, 0.6627451F));
        this.colorMap.put(Color.DARKKHAKI, new java.awt.Color(0.7411765F, 0.7176471F, 0.41960785F));
        this.colorMap.put(Color.DARKMAGENTA, new java.awt.Color(0.54509807F, 0.0F, 0.54509807F));
        this.colorMap.put(Color.DARKOLIVEGREEN, new java.awt.Color(0.33333334F, 0.41960785F, 0.18431373F));
        this.colorMap.put(Color.DARKORANGE, new java.awt.Color(1.0F, 0.54901963F, 0.0F));
        this.colorMap.put(Color.DARKORCHID, new java.awt.Color(0.6F, 0.19607843F, 0.8F));
        this.colorMap.put(Color.DARKRED, new java.awt.Color(0.54509807F, 0.0F, 0.0F));
        this.colorMap.put(Color.DARKSALMON, new java.awt.Color(0.9137255F, 0.5882353F, 0.47843137F));
        this.colorMap.put(Color.DARKSEAGREEN, new java.awt.Color(0.56078434F, 0.7372549F, 0.56078434F));
        this.colorMap.put(Color.DARKSLATEBLUE, new java.awt.Color(0.28235295F, 0.23921569F, 0.54509807F));
        this.colorMap.put(Color.DARKSLATEGRAY, new java.awt.Color(0.18431373F, 0.30980393F, 0.30980393F));
        this.colorMap.put(Color.DARKSLATEGREY, new java.awt.Color(0.18431373F, 0.30980393F, 0.30980393F));
        this.colorMap.put(Color.DARKTURQUOISE, new java.awt.Color(0.0F, 0.80784315F, 0.81960785F));
        this.colorMap.put(Color.DARKVIOLET, new java.awt.Color(0.5803922F, 0.0F, 0.827451F));
        this.colorMap.put(Color.DEEPPINK, new java.awt.Color(1.0F, 0.078431375F, 0.5764706F));
        this.colorMap.put(Color.DEEPSKYBLUE, new java.awt.Color(0.0F, 0.7490196F, 1.0F));
        this.colorMap.put(Color.DIMGRAY, new java.awt.Color(0.4117647F, 0.4117647F, 0.4117647F));
        this.colorMap.put(Color.DIMGREY, new java.awt.Color(0.4117647F, 0.4117647F, 0.4117647F));
        this.colorMap.put(Color.DODGERBLUE, new java.awt.Color(0.11764706F, 0.5647059F, 1.0F));
        this.colorMap.put(Color.FIREBRICK, new java.awt.Color(0.69803923F, 0.13333334F, 0.13333334F));
        this.colorMap.put(Color.FLORALWHITE, new java.awt.Color(1.0F, 0.98039216F, 0.9411765F));
        this.colorMap.put(Color.FORESTGREEN, new java.awt.Color(0.13333334F, 0.54509807F, 0.13333334F));
        this.colorMap.put(Color.FUCHSIA, new java.awt.Color(1.0F, 0.0F, 1.0F));
        this.colorMap.put(Color.GAINSBORO, new java.awt.Color(0.8627451F, 0.8627451F, 0.8627451F));
        this.colorMap.put(Color.GHOSTWHITE, new java.awt.Color(0.972549F, 0.972549F, 1.0F));
        this.colorMap.put(Color.GOLD, new java.awt.Color(1.0F, 0.84313726F, 0.0F));
        this.colorMap.put(Color.GOLDENROD, new java.awt.Color(0.85490197F, 0.64705884F, 0.1254902F));
        this.colorMap.put(Color.GRAY, new java.awt.Color(0.5019608F, 0.5019608F, 0.5019608F));
        this.colorMap.put(Color.GREEN, new java.awt.Color(0.0F, 0.5019608F, 0.0F));
        this.colorMap.put(Color.GREENYELLOW, new java.awt.Color(0.6784314F, 1.0F, 0.18431373F));
        this.colorMap.put(Color.GREY, new java.awt.Color(0.5019608F, 0.5019608F, 0.5019608F));
        this.colorMap.put(Color.HONEYDEW, new java.awt.Color(0.9411765F, 1.0F, 0.9411765F));
        this.colorMap.put(Color.HOTPINK, new java.awt.Color(1.0F, 0.4117647F, 0.7058824F));
        this.colorMap.put(Color.INDIANRED, new java.awt.Color(0.8039216F, 0.36078432F, 0.36078432F));
        this.colorMap.put(Color.INDIGO, new java.awt.Color(0.29411766F, 0.0F, 0.50980395F));
        this.colorMap.put(Color.IVORY, new java.awt.Color(1.0F, 1.0F, 0.9411765F));
        this.colorMap.put(Color.KHAKI, new java.awt.Color(0.9411765F, 0.9019608F, 0.54901963F));
        this.colorMap.put(Color.LAVENDER, new java.awt.Color(0.9019608F, 0.9019608F, 0.98039216F));
        this.colorMap.put(Color.LAVENDERBLUSH, new java.awt.Color(1.0F, 0.9411765F, 0.9607843F));
        this.colorMap.put(Color.LAWNGREEN, new java.awt.Color(0.4862745F, 0.9882353F, 0.0F));
        this.colorMap.put(Color.LEMONCHIFFON, new java.awt.Color(1.0F, 0.98039216F, 0.8039216F));
        this.colorMap.put(Color.LIGHTBLUE, new java.awt.Color(0.6784314F, 0.84705883F, 0.9019608F));
        this.colorMap.put(Color.LIGHTCORAL, new java.awt.Color(0.9411765F, 0.5019608F, 0.5019608F));
        this.colorMap.put(Color.LIGHTCYAN, new java.awt.Color(0.8784314F, 1.0F, 1.0F));
        this.colorMap.put(Color.LIGHTGOLDENRODYELLOW, new java.awt.Color(0.98039216F, 0.98039216F, 0.8235294F));
        this.colorMap.put(Color.LIGHTGRAY, new java.awt.Color(0.827451F, 0.827451F, 0.827451F));
        this.colorMap.put(Color.LIGHTGREEN, new java.awt.Color(0.5647059F, 0.93333334F, 0.5647059F));
        this.colorMap.put(Color.LIGHTGREY, new java.awt.Color(0.827451F, 0.827451F, 0.827451F));
        this.colorMap.put(Color.LIGHTPINK, new java.awt.Color(1.0F, 0.7137255F, 0.75686276F));
        this.colorMap.put(Color.LIGHTSALMON, new java.awt.Color(1.0F, 0.627451F, 0.47843137F));
        this.colorMap.put(Color.LIGHTSEAGREEN, new java.awt.Color(0.1254902F, 0.69803923F, 0.6666667F));
        this.colorMap.put(Color.LIGHTSKYBLUE, new java.awt.Color(0.5294118F, 0.80784315F, 0.98039216F));
        this.colorMap.put(Color.LIGHTSLATEGRAY, new java.awt.Color(0.46666667F, 0.53333336F, 0.6F));
        this.colorMap.put(Color.LIGHTSLATEGREY, new java.awt.Color(0.46666667F, 0.53333336F, 0.6F));
        this.colorMap.put(Color.LIGHTSTEELBLUE, new java.awt.Color(0.6901961F, 0.76862746F, 0.87058824F));
        this.colorMap.put(Color.LIGHTYELLOW, new java.awt.Color(1.0F, 1.0F, 0.8784314F));
        this.colorMap.put(Color.LIME, new java.awt.Color(0.0F, 1.0F, 0.0F));
        this.colorMap.put(Color.LIMEGREEN, new java.awt.Color(0.19607843F, 0.8039216F, 0.19607843F));
        this.colorMap.put(Color.LINEN, new java.awt.Color(0.98039216F, 0.9411765F, 0.9019608F));
        this.colorMap.put(Color.MAGENTA, new java.awt.Color(1.0F, 0.0F, 1.0F));
        this.colorMap.put(Color.MAROON, new java.awt.Color(0.5019608F, 0.0F, 0.0F));
        this.colorMap.put(Color.MEDIUMAQUAMARINE, new java.awt.Color(0.4F, 0.8039216F, 0.6666667F));
        this.colorMap.put(Color.MEDIUMBLUE, new java.awt.Color(0.0F, 0.0F, 0.8039216F));
        this.colorMap.put(Color.MEDIUMORCHID, new java.awt.Color(0.7294118F, 0.33333334F, 0.827451F));
        this.colorMap.put(Color.MEDIUMPURPLE, new java.awt.Color(0.5764706F, 0.4392157F, 0.85882354F));
        this.colorMap.put(Color.MEDIUMSEAGREEN, new java.awt.Color(0.23529412F, 0.7019608F, 0.44313726F));
        this.colorMap.put(Color.MEDIUMSLATEBLUE, new java.awt.Color(0.48235294F, 0.40784314F, 0.93333334F));
        this.colorMap.put(Color.MEDIUMSPRINGGREEN, new java.awt.Color(0.0F, 0.98039216F, 0.6039216F));
        this.colorMap.put(Color.MEDIUMTURQUOISE, new java.awt.Color(0.28235295F, 0.81960785F, 0.8F));
        this.colorMap.put(Color.MEDIUMVIOLETRED, new java.awt.Color(0.78039217F, 0.08235294F, 0.52156866F));
        this.colorMap.put(Color.MIDNIGHTBLUE, new java.awt.Color(0.09803922F, 0.09803922F, 0.4392157F));
        this.colorMap.put(Color.MINTCREAM, new java.awt.Color(0.9607843F, 1.0F, 0.98039216F));
        this.colorMap.put(Color.MISTYROSE, new java.awt.Color(1.0F, 0.89411765F, 0.88235295F));
        this.colorMap.put(Color.MOCCASIN, new java.awt.Color(1.0F, 0.89411765F, 0.70980394F));
        this.colorMap.put(Color.NAVAJOWHITE, new java.awt.Color(1.0F, 0.87058824F, 0.6784314F));
        this.colorMap.put(Color.NAVY, new java.awt.Color(0.0F, 0.0F, 0.5019608F));
        this.colorMap.put(Color.OLDLACE, new java.awt.Color(0.99215686F, 0.9607843F, 0.9019608F));
        this.colorMap.put(Color.OLIVE, new java.awt.Color(0.5019608F, 0.5019608F, 0.0F));
        this.colorMap.put(Color.OLIVEDRAB, new java.awt.Color(0.41960785F, 0.5568628F, 0.13725491F));
        this.colorMap.put(Color.ORANGE, new java.awt.Color(1.0F, 0.64705884F, 0.0F));
        this.colorMap.put(Color.ORANGERED, new java.awt.Color(1.0F, 0.27058825F, 0.0F));
        this.colorMap.put(Color.ORCHID, new java.awt.Color(0.85490197F, 0.4392157F, 0.8392157F));
        this.colorMap.put(Color.PALEGOLDENROD, new java.awt.Color(0.93333334F, 0.9098039F, 0.6666667F));
        this.colorMap.put(Color.PALEGREEN, new java.awt.Color(0.59607846F, 0.9843137F, 0.59607846F));
        this.colorMap.put(Color.PALETURQUOISE, new java.awt.Color(0.6862745F, 0.93333334F, 0.93333334F));
        this.colorMap.put(Color.PALEVIOLETRED, new java.awt.Color(0.85882354F, 0.4392157F, 0.5764706F));
        this.colorMap.put(Color.PAPAYAWHIP, new java.awt.Color(1.0F, 0.9372549F, 0.8352941F));
        this.colorMap.put(Color.PEACHPUFF, new java.awt.Color(1.0F, 0.85490197F, 0.7254902F));
        this.colorMap.put(Color.PERU, new java.awt.Color(0.8039216F, 0.52156866F, 0.24705882F));
        this.colorMap.put(Color.PINK, new java.awt.Color(1.0F, 0.7529412F, 0.79607844F));
        this.colorMap.put(Color.PLUM, new java.awt.Color(0.8666667F, 0.627451F, 0.8666667F));
        this.colorMap.put(Color.POWDERBLUE, new java.awt.Color(0.6901961F, 0.8784314F, 0.9019608F));
        this.colorMap.put(Color.PURPLE, new java.awt.Color(0.5019608F, 0.0F, 0.5019608F));
        this.colorMap.put(Color.RED, new java.awt.Color(1.0F, 0.0F, 0.0F));
        this.colorMap.put(Color.ROSYBROWN, new java.awt.Color(0.7372549F, 0.56078434F, 0.56078434F));
        this.colorMap.put(Color.ROYALBLUE, new java.awt.Color(0.25490198F, 0.4117647F, 0.88235295F));
        this.colorMap.put(Color.SADDLEBROWN, new java.awt.Color(0.54509807F, 0.27058825F, 0.07450981F));
        this.colorMap.put(Color.SALMON, new java.awt.Color(0.98039216F, 0.5019608F, 0.44705883F));
        this.colorMap.put(Color.SANDYBROWN, new java.awt.Color(0.95686275F, 0.6431373F, 0.3764706F));
        this.colorMap.put(Color.SEAGREEN, new java.awt.Color(0.18039216F, 0.54509807F, 0.34117648F));
        this.colorMap.put(Color.SEASHELL, new java.awt.Color(1.0F, 0.9607843F, 0.93333334F));
        this.colorMap.put(Color.SIENNA, new java.awt.Color(0.627451F, 0.32156864F, 0.1764706F));
        this.colorMap.put(Color.SILVER, new java.awt.Color(0.7529412F, 0.7529412F, 0.7529412F));
        this.colorMap.put(Color.SKYBLUE, new java.awt.Color(0.5294118F, 0.80784315F, 0.92156863F));
        this.colorMap.put(Color.SLATEBLUE, new java.awt.Color(0.41568628F, 0.3529412F, 0.8039216F));
        this.colorMap.put(Color.SLATEGRAY, new java.awt.Color(0.4392157F, 0.5019608F, 0.5647059F));
        this.colorMap.put(Color.SLATEGREY, new java.awt.Color(0.4392157F, 0.5019608F, 0.5647059F));
        this.colorMap.put(Color.SNOW, new java.awt.Color(1.0F, 0.98039216F, 0.98039216F));
        this.colorMap.put(Color.SPRINGGREEN, new java.awt.Color(0.0F, 1.0F, 0.49803922F));
        this.colorMap.put(Color.STEELBLUE, new java.awt.Color(0.27450982F, 0.50980395F, 0.7058824F));
        this.colorMap.put(Color.TAN, new java.awt.Color(0.8235294F, 0.7058824F, 0.54901963F));
        this.colorMap.put(Color.TEAL, new java.awt.Color(0.0F, 0.5019608F, 0.5019608F));
        this.colorMap.put(Color.THISTLE, new java.awt.Color(0.84705883F, 0.7490196F, 0.84705883F));
        this.colorMap.put(Color.TOMATO, new java.awt.Color(1.0F, 0.3882353F, 0.2784314F));
        this.colorMap.put(Color.TURQUOISE, new java.awt.Color(0.2509804F, 0.8784314F, 0.8156863F));
        this.colorMap.put(Color.VIOLET, new java.awt.Color(0.93333334F, 0.50980395F, 0.93333334F));
        this.colorMap.put(Color.WHEAT, new java.awt.Color(0.9607843F, 0.87058824F, 0.7019608F));
        this.colorMap.put(Color.WHITE, new java.awt.Color(1.0F, 1.0F, 1.0F));
        this.colorMap.put(Color.WHITESMOKE, new java.awt.Color(0.9607843F, 0.9607843F, 0.9607843F));
        this.colorMap.put(Color.YELLOW, new java.awt.Color(1.0F, 1.0F, 0.0F));
        this.colorMap.put(Color.YELLOWGREEN, new java.awt.Color(0.6039216F, 0.8039216F, 0.19607843F));
    }

    private java.awt.Color toAwtColor(Color color) {
        return (java.awt.Color) this.colorMap.get(color);
    }

    private Color toEngineColor(java.awt.Color color) {
        for (Map.Entry<Color, java.awt.Color> colorColorEntry : this.colorMap.entrySet()) {
            if (((java.awt.Color) colorColorEntry.getValue()).equals(color)) {
                return (Color) colorColorEntry.getKey();
            }
        }
        return Color.NONE;
    }

    class CellLabel extends JLabel {
        private int x;
        private int y;

        public CellLabel(int x, int y) {
            this.setHorizontalAlignment(0);

            this.setVerticalAlignment(0);

            this.x = x;

            this.y = y;

            this.setBackground(java.awt.Color.WHITE);

            this.setOpaque(true);

            this.addMouseListener(Game.this.new CellMouseListener(x, y));

            this.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    super.keyReleased(e);
                }
            });

        }

        public void repaint() {
            if (Game.this.turnTimerMs == 0) {
                super.repaint();
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (Game.this.showCoordinates) {
                String text = this.x + "-" + this.y;

                g.setColor(java.awt.Color.BLACK);

                int fontSize = Game.cellSize / 3;

                g.setFont(new Font("TimesRoman", 0, fontSize));

                g.drawString(text, fontSize / 2, fontSize);
            }
        }
    }

    class CellMouseListener extends MouseAdapter {
        private int x;
        private int y;

        public CellMouseListener(int x, int y) {
            this.x = x;

            this.y = y;

        }

        public void mouseClicked(MouseEvent e) {
            if (Game.this.isMessageShown) {
                Game.this.hideMessageDialog();
            }

            if (Game.cellSize != 0) {
                switch (e.getButton()) {
                    case 1:
                        Game.this.onMouseLeftClick(this.x, this.y);
                        break;
                    case 3:
                        Game.this.onMouseRightClick(this.x, this.y);
                }
            }
        }
    }

    class GameKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent e) {
            if (Game.this.isMessageShown) {
                if (this.getKey(e) == Key.SPACE) {
                    Game.this.hideMessageDialog();
                }

                Game.this.onKeyPress(this.getKey(e));

            } else {
                Game.this.onKeyPress(this.getKey(e));
            }
        }

        public void keyReleased(KeyEvent e) {
            Game.this.onKeyReleased(this.getKey(e));
        }

        private Key getKey(KeyEvent e) {
            switch (e.getKeyCode()) {
                case 10:
                    return Key.ENTER;

                case 19:
                    return Key.PAUSE;

                case 27:
                    return Key.ESCAPE;

                case 32:
                    return Key.SPACE;

                case 37:
                    return Key.LEFT;

                case 38:
                    return Key.UP;

                case 39:
                    return Key.RIGHT;

                case 40:
                    return Key.DOWN;

                default:
                    return Key.UNKNOWN;
            }
        }
    }

    class GameRootPanel extends JPanel {
        public GameRootPanel() {
            super(new FlowLayout(0, 0, 0));

        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(Game.this.backgroundImg, 0, 0, (ImageObserver) null);

            int scoreWidth = (Game.this.width * Game.cellSize - 1) / 2;

            int scoreHeight = 20;

            int x = Game.this.width * Game.cellSize / 2 + 125 - scoreWidth / 2;

            int y = Game.this.height * Game.cellSize + 110 + 6;

            int fontSize = 16;

            g.setColor(java.awt.Color.WHITE);

            g.setFont(new Font("Verdana", 1, fontSize));

            g.fillRect(x, y, scoreWidth, scoreHeight);

            g.setColor(java.awt.Color.BLACK);

            g.drawString("Score: " + Game.this.score, x + scoreWidth / 2 - 35, y + fontSize);

        }

        protected void paintChildren(Graphics g) {
            super.paintChildren(g);

            if (Game.this.isMessageShown) {
                int dialogWidth = (int) Game.this.dialogLabel.getPreferredSize().getWidth();

                int dialogHeight = (int) Game.this.dialogLabel.getPreferredSize().getHeight();

                int messageX = this.getWidth() / 2 - dialogWidth / 2;

                int messageY = this.getHeight() / 2 - dialogHeight / 2;

                if (Game.this.dialogBackGround != null) {
                    g.setColor(Game.this.dialogBackGround);

                    g.fillRect(messageX, messageY, dialogWidth, dialogHeight);
                }

                g.setColor(Game.this.dialogLabel.getForeground());

                g.setFont(Game.this.dialogLabel.getFont());

                g.drawString(Game.this.dialogLabel.getText(), messageX, messageY + (int) ((double) dialogHeight * 0.8));
            }
        }
    }
}
