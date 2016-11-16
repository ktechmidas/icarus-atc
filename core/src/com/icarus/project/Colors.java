package com.icarus.project;
import com.badlogic.gdx.graphics.Color;

class Colors {

    private static Color parse(String hex) {
        String s1 = hex.substring(0, 2);  
        int v1 = Integer.parseInt(s1, 16);  
        float f1 = (float) v1 / 255f;  
        String s2 = hex.substring(2, 4);  
        int v2 = Integer.parseInt(s2, 16);  
        float f2 = (float) v2 / 255f;  
        String s3 = hex.substring(4, 6);  
        int v3 = Integer.parseInt(s3, 16);  
        float f3 = (float) v3 / 255f;  
        return new Color(f1, f2, f3, 1);  
    }

    public static Color colors[] = new Color[]{
        parse("1f3319"),
        parse("2a4522"),
        parse("325228"),
        parse("599248"),
        parse("80d267")
    };
}
