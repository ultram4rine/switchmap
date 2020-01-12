let elem = $("#dragplan");
let img = $("image");
let scale = 0.3;
let xLast = -(img.naturalWidth * scale) / 2;
let yLast = -(img.naturalHeight * scale) / 2;
let xImage = 0;
let yImage = 0;

function addOnWheel(elem, handler) {
  if (elem.addEventListener) {
    if ("onwheel" in document) {
      elem.addEventListener("wheel", handler);
    } else if ("onmousewheel" in document) {
      elem.addEventListener("mousewheel", handler);
    } else {
      elem.addEventListener("MozMousePixelScroll", handler);
    }
  } else {
    text.attachEvent("onmousewheel", handler);
  }
}

addOnWheel(element, function(e) {
  e.preventDefault();

  let delta = e.deltaY || e.detail || e.wheelDelta;

  let xScreen = e.pageX - $(this).offset().left;
  let yScreen = e.pageY - $(this).offset().top;

  xImage = xImage + (xScreen - xLast) / scale;
  yImage = yImage + (yScreen - yLast) / scale;

  if (delta > 0) {
    scale = scale <= 0.305 ? 0.3 : (scale -= 0.05);
  } else {
    scale = scale >= 1.795 ? 1.8 : (scale += 0.05);
  }

  let xNew = (xScreen - xImage) / scale;
  let yNew = (yScreen - yImage) / scale;

  xLast = xScreen;
  yLast = yScreen;

  $(this)
    .css(
      "-moz-transform",
      "scale(" + scale + ") translate(" + xNew + "px, " + yNew + "px)"
    )
    .css("-moz-transform-origin", xImage + "px " + yImage + "px");
});
