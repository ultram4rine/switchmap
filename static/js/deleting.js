function bdel(element){
    var name = $(element).parent().children('a').children('.name').text();
    $.ajax({
        url: "/bdel",
        method: "POST",
        data: {
            name: name
        },
        success: function(){
            location.reload();
        }
    });
};

function fdel(element){
    var href = location.href.split('/');
    var build = href[4];
    var name = $(element).parent().children('a').children('.num').text();
    var num = name.split(' ');
    $.ajax({
        url: "/fdel",
        method: "POST",
        data: {
            build: build,
            num: num[0]
        },
        success: function(){
            location.reload();
        }
    });
};

function swdel(){
    var href = location.href.split('/');
    var i = location.href.lastIndexOf('/');
    var temploc = location.href.substr(0, i);
    var j = temploc.lastIndexOf('/');
    var newloc = location.href.substr(0, j);
    var name = href[5];
    $.ajax({
        url: "/swdel",
        method: "POST",
        data: {
            name: name
        },
        success: function(){
            location.replace(newloc)
        }
    });
};