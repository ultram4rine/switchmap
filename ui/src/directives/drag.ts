import { DirectiveOptions } from "vue";

const apply = (elem: HTMLElement, transform: string) => {
  elem.style.webkitTransform = transform;
  elem.style.transform = transform;
};

const applySw = (elem: HTMLElement, transform: { dx: number; dy: number }) => {
  elem.style.top = transform.dy.toString() + "px";
  elem.style.left = transform.dx.toString() + "px";
};

const directive: DirectiveOptions = {
  inserted: (el) => {
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

    // TODO: fix scale.
    for (const sw of el.getElementsByClassName("switch")) {
      sw.addEventListener("mouseover", () =>
        el.removeEventListener("mousedown", mouseDownHandler)
      );
      sw.addEventListener("mouseout", () =>
        el.addEventListener("mousedown", mouseDownHandler)
      );

      sw.addEventListener("mousedown", function (this: HTMLElement, e: any) {
        if (e.preventDefault) e.preventDefault();

        console.log(this.id);

        const scale =
          Math.round(
            (this.getBoundingClientRect().width / this.offsetWidth) * 10
          ) / 10;
        console.log(scale);

        const style = window.getComputedStyle(this),
          top = parseFloat(style.top),
          left = parseFloat(style.left);
        let lastTransform = { dx: left, dy: top };
        console.log(lastTransform);

        const lastOffset = lastTransform;
        const lastOffsetX = lastOffset ? lastOffset.dx : 0,
          lastOffsetY = lastOffset ? lastOffset.dy : 0;

        const startX = e.pageX - lastOffsetX,
          startY = e.pageY - lastOffsetY;

        const mouseMoveHandler = (event: MouseEvent) => {
          if (event.preventDefault) event.preventDefault();

          console.log(
            event.clientX,
            startX,
            event.clientX - startX,
            (event.clientX - startX) / scale
          );
          const newDx = event.clientX - startX,
            newDy = event.clientY - startY;

          applySw(this, { dx: newDx, dy: newDy });
          lastTransform = { dx: newDx, dy: newDy };
        };

        this.addEventListener("mousemove", mouseMoveHandler);

        this.addEventListener("mouseup", () => {
          this.removeEventListener("mousemove", mouseMoveHandler);
        });
      });
    }
  },
};

export default directive;
