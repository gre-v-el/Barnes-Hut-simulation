package program;

import java.util.List;

public class OcTree {
    // box dimensions
    public float x, y, z, width;

    // center of mass
    public float mx, my, mz, mass;
    public int particleCount;

    // particle held
    public Particle particle;

    public boolean isEmpty = true;
    public boolean isLeaf = true;

    public OcTree[][][] children = new OcTree[2][2][2];


    public OcTree(float xg, float yg, float zg, float widthg){
        x = xg;
        y = yg;
        z = zg;
        width = widthg;

        mx = 0f;
        my = 0f;
        mz = 0f;
        mass = 0f;
        particleCount = 0;
    }

    public void calculateParticles(float dt, OcTree root) {
        if(!isLeaf){
            children[0][0][0].calculateParticles(dt, root);
            children[0][0][1].calculateParticles(dt, root);
            children[0][1][0].calculateParticles(dt, root);
            children[0][1][1].calculateParticles(dt, root);
            children[1][0][0].calculateParticles(dt, root);
            children[1][0][1].calculateParticles(dt, root);
            children[1][1][0].calculateParticles(dt, root);
            children[1][1][1].calculateParticles(dt, root);
            return;
        }
        if(!isEmpty){
            particle.calculate(dt, root);
        }
    }

    public void updateParticles(float dt) {
        if(!isLeaf){
            children[0][0][0].updateParticles(dt);
            children[0][0][1].updateParticles(dt);
            children[0][1][0].updateParticles(dt);
            children[0][1][1].updateParticles(dt);
            children[1][0][0].updateParticles(dt);
            children[1][0][1].updateParticles(dt);
            children[1][1][0].updateParticles(dt);
            children[1][1][1].updateParticles(dt);
            return;
        }
        if(!isEmpty){
            particle.update(dt);
        }
    }

    public void insert(Particle newParticle){
        mx = (mx*particleCount + newParticle.x)/(1+particleCount);
        my = (my*particleCount + newParticle.y)/(1+particleCount);
        mz = (mz*particleCount + newParticle.z)/(1+particleCount);
        mass += newParticle.s;

        particleCount++;


        if(isEmpty){
            particle = newParticle;
            isEmpty = false;
        }
        else{
            if(isLeaf){
                float offset = this.width/2;

                children[0][0][0] = new OcTree(this.x,          this.y,         this.z,         offset);
                children[0][0][1] = new OcTree(this.x,          this.y,         this.z + offset,offset);
                children[0][1][0] = new OcTree(this.x,          this.y + offset,this.z,         offset);
                children[0][1][1] = new OcTree(this.x,          this.y + offset,this.z + offset,offset);
                children[1][0][0] = new OcTree(this.x + offset, this.y,         this.z,         offset);
                children[1][0][1] = new OcTree(this.x + offset, this.y,         this.z + offset,offset);
                children[1][1][0] = new OcTree(this.x + offset, this.y + offset,this.z,         offset);
                children[1][1][1] = new OcTree(this.x + offset, this.y + offset,this.z + offset,offset);

                isLeaf = false;
                insertToChild(particle);
            }
            insertToChild(newParticle);
        }
    }

    private void insertToChild(Particle newParticle){
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 0; k < 2; k++) {
                    if (children[i][j][k].contains(newParticle.x, newParticle.y, newParticle.z)) {
                        children[i][j][k].insert(newParticle);

                        return;
                    }
                }
            }
        }

        //System.out.println("particle out of bounds");
    }

    public boolean contains(float xg, float yg, float zg){
        return  (xg >= x && xg <= x + width &&
                yg >= y && yg <= y + width &&
                zg >= z && zg <= z + width);
    }

    public void getBoxData(List<Float> list, int displayMode, int depth){
        if(depth == 0) return;

        switch (displayMode){
            case 0:
                if(isLeaf && !isEmpty) {
                    list.add(x);
                    list.add(y);
                    list.add(z);
                    list.add(width);
                }
            break;
            case 1:
                if(!isEmpty) {
                    list.add(x);
                    list.add(y);
                    list.add(z);
                    list.add(width);
                }
            break;
            case 2:
                if(isLeaf) {
                    list.add(x);
                    list.add(y);
                    list.add(z);
                    list.add(width);
                }
            break;
            default:
                list.add(x);
                list.add(y);
                list.add(z);
                list.add(width);
            break;
        }


        if(isLeaf) return;

        children[0][0][0].getBoxData(list, displayMode, depth - 1);
        children[0][0][1].getBoxData(list, displayMode, depth - 1);
        children[0][1][0].getBoxData(list, displayMode, depth - 1);
        children[0][1][1].getBoxData(list, displayMode, depth - 1);
        children[1][0][0].getBoxData(list, displayMode, depth - 1);
        children[1][0][1].getBoxData(list, displayMode, depth - 1);
        children[1][1][0].getBoxData(list, displayMode, depth - 1);
        children[1][1][1].getBoxData(list, displayMode, depth - 1);
    }

    public void getParticleData(List<Particle> list){
        if(isLeaf && !isEmpty){
            list.add(particle);
        }
        if(isLeaf) return;

        children[0][0][0].getParticleData(list);
        children[0][0][1].getParticleData(list);
        children[0][1][0].getParticleData(list);
        children[0][1][1].getParticleData(list);
        children[1][0][0].getParticleData(list);
        children[1][0][1].getParticleData(list);
        children[1][1][0].getParticleData(list);
        children[1][1][1].getParticleData(list);
    }
}
