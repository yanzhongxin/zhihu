(function (window, undefined) {
    var Business = Base.getClass('main.util.Business');

    Base.ready({
        initialize: fInitialize
    });

    function fInitialize() {
        Business.followUser();
    }
})();
function getMyDate(str){
    var oDate = new Date(str),
        oYear = oDate.getFullYear(),
        oMonth = oDate.getMonth()+1,
        oDay = oDate.getDate(),
        oHour = oDate.getHours(),
        oMin = oDate.getMinutes(),
        oSen = oDate.getSeconds(),
        oTime = oYear +'-'+ getzf(oMonth) +'-'+ getzf(oDay) +' '+ getzf(oHour) +':'+ getzf(oMin) +':'+getzf(oSen);//最后拼接时间
    return oTime;
};
function formatDate(date){
    var year = date.getFullYear(),
        month = date.getMonth() + 1,//月份是从0开始的
        day = date.getDate(),
        hour = date.getHours(),
        min = date.getMinutes(),
        sec = date.getSeconds();
    var newTime = year + '-' +
        month + '-' +
        day + ' ' +
        hour + ':' +
        min + ':' +
        sec;
    return newTime;
}

function dongtai() {
    history.go(0);
}

//获取用户id为uid的所有的提问
function tiwen(uid) {
    $.ajax({
        type: "GET",
        data: "uid="+uid,
        dataType: "JSON",
        async: false,
        url: "/question/getQuestionsByUserId",
        success: function(data) {
                var temp="<div style=\"font-size: 25px;font-family: 仿宋\">提问</div>";

            for(var index=0;index<data.length;index++){
                //其实index 就是个索
               // temp+="问题id="+data[index].id+"问题标题："+data[index].title+"问题内容："+data[index].content
                temp+="<div style=\"border-bottom: 1px dashed black;width:auto\">\n" +formatDate(new Date(data[index].createdDate))+
                    "我提出了问题 <a href=\"/question/"+data[index].id+"\" style=\"text-decoration:none\" >"+data[index].title+"</a>\n" +
                    "           <p>"+data[index].content+"</p>\n" +
                    "       </div>";
               }

            //alert(temp);
            $("#js-home-feed-list").html(temp);
        }
    });
}

function huida(uid) {
    $.ajax({
        type: "GET",
        data: "uid="+uid,
        dataType: "JSON",
        async: false,
        url: "/getCommitsByUserId",
        success: function(data) {
            var temp="<div style=\"font-size: 25px;font-family: 仿宋\">回答</div>";

            for(var index=0;index<data.comment.length;index++){
                //其实index 就是个索
                // temp+="问题id="+data[index].id+"问题标题："+data[index].title+"问题内容："+data[index].content
                temp+="<div style=\"border-bottom: 1px dashed black;width:auto\">\n" +formatDate(new Date(data.comment[index].createdDate))+
                    "我评论了问题 <a href=\"/question/"+data.question[index].id+"\" style=\"text-decoration:none\" >"+data.question[index].title+"</a>\n" +
                    "           <p>"+"评论内容："+data.comment[index].content+"</p>\n" +
                    "       </div>";
            }
            $("#js-home-feed-list").html(temp);
        }
    });
}

function guanzhuwenti(uid) {
    $.ajax({
        type: "POST",
        data: "uid="+uid,
        dataType: "JSON",
        async: false,
        url: "/getFollowQuerstionsByUserId",
        success: function(data) {
            var temp="<div style=\"font-size: 25px;font-family: 仿宋\">关注的问题</div>";

            for(var index=0;index<data.length;index++){
                //其实index 就是个索
                // temp+="问题id="+data[index].id+"问题标题："+data[index].title+"问题内容："+data[index].content
                temp+="<div style=\"border-bottom: 1px dashed black;width:auto\">\n" +formatDate(new Date(data[index].createdDate))+
                    "我关注了 <a href=\"/question/"+data[index].id+"\" style=\"text-decoration:none\" >"+data[index].title+"</a>\n" +

                    "</div>";
            }
            $("#js-home-feed-list").html(temp);
        }
    });
}
function guanzhuhaoyou(uid) {

}