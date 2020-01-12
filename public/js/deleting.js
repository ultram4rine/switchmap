function bdel(element) {
  let name = $(element)
    .parent()
    .children("a")
    .children(".name")
    .text();

  $.ajax({
    url: "/bdel",
    method: "POST",
    data: {
      name: name
    },
    error: function(jqXHR) {
      alert(jqXHR.responseText);
    },
    success: function() {
      location.reload();
    }
  });
}

function fdel(element) {
  let href = location.href.split("/");
  let build = href[4];
  let name = $(element)
    .parent()
    .children("a")
    .children(".num")
    .text();
  let num = name.split(" ");

  $.ajax({
    url: "/fdel",
    method: "POST",
    data: {
      build: build,
      num: num[0]
    },
    error: function(jqXHR) {
      alert(jqXHR.responseText);
    },
    success: function() {
      location.reload();
    }
  });
}

function swdel() {
  let href = location.href.split("/");
  let name = href[5];

  $.ajax({
    url: "/swdel",
    method: "POST",
    data: {
      name: name
    },
    error: function(jqXHR) {
      alert(jqXHR.responseText);
    },
    success: function() {
      location.replace(newloc);
    }
  });
}
