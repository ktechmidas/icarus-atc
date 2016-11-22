package com.icarus.project;

class Utils {

    public float absMin(float a, float b){ //Returns the value closest to 0, independent of sign.
        if (Math.abs(a) - Math.abs(b) >= 0){
            return b;
        }
        else {
            return a;
        }
    }
}
