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

var scale = 0.3;

addOnWheel(dragplan, function(e) {
    var delta = e.deltaY || e.detail || e.wheelDelta;
    var offset = $(this).offset();
    //var oldscale = dragplan.getBoundingClientRect().width / dragplan.offsetWidth;
    //var width = dragplan.getBoundingClientRect().width;
    //var height = dragplan.getBoundingClientRect().height;

    var X = e.pageX - offset.left;
    var Y = e.pageY - offset.top;

    if (delta > 0) scale -= 0.05;
    else scale += 0.05;

    //var diffwidth = width * scale - width * oldscale;
    //var diffheight = height * scale - height * oldscale;
    //var newtransX = -diffwidth / (width / X);
    //var newtransY = -diffheight / (height / Y);

    dragplan.style.transformOrigin = dragplan.style.WebkitTransformOrigin = (X) + 'px ' + (Y) + 'px';

    dragplan.style.transform = dragplan.style.WebkitTransform = dragplan.style.MsTransform = 'scale(' + scale + ') translate(-50%, -50%)';

    e.preventDefault();
});