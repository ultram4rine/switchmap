function bAdd() {
  let name = document.getElementById("name").value;
  let addr = document.getElementById("addr").value;
  let reg = /^b[1-9]?[0-9]$/;

  if (name.length === 0 || addr.length === 0) {
    alert("Пустое значение");
  } else if (reg.test(addr)) {
    $.ajax({
      url: "/badd",
      method: "POST",
      data: {
        name: name,
        addr: addr
      },
      error: function(jqXHR) {
        alert(jqXHR.responseText);
      },
      success: function() {
        location.reload();
      }
    });
  }
}

function fAdd() {
  let href = location.href.split("/");
  let build = href[4];
  let num = document.getElementById("num").value;
  let reg = /^[1-9]?[0-9]$/;

  if (num.length === 0) {
    alert("Пустое значение");
  } else if (reg.test(num)) {
    $.ajax({
      url: "/fadd",
      method: "POST",
      data: {
        build: build,
        num: num
      },
      error: function(jqXHR) {
        alert(jqXHR.responseText);
      },
      success: function() {
        location.reload();
      }
    });
  }
}

function swAdd() {
  let href = location.href.split("/");
  let build = href[4];
  let floor = href[5];
  let name = document.getElementById("name").value;
  let model = document.getElementById("model").value;

  if (name.length === 0 || model.length === 0) {
    alert("Пустое значение");
  } else {
    $.ajax({
      url: "/swadd",
      method: "POST",
      data: {
        name: name,
        model: model,
        build: build,
        floor: floor
      },
      error: function(jqXHR) {
        alert(jqXHR.responseText);
      },
      success: function() {
        location.reload();
      }
    });
  }
}
