import { DirectiveOptions } from "vue";

const handler = (el: HTMLElement): void => {
  const mouseDownHandler = (e: MouseEvent) => {
    if (e.preventDefault) e.preventDefault();

    const style = window.getComputedStyle(el),
      top = parseFloat(style.top),
      left = parseFloat(style.left);

    const startX = e.pageX - left,
      startY = e.pageY - top;

    const mouseMoveHandler = (event: MouseEvent) => {
      if (event.preventDefault) event.preventDefault();

      const newDx = event.pageX - startX,
        newDy = event.pageY - startY;

      el.style.left = newDx + "px";
      el.style.top = newDy + "px";
    };

    document.addEventListener("mousemove", mouseMoveHandler);

    document.addEventListener("mouseup", () => {
      document.removeEventListener("mousemove", mouseMoveHandler);
    });
  };

  el.onmousedown = mouseDownHandler;

  for (const swEl of el.getElementsByClassName("switch")) {
    const sw = swEl as HTMLElement;

    sw.onmouseover = () => (el.onmousedown = null);
    sw.onmouseout = () => (el.onmousedown = mouseDownHandler);
  }
};

const directive: DirectiveOptions = {
  inserted: handler,
};

export default directive;
