import { DirectiveOptions } from "vue";

const apply = (elem: HTMLElement, transform: string) => {
  elem.style.webkitTransform = transform;
  elem.style.transform = transform;
};

const addOnWheel = (elem: HTMLElement, handler: any) => {
  if (elem.addEventListener) {
    if ("onwheel" in document) {
      elem.addEventListener("wheel", handler);
    } else if ("onmousewheel" in document) {
      elem.addEventListener("mousewheel", handler);
    } else {
      elem.addEventListener("MozMousePixelScroll", handler);
    }
  }
};

const directive: DirectiveOptions = {
  inserted: (el) => {
    let scale = el.getBoundingClientRect().width / el.offsetWidth,
      oldScale;

    let rect, parentRect;
    setTimeout(() => {
      rect = el.getBoundingClientRect();
    }, 0);

    addOnWheel(el, (e: WheelEvent) => {
      e.preventDefault();

      let pgX = e.pageX,
        pgY = e.pageY;

      parentRect = (<Element>el!.parentNode!).getBoundingClientRect();
      rect = el.getBoundingClientRect();

      let delta = Math.max(-1, Math.min(1, e.deltaY || e.detail));

      oldScale = scale;
      scale -= delta / 10;
      if (scale < 0.1) {
        scale = 0.1;
      }
      if (scale > 1.5) {
        scale = 1.5;
      }

      let xPercent = (pgX - rect.left) / rect.width;
      let yPercent = (pgY - rect.top) / rect.height;
      let left = Math.round(
        pgX - parentRect.left - xPercent * ((rect.width * scale) / oldScale)
      );
      let top = Math.round(
        pgY - parentRect.top - yPercent * ((rect.height * scale) / oldScale)
      );

      let transform = `translate(${left}px, ${top}px) scale(${scale})`;
      apply(el, transform);
    });
  },
};

export default directive;
