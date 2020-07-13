import axios, { AxiosResponse } from "axios";

import { config } from "@/config";

import { Floor } from "@/interfaces";

const floorsEndpoint = (build: string) => {
  return `${config.apiURL}/builds/${build}/floors`;
};
const floorEndpoint = (build: string, floor: string) => {
  return `${config.apiURL}/builds/${build}/${floor}`;
};

const getFloorsOf = (build: string) => {
  let floors: Floor[] = [];
  axios
    .get<Floor, AxiosResponse<Floor[]>>(floorsEndpoint(build))
    .then((resp) => (floors = resp.data))
    .catch((err) => console.log(err));

  return floors;
};

const addFloorTo = (build: string, floor: number) => {
  axios
    .post(floorsEndpoint(build), {
      number: floor,
    })
    .catch((err) => console.log(err));
};

const deleteFloorOf = (build: string, floor: string) => {
  axios.delete(floorEndpoint(build, floor)).catch((err) => console.log(err));
};

export { getFloorsOf, addFloorTo, deleteFloorOf };
