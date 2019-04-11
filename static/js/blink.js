setInterval(function() {
    elem = $('.blink');
    var str = elem.text();
    if (str.search('_') != -1) {
        elem.html(elem.html().replace("_", ""));
    } else {
        var newstr = str.substr(0, 1) + '_';
        elem.text(newstr);
    }
}, 500);