import { DirectiveOptions, VNode } from "vue";
import { DirectiveBinding } from "vue/types/options";

const handler = (
  sw: HTMLElement,
  binding: DirectiveBinding,
  vnode: VNode
): void => {
  sw.onmousedown = (e: MouseEvent) => {
    if (e.preventDefault) e.preventDefault();

    if (vnode.componentInstance) {
      vnode.componentInstance.$emit("moving", { detail: sw.id });
    } else {
      vnode.elm?.dispatchEvent(new CustomEvent("moving", { detail: sw.id }));
    }

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
      binding.value.sw.positionTop = newDy;
      binding.value.sw.positionLeft = newDx;
      try {
        binding.value.socket.send(
          JSON.stringify({
            name: sw.id,
            top: binding.value.sw.positionTop,
            left: binding.value.sw.positionLeft,
            moving: true,
          })
        );
      } catch (err) {
        console.log(err);
      }
    };

    if (!binding.value.locked.has(sw.id)) {
      document.addEventListener("mousemove", swMoveHandler);
    }

    sw.addEventListener("mouseup", () => {
      binding.value.socket.send(
        JSON.stringify({
          name: sw.id,
          top: binding.value.sw.positionTop,
          left: binding.value.sw.positionLeft,
          moving: false,
        })
      );
      document.removeEventListener("mousemove", swMoveHandler);
      if (vnode.componentInstance) {
        vnode.componentInstance.$emit("moving", { detail: "" });
      } else {
        vnode.elm?.dispatchEvent(new CustomEvent("moving", { detail: "" }));
      }
    });
  };
};

const directive: DirectiveOptions = {
  inserted: handler,
};

export default directive;
