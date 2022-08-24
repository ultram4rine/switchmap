import { DirectiveBinding } from "vue";

import { updatePosition } from "@/api/switches";

const handler = (sw: HTMLElement, binding: DirectiveBinding): void => {
  sw.onmousedown = (e: MouseEvent) => {
    if (e.preventDefault) e.preventDefault();

    const scale =
      Math.round((sw.getBoundingClientRect().width / sw.offsetWidth) * 10) / 10;

    const style = window.getComputedStyle(sw),
      top = parseFloat(style.top),
      left = parseFloat(style.left);

    const startX = e.pageX - left,
      startY = e.pageY - top;

    const swMoveHandler = (event: MouseEvent) => {
      if (event.preventDefault) event.preventDefault();

      const newDx =
          event.clientX -
          startX +
          (event.clientX - startX - left) * (1 / scale - 1),
        newDy =
          event.clientY -
          startY +
          (event.clientY - startY - top) * (1 / scale - 1);

      sw.style.left = newDx + "px";
      sw.style.top = newDy + "px";
      binding.value.positionTop = newDy;
      binding.value.positionLeft = newDx;
    };

    document.addEventListener("mousemove", swMoveHandler);

    sw.addEventListener("mouseup", () => {
      updatePosition(sw.id, {
        top: binding.value.positionTop,
        left: binding.value.positionLeft,
      });
      document.removeEventListener("mousemove", swMoveHandler);
    });
  };
};

export default {
  mounted: handler,
};
