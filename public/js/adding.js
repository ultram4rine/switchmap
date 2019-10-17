function bAdd(){
    var name = document.getElementById("name").value;
    var addr = document.getElementById("addr").value;
    var reg = /^b[1-9]?[0-9]$/;
    if (name.length == 0 || addr.length == 0) {
        alert("Пустое значение")
    } else if (reg.test(addr)) {
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
    var reg = /^[1-9]?[0-9]$/;
    if (num.length == 0) {
        alert("Пустое значение")
    } else if (reg.test(num)) {
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

function swAdd(){
    var href = location.href.split('/');
    var build = href[4];
    var floor = href[5];
    var name = document.getElementById("name").value;
    var model = document.getElementById("model").value;
    if (name.length == 0 || model.length == 0) {
        alert("Пустое значение")
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
            success: function(){
                location.reload();
            }
        })
    }
};