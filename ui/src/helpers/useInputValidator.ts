import { Ref, ref, watch } from "@vue/composition-api";

export default function (
  startVal: string,
  validators: Array<(input: string) => string | null>,
  onValidate: (val: string) => void
) {
  const input = ref(startVal);
  const errors: Ref<(string | null)[]> = ref([]);

  watch(input, (value) => {
    errors.value = validators.map((validator) => validator(value));
    onValidate(value);
  });

  return {
    input,
    errors,
  };
}
