function showPass(i) {
    $.ajax({
        url:"/pd/showPass",
        type:"POST",
        data:{"i":i},
        dataType:"JSON",

        success:function (json) {
            if (json.state == 200){
                alert(json.data);
            } else {
                alert("失败:" + json.errorMsg);
            }
        },
        error:function (xhr) {
            console.log(xhr.message);
        }

    })

}