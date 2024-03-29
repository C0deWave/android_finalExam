# android_finalExam
모바일 프로그래밍 기말 프로젝트 입니다.

3학년 1학기때 진행한 기말 대체 프로젝트입니다.

---

# 자격증의 민족

자격증을 얻는것을 도와주는 어플입니다.

구글 파이어베이스를 이용해서 일반 회원가입을 이용한 로그인과 페이스북 연동을 이용한 로그인 그리고 구글 연동을 이용한 로그인을 할 수 있습니다.

![스크린샷 2020-07-04 오후 5 44 17](https://user-images.githubusercontent.com/16849874/88369872-e73fe180-cdcb-11ea-9d48-5da0ee3d5d3b.png)

[https://firebase.google.com/docs/auth](https://firebase.google.com/docs/auth)
를 참조해서 로그인을 구현했습니다.

---

![스크린샷 2020-07-04 오후 5 45 16](https://user-images.githubusercontent.com/16849874/88370171-7816bd00-cdcc-11ea-9e31-6c679dbabb1a.png)

처음 로그인을 하면 나중에 어플을 종료하고 다시 들어가도 자동로그인이 되게 구현했습니다.

---

![스크린샷 2020-07-04 오후 5 46 29](https://user-images.githubusercontent.com/16849874/88370527-2a4e8480-cdcd-11ea-92e8-146f56687a51.png)

기본 메뉴는 캘린더, 운세보기, 게시판, 계정정보 관리로 bottom app bar를 만들었습니다.

만들때는 meow 네비게이션 라이브러리를 이용했습니다.

[https://c0dewave.github.io/docs/4-Android/018-meow%EB%84%A4%EB%B9%84%EA%B2%8C%EC%9D%B4%EC%85%98%EA%B5%AC%ED%98%84/](https://c0dewave.github.io/docs/4-Android/018-meow%EB%84%A4%EB%B9%84%EA%B2%8C%EC%9D%B4%EC%85%98%EA%B5%AC%ED%98%84/)참조 바랍니다.

처음 화면은 캘린더 화면입니다.

계정 설정할때 저장된 자격증의 대분류에 맞춰서 기사면 기사시험의 시험일정이 표시되게 만들었습니다.

또한 디데이기능을 넣어서 디데이를 파이어베이스 실시간 데이터베이스에 저장하고 이를 참조해서 다른 어플에서 로그인해도 보이도록 만들었습니다.

---

두번째 화면은 운세 확인 페이지입니다.

![스크린샷 2020-07-04 오후 5 28 02](https://user-images.githubusercontent.com/16849874/88370960-122b3500-cdce-11ea-9fab-11396b1a4d98.png)

scratch view를 이용해서 실제로 복권을 긁는 느낌을 주었습니다.

또한 손가락 애니메이션을 넣어서 직관적으로 긁어서 확인한다는 느낌을 주었습니다.

---

다음은 게시판 기능입니다.

![스크린샷 2020-07-04 오후 5 28 16](https://user-images.githubusercontent.com/16849874/88372101-4142a600-cdd0-11ea-85b5-69ba0e32ecf4.png)

게시판은 구글 파이어베이스의 스토리지 서비스를 연결해서 안드로이드 내의 이미지를 올릴수 있게 만들었습니다.

또한 stagered grid layout을 이용해서 게시글을 최신순이 위에서부터 보이게 만들었습니다.

게시글은 파이어베이스 실시간 데이터 베이스에 들어가지만 게시판의 crud중에 cr만 만들었습니다.

댓글 기능도 있습니다.

![스크린샷 2020-07-04 오후 5 48 36](https://user-images.githubusercontent.com/16849874/88372358-b01fff00-cdd0-11ea-9d0c-dacefd8aa981.png)

---

다음은 계정정보 관리창입니다.

![스크린샷 2020-07-04 오후 5 28 26](https://user-images.githubusercontent.com/16849874/88372403-ccbc3700-cdd0-11ea-8610-6f2a999e09e3.png)

회원 탈퇴를 하거나 자격증 정보를 변경거나 할수 있습니다.

로그아웃을 하면 자동 로그인이 안됩니다.

---

# 결론

해당 프로젝트로 모바일 프로그래밍 과목 A+를 받았습니다.
