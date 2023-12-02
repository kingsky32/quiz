import React from 'react';
import queryString from 'query-string';
import Axios, { AxiosResponse } from 'axios';
import * as StompJs from '@stomp/stompjs';
import dayjs from 'dayjs';
import { useCookies } from 'react-cookie';
import {
  QueryClient,
  QueryClientProvider,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import fileDownload from '../utils/fileDownload';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;
const WS_BASE_URL = process.env.REACT_APP_WEB_SOCKET_BASE_URL;

const TOKEN_STORE_KEY = 'token';

let _token: Jwt.TokenDto.Response | Promise<Jwt.TokenDto.Response> | null =
  null;

namespace Jwt {
  export namespace TokenDto {
    export interface Response {
      jti: string;
      tokenType: string;
      accessToken: string;
      accessTokenExpiresAt: string;
      accessTokenExpiresIn: number;
      refreshToken: string;
      refreshTokenExpiresAt: string;
      refreshTokenExpiresIn: number;
      createdAt: string;
    }
  }
}

type ContextType = [
  Jwt.TokenDto.Response | null,
  React.Dispatch<Jwt.TokenDto.Response | null>,
];

const context = React.createContext<ContextType>([null, () => {}]);

const _queryClient = new QueryClient();

function useContext(): ContextType {
  const [, setCookie, removeCookie] = useCookies([TOKEN_STORE_KEY]);
  const [token, setToken] = React.useContext(context);
  return [
    token,
    (value) => {
      const updatedToken = value && {
        jti: value.jti,
        tokenType: value.tokenType,
        accessToken: value.accessToken,
        accessTokenExpiresAt: dayjs()
          .add(
            dayjs(value.createdAt)
              .add(value.accessTokenExpiresIn, 'millisecond')
              .diff(dayjs(value.createdAt), 'millisecond'),
            'millisecond',
          )
          .format('YYYY-MM-DD HH:mm:ss'),
        accessTokenExpiresIn: value.accessTokenExpiresIn,
        refreshToken: value.refreshToken,
        refreshTokenExpiresAt: dayjs()
          .add(
            dayjs(value.createdAt)
              .add(value.refreshTokenExpiresIn, 'millisecond')
              .diff(dayjs(value.createdAt), 'millisecond'),
            'millisecond',
          )
          .format('YYYY-MM-DD HH:mm:ss'),
        refreshTokenExpiresIn: value.refreshTokenExpiresIn,
        createdAt: value.createdAt,
      };
      _token = updatedToken;
      setToken(updatedToken);
      if (updatedToken) {
        setCookie(TOKEN_STORE_KEY, updatedToken, {
          expires: dayjs()
            .add(updatedToken.refreshTokenExpiresIn, 'millisecond')
            .toDate(),
          path: '/',
        });
      } else {
        removeCookie(TOKEN_STORE_KEY, {
          path: '/',
        });
      }
    },
  ];
}

namespace Api {
  export namespace Pageable {
    export enum Order {
      ASC = 'ASC',
      DESC = 'DESC',
    }

    interface __Request {
      limit?: number;
      sorts?: string[];
    }

    interface __Response<T> {
      totalResults: number;
      resultsPerPage: number;
      limit: number;
      results: T[];
    }

    export namespace Page {
      export interface Request extends __Request {
        page?: number;
      }

      export interface Response<T> extends __Response<T> {
        page: number;
        lastPage: number;
        prevPage: number;
        nextPage: number;
      }
    }

    export namespace Cursor {
      export interface Request<T> extends __Request {
        cursor?: T;
      }

      export interface Response<T, R> extends __Response<R> {
        cursor: T;
        nextCursor: T;
      }
    }
  }

  export async function getToken(
    callback?: (response: Jwt.TokenDto.Response) => void,
  ): Promise<Jwt.TokenDto.Response | null> {
    if (_token) {
      const token = await _token;
      if (
        dayjs(dayjs().format('YYYY-MM-DD HH:mm:ss')).isAfter(
          dayjs(token.accessTokenExpiresAt)
            .subtract(10000, 'millisecond')
            .format('YYYY-MM-DD HH:mm:ss'),
        )
      ) {
        if (
          dayjs(dayjs().format('YYYY-MM-DD HH:mm:ss')).isBefore(
            dayjs(token.refreshTokenExpiresAt)
              .subtract(10000, 'millisecond')
              .format('YYYY-MM-DD HH:mm:ss'),
          )
        ) {
          _token = Axios.post<
            Jwt.TokenDto.Response,
            AxiosResponse<Jwt.TokenDto.Response>,
            Auth.Dto.OauthToken
          >(
            '/api/v1/auth/oauth/token',
            {
              grantType: 'refreshToken',
              jti: token.jti,
              refreshToken: token.refreshToken,
            },
            {
              baseURL: API_BASE_URL,
            },
          ).then(({ data: responseData }) => {
            callback?.(responseData);
            return responseData;
          });
        }
      }
    }
    return _token;
  }

  export interface ProviderProps {
    children?: React.ReactNode;
  }

  export function Provider({ children }: ProviderProps) {
    const [cookie] = useCookies([TOKEN_STORE_KEY]);
    const state = React.useState<Jwt.TokenDto.Response | null>(
      (cookie.token as Jwt.TokenDto.Response) ?? null,
    );
    const [token] = state;
    _token = token;
    return (
      <QueryClientProvider client={_queryClient}>
        <context.Provider value={state}>{children}</context.Provider>
        <ReactQueryDevtools />
      </QueryClientProvider>
    );
  }

  export function useAxios() {
    const queryClient = useQueryClient();
    const axios = React.useRef(
      Axios.create({
        baseURL: API_BASE_URL,
        paramsSerializer(params) {
          return queryString.stringify(params, { arrayFormat: 'none' });
        },
      }),
    ).current;
    const [token, setToken] = useContext();
    if (token) {
      axios.interceptors.request.use(async (value) => {
        if (_token) {
          const t = await getToken(setToken);
          if (t) {
            value.headers.Authorization = `${t.tokenType} ${t.accessToken}`;
          } else {
            delete value.headers.Authorization;
          }
        }
        return value;
      });
      axios.interceptors.response.use(
        (value) => {
          return value;
        },
        async (error) => {
          if (error?.response?.status === 401) {
            setToken(null);
            await queryClient.resetQueries();
            window.location.reload();
          }
          if (
            error.code === 'ERR_NETWORK' ||
            error?.response?.data?.status === 502
          ) {
            error.data = {
              response: {
                data: {
                  status: 502,
                  message: '인터넷 연결 오류',
                },
              },
            };
          }
          throw error;
        },
      );
    }
    return axios;
  }

  export function serializePageableParams({
    sort,
    ...paramsWithoutSort
  }: Record<any, any> & { sort?: Record<string, Pageable.Order> }) {
    const params = { ...paramsWithoutSort };
    if (sort) {
      const sorts = Object.entries(sort);
      if (sorts.length) {
        params.sorts = sorts.reduce<string[]>((previousValue, [key, value]) => {
          return value
            ? [...previousValue, `${key}|${value.toUpperCase()}`]
            : previousValue;
        }, []);
      }
    }
    return params;
  }

  export namespace User {
    export namespace Dto {
      export interface Response {
        id: number;
        username: string;
        name: string;
      }
    }
  }

  export namespace Auth {
    export namespace Dto {
      export interface Login {
        username: string;
        password: string;
      }
      export interface Join {
        email: string;
        name: string;
        username: string;
        password: string;
      }
      export interface Logout {
        grantType: string;
        jti: string;
        refreshToken: string;
      }
      export interface OauthToken {
        grantType: string;
        jti: string;
        refreshToken: string;
      }
      export interface LoginResponse extends Jwt.TokenDto.Response {
        user: User.Dto.Response;
      }
    }

    export namespace Query {
      export const QUERY_KEY = {
        AUTH: 'AUTH',
        ME: 'ME',
      };

      export function useMeQuery() {
        const [token] = useContext();
        const axios = useAxios();
        return useQuery({
          queryKey: [Query.QUERY_KEY.AUTH, QUERY_KEY.ME],
          queryFn() {
            return axios.get<User.Dto.Response>('/api/v1/auth/me');
          },
          enabled: !!token,
        });
      }
    }

    export namespace Mutation {
      export function useLoginMutation() {
        const axios = useAxios();
        const queryClient = useQueryClient();
        const [, setToken] = useContext();
        return useMutation({
          mutationFn(variables: Dto.Login) {
            return axios.post<
              Dto.LoginResponse,
              AxiosResponse<Dto.LoginResponse>,
              Dto.Login
            >('/api/v1/auth/login', variables);
          },
          onSuccess({ data: responseData }) {
            setToken(responseData);
          },
          onSettled() {
            return queryClient.resetQueries();
          },
        });
      }

      export function useJoinMutation() {
        const axios = useAxios();
        const queryClient = useQueryClient();
        const [, setToken] = useContext();
        return useMutation({
          mutationFn(variables: Dto.Join) {
            return axios.post<
              Dto.LoginResponse,
              AxiosResponse<Dto.LoginResponse>,
              Dto.Join
            >('/api/v1/auth/join', variables);
          },
          onSuccess({ data: responseData }) {
            setToken(responseData);
          },
          onSettled() {
            return queryClient.resetQueries();
          },
        });
      }

      export function useLogoutMutation() {
        const axios = useAxios();
        const queryClient = useQueryClient();
        const [token, setToken] = useContext();
        return useMutation({
          mutationFn() {
            return axios.post<boolean, AxiosResponse<boolean>, Dto.Logout>(
              '/api/v1/auth/logout',
              {
                jti: token?.jti as string,
                grantType: 'refreshToken',
                refreshToken: token?.refreshToken as string,
              },
            );
          },
          onSuccess() {
            setToken(null);
            return queryClient.resetQueries();
          },
        });
      }
    }
  }

  export namespace File {
    const minChunkSize = 5 * 1024 * 1024;
    const chunkSize = 5 * 1024 * 1024;

    export function getFilePath(id: number) {
      return `${API_BASE_URL}/api/v1/file/${id}`;
    }

    export namespace Dto {
      export interface Response {
        id: number;
        name: string;
        extension: string;
        serverPath: string;
        contentType: string;
        size: number;
        createdAt: string;
      }

      export interface CreateMultipartUpload {
        name: string;
        contentType: string;
        size: number;
      }

      export interface CreateMultipartUploadResponse {
        id: number;
      }

      export interface UploadPartResponse {
        etag: string;
      }

      export interface CompleteMultipartUploadPart {
        etag: string;
        partNumber: number;
      }

      export interface CompleteMultipartUpload {
        parts: CompleteMultipartUploadPart[];
      }
    }

    export namespace Mutation {
      export type UploadStatusType = 'READY' | 'PROGRESS' | 'DONE' | 'ERROR';

      export interface UploadStatusItem {
        file: File;
        status: UploadStatusType;
        chunks: File[];
        uploadedChunkLength: number;
        response?: Dto.Response;
      }

      export interface UploadStatus {
        count: number;
        totalCount: number;
        progress: number;
        status: UploadStatusType;
        items: UploadStatusItem[];
      }

      export function useFileDownloadMutation() {
        return useMutation({
          mutationFn({ id, filename }: { id: number; filename: string }) {
            return fileDownload(getFilePath(id), filename);
          },
        });
      }

      export function useFileUploadMutation() {
        const axios = useAxios();
        return useMutation({
          async mutationFn(file: File) {
            if (file.size > minChunkSize) {
              const {
                data: { id },
              } = await axios.post<
                Dto.CreateMultipartUploadResponse,
                AxiosResponse<Dto.CreateMultipartUploadResponse>,
                Dto.CreateMultipartUpload
              >('/api/v1/file/create-multipart-upload', {
                contentType: file.type,
                size: file.size,
                name: file.name,
              });
              const parts = await Promise.all(
                [...new Array(Math.ceil(file.size / chunkSize))].map(
                  (_, index) => {
                    const partNumber = index + 1;
                    const formData = new FormData();
                    formData.append(
                      'file',
                      file.slice(index * chunkSize, partNumber * chunkSize),
                    );
                    return axios
                      .post<Dto.UploadPartResponse>(
                        `/api/v1/file/${id}/upload-part`,
                        formData,
                        {
                          params: {
                            partNumber: index + 1,
                          },
                          headers: {
                            'Content-Type': 'multipart/form-data',
                          },
                        },
                      )
                      .then(
                        ({
                          data: responseData,
                        }): Dto.CompleteMultipartUploadPart => {
                          return { etag: responseData.etag, partNumber };
                        },
                      );
                  },
                ),
              );
              return axios.post<
                Dto.Response,
                AxiosResponse<Dto.Response>,
                Dto.CompleteMultipartUpload
              >(`/api/v1/file/${id}/complete-multipart-upload`, {
                parts,
              });
            }
            const formData = new FormData();
            formData.append('file', file);
            return axios.post<Dto.Response>('/api/v1/file', formData, {
              headers: {
                'Content-Type': 'multipart/form-data',
              },
            });
          },
        });
      }

      export function useFilesUploadMutation(
        callback?: (status: UploadStatus) => void,
      ) {
        const axios = useAxios();
        return useMutation({
          async mutationFn(fileList: FileList) {
            const status: UploadStatus = {
              count: 0,
              totalCount: fileList.length,
              progress: 0,
              status: 'READY',
              items: Array.from(fileList).map((file) => {
                return {
                  file,
                  status: 'READY',
                  chunks: [...new Array(Math.ceil(file.size / chunkSize))].map(
                    (_, index) => {
                      return file.slice(
                        index * chunkSize,
                        (index + 1) * chunkSize,
                      ) as File;
                    },
                  ),
                  uploadedChunkLength: 0,
                  response: undefined,
                };
              }),
            };
            callback?.(status);
            try {
              status.status = 'PROGRESS';
              callback?.(status);
              const responses = await Promise.all(
                status.items.map(async (item, itemIndex, itemArray) => {
                  try {
                    status.items[itemIndex].status = 'PROGRESS';
                    callback?.(status);
                    if (item.chunks.length > 1) {
                      const {
                        data: { id },
                      } = await axios.post<
                        Dto.CreateMultipartUploadResponse,
                        AxiosResponse<Dto.CreateMultipartUploadResponse>,
                        Dto.CreateMultipartUpload
                      >('/api/v1/file/create-multipart-upload', {
                        contentType: item.file.type,
                        size: item.file.size,
                        name: item.file.name,
                      });
                      const parts = await Promise.all(
                        item.chunks.map(async (chunk, chunkIndex) => {
                          const formData = new FormData();
                          formData.append('file', chunk);
                          const chunkResponse =
                            await axios.post<Dto.UploadPartResponse>(
                              `/api/v1/file/${id}/upload-part`,
                              formData,
                              {
                                params: {
                                  partNumber: chunkIndex + 1,
                                },
                                headers: {
                                  'Content-Type': 'multipart/form-data',
                                },
                              },
                            );
                          status.items[itemIndex].uploadedChunkLength += 1;
                          callback?.(status);
                          return chunkResponse;
                        }),
                      );
                      const response = await axios.post<
                        Dto.Response,
                        AxiosResponse<Dto.Response>,
                        Dto.CompleteMultipartUpload
                      >(`/api/v1/file/${id}/complete-multipart-upload`, {
                        parts: parts.map((part, partIndex) => {
                          return {
                            etag: part.data.etag,
                            partNumber: partIndex + 1,
                          };
                        }),
                      });
                      status.count += 1;
                      status.progress += status.count / itemArray.length;
                      status.items[itemIndex].status = 'DONE';
                      status.items[itemIndex].response = response.data;
                      callback?.(status);
                      return response;
                    }
                    const formData = new FormData();
                    formData.append('file', item.file);
                    const response = await axios.post<Dto.Response>(
                      '/api/v1/file',
                      formData,
                      {
                        headers: {
                          'Content-Type': 'multipart/form-data',
                        },
                      },
                    );
                    status.count += 1;
                    status.progress += status.count / itemArray.length;
                    status.items[itemIndex].status = 'DONE';
                    status.items[itemIndex].response = response.data;
                    callback?.(status);
                    return response;
                  } catch (error) {
                    status.count += 1;
                    status.progress += status.count / itemArray.length;
                    status.items[itemIndex].status = 'ERROR';
                    callback?.(status);
                    throw error;
                  }
                }),
              );
              status.status = 'DONE';
              status.progress = 1;
              callback?.(status);
              return responses;
            } catch (error) {
              status.status = 'ERROR';
              callback?.(status);
              throw error;
            }
          },
        });
      }
    }
  }

  export namespace QuizCategory {
    export namespace Dto {
      export interface Create {
        parentId?: number;
        name: string;
      }

      export interface Response {
        id: number;
        name: string;
        children: Response[] | null;
      }

      export interface DetailResponse {
        id: number;
        name: string;
        children: DetailResponse[];
      }

      export interface Update {
        parentId?: number;
        name: string;
      }

      export interface DeleteAll {
        ids: number[];
      }
    }

    export namespace Query {
      export const QUERY_KEY = {
        QUIZ_CATEGORY: 'QUIZ_CATEGORY',
      };

      export function useQuizCategoriesQuery() {
        const axios = useAxios();
        return useQuery({
          queryKey: [QUERY_KEY.QUIZ_CATEGORY],
          queryFn() {
            return axios.get<Dto.Response[]>('/api/v1/quiz-category');
          },
        });
      }

      export function useQuizCategoryQuery(id?: number) {
        const axios = useAxios();
        return useQuery({
          queryKey: [QUERY_KEY.QUIZ_CATEGORY, id],
          queryFn() {
            return axios.get<Dto.DetailResponse>(`/api/v1/quiz-category/${id}`);
          },
          enabled: !!id,
        });
      }
    }

    export namespace Mutation {
      export function useCreateQuizCategoryMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: Dto.Create) {
            return axios.post<
              Dto.DetailResponse,
              AxiosResponse<Dto.DetailResponse>,
              Dto.Create
            >('/api/v1/quiz-category', variables);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ_CATEGORY],
            });
          },
        });
      }

      export function useUpdateQuizCategoryMutation(id?: number) {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: Dto.Update) {
            return axios.put<
              Dto.DetailResponse,
              AxiosResponse<Dto.DetailResponse>,
              Dto.Update
            >(`/api/v1/quiz-category/${id}`, variables);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ_CATEGORY],
            });
          },
        });
      }

      export function useDeleteQuizCategoryMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: number) {
            return axios.delete(`/api/v1/quiz-category/${variables}`);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ_CATEGORY],
            });
          },
        });
      }

      export function useDeleteQuizCategoriesMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: number[]) {
            return axios.delete('/api/v1/quiz-category', {
              params: variables,
            });
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ_CATEGORY],
            });
          },
        });
      }
    }
  }

  export namespace QuizHint {
    export namespace Dto {
      export interface Response {
        id: number;
        name: string;
        content: string;
      }
    }
  }

  export namespace QuizAnswer {
    export namespace Dto {
      export interface Response {
        id: number;
        answer: string;
      }
    }
  }

  export namespace Quiz {
    export namespace Dto {
      export interface CreateHint {
        name: string;
        content: string;
        exposedRemainTime: number;
      }

      export interface CreateAnswer {
        answer: string;
      }

      export interface Create {
        quizCategoryId: number;
        title: string;
        content: string;
        soundFileId?: number;
        timeoutMs: number;
        isActive: boolean;
        hints: CreateHint[];
        answers: CreateAnswer[];
      }

      export interface Request extends Pageable.Page.Request {}

      export interface Response {
        id: number;
        quizCategory: QuizCategory.Dto.Response;
        title: string;
        content: string;
        soundFile: File.Dto.Response;
        timeoutMs: number;
        isActive: boolean;
        createdBy: User.Dto.Response;
        createdAt: string;
        updatedAt: string;
      }

      export interface DetailResponse {
        id: number;
        quizCategory: QuizCategory.Dto.DetailResponse;
        title: string;
        content: string;
        soundFile: File.Dto.Response;
        timeoutMs: number;
        isActive: boolean;
        quizHints: QuizHint.Dto.Response[];
        quizAnswers: QuizAnswer.Dto.Response[];
        createdBy: User.Dto.Response;
        createdAt: string;
        updatedAt: string;
      }

      export interface UpdateHint {
        name: string;
        content: string;
        exposedRemainTime: number;
      }

      export interface UpdateAnswer {
        answer: string;
      }

      export interface Update {
        quizCategoryId: number;
        title: string;
        content: string;
        soundFileId: number;
        timeoutMs: number;
        isActive: boolean;
        hints: UpdateHint[];
        answers: UpdateAnswer[];
      }

      export interface DeleteAll {
        ids: number[];
      }
    }

    export namespace Query {
      export const QUERY_KEY = {
        QUIZ: 'QUIZ',
      };

      export function useQuizzesQuery(params: Dto.Request) {
        const axios = useAxios();
        return useQuery({
          queryKey: [QUERY_KEY.QUIZ, params],
          queryFn() {
            return axios.get<Pageable.Page.Response<Dto.Response>>(
              '/api/v1/quiz',
              {
                params,
              },
            );
          },
        });
      }

      export function useQuizQuery(id?: number) {
        const axios = useAxios();
        return useQuery({
          queryKey: [QUERY_KEY.QUIZ, id],
          queryFn() {
            return axios.get<Dto.DetailResponse>(`/api/v1/quiz/${id}`);
          },
          enabled: !!id,
        });
      }
    }

    export namespace Mutation {
      export function useCreateQuizMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: Dto.Create) {
            return axios.post<
              Dto.DetailResponse,
              AxiosResponse<Dto.DetailResponse>,
              Dto.Create
            >('/api/v1/quiz', variables);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ],
            });
          },
        });
      }

      export function useUpdateQuizMutation(id?: number) {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: Dto.Update) {
            return axios.put<
              Dto.DetailResponse,
              AxiosResponse<Dto.DetailResponse>,
              Dto.Update
            >(`/api/v1/quiz/${id}`, variables);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ],
            });
          },
        });
      }

      export function useDeleteQuizMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: number) {
            return axios.delete(`/api/v1/quiz/${variables}`);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ],
            });
          },
        });
      }

      export function useDeleteQuizzesMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: number[]) {
            return axios.delete('/api/v1/quiz', {
              params: variables,
            });
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.QUIZ],
            });
          },
        });
      }
    }
  }

  export namespace RoomQuizCategory {
    export namespace Dto {
      export interface Response {
        quizCategory: QuizCategory.Dto.Response;
      }

      export interface DetailResponse {
        quizCategory: QuizCategory.Dto.Response;
      }
    }
  }

  export namespace Room {
    export enum Status {
      READY = 'READY',
      PLAYING = 'PLAYING',
      DELETED = 'DELETED',
    }

    export namespace Dto {
      export interface Create {
        title: string;
        numberOfQuiz: number;
        isSecret: boolean;
        secretPassword: string;
        quizCategoryIds: number[];
      }

      export interface Request extends Pageable.Cursor.Request<number> {}

      export interface Response {
        id: number;
        status: Status;
        title: string;
        currentNumber: number;
        numberOfQuiz: number;
        isSecret: boolean;
        roomQuizCategories: RoomQuizCategory.Dto.Response[];
        createdBy: User.Dto.Response;
        createdAt: string;
      }

      export interface DetailResponseUser {
        user: User.Dto.Response;
        rank: number;
        count: number;
      }

      export interface DetailResponse {
        id: number;
        status: Status;
        title: string;
        currentNumber: number;
        numberOfQuiz: number;
        isSecret: boolean;
        roomQuizCategories: RoomQuizCategory.Dto.DetailResponse[];
        users: DetailResponseUser[];
        createdBy: User.Dto.Response;
        createdAt: string;
      }

      export interface Update {
        title: string;
        numberOfQuiz: number;
        isSecret: boolean;
        secretPassword: string;
        quizCategoryIds: number[];
      }

      export interface DeleteAll {
        ids: number[];
      }

      export interface Chat {
        roomId: number;
        accountId: number;
        message: string;
      }

      export interface ChatResponse {
        user: User.Dto.Response;
        isCorrect: boolean;
        message: boolean;
      }

      export interface QuestionResponse {
        title: string;
        content: string;
        soundFile: File.Dto.Response;
        timeoutMs: number;
        createdBy: User.Dto.Response;
      }

      export interface HintResponse {
        id: number;
        name: string;
        content: string;
      }

      export interface AnswerResponse {
        answers: string[];
      }
    }

    export namespace Query {
      export const QUERY_KEY = {
        ROOM: 'ROOM',
      };

      export function useRoomsQuery(params: Dto.Request) {
        const axios = useAxios();
        return useQuery({
          queryKey: [QUERY_KEY.ROOM, params],
          queryFn() {
            return axios.get<Pageable.Cursor.Response<number, Dto.Response>>(
              '/api/v1/room',
              { params },
            );
          },
        });
      }

      export function useRoomQuery(id?: number) {
        const axios = useAxios();
        return useQuery({
          queryKey: [QUERY_KEY.ROOM, id],
          queryFn() {
            return axios.get<Dto.DetailResponse>(`/api/v1/room/${id}`);
          },
          enabled: !!id,
        });
      }
    }

    export namespace Mutation {
      export function useCreateRoomMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: Dto.Create) {
            return axios.post<
              Dto.DetailResponse,
              AxiosResponse<Dto.DetailResponse>,
              Dto.Create
            >('/api/v1/room', variables);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.ROOM],
            });
          },
        });
      }

      export function useUpdateRoomMutation(id?: number) {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: Dto.Update) {
            return axios.put<
              Dto.DetailResponse,
              AxiosResponse<Dto.DetailResponse>,
              Dto.Update
            >(`/api/v1/room/${id}`, variables);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.ROOM],
            });
          },
        });
      }

      export function useDeleteRoomMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: number) {
            return axios.delete(`/api/v1/room/${variables}`);
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.ROOM],
            });
          },
        });
      }

      export function useDeleteRoomsMutation() {
        const queryClient = useQueryClient();
        const axios = useAxios();
        return useMutation({
          mutationFn(variables: number[]) {
            return axios.delete('/api/v1/room', {
              params: variables,
            });
          },
          onSettled() {
            return queryClient.invalidateQueries({
              queryKey: [Query.QUERY_KEY.ROOM],
            });
          },
        });
      }
    }

    export namespace WebSocket {
      export interface Client extends StompJs.Client {
        chat(chat: Dto.Chat): void;
        play(): void;
        skip(): void;
        disconnect(): void;
      }
      export function join(
        id: number,
        options?: {
          onChat?: (message: Dto.ChatResponse) => void;
          onQuestion?: (question: Dto.QuestionResponse) => void;
          onHint?: (hint: Dto.HintResponse) => void;
          onAnswer?: (answer: Dto.AnswerResponse) => void;
          onSkip?: () => void;
        },
      ): Client {
        const client = new StompJs.Client({
          brokerURL: `${WS_BASE_URL}/ws/room`,
          onConnect() {
            client.subscribe(`/ws/room/subscribe/${id}/chat`, (message) => {
              const data: Dto.ChatResponse = JSON.parse(message.body);
              options?.onChat?.(data);
            });
            client.subscribe(`/ws/room/subscribe/${id}/question`, (message) => {
              const data: Dto.QuestionResponse = JSON.parse(message.body);
              options?.onQuestion?.(data);
            });
            client.subscribe(`/ws/room/subscribe/${id}/hint`, (message) => {
              const data: Dto.HintResponse = JSON.parse(message.body);
              options?.onHint?.(data);
            });
            client.subscribe(`/ws/room/subscribe/${id}/answer`, (message) => {
              const data: Dto.AnswerResponse = JSON.parse(message.body);
              options?.onAnswer?.(data);
            });
            client.subscribe(`/ws/room/subscribe/${id}/skip`, () => {
              options?.onSkip?.();
            });
          },
        });
        client.activate();
        // @ts-ignore
        return {
          ...client,
          chat(message) {
            if (!client.connected) {
              return;
            }
            client.publish({
              destination: `/ws/room/${id}/chat`,
              body: JSON.stringify(message),
            });
          },
          play() {
            if (!client.connected) {
              return;
            }
            client.publish({
              destination: `/ws/room/${id}/play`,
            });
          },
          skip() {
            if (!client.connected) {
              return;
            }
            client.publish({
              destination: `/ws/room/${id}/skip`,
            });
          },
          disconnect() {
            client.deactivate();
          },
        };
      }
    }
  }
}

export default Api;
