const { CleanWebpackPlugin } = require("clean-webpack-plugin");

module.exports = {
  publicPath: "./src/main/public",
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
  },
};
