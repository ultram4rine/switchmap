import api from ".";

// Returns new URL for plan.
export const getPlan = async (url: string): Promise<string | undefined> => {
  const resp = await api.get(url, {
    responseType: "blob",
  });
  if (resp) {
    return URL.createObjectURL(resp.data);
  }
};
