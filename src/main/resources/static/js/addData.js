window.onload = function () {
    $("#btn_add").click(function () {
        $.ajax({
            url:"/pd/add",
            type:"POST",
            data:$("#data-form").serialize(),
            dataType:"JSON",

            success:function (json) {
                if (json.state == 200){
                $.ajax({
                    url:"/pd/reinit",
                    type:"GET",
                    dataType:"JSON",
                    success:function (json) {
                        if (json.state == 200){
                            location.href = "../show.html";
                        } else {
                            alert("失败:" + json.errorMsg);
                        }
                    },
                    error:function (xhr) {
                        console.log(xhr.message);
                    }
                })
                } else {
                    alert("失败:" + json.errorMsg);
                }
            },
            error:function (xhr) {
                console.log(xhr.message);
            }
        })
    });
}