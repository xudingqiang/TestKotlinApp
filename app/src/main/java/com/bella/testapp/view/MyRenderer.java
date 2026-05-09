package com.bella.testapp.view;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;

import com.bella.testapp.R;
import com.bella.testapp.utils.ShaderUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {

    private Context context;
    private int program;
    private int textureId;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texBuffer;

    private final float[] vertices = {
            -1f,  1f,
            -1f, -1f,
            1f,  1f,
            1f, -1f
    };

    private final float[] texCoords = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    public MyRenderer(Context context) {
        this.context = context;

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        texBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texBuffer.put(texCoords).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        String vertex = ShaderUtils.loadFromAssets(context, "shader/vertex.glsl");
        String fragment = ShaderUtils.loadFromAssets(context, "shader/fragment.glsl");

        program = ShaderUtils.createProgram(vertex, fragment);

        textureId = ShaderUtils.loadTexture(context, R.drawable.cc); // 放一张图片
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);

        GLES30.glUseProgram(program);

        int pos = 0;
        int tex = 1;

        GLES30.glEnableVertexAttribArray(pos);
        GLES30.glVertexAttribPointer(pos, 2, GLES30.GL_FLOAT, false, 0, vertexBuffer);

        GLES30.glEnableVertexAttribArray(tex);
        GLES30.glVertexAttribPointer(tex, 2, GLES30.GL_FLOAT, false, 0, texBuffer);

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }
}