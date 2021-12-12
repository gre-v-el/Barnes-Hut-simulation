package renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private float x, y, z;
    private float pitch, yaw;
    private float FOV, aspect, near, far;

    private Vector3f lookingAt, up, right;

    private Matrix4f projectionMatrix;
    private Matrix4f transformMatrix;

    private float distance;
    private float zoom;

    public Camera(float xg, float yg, float zg, float zoomg, float pitchg, float yawg, float FOVg, float nearg, float farg, float width, float height){
        this.x = xg;
        this.y = yg;
        this.z = zg;

        this.zoom = Math.max(zoomg, 0);
        this.distance = (float) -Math.pow(2, zoomg);

        this.pitch = pitchg;
        this.yaw = yawg;

        this.FOV = (float)Math.toRadians(FOVg);
        this.near = nearg;
        this.far = farg;

        aspect = width/height;

        calcMatrices();
    }

    public void setAspect(float width, float height){
        aspect = width/height;
    }

    public void calcMatrices(){
        projectionMatrix = new Matrix4f().perspective(FOV, aspect, near, far);
        transformMatrix = new Matrix4f().
                translate(new Vector3f(0, 0, distance)).
                rotateX((float)Math.toRadians(pitch)).
                rotateY((float)Math.toRadians(yaw)).
                translate(new Vector3f(x, y, z));
    }

    public void calcVectors(){
        lookingAt = new Vector3f(0f, 0f, 1f).
                rotateX((float)Math.toRadians(-pitch)).
                rotateY((float)Math.toRadians(-yaw));
        up = new Vector3f(0f, 1f, 0f).
                rotateX((float)Math.toRadians(-pitch)).
                rotateY((float)Math.toRadians(-yaw));
        right = new Vector3f(1f, 0f, 0f).
                rotateX((float)Math.toRadians(-pitch)).
                rotateY((float)Math.toRadians(-yaw));
    }

    public void moveBy(float xg, float yg, float zg){
        x += xg;
        y += yg;
        z += zg;
        calcMatrices();
    }
    public void moveTo(float xg, float yg, float zg){
        x = xg;
        y = yg;
        z = zg;
        calcMatrices();
    }
    public void changeZoom(float zoo){
        zoom -= (zoom-zoo > 0)?zoo:0;
        distance = (float) -Math.pow(2, zoom);
        calcMatrices();
    }

    public void rotateBy(float pitchg, float yawg){
        pitch += pitchg;
        yaw += yawg;

        if(pitch > 85) pitch = 85;
        if(pitch < -85) pitch = -85;

        calcMatrices();
    }
    public void rotateTo(float pitchg, float yawg){
        pitch = pitchg;
        yaw = yawg;
        calcMatrices();
    }

    public Matrix4f getProjectionMatrix(){
        return projectionMatrix;
    }

    public Matrix4f getTransformMatrix(){
        return transformMatrix;
    }

    public Vector3f getLookingAt(){
        return lookingAt;
    }
    public Vector3f getUp(){
        return up;
    }
    public Vector3f getRight(){
        return right;
    }

    public float getX() { return x;}
    public float getY() { return y;}
    public float getZ() { return z;}
    public float getDistance() { return distance;}
}
