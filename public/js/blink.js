setInterval(function() {
  let elem = $(".blink span");
  let str = elem.text();

  if (str.search("_") !== -1) {
    elem.html(elem.html().replace("_", ""));
  } else {
    let newstr = str.substr(0, 1) + "_";
    elem.text(newstr);
  }
}, 500);
