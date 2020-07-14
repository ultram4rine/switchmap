interface Config {
  apiURL: string;
}

export let config: Config;

if (!process.env.NODE_ENV || process.env.NODE_ENV === "production") {
  config = {
    apiURL: "/api",
  };
} else {
  config = {
    apiURL: "api",
  };
}
