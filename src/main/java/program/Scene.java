package program;

import renderer.Camera;

public abstract class Scene {

    public Scene(){

    }

    public abstract void init();

    public abstract void inputs(float dt);
    public abstract void update(float dt);
    public abstract void render(float dt);
    public abstract void renderUI();

    public abstract void end();

    public abstract float[] getBackgroundColor();
    public abstract Camera getCamera();
    public abstract void setCameraChange(boolean val);
}
