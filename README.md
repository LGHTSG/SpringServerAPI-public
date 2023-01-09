# SpringServerAPI
### 라고할때살걸 API입니다

----
### 1. 사용법
로컬에 받으신 후 제일 먼저 하셔야 할 것은
1. .gitignore_REMOVE에서 _REMOVE 삭제
2. 각자 브랜치 개설하여 해당 브랜치로 이동, 커밋
> git checkout -b myBranch1(브랜치이름)

- 이에 대한 이유는 아래에서 다룹니다.

src/main/java/com/lghtsg/api 에 각 파트별 디렉토리를 구분해 두었습니다.
해당하는 파트의 디렉토리에서 개발을 진행해주시면 되겠습니다.

테스트 서버에는 여러분들이 올려주신 코드를 기반으로 제가 로컬에서 빌드하고, EC2 서버에 올려 프론트와 협업할 수 있도록 할 계획입니다.
즉 여러분은 서버는 로컬에서, DB는 제가 application.yml 파일에 적어둔 endpoint로 사용해주시면 되겠습니다.

데이터 베이스 또한 하나의 DB (lghtsg) 에서 테이블을 생성해 관리할 예정이니, DATAGRIP 등을 사용하셔서 담당 API에 필요한 테이블을 설계하고
더미 데이터를 넣어 사용하시는 것을 권고드립니다.

config 디렉토리의 Secret 클래스에는 jwt 보안 키 등 비밀키가 포함되어 있을 예정입니다.
어차피 private 레포지이며 저희끼리만 사용해 노출위험이 없지만, 공용으로 필요한 비밀 키는 여기에서 관리해주시면 됩니다.

구현하시는 과정에서 application.yml / build.gradle 파일을 수정해야하는 경우가 생기실 겁니다.
공용으로 사용하는 파일의 충돌을 방지하기 위해 아래와 같은 규칙을 만들으니 필독 부탁드립니다

### 2. 규칙
main branch는 저희 배포 branch입니다. 따라서 파일을 받은 후 새로운 브랜치를 파 작업을 진행하고, 해당 브랜치에 커밋, push 후 pull-request 를 진행해주셔야 합니다.

1. >git pull origin main 

// 현재 작업중인 branch가 있다면 로컬에서도 해당 branch로 이동하여 main 대신 [브랜치이름]을 입력해주시고, 새로운 branch를 판다면 main으로 이동해 main 브랜치를 받아주시면 됩니다. 

2. >git checkout -b newBranch1

// 새로운 branch 개설 / 이동
3. >코드 작업 수행
4. >git add .
5. >git commit -m "added new feature"
6. >git push -u origin newBranch1
   
// -u 옵션으로 현재 작업중인 브랜치를 깃헙에 업로드함과 동시 원격 - 로컬 브랜치를 연결할 수 있습니다.
// 이렇게 하게 되면 추후 같은 branch에서 작업 후 push를 할 때 git push만 해줘도 자동으로 해당 브랜치에 업로드됩니다.
>git push

이렇게 해주신 후 github의 pull-request에 들어가 작업이 완료된 브랜치를 pull request 해주시고, 이 때 우측의 reviewer에 저를 태그해주시면 됩니다.

branch protection rule을 적용해 main 브랜치에 PR이 아니면 머지가 안되게 하려고 하였으나, 유료 서비스라 안했습니다.
따라서 잘못해 main 브랜치에 올라가 충돌이 일어나는 경우 당황하지 마시고 저에게 알려주시면 감사하겠습니다.b

### 3. GIT 명령어 모음

>git branch -r   : 원격저장소(깃허브) 브랜치 상태 확인

>git branch -a   : 로컬저장소(내 컴퓨터) 브랜치 상태 확인 
