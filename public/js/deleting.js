import "/public/node_modules/jquery/dist/jquery.min.js";

$("#bdel").on("click", function() {
  let name = $(this)
    .parent()
    .children("a")
    .children(".name")
    .text();

  $.ajax({
    url: "/delete/build",
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
});

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
    url: "/delete/floor",
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
    url: "/delete/switch",
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
