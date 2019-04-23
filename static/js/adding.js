function bAdd(){
    var name = document.getElementById("name").value;
    var addr = document.getElementById("addr").value;
    if (name.length == 0 || addr.length == 0) {
        alert("Пустое значение")
    } else {
        $.ajax({
            url: "/badd",
            method: "POST",
            data: {
                name: name,
                addr: addr
            },
            success: function(){
                location.reload();
            }
        });
    }
};

function fAdd(){
    var href = location.href.split('/');
    var build = href[4];
    var num = document.getElementById("num").value;
    if (num.length == 0) {
        alert("Пустое значение")
    } else {
        $.ajax({
            url: "/fadd",
            method: "POST",
            data: {
                build: build,
                num: num
            },
            success: function(){
                location.reload();
            }
        });
    }
};