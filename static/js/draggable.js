var click = {
    x: 0,
    y: 0
};

$(function(){
    $draggable = $('.switch');
    Saver.Reorder($draggable);
    var element = document.querySelector('.switch');

    $draggable.draggable({
        start: function(event) {
            click.x = event.clientX;
            click.y = event.clientY;
        },

        drag: function(event, ui) {
            var zoom = element.getBoundingClientRect().width / element.offsetWidth;
            var original = ui.originalPosition;
            ui.position = {
                left: (event.clientX - click.x + original.left) / zoom,
                top:  (event.clientY - click.y + original.top ) / zoom
            };
        },
        
        containment: '#dragplan',

        stop: function(){
            Saver.Reorder($(this));
            Saver.Save();
        }
    })
});

var Saver = {
    $element: null,
    top: 0,
    left: 0,

    Reorder: function($element){
        this.$element = $element;
        this.top = this.$element.css('top').replace(/px/,'');
        this.left = this.$element.css('left').replace(/px/,'');
    },

    Save: function(){
        this.name = this.$element.children('.name').text()
        $.ajax({
            url: "/savepos/" + this.name,
            method: "POST",
            data: {
                top: this.top,
                left: this.left
            }
        })
    }
}
