export const authHeader = (): string => {
  const token = localStorage.getItem("token");
  return token ? token : "";
};

export const macNormalization = (mac: string): string | undefined => {
  if (mac.length === 17) {
    return mac.toLowerCase().replace(/:|-/g, "");
  } else if (mac.length === 12) {
    return mac.toLowerCase();
  }
};

export const macDenormalization = (mac: string): string => {
  return mac
    .toUpperCase()
    .replace(/(.{2})/g, "$1:")
    .slice(0, -1);
};
