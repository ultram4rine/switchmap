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
    }).click(function(){
        Clicker.Click($(this));
    });
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

var Clicker = {
    $element: null,

    Click: function($element){
        this.$element = $element;
        
        if ($element.draggable("option", "disabled")==false)
        $element.draggable('disable')
        else $element.draggable('enable')

        this.name = this.$element.children('.name').text()
        if ($('.switch #'+this.name+'.cir').css('width') == '1px') {
            $('.switch #'+this.name+'.cir').animate({
                width: '+=199px',
                height: '+=199px'
            }, 300);

            $('.switch #'+this.name+' img').animate({
                marginTop: '-95px'
            }, 300)

            $('.switch #'+this.name+'.name').animate({
                marginTop: '100px'
            }, 300)

            $('.switch #'+this.name+'.blink').animate({
                opacity: 0.8,
                marginTop: '0'
            }, 300)
        }
        else {
            $('.switch #'+this.name+'.cir').animate({
                width: "-=199px",
                height: '-=199px'
            }, 300);

            $('.switch #'+this.name+' img').animate({
                marginTop: '0'
            }, 300)

            $('.switch #'+this.name+'.name').animate({
                marginTop: '0'
            }, 300)

            $('.switch #'+this.name+'.blink').animate({
                opacity: 0,
                marginTop: '-23px'
            }, 300)
        }
    }
}
