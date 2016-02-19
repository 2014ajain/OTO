package com.fenchtose.contactsdemo.utils;

import android.content.Context;
import android.graphics.Typeface;

public class Fonts {

    public static final int FONT_ROBOTO_THIN = 0;
    public static final int FONT_ROBOTO_LIGHT = 1;
    private static String[] fontPaths = {"fonts/Roboto-Thin.ttf", "fonts/Roboto-Light.ttf"}; // Array for holding strings of locations of fonts
    private static Typeface typefaces[];                                                     // Array for holding typefaces
    private static boolean fontsLoaded = false;                                              // Boolean to check if fonts are loaded

    public static Typeface getTypeface(int font) {
        if (!fontsLoaded) {
            return Typeface.DEFAULT;
        }
        return typefaces[font];
    }//end getTypeface

    public static void loadFonts(Context context) {
        typefaces = new Typeface[fontPaths.length];
        for (int i = 0; i < fontPaths.length; i++)
            typefaces[i] = Typeface.createFromAsset(context.getAssets(), fontPaths[i]);      // Gets assets
        fontsLoaded = true;
    }//end loadFonts

}
