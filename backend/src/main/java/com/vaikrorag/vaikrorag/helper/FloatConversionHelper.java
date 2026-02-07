package com.vaikrorag.vaikrorag.helper;

import java.util.ArrayList;
import java.util.List;

public class FloatConversionHelper {
    
    public static List<Float> toFloatList(double[] v) {
        List<Float> out = new ArrayList<>(v.length);
        for (double x : v) out.add((float)x);
        return out;
    }
}
