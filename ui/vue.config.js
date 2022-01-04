const { defineConfig } = require("@vue/cli-service");
const { CleanWebpackPlugin } = require("clean-webpack-plugin");

module.exports = defineConfig({
  outputDir: "../src/main/resources/public",
  devServer: {
    port: 8080,
    headers: {
      "Access-Control-Allow-Origin": "*",
    },
  },
  configureWebpack: {
    plugins: [new CleanWebpackPlugin()],
  },
  pluginOptions: {
    vuetify: {
      // https://github.com/vuetifyjs/vuetify-loader/tree/next/packages/vuetify-loader
    },
  },
});
