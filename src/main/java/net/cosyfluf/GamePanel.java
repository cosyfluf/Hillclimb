package net.cosyfluf;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.Joint;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;

import static net.cosyfluf.PhysicsConstants.PIXELS_PER_METER;
import static net.cosyfluf.PhysicsConstants.GRAVITY;
import static net.cosyfluf.PhysicsConstants.TIME_STEP;
import static net.cosyfluf.PhysicsConstants.WHEEL_RADIUS;

public class GamePanel extends JPanel implements ActionListener {

    public static final int LOGICAL_WIDTH = 1200;
    public static final int LOGICAL_HEIGHT = 700;

    private static final int TERRAIN_WIDTH_FACTOR = 5;
    private static final double TERRAIN_OFFSET_Y = LOGICAL_HEIGHT * 0.7;
    private static final int GAME_OVER_FALL_THRESHOLD_Y_PIXELS = 200;
    private static final float COLLECTIBLE_SPAWN_INTERVAL_METERS = 15.0f;
    private static final float COLLECTIBLE_MAX_HEIGHT_METERS = 7.0f;

    private MainFrame mainFrame;
    private List<LevelInfo> availableLevels;
    private LevelInfo currentLevel;
    private int selectedMenuLevelIndex = 0;

    public enum GameState {
        MAIN_MENU,
        GAME_RUNNING,
        GAME_OVER
    }
    private GameState currentState;

    private World world;
    private Car car;
    private Body groundBody;

    private double[] terrainPointsPixels;
    private Vec2[] terrainPointsMeters;
    private List<Collectible> collectibles;

    private Set<Integer> activeKeys = new HashSet<>();
    private Random random;

    private double cameraOffsetX = 0.0;
    private Timer gameTimer;

    private float upsideDownTimer = 0.0f;

    private List<Rectangle> menuButtonsBounds;
    private List<Rectangle> gameOverButtonsBounds;

    public GamePanel(MainFrame mainFrame, List<LevelInfo> levels) {
        this.mainFrame = mainFrame;
        this.availableLevels = levels;

        setPreferredSize(new Dimension(LOGICAL_WIDTH, LOGICAL_HEIGHT));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                activeKeys.add(e.getKeyCode());
                handleInputInState(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                activeKeys.remove(e.getKeyCode());
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClickInState(e.getX(), e.getY());
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMoveInState(e.getX(), e.getY());
            }
        });

        gameTimer = new Timer((int)(TIME_STEP * 1000), this);

        world = new World(GRAVITY);
        collectibles = new ArrayList<>();

        this.currentLevel = availableLevels.get(0);
        generateTerrain();

        menuButtonsBounds = new ArrayList<>();
        gameOverButtonsBounds = new ArrayList<>();

        showMainMenu();
    }

    private void handleMouseClickInState(int mouseX, int mouseY) {

        double scaleX = (double) LOGICAL_WIDTH / getWidth();
        double scaleY = (double) LOGICAL_HEIGHT / getHeight();
        int logicalMouseX = (int)(mouseX * scaleX);
        int logicalMouseY = (int)(mouseY * scaleY);

        if (currentState == GameState.MAIN_MENU) {
            for (int i = 0; i < menuButtonsBounds.size(); i++) {
                if (menuButtonsBounds.get(i).contains(logicalMouseX, logicalMouseY)) {
                    if (i < availableLevels.size()) {
                        startGame(i);
                    } else {
                        System.exit(0);
                    }
                    return;
                }
            }
        } else if (currentState == GameState.GAME_OVER) {
            for (int i = 0; i < gameOverButtonsBounds.size(); i++) {
                if (gameOverButtonsBounds.get(i).contains(logicalMouseX, logicalMouseY)) {
                    if (i == 0) {
                        startGame(mainFrame.getLastPlayedLevelIndex());
                    } else {
                        showMainMenu();
                    }
                    return;
                }
            }
        }
    }

    private void handleMouseMoveInState(int mouseX, int mouseY) {

        double scaleX = (double) LOGICAL_WIDTH / getWidth();
        double scaleY = (double) LOGICAL_HEIGHT / getHeight();
        int logicalMouseX = (int)(mouseX * scaleX);
        int logicalMouseY = (int)(mouseY * scaleY);

        if (currentState == GameState.MAIN_MENU) {
            int oldSelected = selectedMenuLevelIndex;
            selectedMenuLevelIndex = -1;

            for (int i = 0; i < menuButtonsBounds.size(); i++) {
                if (menuButtonsBounds.get(i).contains(logicalMouseX, logicalMouseY)) {
                    selectedMenuLevelIndex = i;
                    break;
                }
            }
            if (oldSelected != selectedMenuLevelIndex) {
                repaint();
            }
        }

    }

    private void handleInputInState(int keyCode) {
        if (currentState == GameState.MAIN_MENU) {
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    selectedMenuLevelIndex = Math.max(0, selectedMenuLevelIndex - 1);
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    selectedMenuLevelIndex = Math.min(availableLevels.size(), selectedMenuLevelIndex + 1);
                    repaint();
                    break;
                case KeyEvent.VK_ENTER:
                    if (selectedMenuLevelIndex < availableLevels.size()) {
                        startGame(selectedMenuLevelIndex);
                    } else if (selectedMenuLevelIndex == availableLevels.size()) {
                        System.exit(0);
                    }
                    break;
            }
        } else if (currentState == GameState.GAME_OVER) {
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    startGame(mainFrame.getLastPlayedLevelIndex());
                    break;
                case KeyEvent.VK_ESCAPE:
                    showMainMenu();
                    break;
            }
        } else if (currentState == GameState.GAME_RUNNING) {

            if (keyCode == KeyEvent.VK_ESCAPE) {
                if (mainFrame.isFullscreen()) {
                    mainFrame.exitFullscreenAndContinueGame();
                } else {
                    showMainMenu();
                }
            }
        }
    }

    public void startGameTimer() {
        if (!gameTimer.isRunning()) {
            gameTimer.start();
        }
    }

    public void stopGameTimer() {
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }

    public void showMainMenu() {
        currentState = GameState.MAIN_MENU;
        stopGameTimer();
        selectedMenuLevelIndex = 0;
        repaint();
    }

    public void startGame(int levelIndex) {
        this.currentLevel = availableLevels.get(levelIndex);
        currentState = GameState.GAME_RUNNING;
        mainFrame.setLastPlayedLevelIndex(levelIndex);
        setupGame();
        startGameTimer();
        requestFocusInWindow();
        repaint();
    }

    public void showGameOverScreen() {
        currentState = GameState.GAME_OVER;
        stopGameTimer();
        repaint();
    }

    private void setupGame() {

        world = new World(GRAVITY);
        world.setContactListener(new MyContactListener(this));

        cleanupPhysicsObjects();

        collectibles.clear();

        generateTerrain();

        float initialCarX = (float) (LOGICAL_WIDTH * currentLevel.initialCarXFactor / PIXELS_PER_METER);

        float frontWheelX_meters = initialCarX + Car.CHASSIS_FRONT_WHEEL_OFFSET_X;
        float rearWheelX_meters = initialCarX - Car.CHASSIS_REAR_WHEEL_OFFSET_X;

        float frontWheelTerrainY_pixels = (float)getTerrainYInterpolatedPixels(frontWheelX_meters * PIXELS_PER_METER, terrainPointsPixels);
        float frontWheelTerrainY_meters = toJBox2dY(frontWheelTerrainY_pixels, LOGICAL_HEIGHT);

        float rearWheelTerrainY_pixels = (float)getTerrainYInterpolatedPixels(rearWheelX_meters * PIXELS_PER_METER, terrainPointsPixels);
        float rearWheelTerrainY_meters = toJBox2dY(rearWheelTerrainY_pixels, LOGICAL_HEIGHT);

        float highestTerrainY_meters = Math.max(frontWheelTerrainY_meters, rearWheelTerrainY_meters);

        float initialCarY = highestTerrainY_meters
                + WHEEL_RADIUS
                + (Car.CHASSIS_HEIGHT / 2)
                + 0.1f;

        car = new Car(world, initialCarX, initialCarY);

        generateCollectibles();

        activeKeys.clear();
        cameraOffsetX = 0.0;
        upsideDownTimer = 0.0f;
    }

    private void cleanupPhysicsObjects() {
        List<Body> bodiesToDestroy = new ArrayList<>();
        for (Body b = world.getBodyList(); b != null; b = b.getNext()) {
            bodiesToDestroy.add(b);
        }
        for (Body b : bodiesToDestroy) {
            world.destroyBody(b);
        }

        List<Joint> jointsToDestroy = new ArrayList<>();
        for (Joint j = world.getJointList(); j != null; j = j.getNext()) {
            jointsToDestroy.add(j);
        }
        for (Joint j : jointsToDestroy) {
            world.destroyJoint(j);
        }

        groundBody = null;
        car = null;
        collectibles.clear();
    }

    private void generateTerrain() {
        terrainPointsPixels = new double[LOGICAL_WIDTH * TERRAIN_WIDTH_FACTOR];
        random = new Random(currentLevel.seed);

        double currentY = TERRAIN_OFFSET_Y;
        double currentSlope = 0.0;
        final double maxSlopeChange = 0.01;
        final double maxSlope = 0.15;

        for (int i = 0; i < terrainPointsPixels.length; i++) {
            terrainPointsPixels[i] = currentY + Math.sin(i * currentLevel.terrainSmoothness) * currentLevel.terrainAmplitude * 0.8 +
                    Math.cos(i * currentLevel.terrainSmoothness * 0.7) * currentLevel.terrainAmplitude * 0.5 +
                    (random.nextDouble() - 0.5) * currentLevel.terrainNoiseFactor;

            currentSlope += (random.nextDouble() - 0.5) * maxSlopeChange;
            currentSlope = Math.max(-maxSlope, Math.min(maxSlope, currentSlope));
            currentY += currentSlope * 15;

            if (currentY > LOGICAL_HEIGHT * 0.95) currentY = LOGICAL_HEIGHT * 0.95;
            if (currentY < LOGICAL_HEIGHT * 0.4) currentY = LOGICAL_HEIGHT * 0.4;
        }

        for (int iter = 0; iter < 4; iter++) {
            double[] smoothed = new double[terrainPointsPixels.length];
            for (int i = 0; i < terrainPointsPixels.length; i++) {
                double sum = terrainPointsPixels[i];
                int count = 1;
                if (i > 0) { sum += terrainPointsPixels[i - 1]; count++; }
                if (i < terrainPointsPixels.length - 1) { sum += terrainPointsPixels[i + 1]; count++; }
                smoothed[i] = sum / count;
            }
            terrainPointsPixels = smoothed;
        }

        terrainPointsMeters = new Vec2[terrainPointsPixels.length];
        for (int i = 0; i < terrainPointsPixels.length; i++) {
            float xMeter = (float) (i / PIXELS_PER_METER);
            float yMeter = toJBox2dY(terrainPointsPixels[i], LOGICAL_HEIGHT);
            terrainPointsMeters[i] = new Vec2(xMeter, yMeter);
        }

        BodyDef groundBd = new BodyDef();
        groundBody = world.createBody(groundBd);

        ChainShape groundShape = new ChainShape();
        groundShape.createChain(terrainPointsMeters, terrainPointsMeters.length);

        FixtureDef groundFd = new FixtureDef();
        groundFd.shape = groundShape;
        groundFd.friction = 0.8f;
        groundBody.createFixture(groundFd);
    }

    private void generateCollectibles() {
        collectibles.clear();
        Random collectibleRandom = new Random(currentLevel.seed + 1);

        float startXOffsetMeters = (float) (LOGICAL_WIDTH * currentLevel.initialCarXFactor / PIXELS_PER_METER + 5.0f);
        float endXOffsetMeters = (float) (terrainPointsPixels.length / PIXELS_PER_METER - (LOGICAL_WIDTH / 4.0 / PIXELS_PER_METER));

        for (float x = startXOffsetMeters; x < endXOffsetMeters; x += COLLECTIBLE_SPAWN_INTERVAL_METERS) {
            if (collectibleRandom.nextFloat() < 0.7f) {
                float terrainYAtX = toJBox2dY(getTerrainYInterpolatedPixels(x * PIXELS_PER_METER, terrainPointsPixels), LOGICAL_HEIGHT);
                float collectibleY = terrainYAtX + (collectibleRandom.nextFloat() * COLLECTIBLE_MAX_HEIGHT_METERS) + 1.0f;

                BodyDef bd = new BodyDef();
                bd.type = BodyType.STATIC;
                bd.position.set(x, collectibleY);

                CircleShape shape = new CircleShape();
                Collectible tempCollectible = new Collectible(null, Collectible.Type.NITRO_RECHARGE);
                shape.setRadius(tempCollectible.getRadiusMeters());

                FixtureDef fd = new FixtureDef();
                fd.shape = shape;
                fd.isSensor = true;
                fd.filter.groupIndex = -1;

                Body collectibleBody = world.createBody(bd);
                collectibleBody.createFixture(fd).setUserData(tempCollectible);

                tempCollectible.body = collectibleBody;
                collectibles.add(tempCollectible);
            }
        }
    }

    private float toJBox2dY(double swingY, int screenHeight) {
        return (float) ((screenHeight - swingY) / PIXELS_PER_METER);
    }

    private double getTerrainYInterpolatedPixels(double px, double[] terrainPoints) {
        int x1 = (int) px;
        if (x1 < 0) return terrainPoints[0];
        if (x1 >= terrainPoints.length - 1) return terrainPoints[terrainPoints.length - 1];

        double y1 = terrainPoints[x1];
        double y2 = terrainPoints[x1 + 1];
        double frac = px - x1;
        return y1 * (1 - frac) + y2 * frac;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.GAME_RUNNING) {
            update();
        }
        repaint();
    }

    private void update() {
        car.forward = activeKeys.contains(KeyEvent.VK_W);
        car.reverse = activeKeys.contains(KeyEvent.VK_S);
        car.nitroInput = activeKeys.contains(KeyEvent.VK_SPACE);
        car.update();

        world.step(PhysicsConstants.TIME_STEP, PhysicsConstants.VELOCITY_ITERATIONS, PhysicsConstants.POSITION_ITERATIONS);

        double carXPixels = car.getX() * PIXELS_PER_METER;
        double targetCameraX = carXPixels - LOGICAL_WIDTH / 3.0;

        if (targetCameraX < 0) {
            cameraOffsetX = 0.0;
        } else if (targetCameraX > terrainPointsPixels.length - LOGICAL_WIDTH) {
            cameraOffsetX = (double) (terrainPointsPixels.length - LOGICAL_WIDTH);
        } else {
            cameraOffsetX = targetCameraX;
        }

        if (car.isUpsideDown()) {
            upsideDownTimer += PhysicsConstants.TIME_STEP;
            if (upsideDownTimer >= PhysicsConstants.UPSIDE_DOWN_GAME_OVER_TIME) {
                System.out.println("Game Over! Auto liegt über Kopf für zu lange Zeit.");
                showGameOverScreen();
            }
        } else {
            upsideDownTimer = 0.0f;
        }

        if (car.getY() < toJBox2dY(LOGICAL_HEIGHT + GAME_OVER_FALL_THRESHOLD_Y_PIXELS, LOGICAL_HEIGHT)) {
            System.out.println("Game Over! Auto zu tief gefallen.");
            showGameOverScreen();
        }

        if (car.getX() * PIXELS_PER_METER > terrainPointsPixels.length - LOGICAL_WIDTH / 4.0) {
            System.out.println("Level geschafft! Zurück zum Menü.");
            showMainMenu();
        }
    }

    public void handleCollectibleCollision(Car car, Collectible collectible) {
        if (collectibles.contains(collectible)) {
            System.out.println("Collectible gesammelt: " + collectible.type);
            switch (collectible.type) {
                case NITRO_RECHARGE:
                    car.addNitroFuel(PhysicsConstants.NITRO_RECHARGE_AMOUNT);
                    break;
            }
            world.destroyBody(collectible.body);
            collectibles.remove(collectible);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        double scaleX = (double) getWidth() / LOGICAL_WIDTH;
        double scaleY = (double) getHeight() / LOGICAL_HEIGHT;
        g2d.scale(scaleX, scaleY);

        Color skyBlueLight = new Color(135, 206, 250);
        Color skyBlueDark = new Color(70, 130, 180);
        GradientPaint skyGradient = new GradientPaint(0, 0, skyBlueLight, 0, LOGICAL_HEIGHT, skyBlueDark);
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);

        if (terrainPointsPixels != null) {
            Color terrainTop = new Color(34, 139, 34);
            Color terrainBottom = new Color(139, 69, 19);

            g2d.setColor(terrainTop);
            g2d.setStroke(new BasicStroke(2f));

            int startX = (int) cameraOffsetX;
            if (startX < 0) startX = 0;

            int endX = startX + LOGICAL_WIDTH;
            if (endX > terrainPointsPixels.length) endX = terrainPointsPixels.length;

            Polygon polygon = new Polygon();
            polygon.addPoint(0, (int) terrainPointsPixels[startX]);
            for (int i = startX + 1; i < endX; i++) {
                polygon.addPoint((int) (i - cameraOffsetX), (int) terrainPointsPixels[i]);
            }
            polygon.addPoint(LOGICAL_WIDTH, LOGICAL_HEIGHT);
            polygon.addPoint(0, LOGICAL_HEIGHT);

            g2d.fill(polygon);
            g2d.setColor(terrainBottom);
            g2d.draw(polygon);
        }

        if (car != null) {
            Graphics2D carG2d = (Graphics2D) g2d.create();
            carG2d.translate(-cameraOffsetX, 0.0);
            car.draw(carG2d, LOGICAL_HEIGHT);
            carG2d.dispose();
        }

        if (collectibles != null) {
            for (Collectible c : collectibles) {
                c.draw(g2d, LOGICAL_HEIGHT, cameraOffsetX);
            }
        }

        drawHUD(g2d);

        if (currentState == GameState.MAIN_MENU) {
            drawMainMenuOverlay(g2d);
        } else if (currentState == GameState.GAME_OVER) {
            drawGameOverOverlay(g2d);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        if (currentState != GameState.GAME_RUNNING) return;

        int barWidth = 200;
        int barHeight = 20;
        int padding = 10;
        int x = LOGICAL_WIDTH - barWidth - padding;
        int y = padding;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, barWidth, barHeight);

        float fillPercentage = car.currentNitroFuel / PhysicsConstants.NITRO_MAX_FUEL;
        int currentFillWidth = (int) (barWidth * fillPercentage);
        if (car.isNitroActive) {
            g2d.setColor(Color.YELLOW);
        } else if (car.currentNitroFuel <= PhysicsConstants.NITRO_MAX_FUEL * 0.2f) {
            g2d.setColor(Color.RED);
        } else {
            g2d.setColor(Color.CYAN);
        }
        g2d.fillRect(x, y, currentFillWidth, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(x, y, barWidth, barHeight);

        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Nitro: " + (int)car.currentNitroFuel + "%", x + barWidth / 2 - 40, y + barHeight + 20);

        if (car.nitroCooldownTimer > 0) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Cooldown: " + String.format("%.1f", car.nitroCooldownTimer) + "s", x + barWidth / 2 - 60, y + barHeight + 40);
        }

        int speed = (int) (car.getSpeedMPS() * PhysicsConstants.METERS_PER_SECOND_TO_KMH_FACTOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(Color.WHITE);
        g2d.drawString(speed + " km/h", padding, LOGICAL_HEIGHT - padding - 20);
    }

    private void drawMainMenuOverlay(Graphics2D g2d) {

        menuButtonsBounds.clear();

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "Hill Climb Game";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (LOGICAL_WIDTH - fm.stringWidth(title)) / 2;
        int titleY = LOGICAL_HEIGHT / 4;
        g2d.drawString(title, titleX, titleY);

        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        int buttonY = LOGICAL_HEIGHT / 2 - availableLevels.size() * 30 / 2;
        for (int i = 0; i < availableLevels.size(); i++) {
            String levelText = "Level " + (i + 1) + ": " + availableLevels.get(i).name;
            menuButtonsBounds.add(drawMenuButton(g2d, levelText, i, buttonY + i * 50));
        }

        menuButtonsBounds.add(drawMenuButton(g2d, "Exit Game", availableLevels.size(), buttonY + availableLevels.size() * 50));
    }

    private Rectangle drawMenuButton(Graphics2D g2d, String text, int index, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int buttonWidth = textWidth + 60;
        int buttonHeight = textHeight + 20;
        int buttonX = (LOGICAL_WIDTH - buttonWidth) / 2;
        Rectangle bounds = new Rectangle(buttonX, y - buttonHeight / 2, buttonWidth, buttonHeight);

        if (index == selectedMenuLevelIndex) {
            g2d.setColor(new Color(60, 179, 113).brighter());
        } else {
            g2d.setColor(new Color(60, 179, 113));
        }
        g2d.fillRoundRect(buttonX, y - buttonHeight / 2, buttonWidth, buttonHeight, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(buttonX, y - buttonHeight / 2, buttonWidth, buttonHeight, 20, 20);
        g2d.drawString(text, buttonX + (buttonWidth - textWidth) / 2, y + textHeight / 4);

        return bounds;
    }

    private void drawGameOverOverlay(Graphics2D g2d) {

        gameOverButtonsBounds.clear();

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, LOGICAL_WIDTH, LOGICAL_HEIGHT);

        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 80));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int titleX = (LOGICAL_WIDTH - fm.stringWidth(gameOverText)) / 2;
        int titleY = LOGICAL_HEIGHT / 3;
        g2d.drawString(gameOverText, titleX, titleY);

        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        gameOverButtonsBounds.add(drawGameOverButton(g2d, "Try Again (ENTER)", 0, LOGICAL_HEIGHT / 2));
        gameOverButtonsBounds.add(drawGameOverButton(g2d, "Main Menu (ESC)", 1, LOGICAL_HEIGHT / 2 + 60));
    }

    private Rectangle drawGameOverButton(Graphics2D g2d, String text, int index, int y) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int buttonWidth = textWidth + 60;
        int buttonHeight = textHeight + 20;
        int buttonX = (LOGICAL_WIDTH - buttonWidth) / 2;
        Rectangle bounds = new Rectangle(buttonX, y - buttonHeight / 2, buttonWidth, buttonHeight);

        g2d.setColor(new Color(30, 144, 255));
        if (index == 1) g2d.setColor(new Color(100, 149, 237));

        g2d.fillRoundRect(buttonX, y - buttonHeight / 2, buttonWidth, buttonHeight, 20, 20);

        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(buttonX, y - buttonHeight / 2, buttonWidth, buttonHeight, 20, 20);
        g2d.drawString(text, buttonX + (buttonWidth - textWidth) / 2, y + textHeight / 4);

        return bounds;
    }
}