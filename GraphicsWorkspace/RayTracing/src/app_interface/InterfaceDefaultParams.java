package app_interface;

import org.joml.Vector3f;

class InterfaceDefaultParams {
	static final String modelFileName = "./Models/ex_02___skybox_with_direction_labels.model";
	static final String modelOpenObjPath = "./Models/";
	static final String saveImagePath = "./";
	static final int depthOfRayTracing = 6;
	static final ExerciseEnum exNum = ExerciseEnum.EX_1_0_Colors_one_color;
	
	//1. Constants
	static final int IMAGE_WIDTH  = 600;
	static final int IMAGE_HEIGHT = 600;
	static final int IMAGE_UPDATE_INTERVAL_IN_MS = 30;
	static final int LABELS_UPDATE_INTERVAL_IN_MS = 250;
	static final int SIZE_OF_UPPER_INTERFACE_ROW_IN_PIXELS = 40;

	//Unsaved parameters - parameters that start with their default value and can change 
    //  during program execution but are not saved between runs.
}
