**Ondaman Cordova Plugin**
=================================
&nbsp;

# **Contents**
**[1. Auth](#1-Auth)**
- [1) getUserInfo](#1-getUserInfo)

**[2. Barcode](#2-Barcode)**
- [1) openBarcodeScanner](#1-openBarcodeScanner)

**[3. Camera](#3-Camera)**
- [1) openCamera](#1-openCamera)

**[4. Location](#3-Location)**
- [1) startLocationService](#1-startLocationService)
- [2) stopLocationService](#2-stopLocationService)

**[5. Navigation](#5-Navigation)**
- [1) finishApp](#1-finishApp)
- [2) logout](#2-logout)

**[6. NFC](#6-NFC)**
- [1) showNFCTagView](#1-showNFCTagView)

**[7. SignPad](#7-SignPad)**
- [1) startSignPad](#1-startSignPad)
&nbsp;





# **1. Auth**

## **1) getUserInfo**
> 로그인 된 사용자 정보 조회

### **Function**
````javascript
cordova.plugins.Auth.getLoginInfo(success, error)
````
### **Callback-Success**
````json
{
	"code": 1000,
	"data": {
		"userData": {
			"email": "gdhong@aiblab.co.kr",
			"name": "홍길동",
			"phone": "010-1234-5678",
			"userid": "gdhong"
		}
	},
	"message": "Success"
}
````
### **Callback-Failure**
````json
{
  "code": "3000",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Success | 성공 |
| 3000 | UserData is empty! | 로그인 오류 |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;





# **2. Barcode**

## **1) openBarcodeScanner**
> 바코드 스캐너 실행
<br> - QR code 스캐닝

### **Function**
````javascript
cordova.plugins.Barcode.openBarcodeScanner(success, error)
````
### **Callback-Success**
````json
{
	"code": 1000,
	"data": {
		"barcode": "12345678990"
	},
	"message": "Success"
}
````
### **Callback-Failure**
````json
{
  "code": "2000",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Success | 성공 |
| 4300 | barcode scanning failed. | 바코드 스캐닝 취소 또는 실패 |
| 4301 | barcode result is null. | 바코드 스캔 결과가 Null |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;





# **3. Camera**

## **1) openCamera**
> Push 토큰 가져오기

### **Function**
````javascript
cordova.plugins.Camera.openCamera(success, error)
````
### **Callback-Success**
````json
{
	"code": 1000,
	"data": {
		"data": "data:image/jpeg;base64,/9j/4AAQSk..." // Base64
	},
	"message": "Success"
}
````
### **Callback-Failure**
````json
{
  "code": "4200",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Success | 성공 |
| 4200 | Camera permission is not granted. | 사진 촬영 권한 없음 |
| 4201 | Image content uri is null. | 사진 촬영 결과(Uri) 가 Null |
| 4202 | Image capture has been canceled. | 사진 촬영 취소 |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;





# **4. Location**

## **1) startLocationService**
> 위치 정보 조회 및 서버 전송 요청(10분단위) 서비스 시작

### **Function**
````javascript
cordova.plugins.Location.startLocationService(success, error)
````
### **Callback-Success**
````json
{
	"code": 1000,
	"message": "Start Location service successfully."
}
````
### **Callback-Failure**
````json
{
  "code": "4300",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Start Location service successfully. | 성공 |
| 4300 | Location permission is not granted. | 위치 정보 수집 권한 없음 |
| 4301 | UserId is not exist. | 서버전송을 위한 사용자 정보가 없음 |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;


## **2) stopLocationService**
> 위치 정보 조회 및 서버 전송 요청 서비스 종료

### **Function**
````javascript
cordova.plugins.Location.stopLocationService(success, error)
````
### **Callback-Success**
````json
{
	"code": 1000,
	"message": "Stop Location service successfully."
}
````
### **Callback-Failure**
````json
{
  "code": "4300",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Stop Location service successfully. | 성공 |
| 4300 | Location permission is not granted. | 위치 정보 수집 권한 없음 |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;




# **5. Navigation**

## **1) finishApp**
> 앱 종료

### **Function**
````javascript
cordova.plugins.Navigation.finishApp()
````
&nbsp;

## **2) logout**
> 로그아웃
<br> - 로그아웃 후 로그인 화면으로 이동

### **Function**
````javascript
cordova.plugins.Navigation.logout()
````
&nbsp;





# **6. NFC**

## **1) showNFCTagView**
> NFC 태그 화면 호출
<br> - NFC logs 데이터 읽기
<br> - 처음 읽을 경우 

### **Function**
````javascript
/*
    tagType
      - 태그 데이터 읽기 : "read_logs_data"
    currentPageIndex
      - 현재 읽을 페이지 index, 처음 읽는 경우 1을 전달
    totalPageCount
      - 태그 logs 데이터 전체 페이지 count, 처음 읽는 경우 전달하지 않음
*/
/* 처음 읽기
   Example: cordova.plugins.NFC.showNFCTagView(success, error, "read_logs_data", 1)
*/
cordova.plugins.NFC.showNFCTagView(success, error, tagType, currentPageIndex)


/* 추가 데이터 페이지 읽기
   Example: cordova.plugins.NFC.showNFCTagView(success, error, "read_logs_data", 2, 2)
*/
cordova.plugins.NFC.showNFCTagView(success, error, tagType, currentPageIndex, totalPageCount)
````

### **Callback-Success**
````json
{
	"code": 1000,
	"data": {
		"tagData": {
			"hasNext": true, // 다음 읽을 페이지가 있다면 true, 없다면 false
			"currentPageIndex": 1, // 읽은 페이지 index
			"totalPageCount": 2, // 전체 페이지 count
			"logs": [{
				"dateTime": "2020-09-28 15:00:37",
				"eventCode": "N",
				"temperature": 27.6
			}, {
				"dateTime": "2020-09-28 15:00:38",
				"eventCode": "N",
				"temperature": 26.2
			}]
		}
	},
	"message": "Success"
}

{
    // Log data object description
    "dateTime": "2020-09-28 15:00:37", // 시간
    /*
        NORMAL("N"),
        TEMPERATURE_START("TS"),
        TEMPERATURE_END("TE"),
        NFC_COMMUNICATION("NC")
    */
    "eventCode": "N", 
    "temperature": 27.6 // 온도(섭씨)
}

````
### **Callback-Failure**
````json
{
  "code": "4800",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Success | 성공 |
| 4800 | Canceled | 태그 취소 |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;





# **7. SingPad**

## **1) startSignPad**
> 서명 화면 호출

### **Function**
````javascript
cordova.plugins.SignPad.startSignPad(success, error)
````
### **Callback-Success**
````json
{
	"code": 1000,
	"data": {
		"signData": "data:image/jpeg;base64,/9j/4AAQSk..." // Base64
	},
	"message": "Success"
}
````
### **Callback-Failure**
````json
{
  "code": "4601",
  "message": "오류가 발생했습니다."
}
````
#### Error Code
| code | message | comment | 
| ------ | :------ | :------ |
| 1000 | Success | 성공 |
| 4601 | Canceled | 서명 취소 |
| 4602 | Not signed | 서명 데이터 없음 |
| 8001 | Unknown plug in is executed. [{action}]" | 정의되지 않은 action 으로 플러그인을 호출 |
&nbsp;
