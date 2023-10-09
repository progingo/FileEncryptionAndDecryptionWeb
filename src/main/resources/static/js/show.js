$(function () {
    $.ajax({
        url:"/pd/show",
        type:"GET",
        dataType:"JSON",

        success:function (json) {
            if (json.state == 200){
                console.log("成功");
                console.log(json.data);

                let data = json.data;
                if (data.length > 1){
                    for (var i = 1;i < data.length;++i){
                        var tr = document.createElement("tr");
                        var td_from = document.createElement("td");
                        var td_name = document.createElement("td");
                        var td_bz = document.createElement("td");

                        td_from.className = "e";
                        td_name.className = "e";
                        td_bz.className = "e";

                        td_from.innerHTML = data[i].from;
                        td_name.innerHTML = data[i].zh;
                        td_bz.innerHTML = data[i].bz;

                        var td_show = document.createElement("td");
                        var but_show = document.createElement("button");
                        but_show.innerHTML = "查看密码";
                        $(but_show).on('click',function () {
                            showPass(this.value);
                        })
                        but_show.value = i;
                        but_show.className="btn_show";
                        td_show.appendChild(but_show);

                        var td_update = document.createElement("td");
                        var but_update = document.createElement("button");
                        but_update.innerHTML = "修改";
                        $(but_update).on('click',function () {
                            updateData(this.value);
                        })
                        but_update.value = i;
                        but_update.className="btn_update";
                        td_update.appendChild(but_update);

                        var td_delete = document.createElement("td");
                        var but_delete = document.createElement("button");
                        but_delete.innerHTML = "删除";
                        $(but_delete).on('click',function () {
                            if (confirm("确认删除?")){
                                deleteData(this.value);
                            }
                        })
                        but_delete.value = i;
                        but_delete.className="btn_del";
                        td_delete.appendChild(but_delete);

                        tr.appendChild(td_from);
                        tr.appendChild(td_name);
                        tr.appendChild(td_bz);
                        tr.appendChild(td_show);
                        tr.appendChild(td_update);
                        tr.appendChild(td_delete);

                        var dataTable = document.getElementById("data-table");
                        dataTable.appendChild(tr);
                    }
                }

            } else {
                alert("失败:" + json.errorMsg);
            }
        },
        error:function (xhr) {
            console.log(xhr.message);
        }
    })
})