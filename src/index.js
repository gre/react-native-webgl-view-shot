//@flow
import React, { Component } from "react";
import { View, findNodeHandle } from "react-native";
import { RNExtension } from "react-native-webgl";

RNExtension.addMiddleware(ext => ({
  ...ext,
  loadTexture: arg => {
    if ("view" in arg && typeof arg.view !== "number") {
      let refPromise; // it's a promise because we likely need to wait onLayout is ready
      if (arg.view instanceof WebGLViewShot) {
        refPromise = arg.view.readyRefPromise;
      } else {
        // assuming another ref was passed, we'll attempt to resolve it
        refPromise = Promise.resolve(arg.view);
      }
      return refPromise.then(ref => {
        const view = findNodeHandle(ref);
        if (!view) {
          throw new Error(
            "findNodeHandle failed to resolve view=" + String(ref)
          );
        }
        return ext.loadTexture({ ...arg, view });
      });
    } else {
      // Pass-in the rest
      return ext.loadTexture(arg);
    }
  }
}));

export default class WebGLViewShot extends Component {
  _resolveRef: (ref: *) => void;
  readyRefPromise: Promise<*> = new Promise(resolve => {
    this._resolveRef = resolve;
  });
  onLayout = (e: *) => {
    const { onLayout } = this.props;
    if (onLayout) onLayout(e);
    this._resolveRef(this.refs._root);
  };
  render() {
    return (
      <View
        {...this.props}
        ref="_root"
        onLayout={this.onLayout}
        collapsable={false}
      />
    );
  }
}
