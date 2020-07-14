import axios, { AxiosResponse } from "axios";
import { ref, Ref } from "@vue/composition-api";

import { config } from "@/config";
import { Floor } from "@/interfaces";

const floorsEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}/floors`;
};
const floorEndpoint = (build: string, floor: string) => {
  return `${config.apiURL}/builds/${build}/${floor}`;
};

export default function () {
  const floors: Ref<Floor[]> = ref([]);
  const floor: Ref<Floor> = ref({} as Floor);

  const floorForm = ref(false);
  const floorNumber = ref("");
  const floorBuildName = ref("");
  const floorBuildShortName = ref("");

  const floorsError = ref("");

  const getFloorsOf = async (b: string) => {
    try {
      const resp = await axios.get<Floor, AxiosResponse<Floor[]>>(
        floorsEndpoint(b)
      );
      floors.value = resp.data;
    } catch (err) {
      floorsError.value = err;
    }
  };

  const addFloorTo = async (b: string, f: number) => {
    try {
      axios.post(floorsEndpoint(b), {
        number: f,
      });
    } catch (err) {
      floorsError.value = err;
    }
  };

  const deleteFloorOf = async (b: string, f: string) => {
    try {
      axios.delete(floorEndpoint(b, f));
    } catch (err) {
      floorsError.value = err;
    }
  };

  return {
    floors,
    floor,

    floorForm,
    floorNumber,
    floorBuildName,
    floorBuildShortName,

    floorsError,

    getFloorsOf,
    addFloorTo,
    deleteFloorOf,
  };
}
