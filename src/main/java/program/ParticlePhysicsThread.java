package program;

public class ParticlePhysicsThread implements Runnable{
    private float dt;
    private OcTree node, root;

    public ParticlePhysicsThread(float dt, OcTree node, OcTree root){
        this.dt = dt;
        this.node = node;
        this.root = root;
    }

    public void run(){

    }
}
