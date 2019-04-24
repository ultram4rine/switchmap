function addOnWheel(elem, handler) {
    if (elem.addEventListener) {
        if ('onwheel' in document) {
            elem.addEventListener("wheel", handler);
        } else if ('onmousewheel' in document) {
            elem.addEventListener("mousewheel", handler);
        } else {
            elem.addEventListener("MozMousePixelScroll", handler);
        }
    } else {
        text.attachEvent("onmousewheel", handler);
    }
}

var elem = document.getElementById("dragplan")
var style = window.getComputedStyle(elem);
var matrix = new WebKitCSSMatrix(style.webkitTransform);
var scale = 0.3;
var xLast = matrix.m41;
var yLast = matrix.m42;
var xImage = 0;
var yImage = 0;

addOnWheel(dragplan, function(e) {
    var delta = e.deltaY || e.detail || e.wheelDelta;

    var xScreen = e.pageX - $(this).offset().left;
    var yScreen = e.pageY - $(this).offset().top;

    xImage = xImage + ((xScreen - xLast) / scale);
    yImage = yImage + ((yScreen - yLast) / scale);

    if (delta > 0) scale -= 0.05;
    else scale += 0.05;

    scale = scale < 0.3 ? 0.3 : (scale > 1.8 ? 1.8 : scale);

    var xNew = (xScreen - xImage) / scale;
    var yNew = (yScreen - yImage) / scale;

    xLast = xScreen;
    yLast = yScreen;

    $(this).css('-moz-transform', 'scale(' + scale + ') translate(' + xNew + 'px, ' + yNew + 'px)').css('-moz-transform-origin', xImage + 'px ' + yImage + 'px');

    e.preventDefault();
});