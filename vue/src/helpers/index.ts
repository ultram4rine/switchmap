export const authHeader = (): string => {
  const token = localStorage.getItem("token");
  return token ? token : "";
};

export const macNormalization = (mac: string): string => {
  if (mac.length === 17) {
    return mac.toLowerCase().replace(/:|-/g, "");
  } else {
    return mac.toLowerCase();
  }
};
