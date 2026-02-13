package neon.tll.data;

import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.Color;

public class tll_reference_sheet {

    // Static initialization
    public static final WeightedRandomPicker<String> meatsoundlist = new WeightedRandomPicker<>();

    // commodity/special items


    public static final String tll_AISWITCHAUTOMATED = "tll_aiswitch_auto";
    public static final String tll_AISWITCHMANUAL = "tll_aiswitch_manual";


    // Constructor (init block from Kotlin)
    public tll_reference_sheet() {
        // Empty constructor as the init block only contained static initialization
    }
}