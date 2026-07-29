import type { RouteObject } from "react-router-dom";
import { useRoutes } from "react-router-dom";
import ProtectedRoute from "../components/auth/ProtectedRoute";
import RootRedirect from "../components/routing/RootRedirect";
import { ROUTES } from "../constants/routes";
import TeamLayout from "../layout/TeamLayout";
import {
  DecisionsPage,
  DecisionsRoutePage,
  GitCommitDetailPage,
  GitPage,
  HomePage,
  LandingPage,
  LoginPage,
  MeetingPage,
  MeetingRoutePage,
  NotFound,
  OnboardingCreateTeamPage,
  SettingsPage,
  SignupPage,
} from "../pages";

const allRoutes: RouteObject[] = [
  // 1. 루트 리다이렉트
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <RootRedirect />
      </ProtectedRoute>
    ),
  },

  // 2. 팀 기반 라우트 (nested)
  {
    path: "/team/:teamId",
    element: (
      <ProtectedRoute>
        <TeamLayout />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: "decisions",
        element: <DecisionsPage />,
      },
      {
        path: "decisions/:decisionId/:applicationId",
        element: <DecisionsRoutePage />,
      },
      {
        path: "meeting",
        element: <MeetingPage />,
      },
      {
        path: "meeting/:meetingId",
        element: <MeetingRoutePage />,
      },
      {
        path: "git",
        element: <GitPage />,
      },
      {
        path: "git/:repositoryId",
        element: <GitPage />,
      },
      {
        path: "git/:repositoryId/:commitHash",
        element: <GitCommitDetailPage />,
      },
      {
        path: "settings",
        element: <SettingsPage />,
      },
    ],
  },

  // 2-1. 온보딩
  {
    path: ROUTES.ONBOARDING_CREATE_TEAM,
    element: (
      <ProtectedRoute>
        <OnboardingCreateTeamPage />
      </ProtectedRoute>
    ),
  },

  // 3. 랜딩
  {
    path: ROUTES.LANDING,
    element: <LandingPage />,
  },

  // 4. 인증 라우트
  {
    path: ROUTES.LOGIN,
    element: <LoginPage />,
  },
  {
    path: ROUTES.SIGNUP,
    element: <SignupPage />,
  },

  // 5. 404
  {
    path: "/*",
    element: <NotFound />,
  },
];

export default function Router() {
  const route = useRoutes(allRoutes);
  return route;
}
