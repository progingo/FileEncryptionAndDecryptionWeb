function deleteData(i) {

    $.ajax({
        url:"/pd/deletePass",
        type:"POST",
        data:{"i":i},
        dataType:"JSON",

        success:function (json) {
            if (json.state == 200){
                alert("删除成功");

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
}