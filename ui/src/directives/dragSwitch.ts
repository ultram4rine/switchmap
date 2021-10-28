import { DirectiveOptions } from "vue";
import { DirectiveBinding } from "vue/types/options";

import { updatePosition } from "../api/switches";

const apply = (elem: HTMLElement, transform: { dx: number; dy: number }) => {
  elem.style.top = transform.dy.toString() + "px";
  elem.style.left = transform.dx.toString() + "px";
};

const handler = (sw: HTMLElement, binding: DirectiveBinding): void => {
  sw.addEventListener("mousedown", (e: MouseEvent) => {
    if (e.preventDefault) e.preventDefault();

    const scale =
      Math.round((sw.getBoundingClientRect().width / sw.offsetWidth) * 10) / 10;

    const style = window.getComputedStyle(sw),
      top = parseFloat(style.top),
      left = parseFloat(style.left);
    let lastTransform = { dx: left, dy: top };

    const lastOffset = lastTransform;
    const lastOffsetX = lastOffset ? lastOffset.dx : 0,
      lastOffsetY = lastOffset ? lastOffset.dy : 0;

    const startX = e.pageX - lastOffsetX,
      startY = e.pageY - lastOffsetY;

    const swMoveHandler = (event: MouseEvent) => {
      if (event.preventDefault) event.preventDefault();

      const newDx =
          event.clientX -
          startX +
          (event.clientX - startX - lastOffsetX) * (1 / scale - 1),
        newDy =
          event.clientY -
          startY +
          (event.clientY - startY - lastOffsetY) * (1 / scale - 1);

      apply(sw, { dx: newDx, dy: newDy });
      lastTransform = { dx: newDx, dy: newDy };
      binding.value.positionTop = newDy;
      binding.value.positionLeft = newDx;
    };

    sw.addEventListener("mousemove", swMoveHandler);

    sw.addEventListener("mouseup", () => {
      updatePosition(sw.id, { top: lastTransform.dy, left: lastTransform.dx });
      sw.removeEventListener("mousemove", swMoveHandler);
    });
    sw.addEventListener("mouseout", () =>
      sw.removeEventListener("mousemove", swMoveHandler)
    );
  });
};

const directive: DirectiveOptions = {
  inserted: handler,
};

export default directive;
