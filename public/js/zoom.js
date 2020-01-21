$(document).ready(() => {
  const el = document.getElementById("dragplan");

  const addOnWheel = (elem, handler) => {
    if (elem.addEventListener) {
      if ("onwheel" in document) {
        elem.addEventListener("wheel", handler);
      } else if ("onmousewheel" in document) {
        elem.addEventListener("mousewheel", handler);
      } else {
        elem.addEventListener("MozMousePixelScroll", handler);
      }
    } else {
      elem.attachEvent("onmousewheel", handler);
    }
  };

  const apply = (el, transform) => {
    el.style.webkitTransform = transform;
    el.style.mozTransform = transform;
    el.style.transform = transform;
  };

  let scale = el.getBoundingClientRect().width / el.offsetWidth,
    oldScale;

  let rect, parentRect;
  setTimeout(() => {
    rect = el.getBoundingClientRect();
  }, 0);

  addOnWheel(el, e => {
    e.preventDefault();

    let pgX = e.pageX,
      pgY = e.pageY;

    parentRect = el.parentNode.getBoundingClientRect();
    rect = el.getBoundingClientRect();

    let delta = Math.max(-1, Math.min(1, e.deltaY || e.detail || e.wheelDelta));
    oldScale = scale;
    scale -= delta / 10;
    if (scale < 0.3) {
      scale = 0.3;
    }
    if (scale > 1.8) {
      scale = 1.8;
    }

    let xPercent = ((pgX - rect.left) / rect.width).toFixed(2);
    let yPercent = ((pgY - rect.top) / rect.height).toFixed(2);
    let left = Math.round(
      pgX - parentRect.left - xPercent * ((rect.width * scale) / oldScale)
    );
    let top = Math.round(
      pgY - parentRect.top - yPercent * ((rect.height * scale) / oldScale)
    );

    let transform = `translate(${left}px, ${top}px) scale(${scale})`;
    apply(el, transform);
  });

  el.addEventListener("mousedown", function(e) {
    if (e.preventDefault) e.preventDefault();

    let style = window.getComputedStyle(el),
      matrix = new WebKitCSSMatrix(
        style.transform || style.webkitTransform || style.mozTransform
      );
    let lastTransform = { dx: matrix.m41, dy: matrix.m42 };

    let lastOffset = lastTransform;
    let lastOffsetX = lastOffset ? lastOffset.dx : 0,
      lastOffsetY = lastOffset ? lastOffset.dy : 0;

    let startX = e.pageX - lastOffsetX,
      startY = e.pageY - lastOffsetY;

    $(document).on("mousemove", function(event) {
      if (event.preventDefault) event.preventDefault();

      let scale = el.getBoundingClientRect().width / el.offsetWidth;
      let newDx = event.pageX - startX,
        newDy = event.pageY - startY;

      apply(el, `translate(${newDx}px, ${newDy}px) scale(${scale})`);
      lastTransform = { dx: newDx, dy: newDy };
    });

    return false;
  });

  $(document).on("mouseup", function() {
    $(this).off("mousemove");
  });
});
