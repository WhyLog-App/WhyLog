import { type AxiosResponse, isAxiosError } from "axios";
import ENDPOINT from "@/constants/endpoint";
import type { ApiResponse } from "@/types/auth";
import type {
  AddRepositoryRequest,
  AddRepositoryResult,
  CheckGitHubTokenStatusResult,
  CommitDetailResult,
  GitHubTokenRequest,
  GitHubTokenResult,
  RepositoryCommitListResult,
  RepositoryItem,
  RepositorySyncResult,
} from "@/types/git";
import { http } from "@/utils/http";

export const registerGitHubToken = async (
  token: string,
): Promise<GitHubTokenResult> => {
  const payload: GitHubTokenRequest = {
    access_token: token,
  };

  const { data } = await http.post<
    GitHubTokenRequest,
    AxiosResponse<ApiResponse<GitHubTokenResult>>
  >(ENDPOINT.GIT.GITHUB_TOKEN, payload);

  if (!data.isSuccess) {
    throw new Error(data.message);
  }

  return data.result;
};

export const addRepository = async (
  teamId: number,
  payload: AddRepositoryRequest,
): Promise<AddRepositoryResult> => {
  const { data } = await http.post<
    AddRepositoryRequest,
    AxiosResponse<ApiResponse<AddRepositoryResult>>
  >(ENDPOINT.TEAMS.REPOSITORIES(teamId), payload);

  if (!data.isSuccess) {
    throw new Error(data.message);
  }

  return data.result;
};

export const checkGitHubTokenStatus =
  async (): Promise<CheckGitHubTokenStatusResult> => {
    try {
      const { data } = await http.get<
        void,
        AxiosResponse<ApiResponse<CheckGitHubTokenStatusResult>>
      >(ENDPOINT.GIT.GITHUB_TOKEN_STATUS);

      if (!data.isSuccess) {
        return { is_registered: false };
      }

      return data.result;
    } catch (error) {
      if (isAxiosError(error) && error.response?.data?.code === "GIT_400_2") {
        return { is_registered: false };
      }
      throw error;
    }
  };

export const getRepositories = async (
  teamId: number,
): Promise<RepositoryItem[]> => {
  const { data } = await http.get<
    void,
    AxiosResponse<ApiResponse<RepositoryItem[]>>
  >(ENDPOINT.TEAMS.REPOSITORIES(teamId));

  if (!data.isSuccess) {
    throw new Error(data.message);
  }

  return data.result;
};

export const getRepositoryCommits = async (
  repositoryId: number,
  cursor?: number,
): Promise<RepositoryCommitListResult> => {
  const { data } = await http.get<
    void,
    AxiosResponse<ApiResponse<RepositoryCommitListResult>>
  >(ENDPOINT.GIT.COMMITS(repositoryId), {
    params: cursor === undefined ? undefined : { cursor },
  });

  if (!data.isSuccess) {
    throw new Error(data.message);
  }

  return data.result;
};

export const getCommitDetail = async (
  repositoryId: number,
  commitHash: string,
): Promise<CommitDetailResult> => {
  const { data } = await http.get<
    void,
    AxiosResponse<ApiResponse<CommitDetailResult>>
  >(ENDPOINT.GIT.COMMIT_DETAIL(repositoryId, commitHash));

  if (!data.isSuccess) {
    throw new Error(data.message);
  }

  return data.result;
};

export const syncRepository = async (
  repositoryId: number,
): Promise<RepositorySyncResult> => {
  const { data } = await http.post<
    void,
    AxiosResponse<ApiResponse<RepositorySyncResult>>
  >(ENDPOINT.GIT.REPOSITORY_SYNC(repositoryId));

  if (!data.isSuccess) {
    throw new Error(data.message);
  }

  return data.result;
};
