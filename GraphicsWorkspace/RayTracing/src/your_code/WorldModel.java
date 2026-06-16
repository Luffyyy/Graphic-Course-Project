/**
 * Lab 4
 * Handed by:
 * 
 * Daniel Rozentsvaig
 * Tomer Roll
 */
package your_code;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import app_interface.ExerciseEnum;
import app_interface.Model;
import app_interface.ModelLight;
import app_interface.ModelMaterial;
import app_interface.ModelSphere;
import app_interface.SphereTexture;

//class holding the world model and render it
public class WorldModel {

	/** The Model object with all the details of the world model that will be rendered */
	Model model;

	/** Your selection enum type (specific to your implementation) */	
	private YourSelectionEnum yourSelection;

	/** The current exercise being executed (from ExerciseEnum) */	
	private static ExerciseEnum exercise = ExerciseEnum.EX_8___Transparency;

	/** image width and height in pixles*/
	private int imageWidth; 
	private int imageHeight;

	/** SphereTexture object of the skybox of the world model */
	SphereTexture skyBoxImageSphereTexture;

	/** The depth of ray tracing used during rendering */
	private static int depthOfRayTracing;
	
	private ErrorLogger errorLogger;
	//to log error you need to add the error type to the enum in the ErrorLogger class
	//and call the method report like this: errorLogger.report(ErrorLogger.ErrorType.EXAMPLE_ERROR_1);

	public WorldModel(int imageWidth, int imageHeight, ErrorLogger errorLogger) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.errorLogger = errorLogger;
	}

	public void setRenderingParams(int depthOfRayTracing) {
		WorldModel.depthOfRayTracing = depthOfRayTracing;
	}

	public void setExercise(ExerciseEnum exercise) {
		WorldModel.exercise = exercise;
	}

	public void setYourSelection(YourSelectionEnum sel) {
		this.yourSelection = sel;
	}

	/** Loads a model from a specified file 
	* @param fileName the path to the model file
	* @return true if the model was loaded successfully, false otherwise
	* @throws Exception if there is an error loading the model file	*/	
	public boolean load(String fileName) {
		try {
			model = new Model(fileName);
			skyBoxImageSphereTexture = new SphereTexture(model.skyBoxImageFileName);
			return true;
		} catch (Exception e) {
			//System.err.println("Failed to load the model file: " + fileName + ".\nDescription: " + e.getMessage());
			return false;
		}
	}

	/** Renders the color of a specific pixel in the image
	* @param x the x coordinate of the pixel
	* @param y the y coordinate of the pixel
	* @return the Vector3f representing the color of the pixel */	
	public Vector3f renderPixel(int x, int y) {
		if (exercise == ExerciseEnum.EX_0___Starting_point)
			return new Vector3f(0);
		else if (exercise == ExerciseEnum.EX_1_0_Colors_one_color) {
			return new Vector3f(1, 1, 0);
		} else if (exercise == ExerciseEnum.EX_1_1_Colors_Random_color) {
			return new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random());
		} else if (exercise == ExerciseEnum.EX_1_2_Colors_Color_space) {
			float w_mat = 1f / (imageWidth - 1);
			float h_mat = 1f / (imageHeight - 1);
			Matrix3f m = new Matrix3f(-w_mat, -h_mat, 1, w_mat, 0, 0, 0, h_mat, 0).transpose();

			return m.transform(new Vector3f(x, y, 1));
		} else if (exercise == ExerciseEnum.EX_1_3_Colors_linear) {
			var c1 = new Vector3f(1, 1, 0); // Yellow
			var c2 = new Vector3f(0, 1, 1); // Cyan

			return c1.mul((float) (imageWidth - 1 - x) / (imageWidth - 1)).add(c2.mul((float) x / (imageWidth - 1)));
		} else {
			var dir = calcPixelDirection(x, y, imageWidth, imageHeight, model.fovXdegree);
			return rayTracing(new Vector3f(), dir, model, skyBoxImageSphereTexture, 0);
		}
	}

	/** Performs ray tracing for a given ray.
	 * @param incidentRayOrigin The origin of the incident ray.
	 * @param incidentRayDirection The direction of the incident ray.
	 * @param model The model containing spheres and materials.
	 * @param skyBoxImageSphereTexture The texture for the skybox.
	 * @param depthLevel The current depth level of the recursion (for limiting recursion).
	 * @return The calculated color for the pixel based on ray tracing and lighting effects. */	
	private static Vector3f rayTracing(Vector3f incidentRayOrigin, Vector3f incidentRayDirection, Model model,
		SphereTexture skyBoxImageSphereTexture, int depthLevel) {
	private static Vector3f rayTracing(
	        Vector3f incidentRayOrigin,
	        Vector3f incidentRayDirection,
	        Model model,
	        SphereTexture skyBoxImageSphereTexture,
	        int depthLevel) {
		
		if (depthLevel == depthOfRayTracing) {
			return new Vector3f();
		}

	    var intersectionResults = rayIntersection(
	            incidentRayOrigin,
	            incidentRayDirection,
	            model.spheres
	    );

	    if (intersectionResults != null) {
	        ModelSphere intersectedSphere = intersectionResults.intersectedSphere;
	        ModelMaterial intersectedSphereMaterial =
	                model.materials.get(intersectedSphere.materialIndex);

	        Vector3f intersectionPoint = intersectionResults.intersectionPoint;
	        Vector3f intersectionNormal = intersectionResults.normal;
	        boolean intersectionFromOutsideOfSphere =
	                intersectionResults.rayFromOutsideOfSphere;

	        SphereTexture intersectedSphereTexture =
	                model.skyBoxImageSphereTextures.get(intersectedSphere.textureIndex);

	        var light = model.lights.get(0);

	        Vector3f color = intersectedSphereMaterial.color;
	        float kColor = intersectedSphereMaterial.kColor;

	        Vector3f returnedColor = new Vector3f(color).mul(kColor);

	        var isShadow = isPointInShadow(
                light.location,
                intersectionPoint,
                intersectionNormal,
                model
	        );

	        // Calculate lighting
	        Vector3f lighting;
	        if (isShadow) {
	            lighting = new Vector3f(intersectedSphereMaterial.ka);
	        } else {
	            Vector3f kd = calcKdCombinedWithTexture(
                    intersectionPoint,
                    intersectedSphere.center,
                    intersectedSphereTexture,
                    intersectedSphereMaterial.kd,
                    intersectedSphereMaterial.kTexture
	            );

	            lighting = lightingEquation(
                    intersectionPoint,
                    intersectionNormal,
                    light.location,
                    kd,
                    intersectedSphereMaterial.ks,
                    intersectedSphereMaterial.ka,
                    intersectedSphereMaterial.shininess
	            );
	        }
	        returnedColor.add(new Vector3f(lighting).mul(intersectedSphereMaterial.kDirect));
	        
	        // Calculate reflected light
	        var refLight = calcReflectedLight(
        		incidentRayDirection,
        		intersectionPoint,
        		intersectionNormal,
        		model,
        		skyBoxImageSphereTexture,
        		depthLevel
	        );
	        returnedColor.add(new Vector3f(refLight).mul(intersectedSphereMaterial.kReflection));

	        // Calculate transmission light
	        var transmissionLight = calcTransmissionLight(
        		incidentRayDirection,
        		intersectionPoint,
        		intersectionNormal,
        		intersectionFromOutsideOfSphere,
        		intersectedSphereMaterial.refractiveIndex,
        		model,
        		skyBoxImageSphereTexture,
        		depthLevel
	        );
	        returnedColor.add(new Vector3f(transmissionLight).mul(intersectedSphereMaterial.kTransmission));

	        return returnedColor;
	    }

	    return skyBoxImageSphereTexture.sampleDirectionFromMiddle(incidentRayDirection);
	}

	
	/** Calculates the direction of a ray for a given pixel in the image.
	 * @param x The x-coordinate of the pixel.
	 * @param y The y-coordinate of the pixel.
	 * @param imageWidth The width of the image.
	 * @param imageHeight The height of the image.
	 * @param fovXdegree The horizontal field of view in degrees.
	 * @return The normalized direction vector of the ray for the given pixel. */	
	static Vector3f calcPixelDirection(int x, int y, int imageWidth, int imageHeight, float fovXdegree) {
		double radFovX = Math.toRadians(fovXdegree); // Convert to radians
		double radFovY = radFovX / imageWidth * imageHeight; // Compute FOV for Y

		float xLeft = -(float) Math.tan(radFovX / 2);
		float yBottom = -(float) Math.tan(radFovY / 2);
		float xDelta = -2 * xLeft / (imageWidth - 1);
		float yDelta = -2 * yBottom / (imageHeight - 1);

		return new Vector3f(xLeft + xDelta * x, yBottom + yDelta * y, -1).normalize();
	}

	/** Calculates the intersection(s) between a ray and a sphere.
	 * @param rayStart The starting point of the ray in world space.
	 * @param rayDirection The normalized direction vector of the ray.
	 * @param sphere The sphere to check for intersection.
	 * @return An IntersectionResults object containing information about the intersection,
	 *         or an empty IntersectionResults object if no intersection occurs. */
	static IntersectionResults rayIntersection(Vector3f rayStart, Vector3f rayDirection, ModelSphere sphere) {
		Vector3f sphereCenter = sphere.center;
		float sphereRadius = sphere.radius;

		// Project the lineToSphere vector onto the line direction to find the distance
		// from
		// the line point to the closest point on the line to the sphere center
		float tm = new Vector3f(sphereCenter).sub(rayStart).dot(rayDirection);

		// Calculate the distance from the closest point on the line to the sphere
		// center
		Vector3f pm = new Vector3f(rayStart).add(new Vector3f(rayDirection).mul(tm));
		float pmDistance = pm.distance(sphereCenter);

		// If the distance from the closest point to the sphere center is greater than
		// the sphere radius,
		// then there are no intersection points
		if (pmDistance > sphereRadius)
			return null;

		// Calculate the distance along the line from the closest point to the
		// intersection points
		float dt = (float) Math.sqrt(sphereRadius * sphereRadius - pmDistance * pmDistance);

		// If the closest point on the line is behind the line point, then there are no
		// intersection points
		if (tm < -dt)
			return null;
		
		// Calculate the intersection points
		if (tm < dt) {
			Vector3f firstIntersectionPoint = pm.add(new Vector3f(rayDirection).mul(dt));
			return new IntersectionResults(true, // intersected
					firstIntersectionPoint, // firstIntersectionPoint
					new Vector3f(sphereCenter).sub(firstIntersectionPoint).normalize(), // normal
					false, // rayFromOutsideOfSphere
					sphere); // intersectedSphere
		} else {
			Vector3f firstIntersectionPoint = pm.sub(new Vector3f(rayDirection).mul(dt));
			return new IntersectionResults(true, // intersected
					firstIntersectionPoint, // firstIntersectionPoint
					new Vector3f(firstIntersectionPoint).sub(sphereCenter).normalize(), // normal
					true, // rayFromOutsideOfSphere
					sphere); // intersectedSphere
		}
	}

	/** Finds the nearest intersection between a ray and a list of spheres.
	 * @param rayStart The starting point of the ray.
	 * @param rayDirection The normalized direction of the ray.
	 * @param spheres The list of spheres to check for intersections.
	 * @return An IntersectionResults object containing information about the nearest intersection,
	 *         or an empty IntersectionResults object if no intersection occurs. */	
	private static IntersectionResults rayIntersection(Vector3f rayStart, Vector3f rayDirection,
			List<ModelSphere> spheres) {
		
		IntersectionResults closest = null;
		float closestDist = 0;

		for (var sphere : spheres) {
			var intersectionResults = rayIntersection(rayStart, rayDirection, sphere);
			if (intersectionResults != null) {
				Vector3f intersectionPoint = intersectionResults.intersectionPoint;
				float currDist = intersectionPoint.distance(rayStart);
				// Check if the current is closer to rayStart, then update the closest
				if (closest == null || currDist < closestDist) {
					closest = intersectionResults;
					closestDist = currDist;
				}
			}
		}
		return closest;
	}

	
	/** Calculates the lighting at a specific point.
	 * @param point The position of the point.
	 * @param pointNormal The normal vector at the point.
	 * @param lightPos The Eye space position of the light source.
	 * @param Kd The diffuse color coefficient of the material.
	 * @param Ks The specular color coefficient of the material.
	 * @param Ka The ambient color coefficient of the material.
	 * @param shininess The shininess parameter for specular highlights.
	 * @return The calculated lighting as a Vector3f representing color. */	
	static Vector3f lightingEquation(Vector3f point, Vector3f PointNormal, Vector3f LightPos, Vector3f Kd,
			Vector3f Ks, Vector3f Ka, float shininess) {
	static Vector3f lightingEquation(Vector3f point, Vector3f PointNormal, Vector3f LightPos, Vector3f Kd, Vector3f Ks,
			Vector3f Ka, float shininess) {

		// Direction vector from point to light source
		Vector3f lightDir = new Vector3f(LightPos).sub(point).normalize();
		
		// Diffuse
		var dotNL = Math.max(PointNormal.dot(lightDir), 0);
		var diffuse = new Vector3f(Kd).mul(dotNL);

		// Specular
		// V is point toward camera
		var V = new Vector3f(point).negate().normalize();
		var R = new Vector3f(PointNormal).mul(2 * new Vector3f(lightDir).dot(PointNormal)).sub(lightDir).normalize();
		var dotVR = (float) Math.pow(Math.max(V.dot(R), 0), shininess); // No specular if light doesn't hit the front
		var specular = new Vector3f(Ks).mul(dotVR);

		// Ambient
		var ambient = new Vector3f(Ka);

		return diffuse.add(specular).add(ambient);
	}


	/**
	 * Calculates the combined diffuse reflection coefficient (Kd) for a point on the surface of a sphere,
	 * taking into account both the base material's Kd and the texture applied to the sphere.
	 * 
	 * @param intersectionPoint the point of intersection on the sphere's surface in 3D space.
	 * @param intersectedSphereCenter the center of the intersected sphere.
	 * @param intersectedSphereTexture the texture applied to the sphere, used to sample color based on direction.
	 * @param intersectedSphereKd the base diffuse reflection coefficient of the sphere's material.
	 * @param kTexture the blending factor between the base Kd and the texture color 
	 *                 (0 for only base Kd, 1 for only texture, values in between for blending).
	 * @return a {@link Vector3f} representing the combined Kd, computed as a weighted blend of the base Kd and the texture color.
	 */
	static Vector3f calcKdCombinedWithTexture(Vector3f intersectionPoint, Vector3f intersectedSphereCenter,
			SphereTexture intersectedSphereTexture, Vector3f intersectedSphereKd, float kTexture) {

		// Calculate dir vector from middle point of sphere to intersection point
		var mid = new Vector3f(intersectionPoint).sub(intersectedSphereCenter);

		// Get texture color in that point in sphere
		var textureColor = intersectedSphereTexture.sampleDirectionFromMiddle(mid);

		// Calculate new diffuse (kd)
		return new Vector3f(intersectedSphereKd).mul(1 - kTexture).add(textureColor.mul(kTexture));
	}

	/**
	 * Determines whether a given point is in shadow with respect to a specified light source.
	 * 
	 * <p>The method calculates a shadow ray originating just above the surface of the given point
	 * (offset slightly along the surface normal to prevent self-intersection) and checks whether
	 * this ray intersects with any objects in the scene model.</p>
	 * 
	 * @param lightLocation the position of the light source in 3D space.
	 * @param point the position of the point being tested for shadow in 3D space.
	 * @param pointNormal the surface normal vector at the point, used to offset the shadow ray origin.
	 * @param model the {@link Model} representing the scene, containing the objects (e.g., spheres) to check for intersection.
	 * @return {@code true} if the point is in shadow (i.e., the shadow ray intersects with any objects), {@code false} otherwise.
	 */
	static boolean isPointInShadow(
	        Vector3f lightLocation,
	        Vector3f point,
	        Vector3f pointNormal,
	        Model model) {

	    Vector3f p = new Vector3f(point).add(new Vector3f(pointNormal).mul(0.05f));
	    Vector3f lightDir = new Vector3f(lightLocation).sub(p).normalize();

	    return rayIntersection(p, lightDir, model.spheres) != null;
	}

	
	/**
	 * Calculates the reflected light at an intersection point, accounting for reflections
	 * within the scene and the skybox texture.
	 * 
	 * <p>The method computes the direction of the reflected ray based on the incident ray 
	 * and the surface normal at the intersection point. It then recursively traces the 
	 * reflected ray through the scene to compute the reflected light contribution.</p>
	 * 
	 * @param incidentRayDirection the direction of the incoming ray hitting the surface.
	 * @param intersectionPoint the point on the surface where the reflection occurs.
	 * @param intersectionNormal       the normal vector at the intersection point.
	 * @param kReflection the reflection coefficient, controlling the intensity of the reflected light.
	 * @param model the {@link Model} representing the scene, containing objects for ray tracing.
	 * @param skyBoxImageSphereTexture the texture of the skybox used to simulate distant reflections.
	 * @param depthLevel the current recursion depth, used to limit the number of reflection bounces.
	 * @return a {@link Vector3f} representing the color/intensity of the reflected light at the intersection point.
	 */
	static Vector3f calcReflectedLight(Vector3f incidentRayDirection, Vector3f intersectionPoint,
			Vector3f intersectionNormal, Model model, SphereTexture skyBoxImageSphereTexture, int depthLevel) {

	    Vector3f rayDirNorm = new Vector3f(incidentRayDirection).normalize();
	    Vector3f refRay = new Vector3f(rayDirNorm)
	            .sub(new Vector3f(intersectionNormal).mul(2f * rayDirNorm.dot(intersectionNormal)))
	            .normalize();

	    Vector3f p = new Vector3f(intersectionPoint).add(new Vector3f(intersectionNormal).mul(0.05f));

		
		return rayTracing(p, refRay, model, skyBoxImageSphereTexture, depthLevel+1);
	}

	
	
	
	/**
	 * Calculates the transmitted (refracted) light at an intersection point, considering the refraction 
	 * properties of the intersected object and the scene environment.
	 * 
	 * <p>The method computes the direction of the transmitted ray based on the incident ray, 
	 * the surface normal, and the refractive index of the intersected sphere. It offsets the ray 
	 * origin slightly to prevent self-intersection and recursively traces the transmitted ray 
	 * through the scene to determine the contribution of transmitted light.</p>
	 * 
	 * @param incidentRayDirection the direction of the incoming ray hitting the surface.
	 * @param intersectionPoint the point on the surface where the refraction occurs.
	 * @param intersectionNormal the normal vector at the intersection point.
	 * @param intersectionFromOutsideOfSphere {@code true} if the intersection occurs when the ray 
	 *                                         enters the sphere from outside, {@code false} if exiting.
	 * @param refractiveIndexIntersectedSphere the refractive index of the intersected sphere material.
	 * @param kTransmission the transmission coefficient, controlling the intensity of the transmitted light.
	 * @param model the {@link Model} representing the scene, containing objects for ray tracing.
	 * @param skyBoxImageSphereTexture the texture of the skybox used to simulate distant light transmission.
	 * @param depthLevel the current recursion depth, used to limit the number of refraction bounces.
	 * @return a {@link Vector3f} representing the color/intensity of the transmitted light at the intersection point.
	 */
	static Vector3f calcTransmissionLight(Vector3f incidentRayDirection, Vector3f intersectionPoint,
			Vector3f intersectionNormal, boolean intersectionFromOutsideOfSphere,
			float refractiveIndexIntersectedSphere, Model model, SphereTexture skyBoxImageSphereTexture,
			int depthLevel) {
		
		var transmitted = YoursUtilities.calcTransmissionRay(
			incidentRayDirection,
			intersectionNormal,
			refractiveIndexIntersectedSphere,
			intersectionFromOutsideOfSphere);
		
		var angle = Math.acos(transmitted.dot(incidentRayDirection.normalize()));
		
		// If the angle is small enough, reflect instead
		if (angle < 0.001) {
			return calcReflectedLight(incidentRayDirection, 
				intersectionPoint,
				intersectionNormal,
				model,
				skyBoxImageSphereTexture,
				depthLevel
			);
		}
		
		// Move point a bit inside the sphere
		var p = new Vector3f(intersectionPoint).sub(intersectionNormal);

		return rayTracing(
			p,
			transmitted,
			model,
			skyBoxImageSphereTexture,
			depthLevel + 1
		);
	}
}


