import { DirectiveOptions } from "vue";

const apply = (elem: HTMLElement, transform: string) => {
  elem.style.webkitTransform = transform;
  elem.style.transform = transform;
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
    let scale = el.getBoundingClientRect().width / el.offsetWidth,
      oldScale;

    let rect: DOMRect;
    setTimeout(() => {
      rect = el.getBoundingClientRect();
    }, 0);

    addOnWheel(el, (e: WheelEvent) => {
      e.preventDefault();

      const pgX = e.pageX,
        pgY = e.pageY;

      const parentRect = (el?.parentNode as Element).getBoundingClientRect();
      rect = el.getBoundingClientRect();

      const delta = Math.max(-1, Math.min(1, e.deltaY || e.detail));

      oldScale = scale;
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

      const transform = `translate(${left}px, ${top}px) scale(${scale})`;
      apply(el, transform);
    });
  },
};

export default directive;
