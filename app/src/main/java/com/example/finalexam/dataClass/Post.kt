package com.example.finalexam.dataClass

class Post {
    /*글의 ID*/
    var postId=""

    /*글 작성자의 ID*/
    var writerId=""

    /*글의 제목*/
    var title=""

    /*글의 메세지*/
    var message=""

    /*글이 쓰여진 시간*/
    var writeTime:Any=Any()
    var descWriteTime:Any=Any()

    /*댓글의 개수*/
    var commentCount=0

    /*첨부한 이미지*/
    var bgUri = ""
}