RemoteView Android Agent
=========================

Android Agent는 RemoteView Viewer(PC(win),Android Viewer, IOS Viewer)에서 Android 단말을 제어하는 앱이다.
시작점은 RemoteView UI, RCMP의 데이터 채널, 모비즌의 화면캡쳐, 인코딩 코드를 합쳐 탄생한 앱이다.

PC Agent와 다르게 세션서버에 연결된 상태가 아니라, MQTT push 서버에 연결된 상태로 서버와 통신은 push 메세지를 이용한다.
(*Doze mode  대응으로 FCM도 들어가 있지만, 단말기 깨우기 용도로만 사용한다. 다른 동작처리는 아직 안함)


APP Info
--------

* packagename : com.rsupport.mobile.agent

* minSdk : 19
  1. SamsungPrinter : 9 (deprecate)
  2. HCI : 16
  3. Vuzix : 16

* module
  + app
  + engine_module
  + rsutil

* flavors
  - asp
  - samsungprinter (deprecate)
  - hci
  - japansoft
  - vuzix
  - zidoo

* Mous Server
  - Rsperm 체크 / 다운로드 lib
  - compile 'com.rsupport.android.mobizen.engine:installer:3.0.0.4'
  - MainActivity.class checkMouseServer() 확인


개발 문서
--------------
+ mqtt 패킷 정의서
 - https://docs.google.com/spreadsheets/d/1pHrZCtbeNH9czn-kpcgxfhvaDfOHE4oTvIUmV-7bu78/edit#gid=0

+ WebInterface 정의
 - https://docs.google.com/spreadsheets/d/1w27fUvllQtaCRmiG5zFZMHD4OSnlLQlakQqLrRnO_a4/edit#gid=985397478

+ 언어리소스 
 - https://docs.google.com/spreadsheets/d/1BkD_dwqVIhVt3YmTpWYfdAzZDbESpJ6KU4Uf9rXWFHI/edit

서버정보
------------
* 공인
  - rview.com
* ST Alpha
  - stap.rview.com
  - stapadm.rview.com (어드민 페이지)

Module
------

1. app
  - mqtt
  - UI
  - Channel
  - Web Query
  - etc
2. engine_module
  - ScreenCapture
  - encoding
  - engine binding
3. rsutiil
  - logger (engine_module에서 사용중. 바꾸고 지워버려도 됨.)

Capture
--------
1. Rsperm(rsperm 은 64비트를 로딩하지 못해서 32비트의 rspdk 를 사용한다.)
  * 4.4 이하 RsScreenCapture 
  * 5.0 이상 RsMediaProjectionStream (Rsperm 내의 createVirtualDisplay)
2. Knox
  * 4.4 RsKnoxStream
  * 5.0 이상 RsMediaProjectionStreamVD 
3. Sony

Encoding
-----------
* 기본 HW인코더를 사용하도록 되어있지만, 몇몇 단말에서 단말기 이슈로 인하여  SW인코더 사용
* SW Encoding 분기
  - 삼성프린터
  - hci 단말
  - japansoft 단말기.
  - Vuzix 안경단말
* EngineConfigSetting 클래스 isSoftEncoding 값으로 설정

Samsung KNOX
-------------
삼성 핸드폰 Rsperm 이 없으므로 Samsung 에서 제공해주는 API를 이용하여 캡쳐 / 제어를 함

- Samsung KNOX key 관리 문서
  + https://docs.google.com/spreadsheets/d/1vUYczJltsYl1I9v1Do55MkAiqREkl3XO6cJdlQuea3E/edit#gid=0
- SEAP  Page
  + https://seap.samsung.com/

- Lib
  * knoxsdk.jar
  * supportlib.jar
  * libscreencap.so (김선태 과장님이 만들어주신 모듈, 4.4 이하에서만 동작하여 64비트 필요 없음.)
  * libremotedesktop_client.so (삼성 knox 모듈)

- 프로세스 
  1. Knox 라이센스 활성화 (EnterpriseLicenseManager.class)
  2. 사용자 동의팝업 발생 (앱설치후 최초 한번) EnterpriseLicenseManager.activateLicense()
  3. 캡쳐는 Native 단에서 가능 (libscreencap.so)
  4. 제어는 RemoteInjection 을 통해서 이벤트 전달.

* initializeScreen(width, height) 으로 캡쳐 사이즈 변경시 터치좌표 화면도 변경되어, 보정해주어야 함.
* WebConnection.reqeustDeviceBlackList() knox 지원 안되는 단말 확인 (ex. Galaxy S3)

BuildServer
-----------
* Asp 빌드
  - http://mbuild.rsupport.com:8080/view/RV/job/RV_AGENT/
* QA 빌드
  - http://mbuild.rsupport.com:8080/view/RV/job/RV_AGENT_QA/
* Flavors
  - http://mbuild.rsupport.com:8080/view/RV/job/RV_AGENT_CUSTOM/

QA 배포
-------------
* 레드마인 일감 QA 요청 변경
* 개발자 체크리스트 작성
  - https://drive.google.com/drive/folders/0B3anuwnSYZn1cERHUG5LT0VRYjg
* 레드마인 자료실 APK 업로드
* stapadm.rview.com 
  - 시스템관리 -> 모바일 앱 업로드 (테스트용) -> 타입 -> Android_agent -> apk업로드
  - stapadm.rview.com/app 으로 apk 다운 확인

SamsungPrinter
--------------
삼성프린터용 apk
* 마켓 배포 아닌 Samsung에 직접 Apk 전달
* 프린터 3세대 모델이 api 9 모델이여서 minsdk 9
* SW인코더 사용
* 삼성프린터 서비스 모드 진입 프로토콜 (rcpMobile_SamsungPrinterServiceMode = 238)
* 부팅시 로그인 계속 시도하도록 코딩(BootComplet 수신 못함)


HCI
--------------
TV모델과 SetTopBox모델
* sw 인코딩 사용
* 5gen 모델 캡쳐 사이즈 크게 하도록 변경
* 마켓 배포 아닌 APK 직접 전달.
* TV특성상 ID PW 자동 입력/ DeviceID는 MAC Address로 설치되게 (AutoInstallInfo.class ID 저장)


DOZE MODE
----------
Doze mode 이슈
Android 6.0 부터 DozeMode 이슈 생김. 
Doze mode에 진입하면 mqtt와의 커넥션이 끊어짐.
Battery Optimization 등록으로 에외 할 수 있지만, Market에 등록된 app 이 아니라 리젝 당함

- 해결책
  * FCM priority high 로 메세지 수신시 전원 ON 키이벤트 날려 화면켜지면서 DozeMode 깨어남

- 예외상황
  * 삼성 7.0 부터 DozeMode 제조사 커스텀으로 PowerManager.isDeviceIdleMode()로 상태 확인 못함.
  * Mqtt 연결후 conectLost 발생시 Doze 모드로 간주한후 단말기 활성화후 재연결 하도룩 수정.
  
  
StringResource
---------------
구글 SpreadSheet 의 언어리소스를 다운받아 Android String resource 로 변환해준다.
구글 SpreadSheet의 구글드라이브에 접근하여 파일을 다운로드 하기 위해서는 credentials.json 파일과 SpreadSheet FieldID 가 필요하다. 

credentials.json 다운로드 
> https://developers.google.com/drive/api/v3/quickstart/java 에서 다운로드 가능

SpreadSheet FieldId - 10GZzmr02UDHAPwRDfYR8EdZD0kQOV6B8SJ7F2DiUTlQ
> https://docs.google.com/spreadsheets/d/10GZzmr02UDHAPwRDfYR8EdZD0kQOV6B8SJ7F2DiUTlQ/edit#gid=1955937470 

실행 
> updateStringResource Task 를 실행하면 app module 에 res 폴더에 언어리소스를 xml 형식으로 다운로드한다. 