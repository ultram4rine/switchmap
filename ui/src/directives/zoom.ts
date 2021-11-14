import { DirectiveOptions } from "vue";

const apply = (
  elem: HTMLElement,
  transform: { dx: number; dy: number; scale: number }
) => {
  elem.style.transform = `translate(${transform.dx}px, ${transform.dy}px) scale(${transform.scale})`;
};

const addOnWheel = (elem: HTMLElement, handler: (e: WheelEvent) => void) => {
  if (elem.addEventListener) {
    if ("onwheel" in document) {
      elem.addEventListener("wheel", handler);
    }
  }
};

const directive: DirectiveOptions = {
  inserted: (el) => {
    let scale = el.getBoundingClientRect().width / el.offsetWidth;

    addOnWheel(el, (e: WheelEvent) => {
      e.preventDefault();

      const pgX = e.pageX,
        pgY = e.pageY;

      const parentRect = (el?.parentNode as Element).getBoundingClientRect();
      const rect = el.getBoundingClientRect();

      const delta = Math.max(-1, Math.min(1, e.deltaY || e.detail));

      const oldScale = scale;
      scale -= delta / 10;
      if (scale < 0.1) {
        scale = 0.1;
      }
      if (scale > 1.5) {
        scale = 1.5;
      }

      const xPercent = (pgX - rect.left) / rect.width;
      const yPercent = (pgY - rect.top) / rect.height;
      const left = Math.round(
        pgX - parentRect.left - xPercent * ((rect.width * scale) / oldScale)
      );
      const top = Math.round(
        pgY - parentRect.top - yPercent * ((rect.height * scale) / oldScale)
      );

      apply(el, { dx: left, dy: top, scale });
    });
  },
};

export default directive;
