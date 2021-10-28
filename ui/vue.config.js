const path = require("path");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");

module.exports = {
  assetsDir: process.env.NODE_ENV === "production" ? "static" : "",
  devServer: {
    public: "localhost:8080",
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  configureWebpack: {
    plugins: [new CleanWebpackPlugin()],
  },
};
