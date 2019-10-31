let memo;
$("#dragplan").draggable({
  start: function() {
    memo = $(this).css("transition");
    $(this).css("transition", "none");
  },
  stop: function() {
    $(this).css("transition", memo);
  }
});
