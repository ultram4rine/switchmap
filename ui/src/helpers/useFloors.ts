import axios, { AxiosResponse } from "axios";
import { ref, Ref } from "@vue/composition-api";

import { config } from "@/config";
import { Build, Floor } from "@/interfaces";

const floorsEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}/floors`;
};
const floorEndpoint = (build: string, floor: string) => {
  return `${config.apiURL}/builds/${build}/${floor}`;
};

export default function () {
  const floors: Ref<Floor[]> = ref([]);

  const floorForm = ref(false);
  const floorNumber = ref("");
  const floorBuildName = ref("");
  const floorBuildShortName = ref("");

  const openFloorForm = (b: Build) => {
    floorBuildName.value = b.name;
    floorBuildShortName.value = b.shortName;
    floorNumber.value = "";
    floorForm.value = true;
  };

  const closeFloorForm = () => {
    floorForm.value = false;
    floorNumber.value = "";

    floorBuildName.value = "";
    floorBuildShortName.value = "";
  };

  const floorError = ref("");

  const getFloorsOf = async (b: string) => {
    try {
      const resp = await axios.get<Floor, AxiosResponse<Floor[]>>(
        floorsEndpoint(b)
      );
      return resp.data;
    } catch (err) {
      console.log(err);
      return [];
    }
  };

  const addFloorTo = async (b: string, f: number) => {
    try {
      axios.post(floorsEndpoint(b), {
        number: f,
      });
    } catch (err) {
      floorError.value = err;
    }
  };

  const deleteFloorOf = async (b: string, f: string) => {
    try {
      axios.delete(floorEndpoint(b, f));
    } catch (err) {
      floorError.value = err;
    }
  };

  return {
    floors,

    floorForm,
    floorNumber,
    floorBuildName,
    floorBuildShortName,

    openFloorForm,
    closeFloorForm,

    floorError,

    getFloorsOf,
    addFloorTo,
    deleteFloorOf,
  };
}
