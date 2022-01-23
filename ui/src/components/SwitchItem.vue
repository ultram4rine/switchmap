<template>
  <v-chip
    :id="sw.name"
    dark
    class="switch ma-2"
    :style="
      sw.positionTop && sw.positionLeft
        ? {
            top: sw.positionTop + 'px',
            left: sw.positionLeft + 'px',
          }
        : { display: 'none' }
    "
  >
    {{ sw.name }}
  </v-chip>
</template>

<script lang="ts">
import {
  defineComponent,
  PropType,
  ref,
  onMounted,
} from "@vue/composition-api";

import { SwitchResponse } from "@/interfaces/switch";

/* 

TODO: if switch is moved here, we have to ignore all received positions of this switch.
TODO: if switch is moved not here, we need to lock this switch from dragging.

*/
export default defineComponent({
  props: {
    sw: { type: Object as PropType<SwitchResponse>, required: true },
    sendSocket: { type: WebSocket, required: true },
  },
  setup(props, { emit }) {
    const moving = ref(false);

    let lastPosX = 0,
      lastPosY = 0;

    onMounted(() => {
      const el = document.getElementById(props.sw.name) as HTMLElement;

      const swmousedown = (ev: MouseEvent) => {
        moving.value = true;
        emit("moving", { name: props.sw.name, val: moving.value });

        const scale =
          Math.round((el.getBoundingClientRect().width / el.offsetWidth) * 10) /
          10;

        const style = window.getComputedStyle(el);
        const top = parseFloat(style.top);
        const left = parseFloat(style.left);

        const startX = ev.pageX - left;
        const startY = ev.pageY - top;

        const swmousemove = (ev: MouseEvent) => {
          ev.preventDefault();

          const newDx =
            ev.clientX -
            startX +
            (ev.clientX - startX - left) * (1 / scale - 1);
          const newDy =
            ev.clientY - startY + (ev.clientY - startY - top) * (1 / scale - 1);

          el.style.left = newDx + "px";
          el.style.top = newDy + "px";

          lastPosX = newDx;
          lastPosY = newDy;

          try {
            props.sendSocket.send(
              JSON.stringify({
                name: props.sw.name,
                top: newDy,
                left: newDx,
                moving: moving.value,
              })
            );
          } catch (err) {
            console.log(err);
          }
        };

        document.addEventListener("mousemove", swmousemove);

        el.addEventListener("mouseup", () => {
          moving.value = false;
          try {
            props.sendSocket.send(
              JSON.stringify({
                name: props.sw.name,
                top: lastPosY,
                left: lastPosX,
                moving: moving.value,
              })
            );
          } catch (err) {
            console.log(err);
          }
          emit("moving", { name: props.sw.name, val: moving.value });
          document.removeEventListener("mousemove", swmousemove);
        });
      };

      el.addEventListener("mousedown", swmousedown);
    });

    return {};
  },
});
</script>

<style>
.switch {
  position: absolute;
}
</style>
