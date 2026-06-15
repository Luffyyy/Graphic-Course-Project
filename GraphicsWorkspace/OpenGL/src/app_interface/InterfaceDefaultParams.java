package app_interface;

import org.joml.Vector3f;

class InterfaceDefaultParams {
	static final String modelFileName = "./Models/models_with_texture/sheep.obj";
	static final String shadersFolder = "./Shaders/08_LightingAndTexture/";
//	static final boolean displayWireframe = false;	

	static final String modelOpenPath = "./Models/";
//	static final String saveImagePath = "./Models/";
//	static final ProjectionTypeEnum projectionType = ProjectionTypeEnum.ORTHOGRAPHIC;
//	static final DisplayTypeEnum displayType = DisplayTypeEnum.FACE_EDGES;
//	static final boolean displayNormals = false;
//	static final ExerciseEnum exNum = ExerciseEnum.EX_0___Starting_point;
	
	//1. Constants
	static final int IMAGE_WIDTH  = 600;
	static final int IMAGE_HEIGHT = 600;
	static final int INITIAL_CAMERA_DISTANCE_FROM_AXIS_CENTER = 5;
	static final float CAMERA_MAX_VERTICAL_ANGLE = 60;
	static final int IMAGE_UPDATE_INTERVAL_IN_MS = 30;
	static final int LABELS_UPDATE_INTERVAL_IN_MS = 250;

	//Unsaved parameters - parameters that start with their default value and can change 
    //  during program execution but are not saved between runs.
	static final float cameraRadius = INITIAL_CAMERA_DISTANCE_FROM_AXIS_CENTER;
	static final Vector3f cameraPos = new Vector3f(0, 0, cameraRadius);
	static final Vector3f cameraLookAtCenter = new Vector3f(0,0,0);
	static final Vector3f cameraUp = new Vector3f(0,1,0);
	static final float cameraAngleHorizontal = 270;
	static final float cameraAngleVertical = 0;

	static final float horizontalFOV = 45;

	static final float modelScale = 1;

	static final float materialDiffuse = 0.75f;
	static final float materialSpecular = 0.2f;
	static final float materialAmbient = 0.4f;
	static final float materialShininess = 40;
	static final Vector3f lightPosition = new Vector3f(10,10,10);
}
