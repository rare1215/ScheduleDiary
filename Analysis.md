## Analysis

### [ Revision history ]

| Revision date | Version # | Description | Author |
| :--- | :---: | :--- | :--- |
| 2026.05.08 | 1.0 | First Writing | |
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |
| | | | |
  
### [ Contents ]
1. Introduction
2. Use case analysis
3. Domain analysis
4. Glossary
5. Reference

### 1. Introduction
1.1. Summary
  최근 Y2K 열풍으로 인해 이전에 유행했던 다이어리 꾸미기가 다시 부상하고 있다.
  이때 종이에 직접 오리고 붙여 꾸미는 아날로그의 매력도 있지만 이를 디지털로 할 수 있으면 어떨까 하는 생각이 들었다. 그 이유는 첫째, 다이어리를 꾸미기 위한 용품들의 자리 차지가 크다. 스티커는 물론이고 마스킹 테이프 등 기본적으로 여러 장의 인쇄물로 이루어져 있기에 부피 차지가 꽤 크다. 둘째, 관리가 힘들다. 앞서 말했듯 양이 많기에 내가 원하는 용품을 찾으려면 오랜 시간이 걸린다. 셋째, 자원 낭비. 다이어리를 꾸밀 때 쓰는 메모지 등은 대량으로 파는 경우가 대다수라 조금 쓰고 쓰지 않게 되어 결국 버리게 된다. 
  기존에도 수많은 다이어리 어플리케이션이 존재한다. 하지만 그들은 자유도가 떨어지고 사용에 어려움이 많다. 그래서 사용자들이 더욱 쉽게 접근하고 사용할 수 있게 되는 것이 목표이다.

1.2. Introduce “Schedule Diary”
 Schedule Diary는 자유로운 페이지에 스케줄과 일기를 채워갈 수 있는 기록 어플리케이션이다. 다이어리를 꾸미는 것을 즐기는 층 외에, 사무적으로 일정을 기록할 필요가 있는 사람들도 사용할 수 있게 자유도를 어느 정도 조절할 수 있는 것이 이 어플리케이션의 특징이다.
 자신의 일정을 효율적으로 관리하고, 추억을 스크랩해 보존하는 등 사람들의 기록 창구를 대신하는 역할로 Schedule Diary를 기획하게 되었다.
 <br><br>

 ### 2. Use case analysis
 2.1. Use Case Analysis

 2.2. Use Case Description <br>
### - Use case #1: Join
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 Schedule Diary의 기능을 사용하기 위해 최초로 시스템에 등록한다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|Schedule Diary가 실행되어 있어야 한다.|
|Trigger|회원 미등록으로 로그인 실패 시, 로그인 화면에서 회원가입 버튼을 눌렀을 때|
|Success Post Condition|Schedule Diary의 회원으로 등록되며 앱에서 제공하는 기능을 사용할 수 있다.|
|Failed Post Condition|Schedule Diary의 회원으로 등록되지 않고, 앱에서 제공하는 기능들을 사용할 수 없다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 회원가입 버튼을 누를 때 시작된다.|
|2|사용자 정보 입력 화면으로 이동한다.|
|3|사용자는 개인정보를 입력한다.|
|4|사용자는 등록 버튼을 누른다.|
|5|서버에서 제대로 된 정보가 입력되었는지 확인한다.|
|6|정보 확인에 성공하면 데이터베이스에 회원 정보를 등록한다.|
|7|회원가입 성공 메시지를 띄우고 로그인 화면으로 이동한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|5|5a. 입력한 정보가 잘못되었을 경우<br>5a.1. 입력한 정보가 잘못되었다고 팝업창으로 띄우고, 사용자가 정보를 다시 수정할 수 있게 정보입력창으로 돌아간다.<br>5a.2. 사용자는 정보 수정 후 등록 버튼을 누른다.<br>5b. 기존에 등록된 회원일 경우<br>5b.1. 이미 등록된 회원이라고 팝업창을 띄운다.<br>5b.2. 회원가입 창을 내리고 로그인 창으로 돌아간다.|

|Related Information||
| :--- | :---: |
|Performance|<=5 Seconds|
|Frequency|사용자 당 최초 등록 시 1회e|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #2: LOgin
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|Schedule Diary 기능의 사용과 이전에 작업한 데이터들에 접근하고자 사용자 정보를 확인하기 위해 사용한다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User, Administrator, Server| 
|Precondition|사용자가 Schedule Diary의 데이터베이스에 회원등록이 된 상태여야 한다.|
|Trigger|사용자가 로그인을 위해 아이디와 비밀번호를 입력하고 로그인 버튼을 눌렀을 때|
|Success Post Condition|Server의 회원 정보와 일치하여 로그인에 성공한다.|
|Failed Post Condition|Server의 회원 정보와 일치하지 않아 로그인에 실패한다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 Schedule Diary에 로그인할 때 시작된다.|
|2|사용자는 시작 화면에서 로그인 버튼을 누르고, 로그인 화면으로 이동한다.|
|3|사용자는 아이디와 비밀번호를 입력하고 확인 버튼을 누른다.|
|4|Server에 저장된 정보와 일치하는지 확인하고 등록된 회원이라면 로그인 성공 창을 띄운다.|
|5|메인 화면으로 돌아간다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|4|4a. 로그인 정보가 일치하지 않는다(비밀번호의 불일치)<br>4a.1. 아이디 혹은 비밀번호가 틀렸다는 팝업창을 띄운다.<br>4a.2. 비밀번호 입력창을 초기화한 로그인 창으로 돌아간다.<br>4b. 등록되지 않은 회원이다(아이디, 비밀번호 둘 다 불일치)<br>4b.1. 등록되지 않은 회원이라고 팝업창을 띄운다.<br>4b.2. 입력창을 초기화하고, 회원가입 창으로 이어지는 창을 띄운다.|

|Related Information||
| :--- | :---: |
|Performance|<=3 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #3: Logout
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 Schedule Diary 로그아웃을 희망할 때 사용한다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User, Administrator, Server| 
|Precondition|사용자가 Schedule Diary에 로그인된 상태여야 한다.|
|Trigger|사용자가 로그아웃 버튼을 눌렀을 때|
|Success Post Condition|로그아웃에 성공해 로그인되지 않은 상태의 메인 화면으로 이동한다.|
|Failed Post Condition|로그아웃에 실패해 로그인 상태가 유지된다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 Schedule Diary에서 로그아웃할 때 시작된다.|
|2|사용자가 로그아웃 버튼을 누른다.|
|3|로그아웃 여부를 묻는 창을 띄운다.|
|4|사용자가 로그아웃을 희망하면 로그아웃하고, 로그인이 되지 않은 메인 화면으로 이동한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|3|3a. 사용자가 로그아웃을 희망하지 않는다.<br>3a.1. 이전의 창으로 돌아가고 로그아웃하지 않는다.|
|4|4a. 사용자가 로그아웃 후 뒤로가기 버튼을 누른다.<br>4a.1. 권한이 일치하지 않아 접근할 수 없다는 창을 띄워, 사용자의 기록에 접근할 수 없게 한다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #4: Create new diary 
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 Schedule Diary에서 새로운 다이어리를 만들어 페이지를 생성할 수 있게 하기 위해 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|Schedule Diary에 로그인되어 있어야 한다.|
|Trigger|사용자가 새 다이어리 생성 버튼을 눌렀을 때|
|Success Post Condition|새 다이어리가 생성된다.|
|Failed Post Condition|새 다이어리가 생성되지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 새 다이어리 만들기 버튼을 눌렀을 때 시작된다.|
|2|다이어리 리스트에 새 다이어리를 추가한다.|
|3|다이어리에 접근, 수정할 수 있다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a. 새 다이어리가 생성되지 않는다.<br>2a.1. 다이어리가 생성되지 않았다는 창을 띄운다.<br>2a.2. 다이어리 리스트로 돌아간다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #5: Create new page
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|다이어리 내에서 사용자가 일정을 등록하거나 꾸밀 수 있는 페이지를 만들기위해 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|Schedule Diary에 로그인이 되어 있고, 페이지가 소속되기 위한 다이어리가 존재해야 한다.|
|Trigger|사용자가 새 페이지 생성 버튼을 눌렀을 때|
|Success Post Condition|선택된 다이어리 내에 새 페이지가 생성된다.|
|Failed Post Condition|선택된 다이어리 내에 새 페이지가 생성되지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 새 페이지 만들기 버튼을 눌렀을 때 시작된다.|
|2|선택된 다이어리의 어느 곳에 페이지를 생성할지 물어본다(맨 앞/ 맨 뒤/ 특정 페이지)|
|3|선택한 다이어리의 위치 내에 페이지를 생성한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|3|3a. 지정된 페이지 한도를 초과해 새 페이지가 생성되지 않는다.<br>3a.1. 페이지 한도를 초과해 페이지가 생성되지 않았다는 창을 띄운다.<br>3a.2. 다이어리 창으로 돌아간다.<br>3b. 새 페이지가 잘못된 곳에 생성된다(지정한 위치 외)<br>3b.1. 페이지가 잘못된 곳에 생성되었다는 창을 띄운다.<br>3b.2. 잘못 생성된 페이지의 삭제 여부를 묻는다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #6: Access Diary
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 작성한 다이어리와 그 페이지들에 접근할 수 있게 하기 위해 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|Schedule Diary에 로그인되어 있어야 한다.|
|Trigger|사용자가 다이어리 리스트 버튼을 누르고, 그 안의 다이어리를 눌렀을 때|
|Success Post Condition|사용자가 작성한 다이어리에 접근된다.|
|Failed Post Condition|사용자가 작성한 다이어리에 접근되지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 다이어리 리스트에서 다이어리를 눌렀을 때 시작된다.|
|2|다이어리가 열리고 그 속의 페이지 창이 열린다.|
|3|페이지를 확인하고 편집할 수 있다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a. 다이어리에 접근이 되지 않는다.<br>2a.1. 사용자에게 다이어리가 정상적으로 열리지 않았으니 다시 열겠는지 여부를 묻는 창을 띄운다.|

|Related Information||
| :--- | :---: |
|Performance|<=2 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #7: Background Edit
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|다이어리 페이지의 배경을 편집할 때 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|다이어리와 페이지가 생성되어 있어야 한다.|
|Trigger|사용자가 페이지 내의 배경 편집 버튼을 눌렀을 때|
|Success Post Condition|페이지 배경 편집 화면으로 이동되고 편집 가능하다.|
|Failed Post Condition|페이지 배경 편집 화면으로 이동하지 않고, 편집 불가능하다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 페이지 배경 편집 버튼을 눌렀을 때 시작된다.|
|2|페이지를 편집할 수 있는 창으로 이동한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a.1. 페이지 배경 편집 창으로 이동하지 않는다.<br>2a.1.1. 배경 편집 화면 이동 실패 메시지를 띄운다.|

|Related Information||
| :--- | :---: |
|Performance|<=2 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #8: Insert text, Object 
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 Schedule Diary에서 제공하는 기본 텍스트와 오브젝트를 페이지에 삽입할 때 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|다이어리와 페이지가 생성되어 있어야 한다.|
|Trigger|사용자가 텍스트, 오브젝트 삽입 버튼을 누를 때|
|Success Post Condition|페이지 내에 텍스트와 오브젝트가 나타난다.|
|Failed Post Condition|페이지 내에 텍스트와 오브젝트가 나타나지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 텍스트, 오브젝트 삽입 버튼을 누를 때 시작된다.|
|2|원하는 오브젝트와 텍스트 편집 창이 나타난다. |
|3|사용자가 오브젝트와 텍스트의 설정을 지정하고 삽입하기 버튼을 누르면 페이지 내에 텍스트, 오브젝트가 나타난다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|3|3a. 텍스트, 오브젝트가 페이지에 나타나지 않는다.<br>3a.1. 삽입 과정에서 발생한 문제점을 창으로 띄워주고 사용자가 다시 삽입을 시도할 수 있게 편집 창으로 돌아간다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #9: Insert Image
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 다이어리의 페이지 내에 원하는 이미지를 삽입할 때 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|다이어리와 페이지가 생성되어 있어야 한다.|
|Trigger|사용자가 이미지 삽입 버튼을 누른다.|
|Success Post Condition|페이지 내에 이미지가 나타난다.|
|Failed Post Condition|페이지 내에 이미지가 나타나지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 이미지 삽입 버튼을 눌렀을 때 시작된다.|
|2|사용자의 저장소로 이동한다.|
|3|사용자가 원하는 이미지를 고를 수 있게 하고, 선택한 이미지를 페이지 내에 삽입한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a. 사용자의 저장소로 연결되지 않는다.<br>2a.1. 사용자의 저장소와 연결되지 않았다는 메시지를 띄우고 페이지 편집 화면으로 돌아온다.|
|3|3a. 사용자가 원하는 이미지가 지정된 용량 크기(15Mbytes)를 초과한다.<br>3a.1. 지정된 용량을 초과한다는 메시지를 띄우고 페이지 편집 창으로 돌아간다.|

|Related Information||
| :--- | :---: |
|Performance|<=3 Seconds(이미지를 페이지에 불러오는데 걸리는 시간)|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #10: Undo
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 이전에 실행한 작업을 실행 취소할 때 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|직전에 수행한 작업이 존재해야 한다.|
|Trigger|사용자가 실행 취소 버튼을 눌렀을 때|
|Success Post Condition|직전에 수행한 작업이 실행 취소된다.|
|Failed Post Condition|직전에 수행한 작업이 실행 취소되지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 실행 취소 버튼을 누르면 시작된다.|
|2|직전에 수행한 작업을 취소한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a. 직전에 수행한 작업이 존재하지 않는다.<br>2a.1. 실행 취소할 작업이 없다는 메시지를 띄운다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #11: Redo
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 이전에 실행한 작업을 재실행할 때 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|실행 취소가 이뤄진 상태여야 한다.|
|Trigger|사용자가 재실행 버튼을 눌렀을 때|
|Success Post Condition|직전에 취소한 작업이 재실행된다.|
|Failed Post Condition|직전에 취소한 작업이 재실행되지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 재실행 버튼을 누르면 시작된다.|
|2|직전에 취소한 작업을 재실행한다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a. 재실행할 작업이 존재하지 않는다.<br>2a.1. 재실행할 작업이 없다는 메시지를 띄운다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #12: Object Edit 
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 페이지 내에 삽입한 요소를 편집하고 속성을 부여할 때 사용된다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User| 
|Precondition|페이지 내에 요소가 삽입되어 있어야 한다.|
|Trigger|사용자가 요소 편집 버튼을 눌렀을 때|
|Success Post Condition|요소가 정상적으로 편집되어 페이지 내에 나타난다.|
|Failed Post Condition|요소의 편집 내용이 제대로 반영되지 않거나, 페이지 내에 나타나지 않는다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 요소 편집 버튼을 누르면 시작된다.|
|2|사용자가 선택한 요소를 편집할 수 있는 창이 띄워진다.|
|3|사용자가 요소를 자유롭게 편집한다.|
|4|사용자가 확인 버튼을 누르면 그가 요소에 반영되어 페이지 내에 나타난다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|3|3a. 사용자가 선택한 속성을 요소에 적용할 수 없다.<br>3a.1. 해당 기능은 이 요소에 적용할 수 없다는 창을 띄우고 편집 화면으로 돌아온다.|
|4|4a. 사용자가 편집한 내용이 반영되지 않은 채 페이지에 나타난다.<br>4a.1. 요소 편집에 실패했다는 창을 띄우고 다시 요소 편집 창으로 돌아온다.|

|Related Information||
| :--- | :---: |
|Performance|<=1 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

### - Use case #13: Save
|GENERAL CHARACTERISTICS||
| :--- | :---: |
|Summary|사용자가 편집을 완료한 다이어리와 페이지를 저장할 때 사용한다.| 
|Scope|Schedule Diary|
|Level|User level|
|Author|Sung JiHyun| 
|Last Update|2026.05.08| 
|Status|Analysis| 
|Primary Actor|User, System| 
|Precondition|다이어리, 페이지가 편집된 상태여야 한다.|
|Trigger|사용자가 저장 버튼을 눌렀을 때|
|Success Post Condition|편집한 내용이 데이터베이스에 저장된다.|
|Failed Post Condition|편집한 내용이 저장되지 않고 초기화된다.|

|MAIN SUCCESS SCENARIO||
| :--- | :---: |
|Step|Action|
|1|사용자가 저장 버튼을 누르면 시작된다.|
|2|저장 중 메시지가 뜨며 저장된다.|
|3|저장이 완료되면 저장 성공 메시지를 띄운다.|

|EXTENSION SCENARIO||
| :--- | :---: |
|Step|Branching Action|
|2|2a. 저장에 실패하는 경우<br>2a.1. 저장에 실패했다는 메시지를 띄우고 편집을 하던 창으로 돌아간다.|

|Related Information||
| :--- | :---: |
|Performance|<=3 Seconds|
|Frequency|Variable|
|Concurrency|None|
|Due Date|2026.05.08|
<br><br>

 ### 3. Domain analysis
 <br><br>
 
 ### 4. Glossary
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
 
 ### 5. Reference
<br><br>
  
