package program;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Particle {
    public float x, y, z, r, g, b, a, s;

    public float velX = 0f, velY = 0f, velZ = 0f;
    public float accX = 0f, accY = 0f, accZ = 0f;
    public float forceX = 0f, forceY = 0f, forceZ = 0f;


    public Particle(float x, float y, float z, float r, float g, float b, float a, float s, float velX, float velY, float velZ){
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.s = s;

        this.velX = velX;
        this.velY = velY;
        this.velZ = velZ;


//        float angle = (float)Math.atan2(x, z);
//        float dist = (float)Math.sqrt(x*x + z*z);
//
//        velX = (float)(Math.sin(angle+Math.PI/2))*dist*0.35f;
//        velZ = (float)(Math.cos(angle+Math.PI/2))*dist*0.35f;
    }

    public void calculate(float dt, OcTree root){
        forceX = 0f;
        forceY = 0f;
        forceZ = 0f;

        manageNode(root);

        accX = forceX/s;
        accY = forceY/s;
        accZ = forceZ/s;

        velX += accX*dt;
        velY += accY*dt;
        velZ += accZ*dt;
    }

    public void setOrbitalVelocity(OcTree root, Vector3f normal, float distFromCenter){
        manageNode(root);

        Vector3f force = new Vector3f(forceX, forceY, forceZ);
        Vector3f vel = new Vector3f(forceX, forceY, forceZ);

        vel.normalize();

        vel.cross(normal);

        //vel.mul(force.length() * 10);
        vel.mul(2000/distFromCenter);

        velX = vel.x;
        velY = vel.y;
        velZ = vel.z;
    }

    private void manageNode(OcTree node){
        if(node.isEmpty) { // nothing to calculate
            return;
        }

        float dx = node.mx - this.x;
        float dy = node.my - this.y;
        float dz = node.mz - this.z;

        if(dx == 0 && dy == 0 && dz == 0) { // don't calculate itself
            return;
        }

        float d = (float)Math.sqrt(dx*dx + dy*dy + dz*dz);
        if(d < 1f){ // when the object is extremely near, the force will be extremely large, so for approximation let's limit this
            d = 1f;
        }

        float factor = node.width / d;

        if(factor <= 0.5){ // far
            float force = (node.mass * this.s)/(d*d);

            // normalize and scale
            forceX += 20f * dx/d * force;
            forceY += 20f * dy/d * force;
            forceZ += 20f * dz/d * force;
        }
        else{ // near
            if(!node.isLeaf) {
                manageNode(node.children[0][0][0]);
                manageNode(node.children[0][0][1]);
                manageNode(node.children[0][1][0]);
                manageNode(node.children[0][1][1]);
                manageNode(node.children[1][0][0]);
                manageNode(node.children[1][0][1]);
                manageNode(node.children[1][1][0]);
                manageNode(node.children[1][1][1]);
            }
        }

    }

    public void update(float dt){
        x += velX*dt;
        y += velY*dt;
        z += velZ*dt;
    }
}
