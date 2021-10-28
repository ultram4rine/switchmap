import { DirectiveOptions } from "vue";

const apply = (elem: HTMLElement, transform: string) => {
  elem.style.webkitTransform = transform;
  elem.style.transform = transform;
};

const applySw = (elem: HTMLElement, transform: { dx: number; dy: number }) => {
  elem.style.top = transform.dy.toString() + "px";
  elem.style.left = transform.dx.toString() + "px";
};

const handler = (el: HTMLElement): void => {
  const mouseDownHandler = (e: MouseEvent) => {
    if (e.preventDefault) e.preventDefault();

    const style = window.getComputedStyle(el),
      matrix = new WebKitCSSMatrix(style.transform || style.webkitTransform);
    let lastTransform = { dx: matrix.m41, dy: matrix.m42 };

    const lastOffset = lastTransform;
    const lastOffsetX = lastOffset ? lastOffset.dx : 0,
      lastOffsetY = lastOffset ? lastOffset.dy : 0;

    const startX = e.pageX - lastOffsetX,
      startY = e.pageY - lastOffsetY;

    const mouseMoveHandler = (event: MouseEvent) => {
      if (event.preventDefault) event.preventDefault();

      const scale = el.getBoundingClientRect().width / el.offsetWidth;
      const newDx = event.pageX - startX,
        newDy = event.pageY - startY;

      apply(el, `translate(${newDx}px, ${newDy}px) scale(${scale})`);
      lastTransform = { dx: newDx, dy: newDy };
    };

    el.addEventListener("mousemove", mouseMoveHandler);

    el.addEventListener("mouseup", () => {
      el.removeEventListener("mousemove", mouseMoveHandler);
    });
  };

  el.addEventListener("mousedown", mouseDownHandler);

  for (const swEl of el.getElementsByClassName("switch")) {
    const sw = swEl as HTMLElement;

    sw.addEventListener("mouseover", () =>
      el.removeEventListener("mousedown", mouseDownHandler)
    );
    sw.addEventListener("mouseout", () =>
      el.addEventListener("mousedown", mouseDownHandler)
    );

    sw.addEventListener("mousedown", (e: MouseEvent) => {
      if (e.preventDefault) e.preventDefault();

      const scale =
        Math.round((sw.getBoundingClientRect().width / sw.offsetWidth) * 10) /
        10;

      const style = window.getComputedStyle(sw),
        top = parseFloat(style.top),
        left = parseFloat(style.left);
      let lastTransform = { dx: left, dy: top };

      const lastOffset = lastTransform;
      const lastOffsetX = lastOffset ? lastOffset.dx : 0,
        lastOffsetY = lastOffset ? lastOffset.dy : 0;

      const startX = e.pageX - lastOffsetX,
        startY = e.pageY - lastOffsetY;

      const mouseMoveHandler = (event: MouseEvent) => {
        if (event.preventDefault) event.preventDefault();

        const newDx =
            event.clientX -
            startX +
            (event.clientX - startX - lastOffsetX) * (1 / scale - 1),
          newDy =
            event.clientY -
            startY +
            (event.clientY - startY - lastOffsetY) * (1 / scale - 1);

        applySw(sw, { dx: newDx, dy: newDy });
        lastTransform = { dx: newDx, dy: newDy };
      };

      sw.addEventListener("mousemove", mouseMoveHandler);

      sw.addEventListener("mouseup", () => {
        sw.removeEventListener("mousemove", mouseMoveHandler);
      });
    });
  }
};

const directive: DirectiveOptions = {
  inserted: handler,
};

export default directive;
