package fr.greweb.rnwebglviewshot;

import javax.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.GLUtils;
import android.util.Base64;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import fr.greweb.rnwebgl.RNWebGLTexture;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Snapshot utility class allow to screenshot a view.
 */
public class RNWebGLTextureView extends RNWebGLTexture implements Runnable, ViewTreeObserver.OnDrawListener {

    final View view;
    final ViewTreeObserver viewTreeObserver;
    final boolean yflip;

    public RNWebGLTextureView(ReadableMap config, View view) {
        super(config, view.getWidth(), view.getHeight());
        boolean continuous = config.hasKey("continuous") && config.getBoolean("continuous");
        yflip = config.hasKey("yflip") && config.getBoolean("yflip");
        this.view = view;
        if (continuous) {
            viewTreeObserver = view.getViewTreeObserver();
            viewTreeObserver.addOnDrawListener(this);
        }
        else {
            viewTreeObserver = null;
        }
        this.runOnGLThread(this);
    }

    @Override
    public void onDraw() {
        this.runOnGLThread(this);
    }

    @Override
    public void destroy() {
        if (viewTreeObserver != null) {
            viewTreeObserver.removeOnDrawListener(this);
        }
        super.destroy();
    }

    public void run() {
        Bitmap bitmap;
        try {
            bitmap = captureView(view);
        }
        catch (Exception e) {
            return;
        }
        if (yflip) {
            Matrix matrix = new Matrix();
            matrix.postScale(1, -1);
            boolean hasAlpha = bitmap.hasAlpha();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.setHasAlpha(hasAlpha);
        }
        int[] textures = new int[1];
        glGenTextures(1, textures, 0);
        glBindTexture(GL_TEXTURE_2D, textures[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        this.attachTexture(textures[0]);
    }

    // Code from react-native-view-shot

    private List<View> getAllChildren(View v) {
        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<View>();
            viewArrayList.add(v);
            return viewArrayList;
        }
        ArrayList<View> result = new ArrayList<View>();
        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            result.addAll(getAllChildren(child));
        }
        return result;
    }

    private Bitmap captureView (View view) {
        int w = view.getWidth();
        int h = view.getHeight();
        if (w <= 0 || h <= 0) {
            throw new RuntimeException("Impossible to snapshot the view: view is invalid");
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Bitmap childBitmapBuffer;
        Canvas c = new Canvas(bitmap);
        view.draw(c);
        List<View> childrenList = getAllChildren(view);
        for (View child : childrenList) {
            if(child instanceof TextureView) {
                ((TextureView) child).setOpaque(false);
                childBitmapBuffer = ((TextureView) child).getBitmap(child.getWidth(), child.getHeight());
                c.drawBitmap(childBitmapBuffer, child.getLeft() + ((ViewGroup)child.getParent()).getLeft() +  child.getPaddingLeft(), child.getTop() + ((ViewGroup)child.getParent()).getTop() + child.getPaddingTop(), null);
            }
        }
        if (bitmap == null) {
            throw new RuntimeException("Impossible to snapshot the view");
        }
        return bitmap;
    }
}
