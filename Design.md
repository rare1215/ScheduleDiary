## Deisign

### [ Revision history ]

| Revision date | Version # | Description | Author |
| :--- | :---: | :--- | :--- |
| | 1.0 | First Writing | |
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
7. Reference<br><br>

### 1. Introduction
  최근 Y2K 열풍으로 인해 이전에 유행했던 다이어리 꾸미기가 다시 알음알음 부상하고 있다. 이는 주로 젊은 여성층을 중심으로 종이에 스티커나 마스킹 테이프 등을 붙이며 꾸미고 SNS에 올려 공유하는 것으로 자주 볼 수 있다.
  이때 종이에 직접 오리고 붙여 꾸미는 아날로그의 매력도 있지만 이를 디지털로 할 수 있으면 어떨까하는 생각이 들었다. 그 이유는 첫째, 다이어리를 꾸미기 위한 용품들의 자리 차지가 크다. 스티커는 물론이고 마스킹 테이프 등 기본적으로 여러 장의 인쇄물로 이루어져 있기에 부피 차지가 꽤 크다. 둘째, 관리가 힘들다. 앞서 말했듯 양이 많기에 내가 원하는 용품을 찾으려면 오랜 시간이 걸린다. 셋째, 자원 낭비. 다이어리를 꾸밀 때 쓰는 메모지 등은 대량으로 파는 경우가 대다수라 조금 쓰고 쓰지 않게 되어 결국 버리게 된다. 이는 자원과 돈 둘 다 낭비된다고 볼 수 있다.
  기존에도 수많은 다이어리 어플리케이션이 존재한다. 하지만 그들은 꾸밈의 자유도가 너무 낮고, 이미지의 첨부도 어렵다. 그래서 조금 더 자유도를 높인 프로그램을 만들어 다이어리를 쓰고 꾸밀 수 있도록 하면 사용자들이 만족감을 느낄 수 있을 것이라 예측한다.<br><br>

### 2. Class diagram
1. Registration
신규 회원 등록 시 사용되는 클래스
1) Attiributes
  - idField:JTextField
  - pwField:JPasswordField
  - nameField:JTextField
  - emailField:JTextField
  - submitBtn:JButton
  - cancelBtn:JButton
2) Methods
   - clickSubmit(): void — 입력된 정보를 바탕으로 EventController에 가입 요청하는 메소드
   - clickCancel(): void — 회원가입을 취소하고 다시 로그인 화면으로 이동하는 메소드

2. Login
시스템 실행 후 로그인 시 사용되는 클래스
1) Attiributes
  - idField:JTextField
  - pwField:JPasswordField
2) Methods
   - loginCheck(idField:JTextField, pwField: JPasswordField): boolean
   - GoRegister(): void - 로그인 화면에서 회원가입 화면으로 이동하는 메소드

3. Interface
사용자에게 UI 화면을 띄워주는 클래스
1) Attiributes
   - currentScreen:String — 현재 활성화된 화면 이름(Login, Registration, Main, Edit 등)
2) Methods
   - renderLoginScreen() - 로그인 화면 출력
   - renderMainScreen(diaryList) - 다이어리들의 리스트를 띄워주는 메인 화면을 출력하는 메소드
   - renderPageEditScreen(page): void — 페이지 편집 화면을 출력하는 메소드
   - renderRegistrationScreen(): void — 회원가입 입력 폼 화면을 화면에 렌더링하는 메소드
   - showPopup(message): void  "용량 초과", "중복된 ID입니다", "회원가입 완료" 등 팝업창을 출력하는 메소

3. EventController
UI에서 발생하는 모든 이벤트(입력, 클릭)를 감지하여 그에 맞는 결과를 실핸하는 클래스
1) Attiributes
   -
   -
2) Methods
   -
   -

3. Interface
사용자에게 UI 화면을 띄워주는 클래스
1) Attiributes
   -
   -
2) Methods
   -
   -
### 3. Sequence diagram


### 4. State machine diagram


### 5. Implementation requirements


### 6. Glossary


### 7. Reference
