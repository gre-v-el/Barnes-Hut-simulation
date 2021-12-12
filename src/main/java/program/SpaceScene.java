package program;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import renderer.Camera;
import renderer.Shader;
import renderer.Window;
import utils.KeyListener;
import utils.MouseListener;
import utils.OpenSimplex2S;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL32.*;

public class SpaceScene extends Scene {
    public float[] backgroundColor = new float[] {0f, 0f, 0f};

    Shader particleShader, boxShader, galaxyShader;

    public Camera camera;
    public boolean didCameraChange = false;


    ExecutorService threadExecutor;

    // x y z   r g b a   s (mass)
    float[] particlesVA = {};
    ArrayList<Particle> particlesToAdd = new ArrayList<>();

    // x y z   width
    float[] boxesVA = {};

    // x y z   pitch yaw roll   r
    float [] galaxyVA = {0, 0, 0, 0, 0, 0, 50};

    OcTree otRoot;

    int particlesVaoID, particlesVboID;
    int particlesProjectionMatrixUniformID, particlesTransformMatrixUniformID;

    int boxesVaoID, boxesVboID;
    int boxesProjectionMatrixUniformID, boxesTransformMatrixUniformID;

    int galaxyVaoID, galaxyVboID;
    int galaxyProjectionMatrixUniformID, galaxyTransformMatrixUniformID;

    private boolean drawDebug;
    private ImInt debugDisplayMode = new ImInt(2);
    ImInt debugDepth = new ImInt(1);
    boolean debugConstraint = false;

    float physicsDT = 0.005f;
    int itps = 50;
    boolean isPlaying = false;

    float secondsFromLastIteration = 0f;

    float secondsFromLastOutput = 0f;
    int iterationsFromLastOutput = 0;
    int outputIPS = 0;

    boolean showGalaxyAdder = false;
    float galaxyAdderX = 0f;
    float galaxyAdderY = 0f;
    float galaxyAdderZ = 0f;
    float galaxyAdderPitch = 0f;
    float galaxyAdderYaw = 0f;
    float galaxyAdderRoll = 0f;
    float galaxyAdderRad = 30f;



    @Override
    public void init() {
        updateGalaxyAdder();

        threadExecutor = Executors.newFixedThreadPool(8);

        camera = new Camera(0f, 0f, 0f, 7, 20f, -10f, 90f, 0.01f, 100000f, (float) Window.getWidth(), (float)Window.getHeight());

        otRoot = new OcTree(-50f, -50f, -50f, 100f);


        // load shaders
        particleShader = new Shader(
                "src\\main\\shaders\\particle.vert",
                "src\\main\\shaders\\particle.frag",
                null);
        particleShader.compile();
        boxShader = new Shader(
                "src\\main\\shaders\\box.vert",
                "src\\main\\shaders\\box.frag",
                "src\\main\\shaders\\box.geom");
        boxShader.compile();
        galaxyShader = new Shader(
                "src\\main\\shaders\\galaxy.vert",
                "src\\main\\shaders\\galaxy.frag",
                "src\\main\\shaders\\galaxy.geom");
        galaxyShader.compile();


        //generateNoiseGrid();

        // set buffers
        updateParticlesVertexArray();
        updateBoxesVertexArray();
        updateGalaxyVertexArray();





        // set uniforms
        particlesProjectionMatrixUniformID = glGetUniformLocation(particleShader.shaderProgramID, "projectionMatrix");
        particlesTransformMatrixUniformID = glGetUniformLocation(particleShader.shaderProgramID, "transformMatrix");

        boxesProjectionMatrixUniformID = glGetUniformLocation(boxShader.shaderProgramID, "projectionMatrix");
        boxesTransformMatrixUniformID = glGetUniformLocation(boxShader.shaderProgramID, "transformMatrix");

        galaxyProjectionMatrixUniformID = glGetUniformLocation(galaxyShader.shaderProgramID, "projectionMatrix");
        galaxyTransformMatrixUniformID = glGetUniformLocation(galaxyShader.shaderProgramID, "transformMatrix");

        // allow points and depth buffer
        glEnable(GL_PROGRAM_POINT_SIZE);
        glEnable(GL_POINT_SPRITE);
        glEnable(GL_DEPTH_TEST);


        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glHint(GL_LINE_SMOOTH, GL_NICEST);
        //glEnable(GL_LINE_SMOOTH);


        // set matrices
        particleShader.use();
        updateMatrix();
        particleShader.detach();

        boxShader.use();
        updateMatrix();
        boxShader.detach();

        galaxyShader.use();
        updateMatrix();
        galaxyShader.detach();

    }

    @Override
    public void inputs(float dt){
        // dolly moving
        if(KeyListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT) && MouseListener.isButtonDown(2)) {
            camera.calcVectors();

            camera.moveBy(  camera.getUp().x * MouseListener.getDy() / 800f * camera.getDistance(),
                    camera.getUp().y * MouseListener.getDy() / 800f * camera.getDistance(),
                    camera.getUp().z * MouseListener.getDy() / 800f * camera.getDistance());

            camera.moveBy(  -camera.getRight().x * MouseListener.getDx() / 800f * camera.getDistance(),
                    -camera.getRight().y * MouseListener.getDx() / 800f * camera.getDistance(),
                    -camera.getRight().z * MouseListener.getDx() / 800f * camera.getDistance());

            didCameraChange = true;
        }
        // drag zooming
        else if(KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && MouseListener.isButtonDown(2)){
            camera.changeZoom(-MouseListener.getDy()/300f);
            didCameraChange = true;
        }
        // rotating
        else if(MouseListener.isButtonDown(2)) {
            camera.rotateBy(MouseListener.getDy() / 5f, MouseListener.getDx() / 5f);
            didCameraChange = true;
        }
        // scroll zooming
        else if(MouseListener.getScrollY() != 0) {
            camera.changeZoom(0.2f*MouseListener.getScrollY());
            didCameraChange = true;
        }

        // hide and show cursor
//        if(MouseListener.isButtonClick(2)){
//            glfwSetInputMode(Window.getWindowID(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
//        }
//        else if(MouseListener.isButtonUnClick(2)){
//            glfwSetInputMode(Window.getWindowID(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
//        }


        // add new points
//        if(MouseListener.isButtonClick(1)){
//            particlesVA = new float[] {};
//            generateNoiseGrid();
//            //generateNewPoint();
//        }

        // show debug
        if(KeyListener.isKeyClick(GLFW_KEY_F3)) {
            drawDebug = !drawDebug;

            boxShader.use();
            updateMatrix();
            boxShader.detach();
        }


        // listeners logic
        MouseListener.endFrame();
        KeyListener.endFrame();
    }


    @Override
    public void update(float dt) {
        secondsFromLastIteration += dt;
        secondsFromLastOutput += dt;

        if(secondsFromLastOutput >= 1f){
            secondsFromLastOutput -= 1f;

            outputIPS = iterationsFromLastOutput;
            iterationsFromLastOutput = 0;
        }

        if(secondsFromLastIteration > 1.0f/itps){
            if(secondsFromLastIteration > 2.0f/itps)    secondsFromLastIteration = 0;
            else    secondsFromLastIteration -= 1.0f/itps;

            iterationsFromLastOutput ++;

            if(isPlaying){// update particles
                //otRoot.calculateParticles(dt, otRoot);


                // make 8 threads and update each octant separately
                if (!otRoot.isLeaf) {
                    CountDownLatch latch = new CountDownLatch(8);

                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            for (int k = 0; k < 2; k++) {
                                int finalK = k;
                                int finalI = i;
                                int finalJ = j;
                                threadExecutor.submit(() -> {
                                    otRoot.children[finalI][finalJ][finalK].calculateParticles(physicsDT, otRoot);
                                    latch.countDown();
                                });
                            }
                        }
                    }

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                otRoot.updateParticles(physicsDT); //TODO: multithread it
            }

            // get particles from the tree and add new
            List<Particle> particleList = new ArrayList<>(); //TODO: make it global and clear() it
            otRoot.getParticleData(particleList);
            particleList.addAll(particlesToAdd);

            particlesToAdd = new ArrayList<>();

            particlesVA = new float[particleList.size() * 8];


            for (int i = 0; i < particleList.size(); i++) {
                particlesVA[8 * i] = particleList.get(i).x;
                particlesVA[8 * i + 1] = particleList.get(i).y;
                particlesVA[8 * i + 2] = particleList.get(i).z;
                particlesVA[8 * i + 3] = particleList.get(i).r;
                particlesVA[8 * i + 4] = particleList.get(i).g;
                particlesVA[8 * i + 5] = particleList.get(i).b;
                particlesVA[8 * i + 6] = particleList.get(i).a;
                particlesVA[8 * i + 7] = particleList.get(i).s;
            }
            updateParticlesVertexArray();


            // construct a new tree
            if (particlesVA.length > 0) {
                float minX = particlesVA[0], maxX = particlesVA[0],
                        minY = particlesVA[1], maxY = particlesVA[1],
                        minZ = particlesVA[2], maxZ = particlesVA[2];

                for (int i = 8; i < particlesVA.length; i += 8) {
                    minX = Math.min(minX, particlesVA[i]);
                    maxX = Math.max(maxX, particlesVA[i]);

                    minY = Math.min(minY, particlesVA[i + 1]);
                    maxY = Math.max(maxY, particlesVA[i + 1]);

                    minZ = Math.min(minZ, particlesVA[i + 2]);
                    maxZ = Math.max(maxZ, particlesVA[i + 2]);
                }

                float avX = (minX + maxX) / 2,
                        avY = (minY + maxY) / 2,
                        avZ = (minZ + maxZ) / 2;
                float width = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ) + 10.0f;

                float finalX = avX - width / 2,
                        finalY = avY - width / 2,
                        finalZ = avZ - width / 2;

                //otRoot = new program.OcTree(-50f, -50f, -50f, 100f);
                otRoot = new OcTree(finalX, finalY, finalZ, width);

                for (int i = 0; i < particleList.size(); i++) {
                    otRoot.insert(particleList.get(i));
                }
            }


            // get boxes from the tree
            if (drawDebug) {
                ArrayList<Float> boxesData = new ArrayList<Float>();
                otRoot.getBoxData(boxesData, debugDisplayMode.get(), debugConstraint?debugDepth.get():-1);
                boxesVA = new float[boxesData.size()];
                for (int i = 0; i < boxesVA.length; i++) {
                    boxesVA[i] = boxesData.get(i);
                }
                updateBoxesVertexArray();
            }
        }
    }

    @Override
    public void render(float dt){
        particleShader.use();
        glBindVertexArray(particlesVaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        if(didCameraChange){
            updateMatrix();
        }
        glDrawArrays(GL_POINTS, 0, particlesVA.length);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
        particleShader.detach();



        if(drawDebug) {
            // draw boxes
            boxShader.use();
            glBindVertexArray(boxesVaoID);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);

            if (didCameraChange) {
                updateMatrix();
            }
            glDrawArrays(GL_POINTS, 0, boxesVA.length);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glBindVertexArray(0);
            boxShader.detach();
        }


        if(showGalaxyAdder){
            galaxyShader.use();
            glBindVertexArray(galaxyVaoID);
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);

            if (didCameraChange) {
                updateMatrix();
            }
            glDrawArrays(GL_POINTS, 0, galaxyVA.length);

            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
            glBindVertexArray(0);
            galaxyShader.detach();
        }

        didCameraChange = false;
    }

    @Override
    public void renderUI(){
        //ImGui.showDemoWindow();
        float alpha = 0.5f;

        ImGui.setNextWindowPos(10, 10);
        ImGui.setNextWindowSize(260, drawDebug?Window.getHeight()-430:Window.getHeight()-130);
        ImGui.setNextWindowBgAlpha(alpha);

        ImGui.begin("variables", ImGuiWindowFlags.NoCollapse  |
                           ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar);
        {
            float[] toSl1 = {physicsDT};
            if(ImGui.dragFloat("dt", toSl1, 0.0001f, 0f, 1f)){
                physicsDT = toSl1[0];
            }
            ImGui.sameLine();
            ImGui.textDisabled("(?)");
            if(ImGui.isItemHovered()){
                ImGui.setTooltip("amount of time to calculate in each iteration");
            }


            int[] toSl2 = {itps};
            if(ImGui.dragInt("it/s", toSl2, 1, 1, 120)){
                itps = toSl2[0];
            }
            ImGui.sameLine();
            ImGui.textDisabled("(?)");
            if(ImGui.isItemHovered()){
                ImGui.setTooltip("target iterations per second");
            }


            ImGui.text("actual it/s: " + outputIPS);

            ImGui.text("");
            ImGui.text("");

            String buttonText = isPlaying?"pause":"play";
            ImGui.setCursorPosX(ImGui.getWindowWidth()/2-50);
            if(ImGui.button(buttonText, 100, 50)){
                isPlaying = !isPlaying;
            }
        }
        ImGui.end();



        ImGui.setNextWindowPos(280, 10);
        ImGui.setNextWindowSize(Window.getWidth()-290, 100);
        ImGui.setNextWindowBgAlpha(alpha);

        ImGui.begin("tools", ImGuiWindowFlags.NoCollapse  |
                           ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar);
        {
            if(ImGui.button("clear", 84, 84)){
                otRoot = new OcTree(-50, -50, -50, 100);
            }
            ImGui.sameLine();
            if(ImGui.button("add\na single\npoint", 84, 84)){
                generateNewPoint();
            }
            ImGui.sameLine();
            if(ImGui.button("add\nnoise\ngrid", 84, 84)){
                generateNoiseGrid();
            }
            ImGui.sameLine();
            if(ImGui.button("add\na\ngalaxy", 84, 84)){
                showGalaxyAdder = !showGalaxyAdder;

                galaxyShader.use();
                updateMatrix();
                galaxyShader.detach();
            }
        }
        ImGui.end();

        if(showGalaxyAdder){
            ImGui.setNextWindowPos(Window.getWidth()-260, Window.getHeight()-460);
            ImGui.setNextWindowSize(250, 450);
            ImGui.setNextWindowBgAlpha(0);

            ImGui.begin("galaxy adder", ImGuiWindowFlags.NoCollapse  |
                    ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);
            {
                float[] toSl1 = {galaxyAdderX};
                if(ImGui.dragFloat("X", toSl1, 0.1f, -Float.MAX_VALUE, Float.MAX_VALUE)){
                    galaxyAdderX = toSl1[0];
                    updateGalaxyAdder();
                }
                float[] toSl2 = {galaxyAdderY};
                if(ImGui.dragFloat("Y", toSl2, 0.1f, -Float.MAX_VALUE, Float.MAX_VALUE)){
                    galaxyAdderY = toSl2[0];
                    updateGalaxyAdder();
                }
                float[] toSl3 = {galaxyAdderZ};
                if(ImGui.dragFloat("Z", toSl3, 0.1f, -Float.MAX_VALUE, Float.MAX_VALUE)){
                    galaxyAdderZ = toSl3[0];
                    updateGalaxyAdder();
                }
                ImGui.text("");

                float[] toSl4 = {galaxyAdderPitch};
                if(ImGui.dragFloat("Pitch", toSl4, 0.01f, -(float)Math.PI, (float)Math.PI)){
                    galaxyAdderPitch = toSl4[0];
                    updateGalaxyAdder();
                }
                float[] toSl5 = {galaxyAdderYaw};
                if(ImGui.dragFloat("Yaw", toSl5, 0.01f, -(float)Math.PI, (float)Math.PI)){
                    galaxyAdderYaw = toSl5[0];
                    updateGalaxyAdder();
                }
                float[] toSl6 = {galaxyAdderRoll};
                if(ImGui.dragFloat("Roll", toSl6, 0.01f, -(float)Math.PI, (float)Math.PI)){
                    galaxyAdderRoll = toSl6[0];
                    updateGalaxyAdder();
                }
                ImGui.text("");

                float[] toSl7 = {galaxyAdderRad};
                if(ImGui.dragFloat("radius", toSl7, 0.02f, 1f, 100f)){
                    galaxyAdderRad = toSl7[0];
                    updateGalaxyAdder();
                }
                ImGui.text("");

                ImGui.setCursorPosX(ImGui.getWindowWidth()/2 - 100);
                if(ImGui.button("add", 200, 35)){
                    showGalaxyAdder = false;
                    generateGalaxy(galaxyAdderX, galaxyAdderY, galaxyAdderZ,
                            galaxyAdderPitch, galaxyAdderYaw, galaxyAdderRoll, galaxyAdderRad, 800);
                }
            }
            ImGui.end();
        }

        if(drawDebug){
            ImGui.setNextWindowPos(10, Window.getHeight()-410);
            ImGui.setNextWindowSize(260, 400);
            ImGui.setNextWindowBgAlpha(alpha);
            ImGui.begin("debug", ImGuiWindowFlags.NoCollapse  |
                    ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);
            {
                ImGui.text("OcTree display mode: ");
                ImGui.radioButton("only full leaf nodes", debugDisplayMode, 0);
                ImGui.radioButton("only full nodes", debugDisplayMode, 1);
                ImGui.radioButton("all leaf nodes", debugDisplayMode, 2);
                ImGui.radioButton("all nodes", debugDisplayMode, 3);

                ImGui.setCursorPosY(ImGui.getCursorPosY()+15);

                if(ImGui.checkbox("constraint depth", debugConstraint)){
                    debugConstraint = !debugConstraint;
                }
                if(debugConstraint){
                    if (ImGui.inputInt("depth", debugDepth, 1, 1)) {
                        if (debugDepth.get() < 1) debugDepth.set(1);
                    }
                    //ImGui.dragIntRange2("") // TODO
                }

                ImGui.setCursorPosX(ImGui.getWindowWidth()/2-50);
                ImGui.setCursorPosY(ImGui.getWindowHeight()-50-8);
                if(ImGui.button("hide", 100, 50)){
                    drawDebug = false;
                }
            }
            ImGui.end();
        }
        else {
            ImGui.setNextWindowPos(10, Window.getHeight()-110);
            ImGui.setNextWindowSize(260, 100);
            ImGui.setNextWindowBgAlpha(alpha);
            ImGui.begin("debug", ImGuiWindowFlags.NoCollapse  |
                    ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);
            {
                ImGui.setCursorPosX(ImGui.getWindowWidth()/2-50);
                if(ImGui.button("show", 100, 50)){
                    drawDebug = true;

                    boxShader.use();
                    updateMatrix();
                    boxShader.detach();
                }
            }
            ImGui.end();
        }
    }

    private void updateGalaxyAdder(){
        // x y z   pitch yaw roll   r
        galaxyVA[0] = galaxyAdderX*2f;
        galaxyVA[1] = galaxyAdderY*2f;
        galaxyVA[2] = galaxyAdderZ*2f;

        galaxyVA[3] = galaxyAdderPitch;
        galaxyVA[4] = galaxyAdderYaw;
        galaxyVA[5] = galaxyAdderRoll;

        galaxyVA[6] = galaxyAdderRad*2f;

        updateGalaxyVertexArray();
    }

    @Override
    public void end(){
        threadExecutor.shutdown();
    }

    private void updateMatrix(){
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            camera.getProjectionMatrix().get(fb);
            glUniformMatrix4fv(particlesProjectionMatrixUniformID, false, fb);
            glUniformMatrix4fv(boxesProjectionMatrixUniformID, false, fb);
            glUniformMatrix4fv(galaxyProjectionMatrixUniformID, false, fb);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            camera.getTransformMatrix().get(fb);
            glUniformMatrix4fv(particlesTransformMatrixUniformID, false, fb);
            glUniformMatrix4fv(boxesTransformMatrixUniformID, false, fb);
            glUniformMatrix4fv(galaxyTransformMatrixUniformID, false, fb);
        }
    }

    private void generateNewPoint(){
        float   x = (float)(Math.random()*100 - 50),
                y = (float)(Math.random()*100 - 50),
                z = (float)(Math.random()*100 - 50),
                r = (float)(Math.random()*0.5 + 0.5),
                g = (float)(Math.random()*0.5 + 0.5),
                b = (float)(Math.random()*0.5 + 0.5),
                s = (float)(Math.random()*2 + 3.0),
                velX = (float)(Math.random()-0.5)*2f,
                velY = (float)(Math.random()-0.5)*2f,
                velZ = (float)(Math.random()-0.5)*2f;


        particlesToAdd.add(new Particle(x, y, z, r, g, b, 1f, s, velX, velY, velZ));

    }

    private void generateNoiseGrid(){
        OpenSimplex2S n1 = new OpenSimplex2S((int)(Math.random()*10000));
        OpenSimplex2S n2 = new OpenSimplex2S((int)(Math.random()*10000));

        int rad = 75;
        for(float x = -rad; x < rad; x += 3){
            for(float y = -rad; y < rad; y += 3) {
                for(float z = -rad; z < rad; z += 3) {
                    if(x*x + y*y + z*z < rad*rad) {
                        float s1 = 0.04f;
                        float s2 = 0.1f;
                        float v1 = (float) n1.noise3_Classic(x * s1, y * s1, z * s1) * 0.5f + 0.5f;
                        float v2 = (float) n2.noise3_Classic(x * s2, y * s2, z * s2) * 0.5f + 0.5f;
                        if (Math.random() * 0.1 > v1 * v2) {
                            float nx = (float)(Math.random() - 0.5 + x);
                            float ny = (float)(Math.random() - 0.5 + y);
                            float nz = (float)(Math.random() - 0.5 + z);

                            float nr = (float) Math.pow(v1, 1.3) * 4f;
                            float ng = (float) Math.pow(v2, 4) * 4f;
                            float nb = (float) Math.pow(v1, 0.6) * 4f;

                            float ns = (float)Math.random()+0.5f;


                            float angle = (float)Math.atan2(x, z);
                            float dist = (float)Math.sqrt(x*x + z*z);

                            float velX = (float)(Math.sin(angle+Math.PI/2))*dist*0.35f;
                            float velZ = (float)(Math.cos(angle+Math.PI/2))*dist*0.35f;

                            particlesToAdd.add(new Particle(nx, ny, nz, nr, ng, nb, 1f, ns, velX, 0f, velZ));
                            //otRoot.insert(nx, ny, nz, nr, ng, nb, 1, ns);
                        }
                    }
                }
            }
        }
    }

    private void generateGalaxy(float x, float y, float z, float pitch, float yaw, float roll, float r, int n){
        // TODO: generate temporal octree only for galaxy particles, pass it to Particle.setOrbitalVelocity, then get particles and assign them to particles to add
        // TODO: add debug window for showing the octree
        // TODO: add galaxy editor window

        float maxX = x;
        float minX = x;
        float maxY = y;
        float minY = y;
        float maxZ = z;
        float minZ = z;

        ArrayList<Particle> galaxyParticles = new ArrayList<>();
        ArrayList<Float> dists = new ArrayList<>();

        galaxyParticles.add(new Particle(x, y, z, 1, 1, 1, 1, 100, 0, 0, 0));
        dists.add(0f);

        for (int i = 1; i < n; i++) {
            float angle = (float)(i * Math.PI*4)/100.3f; //(float)(Math.random() * Math.PI*2);
            float dist = ((float)i/n * 0.9f + 0.1f) * r; //r * ((float)Math.random()*0.8f+0.2f);
            Vector3f particle = new Vector3f(dist, 0, 0).rotateY(angle);

            float velX = (float)((Math.sin(angle/*+Math.PI/2*/)) * 50*Math.pow(1.0/dist, 0.5));
            float velZ = (float)((Math.cos(angle/*+Math.PI/2*/)) * 50*Math.pow(1.0/dist, 0.5));
            Vector3f velocity = new Vector3f(velX, 0, velZ);

            particle.
                    rotateX(pitch).
                    rotateY(yaw).
                    rotateZ(roll).
                    add(x, y, z);

            velocity.
                    rotateX(pitch).
                    rotateY(yaw).
                    rotateZ(roll);

            float   red = (float)(Math.random()*0.5 + 0.5),
                    green = (float)(Math.random()*0.5 + 0.5),
                    blue = (float)(Math.random()*0.5 + 0.5);
            float s = (float)Math.random()*0.05f+0.15f;

            Particle p = new Particle(
                    particle.x,
                    particle.y,
                    particle.z,
                    red,
                    green,
                    blue,
                    0f,
                    s,
                    velocity.x,
                    velocity.y,
                    velocity.z);

            galaxyParticles.add(p);
            dists.add(dist);

            maxX = Math.max(maxX, particle.x);
            minX = Math.min(minX, particle.x);
            maxY = Math.max(maxY, particle.y);
            minY = Math.min(minY, particle.y);
            maxZ = Math.max(maxZ, particle.z);
            minZ = Math.min(minZ, particle.z);
        }

        float avX = (minX + maxX) / 2,
                avY = (minY + maxY) / 2,
                avZ = (minZ + maxZ) / 2;
        float width = Math.max(Math.max(maxX - minX, maxY - minY), maxZ - minZ) + 10.0f;

        float finalX = avX - width / 2,
                finalY = avY - width / 2,
                finalZ = avZ - width / 2;

        OcTree galaxyTree = new OcTree(finalX, finalY, finalZ, width);

        for (int i = 0; i < galaxyParticles.size(); i++) {
            galaxyTree.insert(galaxyParticles.get(i));
        }

//        Vector3f galacticNormal = new Vector3f(0, 1, 0).
//                rotateX(pitch).
//                rotateY(yaw).
//                rotateZ(roll);
//
//        for (int i = 1; i < galaxyParticles.size(); i++) {
//            galaxyParticles.get(i).setOrbitalVelocity(galaxyTree, galacticNormal, dists.get(i));
//        }

        particlesToAdd.addAll(galaxyParticles);
    }

    private void updateParticlesVertexArray(){
        particlesVaoID = glGenVertexArrays();
        glBindVertexArray(particlesVaoID);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(particlesVA.length);
        vertexBuffer.put(particlesVA).flip();


        particlesVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, particlesVboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);


        int positionsSize = 3;
        int colorSize = 4;
        int sizeSize = 1;
        int floatSizeBytes = Float.BYTES;
        int vertexSizeBytes = (positionsSize + colorSize + sizeSize) * floatSizeBytes;

        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize*floatSizeBytes);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, sizeSize, GL_FLOAT, false, vertexSizeBytes, (positionsSize+colorSize)*floatSizeBytes);
        glEnableVertexAttribArray(2);
    }

    private void updateBoxesVertexArray(){
        boxesVaoID = glGenVertexArrays();
        glBindVertexArray(boxesVaoID);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(boxesVA.length);
        vertexBuffer.put(boxesVA).flip();


        boxesVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, boxesVboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);


        //IntBuffer elementBuffer = BufferUtils.createIntBuffer(boxesEA.length);
        //elementBuffer.put(boxesEA).flip();

        //boxesEboID = glGenBuffers();
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, boxesEboID);
        //glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);


        int positionsSize = 3;
        int sizeSize = 1;
        int floatSizeBytes = Float.BYTES;
        int vertexSizeBytes = (positionsSize + sizeSize) * floatSizeBytes;

        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, sizeSize, GL_FLOAT, false, vertexSizeBytes, positionsSize*floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    private void updateGalaxyVertexArray(){
        galaxyVaoID = glGenVertexArrays();
        glBindVertexArray(galaxyVaoID);

        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(galaxyVA.length);
        vertexBuffer.put(galaxyVA).flip();


        galaxyVboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, galaxyVboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);


        int positionsSize = 3;
        int rotationsSize = 3;
        int radiusSize = 1;
        int floatSizeBytes = Float.BYTES;
        int vertexSizeBytes = (positionsSize + rotationsSize + radiusSize) * floatSizeBytes;

        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, rotationsSize, GL_FLOAT, false, vertexSizeBytes, positionsSize*floatSizeBytes);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, radiusSize, GL_FLOAT, false, vertexSizeBytes, (positionsSize + rotationsSize)*floatSizeBytes);
        glEnableVertexAttribArray(2);
    }

    @Override
    public float[] getBackgroundColor(){
        return backgroundColor;
    }

    @Override
    public Camera getCamera() {
        return camera;
    }

    @Override
    public void setCameraChange(boolean val) {
        didCameraChange = val;
    }
}
