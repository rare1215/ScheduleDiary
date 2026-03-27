## Conceptualization

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
1. Business purpose <br>
2. System context diagram <br>
3. Use case list <br>
4. Concept of operation <br>
5. Problem statement <br>
6. Glossary <br>
7. Reference <br><br>

### 1. Business purpose
&nbsp; 최근 Y2K 열풍으로 인해 이전에 유행했던 다이어리 꾸미기가 다시 알음알음 부상하고 있다. 이는 주로 젊은 여성층을 중심으로 종이에 스티커나 마스킹 테이프 등을 붙이며 꾸미고 SNS에 올려 공유하는 것으로 자주 볼 수 있다. <br>
&nbsp; 이때 종이에 직접 오리고 붙여 꾸미는 아날로그의 매력도 있지만 이를 디지털로 할 수 있으면 어떨까하는 생각이 들었다. 그 이유는 첫째, 다이어리를 꾸미기 위한 용품들의 자리 차지가 크다. 스티커는 물론이고 마스킹 테이프 등 기본적으로 여러 장의 인쇄물로 이루어져 있기에 부피 차지가 꽤 크다. 둘째, 관리가 힘들다. 앞서 말했듯 양이 많기에 내가 원하는 용품을 찾으려면 오랜 시간이 걸린다. 셋째, 자원 낭비. 다이어리를 꾸밀 때 쓰는 메모지 등은 대량으로 파는 경우가 대다수라 조금 쓰고 쓰지 않게 되어 결국 버리게 된다. 이는 자원과 돈 둘 다 낭비된다고 볼 수 있다. <br>
&nbsp; 기존에도 수많은 다이어리 어플리케이션이 존재한다. 하지만 그들은 꾸밈의 자유도가 너무 낮고, 이미지의 첨부도 어렵다. 그래서 조금 더 자유도를 높인 프로그램을 만들어 다이어리를 쓰고 꾸밀 수 있도록 하면 사용자들이 만족감을 느낄 수 있을 것이라 예측한다. <br>

### 2. System context diagram

### 3. Use case list
1) 회원가입

|||
| :--- | :--- |
| Actor | Users |
| Description | 유저가 시스템을 사용하기 위해 회원 가입한다. |

2) 로그인

|||
| :--- | :--- |
| Actor | Users |
| Description | 사용자가 자신의 아이디와 비밀번호로 로그인한다. 시스템은 데이터베이스와 정보를 비교하고 일치한다면 로그인을 허용한다. |

3) 로그아웃

|||
| :--- | :--- |
| Actor | Users |
| Description | 로그아웃한다. |

4) 새 다이어리 생성

|||
| :--- | :--- |
| Actor | Users |
| Description | 다이어리 페이지들을 모아 관리할 다이어리를 만든다. |

5) 새 다이어리 페이지 생성

|||
| :--- | :--- |
| Actor | Users |
| Description | 자유롭게 꾸밀 수 있는 다이어리 페이지를 만든다. |

6) 이전 다이어리 조회

|||
| :--- | :--- |
| Actor | Users |
| Description | 이전에 만든 다이어리 페이지들로 이동한다. 미리보기 창이 제공되고, 원하는 페이지를 선택해 해당 페이지로 이동한다. |

7) 배경 편짐

|||
| :--- | :--- |
| Actor | Users |
| Description | 다이어리 페이지의 배경을 편집한다. |

8) 텍스트 입력

|||
| :--- | :--- |
| Actor | Users |
| Description | 페이지에 텍스트를 삽입해서 자유롭게 편집한다. |

9) 오브젝트 삽입

| :--- | :--- |
| Actor | Users |
| Description | 페이지에 사각형이나 원 등 기본 오브젝트 도형들을 삽입할 수 있게 한다. |

10) 이미지 삽입

|||
| :--- | :--- |
| Actor | Users |
| Description | 페이지에 이미지를 삽입할 수 있게 한다. 크기와 각도 조절도 자유롭게 가능하다. |

11) 요소 정렬

|||
| :--- | :--- |
| Actor | Users |
| Description | 선택한 요소들을 정렬할 수 있게 한다. 중앙정렬, 좌우정렬 |

12) 뒤로 돌리기

|||
| :--- | :--- |
| Actor | Users |
| Description | 직전에 실행한 작업을 취소한다. |

13) 다시 실행

|||
| :--- | :--- |
| Actor | Users |
| Description | 실행 취소한 작업을 다시 실행한다. |

14) 테두리 넣기

|||
| :--- | :--- |
| Actor | Users |
| Description | 페이지에 삽입된 요소에 테두리를 넣는다. 테두리의 두께를 자유롭게 지정할 수있다. |

15) 저장하기

|||
| :--- | :--- |
| Actor | Users |
| Description | 페이지를 테이터베이스에 저장한다. |

### 4. Concept of operation
1) 회원가입

|||
| :--- | :--- |
| Purpose | 사용자, 관리자가 시스템에 등록되어 있는지 확인 후 시스템을 이용할 수 있어야 한다. |
| Approach | 생성된 아이디와 비밀번호가 없을 시에 회원가입을 할 수 있게 한다. |
| Dynamics | 어플리케이션을 최초 실행해 계정이 없는 경우. |
| Goals | 로그인을 완료한 사용자들이 권한에 따라 시스템을 이용할 수 있게 한다. |

2) 로그인

|||
| :--- | :--- |
| Purpose | 사용자마다 다이어리 페이지가 분리되기에 개별 사용자를 구분할 수 있어야 한다. |
| Approach | 어플리케이션 실행 시에 아이디와 비밀번호를 받아 로그인을 할 수 있게 한다.  |
| Dynamics | 앱을 실행했을 경우 |
| Goals |  |

3) 로그아웃

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 로그아웃을 하고싶을 경우 |
| Goals |  |

4) 새 다이어리 생성

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 새 다이어리를 만들고 싶을 경우 |
| Goals |  |

5) 새 다이어리 페이지 생성

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 새 페이지를 만들고 싶을 경우 |
| Goals |  |

6) 이전 다이어리 조회

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 이전 다이어리를 보고싶을 경우 |
| Goals |  |

7) 배경 편집

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 배경을 편집하고 싶을 경우 |
| Goals |  |

8) 텍스트 입력

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 페이지에 텍스트를 입력하는 경우 |
| Goals |  |

9) 오브젝트 삽입

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 페이지에 오브젝트를 삽입하는 경우 |
| Goals |  |

10) 이미지 삽입

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 페이지에 이미지를 삽입하는 경우 |
| Goals |  |

11) 요소 정렬

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 특정 요소들을 일정한 규칙에 따라 정렬하는 경우 |
| Goals |  |

12) 뒤로 돌리기

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 이전의 작업을 실행 취소하는 경우 |
| Goals |  |

13) 다시 실행

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 실행 취소한 작업을 다시 복구하는 경우 |
| Goals |  |

14) 테두리 넣기

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 페이지에 삽입한 요소에 테두리를 넣고싶은 경우 |
| Goals |  |

15)

|||
| :--- | :--- |
| Purpose |  |
| Approach |  |
| Dynamics | 페이지를 저장하는 경우 |
| Goals |  |

### 5. Problem statemen

### 6. Glossary

### 7. Reference

