window.onload = function () {
    $("#btn").click(function () {
        $.ajax({
            url:"/pb/add",
            type:"POST",
            data:$("#data-form").serialize(),
            dataType:"JSON",

            success:function (json) {
                if (json.state == 200){
                    console.log("创建成功");
                    location.href = "../index.html";
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