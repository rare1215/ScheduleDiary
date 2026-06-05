## Deisign

### [ Revision history ]

| Revision date | Version # | Description | Author |
| :--- | :---: | :--- | :--- |
| 2026.06.05 | 1.0 | First Writing | |
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |
  
### [ Contents ]
1. Introduction<br>
2. Class diagram<br>
3. Sequence diagram<br>
4. State machine diagram<br>
5. Implementation requirements<br>
6. Glossary<br>
7. Reference
<br><br>


### 1. Introduction
  최근 Y2K 열풍으로 인해 이전에 유행했던 다이어리 꾸미기가 다시 알음알음 부상하고 있다. 이는 주로 젊은 여성층을 중심으로 종이에 스티커나 마스킹 테이프 등을 붙이며 꾸미고 SNS에 올려 공유하는 것으로 자주 볼 수 있다.
  이때 종이에 직접 오리고 붙여 꾸미는 아날로그의 매력도 있지만 이를 디지털로 할 수 있으면 어떨까하는 생각이 들었다. 그 이유는 첫째, 다이어리를 꾸미기 위한 용품들의 자리 차지가 크다. 스티커는 물론이고 마스킹 테이프 등 기본적으로 여러 장의 인쇄물로 이루어져 있기에 부피 차지가 꽤 크다. 둘째, 관리가 힘들다. 앞서 말했듯 양이 많기에 내가 원하는 용품을 찾으려면 오랜 시간이 걸린다. 셋째, 자원 낭비. 다이어리를 꾸밀 때 쓰는 메모지 등은 대량으로 파는 경우가 대다수라 조금 쓰고 쓰지 않게 되어 결국 버리게 된다. 이는 자원과 돈 둘 다 낭비된다고 볼 수 있다.
  기존에도 수많은 다이어리 어플리케이션이 존재한다. 하지만 그들은 꾸밈의 자유도가 너무 낮고, 이미지의 첨부도 어렵다. 그래서 조금 더 자유도를 높인 프로그램을 만들어 다이어리를 쓰고 꾸밀 수 있도록 하면 사용자들이 만족감을 느낄 수 있을 것이라 예측한다.<br><br>


### 2. Class diagram
<img width="803" height="851" alt="image" src="https://github.com/user-attachments/assets/14063414-737e-4211-8f62-7e0d12e22701" />
<br>
< class diagram >

### 1. Registration
신규 회원 등록 시 사용되는 클래스
1) Attiributes
  - idField:JTextField - 사용자가 입력한 아이디
  - pwField:JPasswordField - 사용자가 입력한 패스워드
  - nameField:JTextField - 사용자가 입력한 이름
  - emailField:JTextField - 사용자가 입력한 이메일
  - submitBtn:JButton - 회원가입 버튼
  - cancelBtn:JButton - 회원가입 취소 버튼
2) Methods
   - clickSubmit():void — 입력된 정보를 바탕으로 EventController에 가입 요청하는 메소드
   - clickCancel():void — 회원가입을 취소하고 다시 로그인 화면으로 이동하는 메소드

### 2. Login
시스템 실행 후 로그인 시 사용되는 클래스
1) Attiributes
  - idField:JTextField - 사용자가 입력한 아이디
  - pwField:JPasswordField - 사용자가 입력한 패스워드
2) Methods
   - loginCheck(idField:JTextField, pwField: JPasswordField):boolean - 사용자가 입력한 아이디와 패스워드가 데이터베이스에 있고, 그와 일치하는지 확인하는 메소드

### 3. Interface
사용자에게 UI 화면을 띄워주는 클래스
1) Attiributes
   - currentScreen:String - 현재 활성화된 화면의 이름(Login, Registration, Main, Edit 등)
2) Methods
   - renderLoginScreen() - 로그인 화면을 출력하는 메소드
   - renderMainScreen(diaryList) - 다이어리들의 리스트를 띄워주는 메인 화면을 출력하는 메소드
   - renderPageEditScreen(page): void - 페이지 편집 화면을 출력하는 메소드
   - renderRegistrationScreen(): void - 회원가입 입력 폼 화면을 출력하는 메소드
   - showPopup(message): void  - 용량 초과, 중복된 ID입니다, 회원가입 완료 등의 팝업창을 출력하는 메소드

### 4. EventController
UI에서 발생하는 모든 이벤트(입력, 클릭)를 감지하여 그에 맞는 결과를 실핸하는 클래스
1) Methods
   - handleLoginSubmit(id:String, pw:String) - 로그인 이벤트를 제어하는 메소드
   - handleLogoutRequest():void - 로그아웃 이벤트를 제어하는 베소드
   - handleRegisterRoute():void - 로그인 화면에서 회원가입 화면으로 이동하는 버튼 이벤트의 트리거
   - handleRegistrationSubmit(id, pw, name, email): void - 회원가입 창에서 입력받은 사용자 정보를 받아 등록을 진행하는 메소드
   - handleCreateDiary():void - 새 다이어리를 생성하는 메소드
   - handleCreatePage(position):void - 새 페이지를 생성하는 메소드
   - handleInsertElement(type) - 텍스트, 오브젝트, 이미지 삽입의 이벤트 트리거
   - handleEditElement(elementId):void - 텍스트, 오브젝트, 이미지 편집의 이벤트 트리거
   - handleUndo():void - 뒤로 돌리기 버튼 핸들링
   - handleRedo(): void - 다시 실행 버튼 핸들링
   - handleSave(): void - 저장 버튼 클릭 시 DataController로 데이터 전달하는 메소드

### 5. DataController
사용자가 입력한 모든 데이터를 저장, 관리, 로드하는 클래스
1) Attiributes
   - dbConnection:Object - 데이터베이스에 연결하는 객체
   - storageStatus:Object - 현재 사용자의 저장소 용량 상태 및 경로 관리
2) Methods
   - registerNewUser(id, pw, name, email):boolean - DB 혹은 텍스트 파일 저장소에 신규 회원 정보 등록 및 전용 다이어리 공간을 동적으로 할당하는 메소드
   - savePageData(userId, pageData):boolean - 완성된 페이지를 DB에 저장하는 메소드
   - loadDiaryList(userId): List - 메인 화면 진입 시 해당 사용자의 다이어리 목록을 리스트로 불러오는 메소드
   - loadPageData(diaryId, pageId):AdPage - 저장된 페이지 데이터를 로드하는 메소드
   - isolateUserData(userId):void - 사용자 개개인의 일기 데이터가 섞이지 않도록 저장 공간을 격리하는 메소드

### 6. ObjectController
텍스트, 오브젝트, 이미지 등 페이지 내에 작성되는 개체들의 속성 변경 및 정렬, 실행 취소 기능을 담당하는 클래스
1) Attiributes
   - undoStack:Stack - 뒤로 돌리기(Undo)를 위한 직전 작업 이력 스택
   - redoStack:Stack - 다시 실행(Redo)을 위한 취소된 작업 이력 스택
2) Methods
   - createElement(type):Object - 기본 오브젝트 및 빈 텍스트 박스 객체를 생성하는 메소드
   - modifyElement(elementId, attributes):void - 요소의 크기, 각도, 색상, 두께, 폰트 정보 수정하는 메소드
   - alignElements(elements, alignmentType):void - 선택된 요소들을 규칙(중앙, 좌측, 우측)에 맞춰 자동 정렬하는 메소드
   - applyBorder(elementId, thickness):void - 선택한 요소 바깥에 지정한 두께의 테두리를 생성하는 메소드
   - undo():void - 작업 기록을 한 단계씩 실행 취소하는 메소드
   - redo():void - 작업 기록을 한 단계씩 재실행하는 메소드

### 7. ImageLoader
사용자의 로컬 저장소로부터 이미지를 불러오고, 이미지의 용량 체크, 관리 및 최적화를 하는 클래스
1) Attiributes
   - maxImageSize:long = 15,728,640(15Mbytes) - 파일 최대 허용 용량 제한 변수
2) Methods
   - browseLocalStorage():String - 모바일 디바이스 파일 탭/갤러리 연동 후 선택된 파일 경로를 반환하는 메소드
   - validateImageSize(filePath):boolean - 파일이 15MB 이하인지 검증하여 초과 시 예외 처리를 하는 트리거 메소드
   - optimizeImage(filePath):Object - 시스템에 무리가 가는 이미지 작업를 경량화하여 앱이 정지하는 현상을 방지하는 메소드
   - uploadImageToDB(image):String - 최적화된 이미지 데이터를 저장소에 보관하는 메소드

### 8. AdDiary
다이어리, 페이지를 묶어 관리하는 클래스
1) Attiributes
   - diaryId:String - 다이어리 고유 식별자
   - diaryName:String - 다이어리 제목
   - ownerId:String - 사용자의 ID
   - pages:List<AdPage> - 다이어리에 속한 페이지들의 리스트
2) Methods
   - addPage(page, position):void - 다이어리 내 지정된 위치(맨 앞, 맨 뒤, 특정 페이지 사이)에 새 페이지 삽입하는 메소드
   - removePage(pageId):void - 특정 페이지를 제거하는 메소드
   - getPageList():List - 다이어리 내 전체 페이지 리스트를 획득하는 메소드
  
### 9. EditPage
사용자가 텍스트, 오브젝트, 이미지를 올리고 배경을 편집하는 실질적인 작업 영역이 되는 클래스
1) Attiributes
   - pageId:String - 페이지 고유 식별자
   - diaryId:String - 소속된 다이어리 ID
   - pageNumber:int - 다이어리 내 순번
   - backgroundStyle:Map - 배경 이미지 및 배경 설정 정보
   - elementsList:List - 페이지 내에 삽입된 모든 요소(텍스트박스, 도형, 이미지 객체)들의 리스트
2) Methods
   - setBackground(bgData):void - 배경 이미지 편집 및 적용
   - addElement(element):void - 요소 삽입 시 페이지 삽입 요소 리스트에 추가하는 메소드
   - updateElement(elementId, updatedData):void - 요소 업데이트 시 페이지 삽입 요소 리스트를 업데이트 하는 메소드
   - removeElement(elementId):void - 요소 삭제 시 페이지 삽입 요소 리스트에서 제거하는 메소드
<br><br>

   
### 3. Sequence diagram
1) Registration
<img width="1043" height="728" alt="image" src="https://github.com/user-attachments/assets/6a9c34e7-b673-476f-ac3b-69fb55f85885" />



### 4. State machine diagram
<img width="1759" height="701" alt="image" src="https://github.com/user-attachments/assets/032d8c26-ab56-4578-a03c-553cb196496b" />
 회원가입 후 로그인을 하면 앱 사용 권한을 얻게 된다. 권한을 얻은 사용자는 자유롭게 본인의 영역에 다이어리와 페이지를 생성하고, 수정, 편집, 저장을 할 수 있다. 사용자가 작성한 항목들은 데이터베이스에서 관리되며 이전에 작성한 기록들도 열람할 수 있다. 서버측은 이 일련의 과정을 관리 감독하고, 사용자의 등록 정보 또한 관리한다(사용자 등록, 조회, 삭제 등)

### 5. Implementation requirements
1) Hardware Requirements
   - CPU: Intel Core i3 이상
   - RAM: 8 GByte 이상
   - HDD/SDD: 5 GByte 이상
2) Software Requirements
   - Windows 10 이상
   - Implementation Language: Java (Version 11 이상)


 ### 6. Glossary
 | 용어 | 설명 |
| :--- | :--- |
| Schedule Diary | 프로젝트로 만들어지는 앱의 이름 |
| 사용자 | 프로그램을 실질적으로 이용하는 사람들 |
| 데이터베이스, 저장소 | 정보를 저장하고 관리하는 곳 |
| 어플리케이션, 앱 | 사용자들이 실제 사용하는 응용 프로그램 |
| 최적화 | 앱 구동을 효율적인 방식으로 만드는 것 |
| 다이어리 | 사용자가 작업한 페이지들을 묶는 폴더의 개념 |
| 페이지 | 사용자가 앱에서 작업할 수 있는 작업 영역 |
| UI | 사용자 인터페이스의 약자. 사용자가 보는 화면 |
<br><br>


### 7. Reference
