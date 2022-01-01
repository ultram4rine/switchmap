import { object, string } from "yup";

export const BuildSchema = object({
  name: string().required(),
  shortName: string().required(),
});
