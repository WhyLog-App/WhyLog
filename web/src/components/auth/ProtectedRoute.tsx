import type { ReactElement } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { ROUTES } from "@/constants/routes";
import { tokenStore } from "@/utils/tokenStore";

interface ProtectedRouteProps {
  children: ReactElement;
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const location = useLocation();
  const hasToken = tokenStore.hasToken();

  if (!hasToken) {
    return <Navigate to={ROUTES.LANDING} replace state={{ from: location }} />;
  }

  return children;
}

export default ProtectedRoute;
