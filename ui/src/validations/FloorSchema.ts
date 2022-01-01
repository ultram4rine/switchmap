import { object, number } from "yup";

export const FloorSchema = object({
  number: number().positive().required(),
});
