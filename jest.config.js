module.exports = {
  preset: "@vue/cli-plugin-unit-jest/presets/typescript-and-babel",
  collectCoverage: true,
  collectCoverageFrom: [
    "src/**/*.{ts,vue}",
    "!src/main.ts", // No need to cover bootstrap file
  ],
  coverageDirectory: "coverage",
  coverageReporters: ["lcov"],
  transform: { "vee-validate/dist/rules": "babel-jest" },
  transformIgnorePatterns: [
    "<rootDir>/node_modules/(?!vee-validate/dist/rules)",
  ],
};
