window.onload = function () {
    $("#btn").click(function () {
        $.ajax({
            url:"/pd/init",
            type:"POST",
            data:$("#data-form").serialize(),
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
    });
}