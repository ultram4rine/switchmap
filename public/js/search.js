import "/public/node_modules/jquery/dist/jquery.min.js";

$("form input").keyup(function() {
  let data = this.value.split(" ");
  let rows = $("tbody").find("tr");

  if (this.value === "") {
    rows.show();
    return;
  }

  rows.hide();

  rows
    .filter(function(_i, _v) {
      for (let j in data) {
        if (
          $(this)
            .find("td#name")
            .is(`:contains('${data[j]}')`)
        ) {
          return true;
        }
      }
      return false;
    })
    .show();
});
