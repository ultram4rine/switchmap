export const authHeader = (): string => {
  const token = localStorage.getItem("token");
  return token ? token : "";
};
