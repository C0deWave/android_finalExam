package com.example.finalexam

//자격증 JSON을 파싱하기 위한 클래스 입니다.
//자격증 Api가 공공데이터 포털에 문의를 해도 잘 작동하지 않아 시간이 없어 우선 assert의 json으로 들어가 있습니다.

class Certification(val SERIESCD : Int,
                    val SERIESNM : String,
                    val JMCD : Int,
                    val JMFLDNM : String,
                    val OBLIGFLDCD : Int,
                    val OBLIGFLDNM : String,
                    val MDOBLIGFLDCD : Int,
                    val MDOBLIGFLDNM :String
                    ) {
}
