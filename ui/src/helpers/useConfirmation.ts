import { ref } from "@vue/composition-api";

export default function () {
  const confirmation = ref(false);
  const name = ref("");

  return {
    confirmation,
    name,
  };
}
