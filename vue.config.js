const { CleanWebpackPlugin } = require("clean-webpack-plugin");
const path = require("path");

module.exports = {
  outputDir: "./src/main/resources/public",
  assetsDir: process.env.NODE_ENV === "production" ? "static" : "",
  devServer: {
    port: 8080,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  configureWebpack: {
    plugins: [new CleanWebpackPlugin()],
    resolve: {
      alias: {
        "~": path.resolve(__dirname, "src/main/typescript"),
        "@": path.resolve(__dirname, "src/main/vue"),
      },
    },
  },
};
