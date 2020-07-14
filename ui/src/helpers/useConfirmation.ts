import { ref } from "@vue/composition-api";

export default function () {
  const value = ref("");
  const name = ref("");

  return {
    value,
    name,
  };
}
