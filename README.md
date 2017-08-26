
# react-native-webgl-view-shot ![](https://img.shields.io/npm/v/react-native-webgl-view-shot.svg)

React Native WebGL extension to rasterize a view as a GL Texture. The library extends the [Texture Config Formats of `react-native-webgl`](https://github.com/react-community/react-native-webgl#texture-config-formats) to add `{ view }` config.

**[Example](example/App.js)**

![u](https://user-images.githubusercontent.com/211411/29744347-9d39247a-8aa3-11e7-8f2a-040979a55d9f.gif)


## Install

```bash
yarn add react-native-webgl-view-shot
react-native link react-native-webgl-view-shot
```

## Usage

```js
import WebGLViewShot from "react-native-webgl-view-shot";

// render this somewhere...

<WebGLViewShot ref="shotRef">
   ...something to rasterize
</WebGLViewShot>

// then you can give the ref to react-native-webgl's loadConfig:

gl.getExtension("RN").loadConfig({
  view: this.refs.shotRef
}).then(({ texture }) => {
  // texture hold the rasterize image of the view, shoot at the time you called loadConfig
});

// But you can also enable continuous rasterization:

gl.getExtension("RN").loadConfig({
  view: this.refs.shotRef,
  continuous: true
}).then(({ texture }) => {
  // the texture will continuously be in sync with the View content (NB beware of some delay)
  // ... use texture like a normal WebGLTexture
});
```

There are 3 cases the view continuous rasterization should stop:

- the view was unmounted.
- `unloadConfig(texture)` was called.
- WebGLView was unmounted.

### Supported views

The list of supported / rasterizable content is the same as listed in the library [react-native-view-shot](https://github.com/gre/react-native-view-shot#interoperability-table) (even though that library is not directly used at the moment, some native code was taken from it).

### Advanced notes

It is technically possible to just pass-in a View ref without using the `WebGLViewShot` component. However be aware of two things: (1) you still need to `import "react-native-webgl-view-shot"` so the format is extended, (2) you might need to use a wrapping `<View collapsable={false}>` for the capture to work out.
