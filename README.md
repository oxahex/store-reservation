# Store Reservation

구현 설명

## Exception
각 예외에 대해 `GlocalExceptionHandler`에서 일괄 처리하도 `@RestControllerAdvice`로 구현하였습니다.
1. 올바르지 않은 유저 입력
2. 비즈니스 로직 내 오류 처리


올바르지 않은 입력(`Type Mismatch`) 케이스의 경우 `@Valid` 검증 실패로 `MethodArgumentNotValidException` 발생 시 400 에러를 반환합니다.

비즈니스 로직 내에서 발생한 오류의 경우 각 케이스에 대해 `CustomException`으로 처리하였습니다.

위에서 정의되지 않은 오류의 경우 `Exception`으로 처리, 500 상태 코드를 반환합니다.

## 권한
권한의 경우 spring security와 jwt를 사용했습니다.
기본적으로 `/auth/login`, `/auth/signup`을 제외한 모든 경로는 `ROLE_USER`와 `ROLE_PARTNERS` 권한을 요구합니다.
매장을 등록하거나, 예약 승인/거절과 같이 매장 점주만 접근 가능한 경로는 `ROLE_PARTNERS` 권한을 가진 경우에만 허용합니다.

권한의 경우 `JwtFilter`에서 `OncePerRequestFilter`를 구현해 필터 방식으로 처리하였습니다.
내부적으로 컨트롤러 진입 전에 Jwt Token을 먼저 확인하고, 담겨있는 권한을 가지고 `UsernamePasswordAuthenticationToken` 객체를 생성해 이를 `SecurityContextHoder`에 담도록 처리했습니다.

```java
// 권한 부여
UsernamePasswordAuthenticationToken authenticationToken =
		new UsernamePasswordAuthenticationToken(
                email, null, List.of(new SimpleGrantedAuthority(role))
		);

// Detail: 인증 된 유저로 넘김
authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
SecurityContextHolder.getContext().setAuthentication(authenticationToken);
filterChain.doFilter(request, response);
```

## 회원가입
유저 이름, 이메일, 비밀번호, 핸드폰 번호 정보를 받아 데이터베이스에 저장합니다.

## 로그인
등록된 유저 이메일, 비밀번호로 유저가 확인되면 JWT 토큰을 발급합니다.
JWT 토큰은 유저 이메일과 권한 정보를 포함합니다.

## 파트너스 등록
JWT를 확인해 로그인 유저인지 식별하고, 파트너스에 등록하는 기능입니다.
사업자 번호를 받아 해당 정보가 유효한지 확인 되면 파트너스에 등록하는 시나리오 입니다.
> 구현상 사업자 번호를 받고 있으나 이 부분을 실제로 검증하는 부분은 구현하지 않았습니다.

파트너스 등록 시 `ROLE_PARTNERS` 유저로 전환됩니다.(DB 정보 변경)


## 상점 등록
JWT에 담긴 정보로 권한을 확인해 파트너스 유저인지 확인하고, 상점 등록을 진행합니다.

- 있는 사용자만 등록 가능하도록 이전에 처리하였으므로, ROLE_PARTNERS 만 등록 가능 
- 등록된 파트너스 ID(사업자 등록 번호)와 요청 유저의 사업자 번호가 불일치하는 경우 등록 불가 
- 이미 등록된 매장인 경우 등록 불가(사업자 등록 번호와 매장은 1:1이므로 사업자 등록 번호로 매장 중복 확인 가능)

## 상점 조회
확인된 로그인 유저는 등록된 상점 목록을 조회할 수 있습니다.

- 별점 순 조회(별점 높은 순)
- 최신 순 조회(가장 최근에 등록된 매장 순)



## 리뷰 등록


 