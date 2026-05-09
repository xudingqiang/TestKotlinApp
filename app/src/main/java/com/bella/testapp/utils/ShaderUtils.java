package com.bella.testapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.io.InputStream;

public class ShaderUtils {
    public static String loadFromAssets(Context context, String path) {
        try {
            InputStream is = context.getAssets().open(path);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new String(buffer);
        } catch (Exception e) {
            return "";
        }
    }

    public static int createProgram(String vertex, String fragment) {
        int v = loadShader(GLES30.GL_VERTEX_SHADER, vertex);
        int f = loadShader(GLES30.GL_FRAGMENT_SHADER, fragment);

        int program = GLES30.glCreateProgram();
        GLES30.glAttachShader(program, v);
        GLES30.glAttachShader(program, f);
        GLES30.glLinkProgram(program);

        return program;
    }

    private static int loadShader(int type, String code) {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, code);
        GLES30.glCompileShader(shader);
        return shader;
    }

    public static int loadTexture(Context context, int resId) {
        int[] texture = new int[1];
        GLES30.glGenTextures(1, texture, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resId);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture[0]);
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);

        bitmap.recycle();
        return texture[0];
    }
}
