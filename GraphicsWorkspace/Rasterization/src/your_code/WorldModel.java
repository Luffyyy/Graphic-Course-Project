/**
 * Handed by:
 * 
 * Daniel Rozentsvaig
 * Tomer Roll
 * 
 * lab_8
 */

package your_code;

import java.nio.IntBuffer;
import java.util.Random;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import app_interface.DisplayTypeEnum;
import app_interface.ExerciseEnum;
import app_interface.IntBufferWrapper;
import app_interface.ProjectionTypeEnum;

public class WorldModel {

	// type of rendering
	public ProjectionTypeEnum projectionType;
	public DisplayTypeEnum displayType;
	public boolean displayNormals;
	public YourSelectionEnum yourSelection;
	
	// camera location parameters
	public Vector3f cameraPos = new Vector3f();
	public Vector3f cameraLookAtCenter = new Vector3f();
	public Vector3f cameraUp = new Vector3f();
	public float horizontalFOV;

	// transformation parameters
	public float modelScale;

	// lighting parameters
	public float lighting_Diffuse;
	public float lighting_Specular;
	public float lighting_Ambient;
	public float lighting_sHininess;
	public Vector3f lightPositionWorldCoordinates = new Vector3f();
	
	public ExerciseEnum exercise;

	private int imageWidth;
	private int imageHeight;

	private ObjectModel object1;
	
	float zBuffer[][];
	
	private int counter = 0;
	
	ErrorLogger errorLogger;
	
	public WorldModel(int imageWidth, int imageHeight, ErrorLogger errorLogger) {
		this.imageWidth  = imageWidth;
		this.imageHeight = imageHeight;
		this.zBuffer = new float[imageWidth][imageHeight];
		this.errorLogger = errorLogger;
	}


	public boolean load(String fileName) {
		object1 = new ObjectModel(this, imageWidth, imageHeight);
		return object1.load(fileName);
	}
	
	public boolean modelHasTexture() {
		return object1.objectHasTexture();
	}
	

	
	public void render(IntBufferWrapper intBufferWrapper) {
		counter+=1;
		intBufferWrapper.imageClear();
		clearZbuffer();
		object1.initTransfomations();
		
		Random rand = new Random();

		if (exercise.ordinal() == ExerciseEnum.EX_3_1_Object_transformation___translation.ordinal()) {
			Matrix4f mat = new Matrix4f()
					.translate(new Vector3f(rand.nextFloat() * 3, rand.nextFloat() * 3, 0));
			object1.setModelM(mat);
		}
	
		if (exercise.ordinal() == ExerciseEnum.EX_3_2_Object_transformation___scale.ordinal()) {

			float sinWave = 1f + (float)Math.sin((float)counter/6f)*0.1f;
			Matrix4f mat = new Matrix4f()
					.translate(new Vector3f(300f, 300f, 0))
					.scale(sinWave)
					.translate(new Vector3f(-300f, -300f, 0));
			object1.setModelM(mat);


		}

		if (exercise.ordinal() == ExerciseEnum.EX_3_3_Object_transformation___4_objects.ordinal()) {
			Matrix4f mat = new Matrix4f().scale(0.5f);

			object1.setModelM(mat);
			object1.render(intBufferWrapper);
			
			mat.translate(new Vector3f(0, 600, 0));
			object1.render(intBufferWrapper);
			
			mat.translate(new Vector3f(600, 0, 0));
			object1.render(intBufferWrapper);
			
			mat.translate(new Vector3f(0, -600, 0));
		}



			if(projectionType==ProjectionTypeEnum.ORTHOGRAPHIC) {
				object1.setProjectionM(new Matrix4f().ortho(-1.5f, 1.5f, -1.5f, 1.5f, 0, 100));
				object1.setViewportM(YoursUtilities.createViewportMatrix(0, 0, imageWidth, imageHeight));
				object1.setLookatM(new Matrix4f().lookAt(cameraPos, cameraLookAtCenter, cameraUp));
			}


			
			if(projectionType==ProjectionTypeEnum.PERSPECTIVE) {



			}
			

		
		object1.render(intBufferWrapper);
	}
	
	private void clearZbuffer() {
		for(int i=0; i<imageHeight; i++)
			for(int j=0; j<imageWidth; j++)
				zBuffer[i][j] = 1;
	}	
}