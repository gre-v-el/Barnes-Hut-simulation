package renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class Shader {
    public int shaderProgramID;

    private String vertexSource, fragmentSource, geometrySource = null;
    private String vertFile, fragFile, geoFile;

    public Shader(String vertFile, String fragFile, String geoFile){
        this.vertFile = vertFile;
        this.fragFile = fragFile;
        this.geoFile = geoFile;

        // read and split the file
        try{
            vertexSource = new String(Files.readAllBytes(Paths.get(vertFile)));
            fragmentSource = new String(Files.readAllBytes(Paths.get(fragFile)));
            if(geoFile != null)
                geometrySource = new String(Files.readAllBytes(Paths.get(geoFile)));
        } catch(IOException e){
            e.printStackTrace();
            assert false: "Could not open file for shader";
        }

    }

    public void compile(){
        int vertexID, fragmentID, geometryID = 0;

        vertexID = glCreateShader(GL_VERTEX_SHADER);

        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if(success == GL_FALSE){
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: \n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);

        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if(success == GL_FALSE){
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: \n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        if(geometrySource != null){
            geometryID = glCreateShader(GL_GEOMETRY_SHADER);

            glShaderSource(geometryID, geometrySource);
            glCompileShader(geometryID);

            success = glGetShaderi(geometryID, GL_COMPILE_STATUS);
            if(success == GL_FALSE){
                int len = glGetShaderi(geometryID, GL_INFO_LOG_LENGTH);
                System.out.println("Error: \n\tGeometry shader compilation failed.");
                System.out.println(glGetShaderInfoLog(geometryID, len));
                assert false : "";
            }
        }

        shaderProgramID = glCreateProgram();
        glAttachShader(shaderProgramID, vertexID);
        glAttachShader(shaderProgramID, fragmentID);
        if(geometrySource != null) {
            glAttachShader(shaderProgramID, geometryID);
        }
        glLinkProgram(shaderProgramID);

        success = glGetProgrami(shaderProgramID, GL_LINK_STATUS);
        if(success == GL_FALSE){
            int len = glGetProgrami(shaderProgramID, GL_INFO_LOG_LENGTH);
            System.out.println("Error: \n\tLinking shaders failed.");
            System.out.println(glGetProgramInfoLog(fragmentID, len));
            assert false : "";
        }
    }

    public void use(){
        glUseProgram(shaderProgramID);
    }

    public void detach(){
        glUseProgram(0);
    }
}
