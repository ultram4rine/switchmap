$(document).on("form input", function(ev){
    if ($(ev.target).val().length > 0){
        var regexp = $(ev.target).val();
        $('tr[id]').hide();
        $('[id^=' + regexp + ']').show();
    } else {
        $('tr[id]').show();
    }
});