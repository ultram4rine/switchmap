import { DirectiveOptions } from "vue";

const apply = (
  elem: HTMLElement,
  transform: { dx: number; dy: number; scale: number }
) => {
  elem.style.transform = `translate(${transform.dx}px, ${transform.dy}px) scale(${transform.scale})`;
};

const handler = (el: HTMLElement): void => {
  const mouseDownHandler = (e: MouseEvent) => {
    if (e.preventDefault) e.preventDefault();

    const scale = el.getBoundingClientRect().width / el.offsetWidth;

    const style = window.getComputedStyle(el),
      matrix = new WebKitCSSMatrix(style.transform);

    const startX = e.pageX - matrix.m41,
      startY = e.pageY - matrix.m42;

    const mouseMoveHandler = (event: MouseEvent) => {
      if (event.preventDefault) event.preventDefault();

      const newDx = event.pageX - startX,
        newDy = event.pageY - startY;

      apply(el, { dx: newDx, dy: newDy, scale });
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
  }
};

const directive: DirectiveOptions = {
  inserted: handler,
};

export default directive;
