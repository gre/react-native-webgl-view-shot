import React, { Component } from "react";
import { StyleSheet, Text, ScrollView, TextInput } from "react-native";
import { WebGLView } from "react-native-webgl";
import WebGLViewShot from "react-native-webgl-view-shot";

export default class App extends Component {
  state = {
    text: "Hello World"
  };
  _raf: *;

  onChangeText = text => this.setState({ text });

  onContextCreate = (gl: WebGLRenderingContext) => {
    const rngl = gl.getExtension("RN");

    gl.viewport(0, 0, gl.drawingBufferWidth, gl.drawingBufferHeight);
    const buffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buffer);
    gl.bufferData(
      gl.ARRAY_BUFFER,
      new Float32Array([-1, -1, -1, 4, 4, -1]),
      gl.STATIC_DRAW
    );
    const vertexShader = gl.createShader(gl.VERTEX_SHADER);
    gl.shaderSource(
      vertexShader,
      `\
attribute vec2 p;
varying vec2 uv;
void main() {
  gl_Position = vec4(p,0.0,1.0);
  uv = 0.5 * (p+1.0);
}`
    );
    gl.compileShader(vertexShader);
    const fragmentShader = gl.createShader(gl.FRAGMENT_SHADER);
    gl.shaderSource(
      fragmentShader,
      `\
precision highp float;
varying vec2 uv;
uniform sampler2D t;
uniform float time;
void main() {
  gl_FragColor = texture2D(t, uv + vec2(
    0.03 * cos(0.5 * time + 20.0 * uv.x),
    0.03 * sin(0.5 * time + 20.0 * uv.y)
  )) + vec4(
    sin(3.0 * time + uv.x),
    cos(time + 0.5 * uv.y),
    0.2 * cos(2.0 * time - uv.x * uv.y),
    0.0
  );
}`
    );

    gl.compileShader(fragmentShader);
    var program = gl.createProgram();
    gl.attachShader(program, vertexShader);
    gl.attachShader(program, fragmentShader);
    gl.linkProgram(program);
    gl.useProgram(program);
    var p = gl.getAttribLocation(program, "p");
    gl.enableVertexAttribArray(p);
    gl.vertexAttribPointer(p, 2, gl.FLOAT, false, 0, 0);
    const tLocation = gl.getUniformLocation(program, "t");
    const timeLocation = gl.getUniformLocation(program, "time");
    rngl
      .loadTexture({
        view: this.refs.viewShot,
        continuous: true,
        yflip: true
      })
      .then(({ texture }) => {
        gl.activeTexture(gl.TEXTURE0);
        gl.bindTexture(gl.TEXTURE_2D, texture);
        gl.uniform1i(tLocation, 0);

        let startTime;
        const loop = (time: number) => {
          if (!startTime) startTime = time;
          this._raf = requestAnimationFrame(loop);
          gl.uniform1f(timeLocation, (time - startTime) / 1000);
          gl.drawArrays(gl.TRIANGLES, 0, 3);
          gl.flush();
          rngl.endFrame();
        };
        this._raf = requestAnimationFrame(loop);
      });
  };

  componentWillUnmount() {
    cancelAnimationFrame(this._raf);
  }

  render() {
    const { text } = this.state;
    return (
      <ScrollView style={styles.root} contentContainerStyle={styles.container}>
        <TextInput
          style={styles.input}
          value={text}
          onChangeText={this.onChangeText}
        />
        <WebGLView
          style={styles.glView}
          onContextCreate={this.onContextCreate}
        />
        <WebGLViewShot ref="viewShot" style={styles.shot}>
          <Text style={styles.text}>
            {text}
          </Text>
        </WebGLViewShot>
      </ScrollView>
    );
  }
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: "#f6f6f6"
  },
  container: {
    paddingVertical: 20,
    backgroundColor: "#f6f6f6"
  },
  input: {
    height: 40,
    borderColor: "gray",
    borderWidth: 1
  },
  glView: {
    width: 300,
    height: 200
  },
  shot: {
    width: 300,
    height: 200,
    alignItems: "center",
    justifyContent: "center",
    backgroundColor: "black"
  },
  text: {
    fontWeight: "bold",
    fontSize: 40,
    color: "white"
  }
});
