import { isAxiosError } from "axios";
import type { ApiResponse } from "@/types/auth";

export const mypageErrorMessage = (error: unknown, fallback: string) => {
  if (isAxiosError<ApiResponse<unknown>>(error)) {
    return error.response?.data?.message ?? fallback;
  }
  return fallback;
};

export const logMypageRequestFailure = (context: string, error: unknown) => {
  console.error(context, {
    status: isAxiosError(error) ? error.response?.status : undefined,
  });
};
