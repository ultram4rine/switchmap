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

export const uploadPlan = async (
  build: string,
  floor: number,
  image: File
): Promise<void> => {
  const formData = new FormData();
  formData.append("planFile", image);
  await api.post(`/plan/${build}/${floor}`, formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};
