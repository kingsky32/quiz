import React from 'react';
import dayjs from 'dayjs';
import { Link, Route, Routes, useLocation } from 'react-router-dom';
import routes, { Route as IRoute } from './routes';
import classNames from '../utils/classNames';
import Api from '../apis/api';

function App() {
  const meQuery = Api.Auth.Query.useMeQuery();
  const logoutMutation = Api.Auth.Mutation.useLogoutMutation();
  const location = useLocation();
  const renderRoutes = React.useCallback(() => {
    function recursive(route: IRoute): React.ReactNode[] {
      return [
        <Route
          key={`Route-${route.path}`}
          path={route.path}
          element={route.element}
        />,
        ...(route.children?.reduce<React.ReactNode[]>(
          (previousValue, currentValue) => {
            previousValue.push(
              <Route
                key={`Route-${currentValue.path}`}
                path={currentValue.path}
                element={currentValue.element}
              />,
            );
            currentValue.children?.forEach((child) => {
              previousValue.push(...(recursive(child) ?? []));
            });
            return previousValue;
          },
          [],
        ) ?? []),
      ];
    }
    return routes.reduce<React.ReactNode[]>((previousValue, currentValue) => {
      return [...previousValue, ...(recursive(currentValue) ?? [])];
    }, []);
  }, []);

  return (
    <div className="flex min-h-screen flex-col">
      <header className="mx-auto flex w-full max-w-[1200px] items-center px-32 py-24">
        <h1 className="flex-1 justify-start">
          <Link
            to="/"
            className="text-24 font-[600] text-[#111817] no-underline"
          >
            퀴즈
          </Link>
        </h1>
        <nav className="flex flex-1 items-center justify-center gap-48">
          <Link
            to="/"
            className={classNames(
              'text-14 font-[600] text-[#111817] no-underline transition-all hover:opacity-100',
              location.pathname === '/' ? 'opacity-100' : 'opacity-50',
            )}
          >
            Room
          </Link>
          <Link
            to="/room/create"
            className={classNames(
              'text-14 font-[600] text-[#111817] no-underline transition-all hover:opacity-100',
              location.pathname === '/room/create'
                ? 'opacity-100'
                : 'opacity-50',
            )}
          >
            Create Room
          </Link>
        </nav>
        <div className="flex flex-1 items-center justify-end gap-24">
          {meQuery.data ? (
            <>
              <span className="text-14 font-[600] text-[#111817]">
                {meQuery.data.data.name}
              </span>
              <button
                type="button"
                className="cursor-pointer border-none bg-transparent text-14 font-[600] text-[#111817]"
                onClick={() => {
                  logoutMutation.mutate();
                }}
              >
                Sign out
              </button>
            </>
          ) : (
            <>
              <Link
                to="/auth/sign-in"
                className="text-14 font-[600] text-[#111817] no-underline"
              >
                Sign in
              </Link>
              <Link
                to="/auth/sign-up"
                className="rounded-6 bg-[#4F46E5] px-12 py-10 text-14 font-[600] text-[#FFFFFF] no-underline transition-all hover:bg-[#6366F1]"
              >
                Sign up
              </Link>
            </>
          )}
        </div>
      </header>
      <main className="w-full grow-[1]">
        <Routes>{renderRoutes()}</Routes>
      </main>
      <footer className="mx-auto flex w-full max-w-[1200px] flex-col items-center justify-center gap-20 border-t-1 border-t-[#0f172a0d] py-40">
        <h1>
          <Link
            to="/"
            className="text-24 font-[600] text-[#111817] no-underline"
          >
            퀴즈
          </Link>
        </h1>
        <p className="text-14 font-[300] text-[#64748B]">
          © {dayjs().year()} 퀴즈. All rights reserved.
        </p>
      </footer>
    </div>
  );
}

export default App;
