package openGL_code;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_CCW;
import static com.jogamp.opengl.GL.GL_COLOR_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_ELEMENT_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_REPEAT;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TEXTURE_WRAP_S;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;

import app_interface.ErrorLogger;
import app_interface.OBJLoader;
import app_interface.OpenGlUtilities;
import app_interface.TriangleFace;
import app_interface.VertexData;

public class OpenGlRenderer implements GLEventListener {

	// Fields and methods related to the OpenGL panel construction.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private GLJPanel openGlPanel;

	public OpenGlRenderer(String modelFilename, String shadersFolder, Vector3f cameraPos, Vector3f cameraLookAtCenter,
			Vector3f cameraUp, float horizontalFOV, float modelScale, float materialDiffuse, float materialAmbient,
			float materialSpecular, float materialShininess, Vector3f lightPositionWorldCoordinates) {

		// Initialize the OpenGL panel.
		openGlPanel = new GLJPanel();
		openGlPanel.addGLEventListener(this);
		Animator animator = new Animator(openGlPanel);
		animator.start();

		// Load model, shaders, and set initial parameters.
		changeModel(modelFilename);
		changeShaders(shadersFolder);
		setCameraLocation(cameraPos, cameraLookAtCenter, cameraUp, horizontalFOV);
		setModelTransformation(modelScale);
		setLightingParams(materialDiffuse, materialAmbient, materialSpecular, materialShininess,
				lightPositionWorldCoordinates);
	}

	public GLJPanel getOpenGlPanel() {
		return openGlPanel;
	}

	// Fields and methods related to transformation matrices.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private Matrix4f modelM = new Matrix4f();
	private Matrix4f lookatM = new Matrix4f();
	private Matrix4f projectionM = new Matrix4f();
	private Matrix3f normalFromObjectCoordToEyeCoordM;

	public void setCameraLocation(Vector3f cameraPos, Vector3f cameraLookAtCenter, Vector3f cameraUp,
			float horizontalFOV) {
		lookatM = new Matrix4f().lookAt(cameraPos, cameraLookAtCenter, cameraUp);
		projectionM = calcProjectionM();
		normalFromObjectCoordToEyeCoordM = calcNormalTransformationMatrix();
	}

	public void setModelTransformation(float modelScale) {
		modelM = new Matrix4f().scale(modelScale);
		normalFromObjectCoordToEyeCoordM = calcNormalTransformationMatrix();
	}

	private Matrix4f calcProjectionM() {
		float fovy = (float) (30f / 180 * Math.PI), aspect = 1f, zNear = 1f, zFar = 100f;
		aspect = (float) openGlPanel.getWidth() / (float) openGlPanel.getHeight();
		return new Matrix4f().perspective(fovy, aspect, zNear, zFar);
	}

	private Matrix3f calcNormalTransformationMatrix() {
		Matrix4f modelViewMatrix = new Matrix4f(lookatM).mul(modelM);
		return new Matrix3f(modelViewMatrix).invert().transpose();
	}

	// Fields and methods related to lighting parameters.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private Vector4f materialDiffuse;
	private Vector4f materialAmbient;
	private Vector4f materialSpecular;
	private float materialShininess;
	private Vector3f lightPositionWorldCoordinates;
	private Vector3f lightPositionEyeCoord = new Vector3f();

	public void setLightingParams(float materialDiffuse, float materialAmbient, float materialSpecular,
			float materialShininess, Vector3f lightPositionWorldCoordinates) {
		this.materialDiffuse = new Vector4f(materialDiffuse, materialDiffuse, materialDiffuse, 1f);
		this.materialAmbient = new Vector4f(materialAmbient, materialAmbient, materialAmbient, 1f);
		this.materialSpecular = new Vector4f(materialSpecular, materialSpecular, materialSpecular, 1f);
		this.materialShininess = materialShininess;
		this.lightPositionWorldCoordinates = lightPositionWorldCoordinates;
	}

	
	// Fields and methods related to the OpenGL panel callbacks.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private boolean fileLoaded;
	private boolean updateShaders;

	// GLEventListener method: Called during the initialization of the OpenGL
	// context
	@Override
	public void init(GLAutoDrawable drawable) {
	}

	public void changeShaders(String shadersFolder) {
		this.shadersFolder = shadersFolder;
		updateShaders = true;
	}

	public void changeModel(String modelFilename) {
		parseObjFile(modelFilename);
		updateModel = true;
	}

	// GLEventListener method: Called to perform rendering for each frame
	@Override
	public void display(GLAutoDrawable drawable) {
		if (updateModel) {
			updateModel = false;
			modelUploadingToOpenGlBuffers();
			textureUploadingToOpenGlBuffers();
		}
		if (updateShaders) {
			updateShaders = false;
			shadersUploadingToOpenGl();
		}

		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Enable face culling and set front face orientation.
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);

		// Enable depth testing and set the depth test function.
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		
		// Clear color and depth buffers before rendering.
		gl.glClearColor(0.0f, 0.0f, 0.f, 1.0f);
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		// Use the shader program.
		gl.glUseProgram(shadersProgram);

		// Transform the light source to eye coordinates.
		Vector4f t1 = new Vector4f(lightPositionWorldCoordinates, 1f);
		lookatM.transform(t1);
		lightPositionEyeCoord = new Vector3f(t1.x, t1.y, t1.z);

		// Upload the uniform variables to the shader.
		uniformsUploadingToOpenGl();

		// Send the vertex buffer data to the OpenGL pipeline for rendering.
		sendBuffersToOpenGlPipeline();

		// Send texture to OpenGl pipeline
		sendTextureToOpenGlPipeline();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		projectionM = calcProjectionM();
	}

	public void dispose(GLAutoDrawable drawable) {
		// it is better to add code the deallocate resources here...
	}


	// Fields and methods related to parsing the model, uploading it to the graphics
	// card buffers, and sending it to the graphics pipeline for rendering.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private boolean updateModel;
	private List<VertexData> verticesData;
	private List<TriangleFace> faces;
	private int numIndices; // Number of indices in the torus model
	private String textureFilename;

	private void parseObjFile(String fileName) {
		OBJLoader objLoader = new OBJLoader();
		try {
			objLoader.loadOBJ(fileName);
			verticesData = objLoader.getVertices();
			faces = objLoader.getFaces();
			textureFilename = objLoader.getTextureFilename();
			fileLoaded = true;
		} catch (IOException e) {
			fileLoaded = false;
		}
	}

	public boolean getImageLoaded() {
		return fileLoaded;
	}

	
	
	// Fields and methods related to uploading Vertex attributes to the vertex buffers
	////////////////////////////////////////////////////////////////////////////////////
	private int[] vbo = new int[4]; // Array to hold the IDs of the Vertex Buffer Objects
	
	private void modelUploadingToOpenGlBuffers() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		// Create arrays for vertex positions, texture coordinates, and normals.
		float[] pointObjectCoordArray = new float[verticesData.size() * 3]; // 3 floats per vertex (x, y, z)
		float[] textureCoordArray = new float[verticesData.size() * 2]; // 2 floats per texture coordinate (u, v)
		float[] normalObjectCoordArray = new float[verticesData.size() * 3]; // 3 floats per normal (nx, ny, nz)
		int[] indicesArray = new int[faces.size() * 3];

		// Populate the arrays from the lists.
		for (int i = 0; i < verticesData.size(); i++) {
			pointObjectCoordArray[i * 3] = (float) verticesData.get(i).pointObjectCoordinates.x();
			pointObjectCoordArray[i * 3 + 1] = (float) verticesData.get(i).pointObjectCoordinates.y();
			pointObjectCoordArray[i * 3 + 2] = (float) verticesData.get(i).pointObjectCoordinates.z();
			textureCoordArray[i * 2] = (float) verticesData.get(i).textureCoordinates.x();
			textureCoordArray[i * 2 + 1] = (float) verticesData.get(i).textureCoordinates.y();
			normalObjectCoordArray[i * 3] = (float) verticesData.get(i).normalObjectCoordinates.x();
			normalObjectCoordArray[i * 3 + 1] = (float) verticesData.get(i).normalObjectCoordinates.y();
			normalObjectCoordArray[i * 3 + 2] = (float) verticesData.get(i).normalObjectCoordinates.z();
		}
		numIndices = faces.size() * 3;
		for (int i = 0; i < faces.size(); i++) {
			indicesArray[i * 3] = faces.get(i).indices[0];
			indicesArray[i * 3 + 1] = faces.get(i).indices[1];
			indicesArray[i * 3 + 2] = faces.get(i).indices[2];
		}

//		// Generate and bind a Vertex Array Object (VAO).
//		gl.glGenVertexArrays(1, vao, 0);
//		gl.glBindVertexArray(vao[0]);

		// Generate Vertex Buffer Objects (VBOs).
		gl.glGenBuffers(4, vbo, 0);

		// Use the helper method to create and bind the VBOs.
		createAndBindBuffer(gl, GL_ARRAY_BUFFER, vbo[0], pointObjectCoordArray); // Vertex positions
		createAndBindBuffer(gl, GL_ARRAY_BUFFER, vbo[1], normalObjectCoordArray); // Vertex normals
		createAndBindBuffer(gl, GL_ARRAY_BUFFER, vbo[2], textureCoordArray); // Texture coordinates
		createAndBindBuffer(gl, GL_ELEMENT_ARRAY_BUFFER, vbo[3], indicesArray); // Element indices
	}

	// Helper method to create and bind a buffer
	private void createAndBindBuffer(GL4 gl, int target, int bufferId, float[] data) {
		gl.glBindBuffer(target, bufferId);
		FloatBuffer buffer = Buffers.newDirectFloatBuffer(data);
		gl.glBufferData(target, buffer.limit() * 4, buffer, GL_STATIC_DRAW);
	}
	
	// Helper method to create and bind a buffer
	private void createAndBindBuffer(GL4 gl, int target, int bufferId, int[] data) {
		gl.glBindBuffer(target, bufferId);
		IntBuffer buffer = Buffers.newDirectIntBuffer(data);
		gl.glBufferData(target, buffer.limit() * 4, buffer, GL_STATIC_DRAW);
	}
	
	private void sendBuffersToOpenGlPipeline() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		// Bind the VBO containing vertex positions and specify the attribute pointer
	    bindAttribute(gl, 0, vbo[0], 3); // position

		// Bind the VBO containing vertex normals and specify the attribute pointer
	    bindAttribute(gl, 1, vbo[1], 3); // normal

		if (textureFilename != null) {
			bindAttribute(gl, 2, vbo[2], 2);
		}
	    
		// Bind the Element Array Buffer (EAB) containing the indices and draw the
		// indexed triangles
		gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo[3]);
		gl.glDrawElements(GL_TRIANGLES, numIndices, GL_UNSIGNED_INT, 0);
	}

	// Helper method to bind a buffer and associate it with a vertex attribute
	private void bindAttribute(GL4 gl, int attributeIndex, int bufferId, int size) {
	    gl.glBindBuffer(GL_ARRAY_BUFFER, bufferId);
	    gl.glVertexAttribPointer(attributeIndex, size, GL_FLOAT, false, 0, 0);
	    gl.glEnableVertexAttribArray(attributeIndex);
	}
	
	
	
	// Fields and methods related to uploading shaders to the graphics card.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private String shadersFolder;
	private int shadersProgram; // ID of the compiled and linked shader program

	private void shadersUploadingToOpenGl() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		String vertexShaderFilename = shadersFolder + "\\vertexShader.glsl";
		String fragmentShaderFilename = shadersFolder + "\\fragmentShader.glsl";
		if (shadersProgram != 0) 
			gl.glDeleteProgram(shadersProgram);
		shadersProgram = OpenGlUtilities.createShaderProgram(vertexShaderFilename, fragmentShaderFilename);
		if(shadersProgram==0) 
			ErrorLogger.getInstance().report("Fail to load shaders program");
			
	}

	public boolean isShaderFilesExist() {
	    String vertexShaderFilename = shadersFolder + "\\vertexShader.glsl";
	    String fragmentShaderFilename = shadersFolder + "\\fragmentShader.glsl";
	    File vertexFile = new File(vertexShaderFilename);
	    File fragmentFile = new File(fragmentShaderFilename);
	    return vertexFile.exists() && fragmentFile.exists();
	}	



	// Fields and methods related to uploading uniforms to the graphics card.
	//////////////////////////////////////////////////////////////////////////////////////////////
	private void uniformsUploadingToOpenGl() {
	    GL4 gl = (GL4) GLContext.getCurrentGL();

	    // Upload transformation matrices
	    uploadUniform(gl, "modelM", modelM);
	    uploadUniform(gl, "lookatM", lookatM);
	    uploadUniform(gl, "projectionM", projectionM);
	    uploadUniform(gl, "normalFromObjectCoordToEyeCoordM", normalFromObjectCoordToEyeCoordM);

	    // Upload light position
	    uploadUniform(gl, "lightPositionEyeCoord", lightPositionEyeCoord);
	    
	    // Upload material properties
	    uploadUniform(gl, "materialAmbient", materialAmbient);
	    uploadUniform(gl, "materialDiffuse", materialDiffuse);
	    uploadUniform(gl, "materialSpecular", materialSpecular);
	    uploadUniform(gl, "materialShininess", materialShininess);
	}

	// Overloaded methods for different JOML types
	private void uploadUniform(GL4 gl, String name, Matrix4f matrix) {
	    FloatBuffer buffer = Buffers.newDirectFloatBuffer(16);
	    buffer.clear();
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glUniformMatrix4fv(loc, 1, false, matrix.get(buffer));
	}

	private void uploadUniform(GL4 gl, String name, Matrix3f matrix) {
	    FloatBuffer buffer = Buffers.newDirectFloatBuffer(16);
	    buffer.clear();
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glUniformMatrix3fv(loc, 1, false, matrix.get(buffer));
	}

	private void uploadUniform(GL4 gl, String name, Vector4f vector) {
	    FloatBuffer buffer = Buffers.newDirectFloatBuffer(4);
	    vector.get(buffer);
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glProgramUniform4fv(shadersProgram, loc, 1, buffer);
	}

	private void uploadUniform(GL4 gl, String name, Vector3f vector) {
	    FloatBuffer buffer = Buffers.newDirectFloatBuffer(3);
	    vector.get(buffer);
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glProgramUniform3fv(shadersProgram, loc, 1, buffer);
	}

	private void uploadUniform(GL4 gl, String name, Vector2f vector) {
	    FloatBuffer buffer = Buffers.newDirectFloatBuffer(2);
	    vector.get(buffer);
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glProgramUniform2fv(shadersProgram, loc, 1, buffer);
	}

	private void uploadUniform(GL4 gl, String name, float value) {
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glProgramUniform1f(shadersProgram, loc, value);
	}

	private void uploadUniform(GL4 gl, String name, int value) {
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glProgramUniform1i(shadersProgram, loc, value);
	}

	private void uploadUniform(GL4 gl, String name, boolean value) {
	    int loc = gl.glGetUniformLocation(shadersProgram, name);
	    gl.glProgramUniform1i(shadersProgram, loc, value ? 1 : 0);
	}

	
	
	// Fields and methods related to uploading Textures
	////////////////////////////////////////////////////////////////////////////////////
	private int textureID;

	private void textureUploadingToOpenGlBuffers() {
		GL4 gl = (GL4) GLContext.getCurrentGL();

		if (textureFilename != null) {
			textureID = OpenGlUtilities.loadTexture(textureFilename);
			gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		}
	}
	
	private void sendTextureToOpenGlPipeline() {
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, textureID);
	}
	
}
