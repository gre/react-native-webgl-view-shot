package fr.greweb.rnwebglviewshot;

import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import fr.greweb.rnwebgl.RNWebGLTextureCompletionBlock;

public class RNWebGLTextureViewUIBlock implements UIBlock {
    private int tag;
    private ReadableMap config;
    private RNWebGLTextureCompletionBlock callback;

    public RNWebGLTextureViewUIBlock(ReadableMap config, int tag, RNWebGLTextureCompletionBlock callback) {
        this.config = config;
        this.tag = tag;
        this.callback = callback;
    }

    @Override
    public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
        View view = nativeViewHierarchyManager.resolveView(tag);
        if (view == null) {
            callback.call(new Exception("No view found with reactTag: " + tag), null);
            return;
        }
        callback.call(null, new RNWebGLTextureView(config, view));
    }
}
