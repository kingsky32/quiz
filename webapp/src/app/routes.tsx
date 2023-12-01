import React from 'react';
import HomePage from './page';
import RoomDetailPage from './room/[id]/page';
import RoomCreatePage from './room/create/page';
import AuthSignInPage from './auth/sign-in/page';
import AuthSignUpPage from './auth/sign-up/page';

export interface Route {
  path: string;
  name: string;
  auth?: boolean;
  element?: React.ReactNode;
  children?: Route[];
}

const routes: Route[] = [
  {
    path: '/',
    name: 'Room',
    auth: true,
    element: <HomePage />,
    children: [
      {
        path: '/room/create',
        name: 'Create Room',
        auth: true,
        element: <RoomCreatePage />,
      },
      {
        path: '/room/:id',
        name: 'Detail Room',
        auth: true,
        element: <RoomDetailPage />,
      },
    ],
  },
  {
    path: '/auth/sign-in',
    name: 'Sign in',
    auth: false,
    element: <AuthSignInPage />,
  },
  {
    path: '/auth/sign-up',
    name: 'Sign Up',
    auth: false,
    element: <AuthSignUpPage />,
  },
];

export default routes;
