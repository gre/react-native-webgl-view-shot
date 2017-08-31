
package fr.greweb.rnwebglviewshot;

import fr.greweb.rnwebgl.RNWebGLTextureCompletionBlock;
import fr.greweb.rnwebgl.RNWebGLTextureConfigLoader;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.UIManagerModule;

public class RNWebGLTextureViewLoader extends ReactContextBaseJavaModule  implements RNWebGLTextureConfigLoader {

    private final ReactApplicationContext reactContext;

    public RNWebGLTextureViewLoader(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public boolean canLoadConfig(ReadableMap config) {
        return config.hasKey("view");
    }

    @Override
    public void loadWithConfig(ReadableMap config, RNWebGLTextureCompletionBlock callback) {
        int tag = config.getInt("view");
        try {
            UIManagerModule uiManager = this.reactContext.getNativeModule(UIManagerModule.class);
            uiManager.addUIBlock(new RNWebGLTextureViewUIBlock(config, tag, callback));
        }
        catch (Exception e) {
            callback.call(e, null);
        }
    }

    @Override
    public String getName() {
        return "RNWebGLTextureViewLoader";
    }
}
