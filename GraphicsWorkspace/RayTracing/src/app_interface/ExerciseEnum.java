package app_interface;

/**
 * Enum representing the exercises available in the user interface.
 * Each enum constant corresponds to a specific exercise, with a 
 * description detailing its topic.
 */
public enum ExerciseEnum  {
    EX_0___Starting_point                          ("Exercise 0   - Starting point"),
    EX_1_0_Colors_one_color                        ("Exercise 1.0 - Colors - One color"),
    EX_1_1_Colors_Random_color                     ("Exercise 1.1 - Colors - Random color"),
    EX_1_2_Colors_Color_space                      ("Exercise 1.2 - Colors - Color space"),
    EX_1_3_Colors_linear                           ("Exercise 1.3 - Colors - Linear colors"),
    EX_2___Rays_calculation                        ("Exercise 2   - Rays calculation"),
    EX_3_1_Intersection_One_sphere                 ("Exercise 3.1 - Intersection - One sphere"),
    EX_3_2_Intersection_One_sphere_with_color      ("Exercise 3.2 - Intersection - One sphere with color"),
    EX_3_3_Intersection_List_of_spheres            ("Exercise 3.3 - Intersection - List of spheres"),
    EX_3_4_Intersection_Finding_the_nearest_sphere ("Exercise 3.4 - Intersection - Finding the nearest sphere"),
    EX_4_1_Lighting_Diffusive                      ("Exercise 4.1 - Lighting - Diffusive"),
    EX_4_2_Lighting_Ambient                        ("Exercise 4.2 - Lighting - Ambient"),
    EX_4_3_Lighting_Specular                       ("Exercise 4.3 - Lighting - Specular"),
//    EX_4_4_Lighting_few_light_source               ("Exercise 4.4 - Lighting - few light source"),
    EX_5___Texture                                 ("Exercise 5   - Texture"),
    EX_6___Shadow                                  ("Exercise 6   - Shadow"),
    EX_7___Reflection                              ("Exercise 7   - Reflection"),
    EX_8___Transparency                            ("Exercise 8   - Transparency");

    private final String description;

    private ExerciseEnum(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }
}
