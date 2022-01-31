<script lang="ts">
  import { Icon } from "@smui/icon-button";
  import { Svg } from "@smui/common/elements";

  export let icon: string;
</script>

<Icon component={Svg} viewBox="0 0 24 24">
  <path fill="currentColor" d={icon} />
</Icon>
