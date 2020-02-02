import { $ } from "jquery";
import { draggable } from "jquery-ui-dist";

window.$ = $;

function reload(elem) {
  sw_name = $(elem).attr("id");

  $.ajax({
    url: "/update/switch",
    method: "POST",
    data: {
      name: sw_name
    },
    error: function(jqXHR) {
      alert(jqXHR.responseText);
    },
    success: function() {
      location.reload();
    }
  });
}

let click = {
  x: 0,
  y: 0
};

$(function() {
  let $draggable = $(".switch");
  Saver.Reorder($draggable);
  let element = document.querySelector(".switch");

  $draggable
    .draggable({
      containment: "#dragplan", //not working cause drag

      start: function(event) {
        click.x = event.clientX;
        click.y = event.clientY;
      },

      drag: function(event, ui) {
        let zoom = element.getBoundingClientRect().width / element.offsetWidth;
        let original = ui.originalPosition;
        ui.position = {
          left: (event.clientX - click.x + original.left) / zoom,
          top: (event.clientY - click.y + original.top) / zoom
        };
      },

      stop: function() {
        Saver.Reorder($(this));
        Saver.Save();
      }
    })
    .click(function() {
      Clicker.Click($(this).children(".image, .name"));
    });
});

let Saver = {
  $element: null,
  top: 0,
  left: 0,

  Reorder: function($element) {
    this.$element = $element;
    this.top = this.$element.css("top").replace(/px/, "");
    this.left = this.$element.css("left").replace(/px/, "");
  },

  Save: function() {
    this.name = this.$element.children(".name").text();
    $.ajax({
      url: "/savepos",
      method: "POST",
      data: {
        name: this.name,
        top: this.top,
        left: this.left
      }
    });
  }
};

let Clicker = {
  $element: null,

  Click: function($element) {
    this.$element = $element;

    if ($element.parent().draggable("option", "disabled") == false)
      $element.parent().draggable("disable");
    else $element.parent().draggable("enable");

    this.name = this.$element.attr("id");
    var nameheight =
      parseInt($(`.switch #${this.name}.name`).css("height")) + 109;

    if ($(`.switch #${this.name}.cir`).css("width") === "1px") {
      $(`.switch #${this.name}.cir`).animate(
        {
          width: "+=199px",
          height: "+=199px"
        },
        300
      );

      $(`.switch #${this.name} img`).animate(
        {
          marginTop: "-95px"
        },
        300
      );

      $(`.switch #${this.name}.name`).animate(
        {
          marginTop: "100px"
        },
        300
      );

      $(`.switch #${this.name}.change`).animate(
        {
          opacity: 0.8,
          marginTop: "-112px"
        },
        100
      );

      $(`.switch #${this.name}.blink`).animate(
        {
          opacity: 0.8,
          marginTop: nameheight + "px"
        },
        100
      );

      $(`.switch #${this.name}.reload`).animate(
        {
          opacity: 0.8,
          marginLeft: "-82px"
        },
        100
      );

      $(`.switch #${this.name}.download`).animate(
        {
          opacity: 0.8,
          marginLeft: "153px"
        },
        100
      );
    } else {
      $(`.switch #${this.name}.download`).animate(
        {
          opacity: 0,
          marginLeft: "0"
        },
        50
      );

      $(`.switch #${this.name}.reload`).animate(
        {
          opacity: 0,
          marginLeft: "0"
        },
        50
      );

      $(`.switch #${this.name}.blink`).animate(
        {
          opacity: 0,
          marginTop: "0"
        },
        50
      );

      $(`.switch #${this.name}.change`).animate(
        {
          opacity: 0,
          marginTop: "0"
        },
        50
      );

      $(`.switch #${this.name}.cir`).animate(
        {
          width: "-=199px",
          height: "-=199px"
        },
        300
      );

      $(`.switch #${this.name} img`).animate(
        {
          marginTop: "0"
        },
        300
      );

      $(`.switch #${this.name}.name`).animate(
        {
          marginTop: "0"
        },
        300
      );
    }
  }
};
