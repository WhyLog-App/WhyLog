/**
 * 현재 경로에 컨텍스트 패널이 존재하는지 판정한다.
 * Panel 컴포넌트와 MobileTopBar가 동일한 기준을 공유하기 위해 추출.
 */
export const hasPanelForPath = (pathname: string): boolean =>
  /\/decisions(\/|$)/.test(pathname) ||
  /\/meeting(\/|$)/.test(pathname) ||
  /\/git(\/|$)/.test(pathname);
