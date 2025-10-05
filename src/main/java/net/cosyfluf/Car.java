package net.cosyfluf;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.WheelJoint;
import org.jbox2d.dynamics.joints.WheelJointDef;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import static net.cosyfluf.PhysicsConstants.PIXELS_PER_METER;
import static net.cosyfluf.PhysicsConstants.TIME_STEP;
import static net.cosyfluf.PhysicsConstants.WHEEL_RADIUS;

public class Car {
    public Body chassisBody;
    public Body frontWheelBody;
    public Body rearWheelBody;

    public WheelJoint frontWheelJoint;
    public WheelJoint rearWheelJoint;

    public static final float CHASSIS_WIDTH = 2.5f;
    public static final float CHASSIS_HEIGHT = 0.8f;
    public static final float CHASSIS_FRONT_WHEEL_OFFSET_X = CHASSIS_WIDTH * 0.35f;
    public static final float CHASSIS_REAR_WHEEL_OFFSET_X = CHASSIS_WIDTH * 0.35f;
    public static final float CHASSIS_WHEEL_OFFSET_Y = CHASSIS_HEIGHT * 0.4f;

    private final float chassisDensity = 0.8f;
    private final float wheelDensity = 1.0f;

    private final float springFrequencyHz = 8.0f;
    private final float springDampingRatio = 0.7f;

    private final float forwardMotorSpeed = -20.0f;
    private final float reverseMotorSpeed = 10.0f;
    private final float motorTorque = 200.0f;
    private final float idleTorque = 10.0f;

    public boolean forward = false;
    public boolean reverse = false;
    public boolean nitroInput = false;

    public float currentNitroFuel = PhysicsConstants.NITRO_MAX_FUEL;
    public boolean isNitroActive = false;
    public float nitroCooldownTimer = 0.0f;

    public Car(World world, float initialX, float initialY) {

        BodyDef bd = new BodyDef();
        bd.type = BodyType.DYNAMIC;
        bd.position.set(initialX, initialY);
        bd.angularDamping = 1.5f;

        PolygonShape chassisShape = new PolygonShape();
        chassisShape.setAsBox(CHASSIS_WIDTH / 2, CHASSIS_HEIGHT / 2);

        FixtureDef fd = new FixtureDef();
        fd.shape = chassisShape;
        fd.density = chassisDensity;
        fd.friction = 0.6f;
        fd.restitution = 0.1f;

        chassisBody = world.createBody(bd);
        chassisBody.createFixture(fd).setUserData(this);

        CircleShape wheelShape = new CircleShape();
        wheelShape.setRadius(WHEEL_RADIUS);

        FixtureDef wheelFd = new FixtureDef();
        wheelFd.shape = wheelShape;
        wheelFd.density = wheelDensity;
        wheelFd.friction = 2.5f;
        wheelFd.restitution = 0.2f;

        BodyDef wheelBdFront = new BodyDef();
        wheelBdFront.type = BodyType.DYNAMIC;
        wheelBdFront.position.set(initialX + CHASSIS_FRONT_WHEEL_OFFSET_X, initialY - CHASSIS_WHEEL_OFFSET_Y);
        frontWheelBody = world.createBody(wheelBdFront);
        frontWheelBody.createFixture(wheelFd);

        BodyDef wheelBdRear = new BodyDef();
        wheelBdRear.type = BodyType.DYNAMIC;
        wheelBdRear.position.set(initialX - CHASSIS_REAR_WHEEL_OFFSET_X, initialY - CHASSIS_WHEEL_OFFSET_Y);
        rearWheelBody = world.createBody(wheelBdRear);
        rearWheelBody.createFixture(wheelFd);

        WheelJointDef wjd = new WheelJointDef();
        wjd.initialize(chassisBody, frontWheelBody, frontWheelBody.getPosition(), new Vec2(0.0f, -1.0f));
        wjd.motorSpeed = 0.0f;
        wjd.maxMotorTorque = motorTorque;
        wjd.enableMotor = true;
        wjd.frequencyHz = springFrequencyHz;
        wjd.dampingRatio = springDampingRatio;
        frontWheelJoint = (WheelJoint) world.createJoint(wjd);

        wjd.initialize(chassisBody, rearWheelBody, rearWheelBody.getPosition(), new Vec2(0.0f, -1.0f));
        wjd.motorSpeed = 0.0f;
        wjd.maxMotorTorque = motorTorque;
        wjd.enableMotor = true;
        wjd.frequencyHz = springFrequencyHz;
        wjd.dampingRatio = springDampingRatio;
        rearWheelJoint = (WheelJoint) world.createJoint(wjd);
    }

    public void update() {
        float currentMotorSpeed = 0.0f;
        float currentMotorTorque = idleTorque;

        if (forward) {
            currentMotorSpeed = forwardMotorSpeed;
            currentMotorTorque = motorTorque;
        } else if (reverse) {
            currentMotorSpeed = reverseMotorSpeed;
            currentMotorTorque = motorTorque;
        } else {
            currentMotorSpeed = 0.0f;
            currentMotorTorque = idleTorque;
        }

        if (nitroCooldownTimer > 0) {
            nitroCooldownTimer -= TIME_STEP;
            if (nitroCooldownTimer < 0) nitroCooldownTimer = 0;
            isNitroActive = false;
        }

        if (nitroInput && currentNitroFuel > 0 && nitroCooldownTimer == 0) {
            isNitroActive = true;
        } else {
            isNitroActive = false;
        }

        if (isNitroActive && currentNitroFuel > 0) {
            currentMotorTorque *= PhysicsConstants.NITRO_FORCE_MULTIPLIER;
            currentNitroFuel -= PhysicsConstants.NITRO_CONSUMPTION_RATE * TIME_STEP;
            if (currentNitroFuel < 0) {
                currentNitroFuel = 0;
                isNitroActive = false;
                nitroCooldownTimer = PhysicsConstants.NITRO_COOLDOWN_TIME;
            }
        } else if (currentNitroFuel == 0 && isNitroActive) {
            isNitroActive = false;
            nitroCooldownTimer = PhysicsConstants.NITRO_COOLDOWN_TIME;
        }

        frontWheelJoint.setMotorSpeed(currentMotorSpeed);
        frontWheelJoint.setMaxMotorTorque(currentMotorTorque);
        rearWheelJoint.setMotorSpeed(currentMotorSpeed);
        rearWheelJoint.setMaxMotorTorque(currentMotorTorque);
    }

    public void addNitroFuel(float amount) {
        currentNitroFuel = Math.min(PhysicsConstants.NITRO_MAX_FUEL, currentNitroFuel + amount);
    }

    public float getSpeedMPS() {
        return chassisBody.getLinearVelocity().length();
    }

    public boolean isUpsideDown() {
        float angle = chassisBody.getAngle();
        return Math.abs(angle) > Math.PI * 0.75f;
    }

    private float toSwingY(float jbox2dY, int screenHeight) {
        return screenHeight - (jbox2dY * PIXELS_PER_METER);
    }

    public float getX() { return chassisBody.getPosition().x; }
    public float getY() { return chassisBody.getPosition().y; }
    public float getAngle() { return chassisBody.getAngle(); }

    public void draw(Graphics2D g2d, int screenHeight) {
        g2d.setColor(new Color(178, 34, 34));

        Vec2 chassisPos = chassisBody.getPosition();
        float chassisAngle = chassisBody.getAngle();

        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(chassisPos.x * PIXELS_PER_METER, toSwingY(chassisPos.y, screenHeight));
        g2d.rotate(-chassisAngle);

        g2d.fill(new Rectangle2D.Double(-CHASSIS_WIDTH / 2 * PIXELS_PER_METER, -CHASSIS_HEIGHT / 2 * PIXELS_PER_METER,
                CHASSIS_WIDTH * PIXELS_PER_METER, CHASSIS_HEIGHT * PIXELS_PER_METER));

        g2d.setColor(new Color(205, 92, 92));
        double cabinWidth = CHASSIS_WIDTH / 2 * PIXELS_PER_METER;
        double cabinHeight = CHASSIS_HEIGHT / 2 * PIXELS_PER_METER;
        g2d.fill(new Rectangle2D.Double(-cabinWidth / 2, -CHASSIS_HEIGHT / 2 * PIXELS_PER_METER - cabinHeight, cabinWidth, cabinHeight));

        g2d.setColor(new Color(173, 216, 230, 180));
        Polygon windshield = new Polygon();
        windshield.addPoint((int) (cabinWidth / 4), (int) (-CHASSIS_HEIGHT / 2 * PIXELS_PER_METER - cabinHeight + 2));
        windshield.addPoint((int) (cabinWidth / 2 - 2), (int) (-CHASSIS_HEIGHT / 2 * PIXELS_PER_METER - cabinHeight + 2));
        windshield.addPoint((int) (cabinWidth / 2 - 5), (int) (-CHASSIS_HEIGHT / 2 * PIXELS_PER_METER - cabinHeight / 2));
        windshield.addPoint((int) (cabinWidth / 4 + 5), (int) (-CHASSIS_HEIGHT / 2 * PIXELS_PER_METER - cabinHeight / 2));
        g2d.fill(windshield);

        g2d.setColor(new Color(255, 255, 0));
        g2d.fill(new Ellipse2D.Double(CHASSIS_WIDTH / 2 * PIXELS_PER_METER - 5, -CHASSIS_HEIGHT / 4 * PIXELS_PER_METER, 8, 8));
        g2d.fill(new Ellipse2D.Double(CHASSIS_WIDTH / 2 * PIXELS_PER_METER - 5, CHASSIS_HEIGHT / 4 * PIXELS_PER_METER - 8, 8, 8));

        g2d.setTransform(oldTransform);

        g2d.setColor(Color.BLACK);
        drawWheel(g2d, frontWheelBody, screenHeight);
        drawWheel(g2d, rearWheelBody, screenHeight);

        if (isNitroActive && currentNitroFuel > 0) {
            g2d.setColor(Color.ORANGE);
            float flameSize = (float) (Math.random() * 10 + 10);
            Vec2 chassisBackLocalPoint = new Vec2(-CHASSIS_WIDTH / 2 + 0.1f, 0.0f);
            Vec2 chassisBackWorldPoint = chassisBody.getWorldPoint(chassisBackLocalPoint);

            float drawX = chassisBackWorldPoint.x * PIXELS_PER_METER - (flameSize / 2);
            float drawY = toSwingY(chassisBackWorldPoint.y, screenHeight);

            g2d.fillOval((int)drawX, (int)(drawY - flameSize / 2), (int)flameSize, (int)flameSize);
            g2d.setColor(Color.RED);
            flameSize *= 0.7;
            g2d.fillOval((int)drawX, (int)(drawY - flameSize / 2), (int)flameSize, (int)flameSize);
        }
    }

    private void drawWheel(Graphics2D g2d, Body wheel, int screenHeight) {
        Vec2 wheelPos = wheel.getPosition();
        float wheelAngle = wheel.getAngle();

        AffineTransform oldTransform = g2d.getTransform();
        g2d.translate(wheelPos.x * PIXELS_PER_METER, toSwingY(wheelPos.y, screenHeight));
        g2d.rotate(-wheelAngle);

        g2d.fill(new Ellipse2D.Double(-WHEEL_RADIUS * PIXELS_PER_METER, -WHEEL_RADIUS * PIXELS_PER_METER,
                WHEEL_RADIUS * 2 * PIXELS_PER_METER, WHEEL_RADIUS * 2 * PIXELS_PER_METER));

        g2d.setColor(Color.GRAY);
        float hubRadius = WHEEL_RADIUS * 0.4f;
        g2d.fill(new Ellipse2D.Double(-hubRadius * PIXELS_PER_METER, -hubRadius * PIXELS_PER_METER,
                hubRadius * 2 * PIXELS_PER_METER, hubRadius * 2 * PIXELS_PER_METER));

        g2d.setTransform(oldTransform);
    }
}