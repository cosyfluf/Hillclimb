package net.cosyfluf;

import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.callbacks.ContactImpulse;

public class MyContactListener implements ContactListener {

    private GamePanel gamePanel;

    public MyContactListener(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Object userDataA = fixtureA.getUserData();
        Object userDataB = fixtureB.getUserData();

        if (userDataA instanceof Car && userDataB instanceof Collectible) {
            gamePanel.handleCollectibleCollision((Car)userDataA, (Collectible)userDataB);
        } else if (userDataB instanceof Car && userDataA instanceof Collectible) {
            gamePanel.handleCollectibleCollision((Car)userDataB, (Collectible)userDataA);
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        boolean isCollectible = (fixtureA.getUserData() instanceof Collectible || fixtureB.getUserData() instanceof Collectible);

        if (isCollectible) {
            contact.setEnabled(false);
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}